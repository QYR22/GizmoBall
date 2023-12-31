package gizmoball.ui;

import gizmoball.engine.AbstractWorld;
import gizmoball.engine.Settings;
import gizmoball.engine.geometry.XXYY;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Polygon;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.Ball;
import gizmoball.game.entity.Flipper;
import gizmoball.ui.component.*;
import gizmoball.ui.visualize.DefaultCanvasRenderer;
import gizmoball.ui.visualize.GizmoPhysicsBody;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/* 通过@FXML注入FXML文件，fxml中定义了用户界面布局，通过`fx:id`唯一标识符指定元素。
* 在PlayerPanel中实现对这些组件的控制。 */
@Slf4j
public class PlayerPanel extends Application implements Initializable {

    /* 渲染弹球游戏界面的canvas */
    @FXML
    Canvas gizmoCanvas;
    /* 游戏组件面板 i.e.球、圆形、矩形、三角形 挡板flipper、管道pipe */
    @FXML
    GridPane gizmoGridPane;

    /* 操作选项面板 i.e.删除、上下左右移动、缩放、旋转... */
    @FXML
    HBox upperHBox;
    @FXML
    HBox lowerHBox;

    @FXML
    javafx.scene.shape.Rectangle gizmoOutlineRectangle;

    @FXML
    MenuItem menuItemLoad;

    @FXML
    MenuItem menuItemSave;

    @FXML
    MenuItem menuItemClear;

    @FXML
    MenuItem menuItemAbout;

    @FXML
    ImageView previewImageView;

    @FXML
    AnchorPane anchorPane;

    // 游戏世界
    private GridWorld world;

    // true: 编辑模式  false: 设计模式
    private boolean inDesign = true;

    // 当前选中的组件
    private GizmoPhysicsBody selectedBody;
    // 操作
    private GizmoOpHandler gizmoOpHandler;

    // 拖放传参的key
    private static final DataFormat GIZMO_TYPE_DATA = new DataFormat("gizmo");

    private static Vector2 preferredSize;

    private Stage primaryStage;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledFuture<?>[] scheduledFuture = new ScheduledFuture<?>[1];

    private Runnable r;

    // 可移动组件图标
    private static final DraggableGizmoComponent[] gizmos = {
            new DraggableGizmoComponent("icons/rectangle.png", "矩形", GizmoType.RECTANGLE),
            new DraggableGizmoComponent("icons/circle.png", "圆", GizmoType.CIRCLE),
            new DraggableGizmoComponent("icons/triangle.png", "三角形", GizmoType.TRIANGLE),
            new DraggableGizmoComponent("icons/black_hole.png", "黑洞", GizmoType.BLACK_HOLE),
            new DraggableGizmoComponent("icons/ball.png", "球", GizmoType.BALL),
            new DraggableGizmoComponent("icons/pipe.png", "管道", GizmoType.PIPE),
            new DraggableGizmoComponent("icons/curved_pipe.png", "弯道", GizmoType.CURVED_PIPE),
            new DraggableGizmoComponent("icons/left_flipper.png", "左挡板", GizmoType.LEFT_FLIPPER),
            new DraggableGizmoComponent("icons/right_flipper.png", "右挡板", GizmoType.RIGHT_FLIPPER),
    };

    // 操作组件图标
    private static final CommandComponent[] gizmoOps = {
            new CommandComponent("icons/delete.png", "删除", GizmoCommand.REMOVE),
            new CommandComponent("icons/zoom_out.png", "缩小", GizmoCommand.ZOOM_OUT),
            new CommandComponent("icons/zoom_in.png", "放大", GizmoCommand.ZOOM_IN),
            new CommandComponent("icons/rotate_right.png", "右旋", GizmoCommand.ROTATE_RIGHT),

            new CommandComponent("icons/move_up.png", "上移", GizmoCommand.MOVE_UP),
            new CommandComponent("icons/move_right.png", "右移", GizmoCommand.MOVE_RIGHT),
            new CommandComponent("icons/move_down.png", "下移", GizmoCommand.MOVE_DOWN),
            new CommandComponent("icons/move_left.png", "左移", GizmoCommand.MOVE_LEFT),
    };
    // 总开关
    private static final ImageLabelComponent[] gameOps = {
            new ImageLabelComponent("icons/play.png", "开始游戏"),
            new ImageLabelComponent("icons/design.png", "设计模式"),
    };
    // javafx启动类编写规则 必须重写start
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("main.fxml")));
        // 关闭窗口直接结束
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        primaryStage.getIcons().add(new Image("icons/ball.png"));
        primaryStage.setTitle("GizmoBall");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        this.primaryStage = primaryStage;
        root.requestFocus();
    }
    // 游戏组件面板 球、圆形、矩形、三角形 挡板flipper、管道pipe、弯管道
    private void initGizmoGridPane() {
        for (int i = 0; i < gizmos.length; i++) {
            DraggableGizmoComponent gizmo = gizmos[i];
            gizmoGridPane.add(gizmo.createVBox(), i % 3, i / 3); // 3行3列
            // 添加拖放事件监听器
            // 拖放传参为gizmo的类型
            int finalI = i;
            gizmo.getImageWrapper().setOnDragDetected(event -> {
                if (!inDesign) {
                    return;
                }
                Dragboard db = gizmo.getImageView().startDragAndDrop(TransferMode.ANY);
                db.setDragView(gizmo.getImageView().getImage());
                ClipboardContent content = new ClipboardContent();
                content.put(GIZMO_TYPE_DATA, finalI);
                db.setContent(content);
                event.consume();
            });
        }
    }
    // 初始化操作选项
    private void initGizmoOp() {
        for (CommandComponent gizmoOp : gizmoOps) {
            gizmoOp.createVBox().setMaxWidth(70);
            gizmoOp.getImageWrapper().setOnMouseClicked(event -> {
                bindGizmoOp(gizmoOp.getGizmoCommand());
            });
        }
    }
    // 操作绑定
    private void bindGizmoOp(GizmoCommand command) {
        if (selectedBody == null || !inDesign) {
            return;
        }
        try {
            boolean success = gizmoOpHandler.handleCommand(command, selectedBody);
            if (success) {
                if (command == GizmoCommand.REMOVE) {
                    selectedBody = null;
                }
                highlightSelectedBody();
                drawGizmo(gizmoCanvas.getGraphicsContext2D());
            }
        } catch (Exception e) {
            Toast.makeText(primaryStage, "操作物件失败: " + e.getMessage(), 2000, 500, 500);
            log.error("操作物件失败: {}", e.getMessage());
        }
    }

    private void initGameOpHBox() {
        r = () -> {
            try {
                world.tick();
                Platform.runLater(() -> drawGizmo(gizmoCanvas.getGraphicsContext2D()));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    designGame();
                    Toast.makeText(primaryStage, e.getMessage(), 2000, 500, 500);
                    log.error("游戏发生异常: {}", e.getMessage());
                });
            }
        };
        // 开始游戏
        ImageLabelComponent play = gameOps[0];
        play.createVBox();
        play.getImageWrapper().setOnMouseClicked(event -> {
            startGame();
        });
        /* 用lambda表达式已优化
        play.getImageWrapper().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                startGame();
            }
        });
        * */

        // 暂停游戏 设计模式
        ImageLabelComponent design = gameOps[1];
        design.createVBox();
        design.getImageWrapper().setOnMouseClicked(event -> {
            designGame();
        });
    }

    private void startGame() {
        if (!inDesign) {
            return;
        }
        selectedBody = null;
        highlightSelectedBody();
        inDesign = false;
        world.snapshot();
        scheduledFuture[0] = scheduledExecutorService.scheduleAtFixedRate(r, 0, (long) (1000.0 / Settings.TICKS_PER_SECOND),
                TimeUnit.MILLISECONDS);
    }

    private void designGame() {
        if (inDesign) {
            return;
        }
        selectedBody = null;
        highlightSelectedBody();
        inDesign = true;
        scheduledFuture[0].cancel(true);

        try {
            world.restore();
        } catch (RuntimeException e) {
            Toast.makeText(primaryStage, e.getMessage(), 2000, 500, 500);
            log.error("恢复游戏失败: {}", e.getMessage());
        }
        drawGizmo(gizmoCanvas.getGraphicsContext2D());
    }

    /* 初始化游戏操作按钮 */
    private void initGizmoOpHBox() {
        // 初始物件操作
        initGizmoOp();
        // 初始化游戏操作（开始，设计）
        initGameOpHBox();

        for (int i = 0; i < 4; i++) {
            upperHBox.getChildren().add(gizmoOps[i].getVBox());
        }

        upperHBox.setSpacing(20);
        // lower
        lowerHBox.getChildren().add(gameOps[0].getVBox());

        BorderPane borderPane = new BorderPane();
        // 上移
        BorderPane.setAlignment(gizmoOps[4].getImageWrapper(), Pos.CENTER);
        // 右移
        BorderPane.setAlignment(gizmoOps[5].getImageWrapper(), Pos.CENTER_RIGHT);
        // 下移
        BorderPane.setAlignment(gizmoOps[6].getImageWrapper(), Pos.CENTER);
        // 左移
        BorderPane.setAlignment(gizmoOps[7].getImageWrapper(), Pos.CENTER_LEFT);

        borderPane.setTop(gizmoOps[4].getImageWrapper());
        borderPane.setRight(gizmoOps[5].getImageWrapper());
        borderPane.setBottom(gizmoOps[6].getImageWrapper());
        borderPane.setLeft(gizmoOps[7].getImageWrapper());

        Pane pane = new Pane();
        pane.setPrefWidth(60);
        pane.setPrefHeight(60);
        borderPane.setCenter(pane);
        lowerHBox.getChildren().add(borderPane);
        lowerHBox.getChildren().add(gameOps[1].getVBox());
    }

    /* 初始化世界 */
    private void initWorld() {
        double worldWidth = gizmoCanvas.getWidth();
        double worldHeight = gizmoCanvas.getHeight();
        world = new GridWorld(AbstractWorld.EARTH_GRAVITY, (int) worldWidth, (int) worldHeight, 30);
        preferredSize = new Vector2(world.getGridSize(), world.getGridSize());
        gizmoOpHandler = new GizmoOpHandler(world);
    }
    // xxx 文件操作
    private void initMenuItem() {
        // FileChooser文件选择对话框
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件");
        // 过滤器： 1. 筛选导入的json类型文件 2. .* 所有类型文件
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gizmo", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

        menuItemLoad.setOnAction(event -> {
            if (!inDesign) return;
            // File对象file保存选中的文件
            fileChooser.setInitialDirectory(new File("."));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    world.restore(file);
                    drawGizmo(gizmoCanvas.getGraphicsContext2D());
                } catch (Exception e) {
                    Toast.makeText(primaryStage, "加载文件失败: " + e.getMessage(), 1500, 200, 200);
                    log.error("加载文件失败: {}", e.getMessage());
                }
            }
        });

        menuItemSave.setOnAction(event -> {
            if (!inDesign) return;
            // set current time as filename
            String time = LocalDateTime.now().format(formatter);
            // 设置文件名
            fileChooser.setInitialFileName("customize" + time + ".json");
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try { // 保存当前游戏内容快照
                    world.snapshot(file);
                } catch (Exception e) {
                    Toast.makeText(primaryStage, "保存文件失败: " + e.getMessage(), 1500, 200, 200);
                    log.error("保存文件失败: {}", e.getMessage());
                }
            }
        });

        menuItemClear.setOnAction(event -> {
            //判断处于设计模式
            if (inDesign) {
                world.removeAllBodies();
                world.initBoundary();
                drawGizmo(gizmoCanvas.getGraphicsContext2D());
            }
        });

        menuItemAbout.setOnAction(event -> {
            if (!inDesign) {
                return;
            }
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("仓库地址");
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("icons/ball.png"));

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10, 10, 10, 10));

            Label content = new Label("弹球游戏");

            Label githubRepository = new Label("Github repository:");
            Hyperlink repository = new Hyperlink("https://github.com/QYR22/GizmoBall");
            repository.setOnAction(e -> {
                HostServices hostServices = PlayerPanel.this.getHostServices();
                hostServices.showDocument(repository.getText());
            });
            grid.add(content, 1, 0);
            grid.add(githubRepository, 0, 3);
            grid.add(repository, 1, 3);
            dialog.getDialogPane().setContent(grid);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.showAndWait();

        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initWorld();
        // 初始化界面
        initGizmoGridPane();
        initGizmoOpHBox();
        initCanvas();
        initMenuItem();

        // 绑定鼠标点击事件
        anchorPane.setOnMouseClicked(event -> {
            gizmoCanvas.requestFocus();
        });
        // 绑定键盘按键事件
        anchorPane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                case A:
                    if(inDesign) bindGizmoOp(GizmoCommand.MOVE_LEFT);
                    else world.flipperUp(Flipper.Direction.LEFT);
                    break;
                case RIGHT:
                case D:
                    if(inDesign) bindGizmoOp(GizmoCommand.MOVE_RIGHT);
                    else world.flipperUp(Flipper.Direction.RIGHT);
                    break;
                case UP:
                case W:
                    bindGizmoOp(GizmoCommand.MOVE_UP);
                    break;
                case DOWN:
                case S:
                    bindGizmoOp(GizmoCommand.MOVE_DOWN);
                    break;
                case F1:
                    isDebugMode = !isDebugMode;
                    break;
                case DELETE:
                    bindGizmoOp(GizmoCommand.REMOVE);
                    break;
                case SHIFT:
                    bindGizmoOp(GizmoCommand.ROTATE_LEFT);
                    break;
                case CONTROL:
                    bindGizmoOp(GizmoCommand.ROTATE_RIGHT);
                    break;
                case ENTER:
                    if (inDesign) {
                        startGame();
                    } else {
                        designGame();
                    }
                    break;
            }
        });
        // 松开按键释放flipper
        anchorPane.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT:
                    world.flipperDown(Flipper.Direction.LEFT);
                    break;
                case RIGHT:
                    world.flipperDown(Flipper.Direction.RIGHT);
                    break;
                case A:
                    if(!inDesign) world.flipperDown(Flipper.Direction.LEFT);
                    break;
                case D:
                    if(!inDesign) world.flipperDown(Flipper.Direction.RIGHT);
                    break;
            }
        });
        // 鼠标滚轮事件绑定
        anchorPane.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            if (deltaY > 0) {
                bindGizmoOp(GizmoCommand.ZOOM_IN);
            } else if (deltaY < 0) {
                bindGizmoOp(GizmoCommand.ZOOM_OUT);
            }
        });
    }

    // ------------canvas-----------------
    /* 选中物体荧光高亮边 */
    protected void highlightSelectedBody() {
        if (selectedBody == null) {
            gizmoOutlineRectangle.setVisible(false);
            return;
        }
        XXYY xxyy = selectedBody.getShape().createXXYY();
        GeometryUtil.padToSquare(xxyy);
        gizmoOutlineRectangle.setX(xxyy.minX);
        gizmoOutlineRectangle.setY(world.boundaryXXYY.maxY - xxyy.maxY);
        gizmoOutlineRectangle.setWidth(xxyy.maxX - xxyy.minX);
        gizmoOutlineRectangle.setHeight(xxyy.maxY - xxyy.minY);
        gizmoOutlineRectangle.setVisible(true);
    }

    private void initCanvas() {
        // 设置坐标系转换
        GraphicsContext gc = gizmoCanvas.getGraphicsContext2D();
        // gc.setFill(Color.DARKGRAY);
        // gc.fillRect(0,0,gizmoCanvas.getWidth(),gizmoCanvas.getHeight());
        Affine affine = new Affine();
        // 正常设置
        affine.appendScale(1, -1);
        affine.appendTranslation(0, -gizmoCanvas.getHeight());
        gc.setTransform(affine);

        // 拖放监听器 鼠标点击时绑定
        Canvas target = gizmoCanvas;
        target.setOnMouseClicked(event -> {
            target.requestFocus();
            if (event.getButton() == MouseButton.PRIMARY) {
                double x = event.getX();
                double y = world.boundaryXXYY.maxY - event.getY();
                // 获取当前index
                int[] index = world.getGridIndex(x, y);
                if (index != null) {
                    // 获取当前的网格位置 并高亮
                    selectedBody = world.gizmoGridBodies[index[0]][index[1]];
                    highlightSelectedBody();
                }
            }
        });
        // 对拖放手势 ref:https://blog.idrsolutions.com/how-to-implement-drag-and-drop-function-in-a-javafx-application/
        // 拖到画布上时显示物件预览/能否拖放
        target.setOnDragOver(event -> {
            if (event.getGestureSource() != target) {
                Dragboard db = event.getDragboard();
                int gizmoIndex = (int) db.getContent(GIZMO_TYPE_DATA);
                DraggableGizmoComponent gizmo = gizmos[gizmoIndex];
                // 显示预览图片
                double x = event.getX();
                double y = event.getY();
                int[] index = world.getGridIndex(x, y);
                if (index != null) {
                    PhysicsBody[][] gridBodies = world.gizmoGridBodies;
                    int i = index[0];
                    int j = gridBodies[0].length - index[1] - 1;
                    if (gridBodies[i][j] == null) {
                        previewImageView.setVisible(true);
                        previewImageView.setImage(gizmo.getImage());
                        previewImageView.setLayoutX(index[0] * world.getGridSize());
                        previewImageView.setLayoutY(index[1] * world.getGridSize());
                        event.acceptTransferModes(TransferMode.COPY);
                    }
                }
            }
            event.consume();
        });
        // 拖放手势离开目标方格时触发事件
        target.setOnDragExited(event -> {
            previewImageView.setVisible(false);
            event.consume();
        });
        // 用户松开鼠标按键触发事件
        target.setOnDragDropped(event -> {
            if (!inDesign) {
                return;
            }
            Dragboard db = event.getDragboard();
            int gizmoIndex = (int) db.getContent(GIZMO_TYPE_DATA);
            DraggableGizmoComponent gizmo = gizmos[gizmoIndex];

            int gridSize = world.getGridSize();
            Vector2 transformedCenter = new Vector2(event.getX(), world.boundaryXXYY.maxY - event.getY());
            // 以鼠标所在的点创建一个格子大小的XXYY
            XXYY centerXXYY = new XXYY(-gridSize / 2.0, -gridSize / 2.0, gridSize / 2.0, gridSize / 2.0);
            centerXXYY.translate(transformedCenter);
            // 移到边界内
            Vector2 offsetToBoundary = GeometryUtil.offsetToBoundary(centerXXYY, world.boundaryXXYY);
            transformedCenter.add(offsetToBoundary);
            centerXXYY.translate(offsetToBoundary);
            // 对齐到网格
            Vector2 snapped = GeometryUtil.snapToGrid(centerXXYY, gridSize, gridSize);
            transformedCenter.add(snapped);
            GizmoPhysicsBody pb = gizmo.createPhysicsBody(preferredSize, transformedCenter);
            try {
                gizmoOpHandler.addGizmo(pb);
            } catch (Exception e) {
                Toast.makeText(primaryStage, e.getMessage(), 1500, 200, 200);
            }
            drawGizmo(gc);
            previewImageView.setVisible(false);
            event.setDropCompleted(true);
            event.consume();
        });
        drawGizmo(gc);
    }

    private void clearCanvas(GraphicsContext gc) {
        gc.clearRect(0, 0, gizmoCanvas.getWidth(), gizmoCanvas.getHeight());
        gc.setFill(Color.web("#161A30"));
        gc.fillRect(0, 0, gizmoCanvas.getWidth(), gizmoCanvas.getHeight());
    }
    // 渲染游戏界面背景 画出网格
    private void drawGrid(GraphicsContext gc) {
        int gridSize = world.getGridSize();
        gc.setStroke(Color.LIGHTBLUE);
        gc.setLineWidth(1);
        for (int i = 0; i < gizmoCanvas.getWidth(); i += gridSize) {
            gc.strokeLine(i, 0, i, gizmoCanvas.getHeight());
        }
        for (int i = 0; i < gizmoCanvas.getHeight(); i += gridSize) {
            gc.strokeLine(0, i, gizmoCanvas.getWidth(), i);
        }
    }

    private static final DefaultCanvasRenderer canvasRenderer = DefaultCanvasRenderer.INSTANCE;

    private void drawGizmo(GraphicsContext gc) {
        clearCanvas(gc);
        drawGrid(gc);

        List<GizmoPhysicsBody> bodies = world.getBodies();
        for (GizmoPhysicsBody physicsBody : bodies) {
            if (physicsBody != null) {
                physicsBody.drawToCanvas(gc);
                if (isDebugMode) {
                    if (physicsBody.getShape() instanceof Ball) {
                        Vector2 linearVelocity = physicsBody.getLinearVelocity().copy();
                        Vector2 normalized = linearVelocity.copy().right().getNormalized();
                        double angularVelocity = physicsBody.getAngularVelocity() * 20;
                        Transform transform = physicsBody.getShape().getTransform();
                        gc.setStroke(Color.GREEN);
                        gc.strokeLine(transform.x, transform.y, transform.x + linearVelocity.x, transform.y + linearVelocity.y);
                        gc.setStroke(Color.RED);
                        gc.strokeLine(transform.x, transform.y, transform.x + normalized.x * angularVelocity, transform.y + normalized.y * angularVelocity);
                    } else {
                        AbstractShape shape = physicsBody.getShape();
                        if (shape instanceof Polygon) {
                            Polygon shape1 = (Polygon) shape;
                            Vector2[] normals = shape1.getNormals();
                            Transform transform = physicsBody.getShape().getTransform();
                            for (Vector2 normal : normals) {
                                Vector2 multiply = normal.copy().multiply(30);
                                Vector2 transformed = transform.getTransformed(multiply);
                                gc.setStroke(Color.YELLOW);
                                gc.strokeLine(transform.x, transform.y, transformed.x, transformed.y);
                            }
                        }
                    }
                }
            } else {
                canvasRenderer.drawToCanvas(gc, physicsBody);
            }
        }
    }
    private boolean isDebugMode = false;
}

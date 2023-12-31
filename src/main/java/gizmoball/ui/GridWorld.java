package gizmoball.ui;

import gizmoball.engine.geometry.XXYY;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.physics.Mass;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.GizmoWorld;
import gizmoball.ui.component.GizmoType;
import gizmoball.ui.file.PersistentUtil;
import gizmoball.ui.visualize.GizmoPhysicsBody;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gizmoball.game.GizmoSettings.BOUNDARY_BUFFER;

@Getter
@Slf4j
public class GridWorld extends GizmoWorld {

    /* 网格边界XXYY */
    protected XXYY boundaryXXYY;

    /* 每个网格中对应的PhysicsBody 一个PhysicsBody e.g.triangle三角、圆、方形、占满完整的一格 */
    protected GizmoPhysicsBody[][] gizmoGridBodies;

    protected int gridSize;

    public GridWorld(Vector2 gravity) {
        this(gravity, 600, 600);
    }

    public GridWorld(Vector2 gravity, int width, int height) {
        this(gravity, width, height, 30);
    }

    public GridWorld(Vector2 gravity, int width, int height, int gridSize) {
        super(gravity);
        this.gridSize = gridSize;
        boundaryXXYY = new XXYY(0, 0, width, height);
        gizmoGridBodies = new GizmoPhysicsBody[(int) (width / gridSize)][(int) (height / gridSize)];
        initBoundary();
    }

    /** xxx 获取某个点对应的格子的下标
     * x : [0, GRID_SIZE)对应下标0
     * @return 如果超出格子范围，返回null; 范围内，则返回格子下标长度为2的数组[i, j]，对应gizmoGridBodies[i][j]
     */
    public int[] getGridIndex(double x, double y) {
        x = Precision.round(x, 10);
        y = Precision.round(y, 10);
        if (x < boundaryXXYY.minX || x > boundaryXXYY.maxX
                || y < boundaryXXYY.minY || y > boundaryXXYY.maxY) {
            return null;
        }
        int[] index = new int[2];
        index[0] = (int) (x / gridSize);
        index[1] = (int) (y / gridSize);
        return index;
    }

    private int[] getGridIndex(Vector2 position) {
        return getGridIndex(position.x, position.y);
    }

    public void setGrid(XXYY xxyy, GizmoPhysicsBody body) {
        int[] bottomLeft = getGridIndex(xxyy.getMinX(), xxyy.getMinY());
        int width = (int) Math.ceil((xxyy.getMaxX() - xxyy.getMinX()) / gridSize);
        int height = (int) Math.ceil((xxyy.getMaxY() - xxyy.getMinY()) / gridSize);

        for (int i = bottomLeft[0]; i < bottomLeft[0] + width; i++) {
            for (int j = bottomLeft[1]; j < bottomLeft[1] + height; j++) {
                gizmoGridBodies[i][j] = body;
            }
        }
    }
    /* xxx 检查`XXYY`指定范围的格子是否已被占用 同时会检查是否越界 */
    public boolean checkOverlay(XXYY xxyy, PhysicsBody body) {
        int[] bottomLeft = getGridIndex(xxyy.getMinX(), xxyy.getMinY());
        if (bottomLeft == null) {
            return true;
        }
        int[] topRight = getGridIndex(xxyy.getMaxX(), xxyy.getMaxY());
        if (topRight == null) {
            return true;
        }
        int width = (int) Math.ceil((xxyy.getMaxX() - xxyy.getMinX()) / gridSize);
        int height = (int) Math.ceil((xxyy.getMaxY() - xxyy.getMinY()) / gridSize);

        for (int i = bottomLeft[0]; i < bottomLeft[0] + width; i++) {
            for (int j = bottomLeft[1]; j < bottomLeft[1] + height; j++) {
                if (gizmoGridBodies[i][j] != null && body != gizmoGridBodies[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }

    public void initBoundary() {
        double worldWidth = boundaryXXYY.maxX;
        double worldHeight = boundaryXXYY.maxY;
        // 下
        createBoundary(worldWidth / 2 + BOUNDARY_BUFFER, worldHeight / 2, worldWidth / 2, -worldHeight / 2);
        // 上
        createBoundary(worldWidth / 2 + BOUNDARY_BUFFER, worldHeight / 2, worldWidth / 2 + BOUNDARY_BUFFER, worldHeight + worldHeight / 2);
        // 左
        createBoundary(worldWidth / 2, worldHeight / 2, -worldWidth / 2, worldHeight / 2);
        // 右
        createBoundary(worldWidth / 2, worldHeight / 2, worldWidth + worldWidth / 2, worldHeight / 2);
    }

    private void createBoundary(double halfWidth, double halfHeight, double x, double y) {
        Rectangle rectangle = new Rectangle(halfWidth, halfHeight);
        rectangle.getTransform().setX(x);
        rectangle.getTransform().setY(y);
        GizmoPhysicsBody border = new GizmoPhysicsBody(rectangle, GizmoType.BOUNDARY);
        border.setMass(new Mass(new Vector2(), 0.0, 0.0));
        border.setRestitution(0.95);
        border.setFriction(0.5);
        // 不放入格子
        super.addBody(border);
    }

    public void addBodyToGrid(PhysicsBody body) {
        if (body instanceof GizmoPhysicsBody) {
            this.addBody((GizmoPhysicsBody) body);
        }
    }

    @Override
    public void addBody(GizmoPhysicsBody body) {
        XXYY xxyy = body.getShape().createXXYY();
        GeometryUtil.padToSquare(xxyy);
        if (checkOverlay(xxyy, body)) {
            throw new IllegalArgumentException("物件重叠");
        }
        super.addBody(body);
        setGrid(xxyy, body);
    }

    @Override
    public void removeBody(GizmoPhysicsBody body) {
        super.removeBody(body);
    }
    // 清除 将所有格子中对象置空
    @Override
    public void removeAllBodies() {
        super.removeAllBodies();
        for (GizmoPhysicsBody[] gizmoGridBody : this.gizmoGridBodies) {
            Arrays.fill(gizmoGridBody, null);
        }
    }

    private String snapshot;

    /** 获取当前世界的物体快照到默认文件".snapshot.json"
     * @return json格式的字符串表示每一个物体
     */
    public String snapshot() {
        return snapshot(new File(".snapshot.json"));
    }

    /**
     * 获取当前世界的物体快照到指定文件
     * @return json格式的字符串表示每一个物体
     */
    public String snapshot(File file) {
        try {
            log.info("快照.snapshot.json正在保存，wait for a minute.");
            List<PhysicsBody> bodiesToJson = new ArrayList<>();
            bodyTypeMap.forEach((k, v) -> {
                if (k != GizmoType.BOUNDARY) {
                    bodiesToJson.addAll(v);
                }
            });
            // 将每个物体对象转换为json数据格式写入`.snapshot.json`中
            snapshot = PersistentUtil.toJsonString(bodiesToJson);
            log.debug("take snapshot: {}", snapshot);
            PersistentUtil.write(snapshot, file);
            log.info("已保存{}个物体对象信息到快照文件中。", bodiesToJson.size());
        } catch (Exception e) {
            log.error("snapshot error", e);
        }
        return snapshot;
    }

    public void restore() throws RuntimeException {
        restore(snapshot);
    }

    /* 恢复世界的物体 从`.snapshot.json`中获取json字符串 */
    public void restore(String snapshot) throws RuntimeException {
        try {
            log.info("读取`.snapshot.json`加载ing");
            // json数据 <-> object 转换
            List<PhysicsBody> obj = PersistentUtil.fromJsonString(snapshot);
            removeAllBodies();
            initBoundary();
            // 所有对象在格子上显示
            obj.forEach(this::addBodyToGrid);

            log.info("成功从`.snapshot.json`中加载{}个物体", obj.size());
        } catch (IOException e) {
            log.error("restore error", e);
            throw new RuntimeException(e);
        }
    }

    public void restore(File file) throws RuntimeException {
        try {
            restore(PersistentUtil.readFromFile(file));
        } catch (IOException e) {
            log.error("restore error", e);
            throw new RuntimeException(e);
        }
    }
    @Override
    public void tick() throws RuntimeException{
        super.tick();
        int ballSize = bodyTypeMap.get(GizmoType.BALL).size();
        // 没有放球游戏不开始。
        if (ballSize == 0) {
            throw new RuntimeException("游戏结束");
        }
    }
}

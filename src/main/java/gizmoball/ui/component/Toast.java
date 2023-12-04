package gizmoball.ui.component;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/* 向玩家展示消息，e.g.游戏结束 + 各类异常*/
public class Toast {

    public static void makeText(Stage ownerStage, String toastMsg, int toastDelay, int fadeInDelay, int fadeOutDelay) {
        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        // 这个配色好
        Text text = new Text(toastMsg);
        text.setFill(Color.web("#007f7f"));
        text.setFont(Font.font(20));
        Image image = new Image("icons/warning.png");
        ImageView imageView = new ImageView(image);

        GridPane root = new GridPane();
        root.setHgap(5);
        root.add(imageView, 0, 1);
        root.add(text, 2, 1);
        root.setStyle("-fx-background-radius: 5; -fx-background-color: rgba(255, 255, 230); -fx-padding: 10px;" +
                "-fx-border-color: rgba(230, 162, 60,0.2); -fx-border-width: 1;-fx-border-radius: 5;");
        root.setOpacity(0);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);
        toastStage.show();
        // 这里用时间轴对象控制弹窗的显示时间
        // 控制模式：显示 --> 淡出
        // 显示：让线程休眠一段时间用于展示弹框信息
        Timeline fadeInTimeline = new Timeline();
        KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(fadeInDelay), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
        fadeInTimeline.getKeyFrames().add(fadeInKey1);
        fadeInTimeline.setOnFinished(ae ->
                new Thread(() -> {
                    try {
                        Thread.sleep(toastDelay);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Timeline fadeOutTimeline = new Timeline();
                    KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(fadeOutDelay), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
                    fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
                    fadeOutTimeline.setOnFinished(aeb -> toastStage.close());
                    fadeOutTimeline.play();
                }).start());
        fadeInTimeline.play();
    }
}

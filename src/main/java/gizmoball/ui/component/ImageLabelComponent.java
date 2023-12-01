package gizmoball.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.Data;

import java.util.Objects;

@Data
public class ImageLabelComponent {

    private Image image;

    private String labelText;

    private double imageWidth;

    private double imageHeight;

    private VBox vBox;

    private ImageView imageView;

    /* 因为SVG图片虽然是200*200，但并不是全占满的，点击透明处无效。
    为了点击方格都能选中该物件 解决方法：包装ImageView，添加事件监听器。 */
    private Pane imageWrapper;

    private Label label;

    public ImageLabelComponent(String resource, String labelText) {
        this(resource, labelText, 60, 60);
    }

    public ImageLabelComponent(String resource, String labelText, int imageWidth, int imageHeight) {
        this.image = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resource)),
                imageWidth, imageHeight, true, true);
        this.labelText = labelText;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public ImageLabelComponent(Image image, String labelText) {
        this.image = image;
        this.labelText = labelText;
        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
    }

    public VBox createVBox() {
        vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);

        imageWrapper = new Pane();
        imageWrapper.setMaxSize(imageWidth, imageHeight);
        imageWrapper.setCursor(javafx.scene.Cursor.HAND);

        imageView = new ImageView(image);
        imageView.setFitHeight(imageHeight);
        imageView.setFitWidth(imageWidth);
        imageWrapper.getChildren().add(imageView);
        vBox.getChildren().add(imageWrapper);

        label = new Label(this.labelText);
        label.setPrefWidth(100);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        vBox.getChildren().add(label);

        return vBox;
    }
}

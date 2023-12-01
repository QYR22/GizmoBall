package gizmoball.ui.visualize;

import javafx.scene.paint.Paint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 做一个数据封装
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SVGPath {

    protected String path;

    protected Paint fill;

}

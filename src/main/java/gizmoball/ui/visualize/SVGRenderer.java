package gizmoball.ui.visualize;

import gizmoball.engine.geometry.XXYY;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

/* 仅支持只包含path节点的svg，svg不能被格式化
   `javafx.scene.image.Image`不支持SVG
 */
public class SVGRenderer implements CanvasRenderer {

    private final SVGNode svgNode;

    public SVGRenderer(String resource) {
        this.svgNode = SVGNode.fromResource(getClass().getClassLoader().getResourceAsStream(resource));
    }

    @Override
    public void drawToCanvas(GraphicsContext gc, PhysicsBody body) {
        AbstractShape shape = body.getShape();
        Transform transform = shape.getTransform();

        XXYY xxyy = body.getShape().createXXYY();
        double shapeHeight = xxyy.maxY - xxyy.minY;
        double shapeWidth = xxyy.maxX - xxyy.minX;

        if (svgNode != null) {
            gc.save();

            Affine affine = new Affine();
            affine.appendRotation(transform.getAngle(), transform.x, transform.y);
            affine.appendTranslation(transform.getX() - shapeWidth / 2,
                    transform.getY() - shapeHeight / 2 + shapeHeight);
            affine.appendScale(shapeHeight / 1024, -shapeWidth / 1024);
            gc.transform(affine);

            for (SVGPath svgPath : svgNode.getSvgPaths()) {
                // fill必须放在循环中，不然svg中只能有一种fill
                gc.beginPath();
                gc.appendSVGPath(svgPath.getPath());
                gc.setFill(svgPath.getFill());
                gc.fill();
                gc.closePath();
            }
            gc.restore();
        }
    }
}

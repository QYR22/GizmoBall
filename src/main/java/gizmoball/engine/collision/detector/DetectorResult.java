package gizmoball.engine.collision.detector;

import gizmoball.engine.geometry.shape.AbstractShape;
import lombok.AllArgsConstructor;
import lombok.Data;

//碰撞检测的结果
@Data
@AllArgsConstructor
public class DetectorResult {
    private boolean hasCollision;
    private AbstractShape approximateShape;
}

package gizmoball.game.filter;

import gizmoball.engine.collision.CollisionFilter;
import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.Ball;
import gizmoball.game.entity.CurvedPipe;
import lombok.AllArgsConstructor;

import static gizmoball.game.GizmoSettings.PIPE_PIERCE_BIAS;
@AllArgsConstructor
public class CurvedPipeCollisionFilter implements CollisionFilter {

    private final Vector2 gravity;

    @Override
    public boolean isAllowedBroadPhase(PhysicsBody body1, PhysicsBody body2) {
        return true;
    }

    @Override
    public boolean isAllowedNarrowPhase(PhysicsBody body1, PhysicsBody body2) {
        return true;
    }

    @Override
    public boolean isAllowedManifold(PhysicsBody body1, PhysicsBody body2, AbstractShape shape, Penetration penetration) {
        AbstractShape shape1=body1.getShape();
        AbstractShape shape2=body2.getShape();
        if(!(shape2 instanceof CurvedPipe)){
            return true;
        }
        Ball ball=(Ball) shape1;
        CurvedPipe curvedPipe=(CurvedPipe) shape2;
        Transform transform1=curvedPipe.getTransform();
        Transform transform2=ball.getTransform();
        Vector2 curvedPipe_v1=transform1.getTransformed(curvedPipe.getVertices()[0]);
        Vector2 curvedPipe_v2=transform1.getTransformed(curvedPipe.getVertices()[1]);
        Vector2 curvedPipe_v3=transform1.getTransformed(curvedPipe.getVertices()[2]);
        Vector2 edge_cv2=curvedPipe_v2;
        Vector2 edge_cv3=new Vector2(transform2.getX(),transform2.getY());
        Vector2 r1=curvedPipe_v2.to(curvedPipe_v1);
        Vector2 r2=curvedPipe_v2.to(curvedPipe_v3);
        Vector2 c2c=edge_cv2.to(edge_cv3);
        boolean isInSide=r1.cross(c2c)*c2c.cross(r2)>=0 && r1.cross(c2c)* r1.cross(r2)>=0;
        boolean isIndsideQuar=c2c.getMagnitude()<curvedPipe.getRadius();
        if(isIndsideQuar&&isInSide){
            maintainPipeProperty(body1,body2,edge_cv3,edge_cv2);
            if(c2c.getMagnitude()+ball.getRadius()>=curvedPipe.getRadius()){
                penetration.getNormal().negate();
                penetration.setDepth(ball.getRadius()*2- penetration.getDepth());
                return true;
            }
            return false;
        }else if(isIndsideQuar){
            double magnitude1 = curvedPipe_v1.to(edge_cv3).getMagnitude();
            double magnitude2 = curvedPipe_v3.to(edge_cv3).getMagnitude();
            if (magnitude1 + PIPE_PIERCE_BIAS < ball.getRadius() || magnitude2 + PIPE_PIERCE_BIAS < ball.getRadius()) {
                return true;
            }
            if (penetration.getNormal().dot(r1.getNormalized()) < 1e5 * Epsilon.E ||
                    penetration.getNormal().dot(r2.getNormalized()) < 1e5 * Epsilon.E) {
                return false;
            }
            return true;
        } else if (isInSide) {
            return true;
        }else{
            double magnitude0 = c2c.project(r1.getNormalized()).getMagnitude();
            double magnitude1 = c2c.project(r2.getNormalized()).getMagnitude();
            if (magnitude0 - PIPE_PIERCE_BIAS <= r1.getMagnitude() &&
                    magnitude1 - PIPE_PIERCE_BIAS <= r1.getMagnitude()) {
                return false;
            }
            return true;
        }
    }

    private void maintainPipeProperty(PhysicsBody body1, PhysicsBody body2, Vector2 edge_cv3, Vector2 edge_cv2) {
    }
}

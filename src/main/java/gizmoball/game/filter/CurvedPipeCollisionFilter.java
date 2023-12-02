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

/*碰撞器过滤接口：处理球和弯管之间的碰撞检测和相应*/
@AllArgsConstructor
public class CurvedPipeCollisionFilter implements CollisionFilter {

    private final Vector2 gravity;
    /*用于在不同的碰撞检测阶段判断是否允许两个物体发生碰撞的方法*/
    //BroadPhase：粗略的筛查阶段
    @Override
    public boolean isAllowedBroadPhase(PhysicsBody body1, PhysicsBody body2) {
        return true;
    }
    //NarrowPhase：精确检测阶段
    @Override
    public boolean isAllowedNarrowPhase(PhysicsBody body1, PhysicsBody body2) {
        return true;
    }
    /*根据球和弯管的形状、位置、方向信息判断球和弯管的相对位置关系（内部/外部/边缘），返回布尔值表示是否允许碰撞发生*/
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
        //计算弯管的三个顶点和球中心点的向量，以及弯管两条边的向量
        Vector2 curvedPipe_v1=transform1.getTransformed(curvedPipe.getVertices()[0]);
        Vector2 curvedPipe_v2=transform1.getTransformed(curvedPipe.getVertices()[1]);
        Vector2 curvedPipe_v3=transform1.getTransformed(curvedPipe.getVertices()[2]);
        Vector2 edge_cv2=curvedPipe_v2;
        Vector2 edge_cv3=new Vector2(transform2.getX(),transform2.getY());
        Vector2 r1=curvedPipe_v2.to(curvedPipe_v1);
        Vector2 r2=curvedPipe_v2.to(curvedPipe_v3);
        Vector2 c2c=edge_cv2.to(edge_cv3);
        //计算球的中心点是否在弯管内部，即是否在弯管的两条边所构成的扇形区域内
        boolean isInSide=r1.cross(c2c)*c2c.cross(r2)>=0 && r1.cross(c2c)* r1.cross(r2)>=0;
        boolean isIndsideQuar=c2c.getMagnitude()<curvedPipe.getRadius();
        if(isIndsideQuar&&isInSide){
            //球中心点在弯管内部
            maintainPipeProperty(body1,body2,edge_cv3,edge_cv2);//维持弯管的特性
            if(c2c.getMagnitude()+ball.getRadius()>=curvedPipe.getRadius()){
                //判断球的边缘是否与弯管的两个端点相交，是则返回true，允许碰撞
                penetration.getNormal().negate();
                penetration.setDepth(ball.getRadius()*2- penetration.getDepth());
                return true;
            }
            //表示不允许碰撞
            return false;
        }else if(isIndsideQuar){
            //球中心点在弯管半径内
            double magnitude1 = curvedPipe_v1.to(edge_cv3).getMagnitude();
            double magnitude2 = curvedPipe_v3.to(edge_cv3).getMagnitude();
            //判断球的边缘是否与弯管的两个端点相交，是则返回true，表示允许碰撞
            if (magnitude1 + PIPE_PIERCE_BIAS < ball.getRadius() || magnitude2 + PIPE_PIERCE_BIAS < ball.getRadius()) {
                return true;
            }
            //判断穿透的法线方向是否与弯管的两条边的方向垂直，是则返回false，表示不允许碰撞
            if (penetration.getNormal().dot(r1.getNormalized()) < 1e5 * Epsilon.E ||
                    penetration.getNormal().dot(r2.getNormalized()) < 1e5 * Epsilon.E) {
                return false;
            }
            return true;
        } else if (isInSide) {
            return true;
        }else{
            //判断球的中心点到弯管的两条边的投影长度是否小于等于弯管的两条边的长度，如果是，返回false，表示不允许碰撞
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

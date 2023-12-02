package gizmoball.engine.collision;

import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;

//  碰撞检测的过滤器，宽碰撞检测->窄碰撞检测->计算碰撞流形
public interface CollisionFilter {

    boolean isAllowedBroadPhase(PhysicsBody body1, PhysicsBody body2);

    boolean isAllowedNarrowPhase(PhysicsBody body1, PhysicsBody body2);

    boolean isAllowedManifold(PhysicsBody body1, PhysicsBody body2, AbstractShape shape, Penetration penetration);

}

package gizmoball.game.listener;

import gizmoball.engine.collision.CollisionFilter;
import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.detector.DetectorResult;
import gizmoball.engine.collision.detector.DetectorUtil;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.Ball;
import gizmoball.game.entity.BlackHole;

import gizmoball.ui.visualize.GizmoPhysicsBody;
import javafx.util.Pair;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class BlackHoleListener implements TickListener{
    private final List<PhysicsBody> balls;
    private final List<PhysicsBody> blackHoles;
    private final List<GizmoPhysicsBody> allBodies;
    private final BasicCollisionDetector basicCollisionDetector = new BasicCollisionDetector() {
        @Override
        public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect(List<PhysicsBody> bodies1, List<PhysicsBody> bodies2, List<CollisionFilter> filters) {
            List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds = new ArrayList<>();
            for (PhysicsBody body1 : bodies1) {
                Ball ball = (Ball) body1.getShape();
                for (PhysicsBody body2 : bodies2) {
                    //对每一对球和黑洞进行碰撞检测，调用私有方法gravityAccumulation，用于计算球受到黑洞的引力作用，以及DetectorUtil.circleDetect，用于判断球和黑洞是否相交
                    BlackHole blackhole = (BlackHole) body2.getShape();
                    gravityAccumulation(body1, body2);
                    DetectorResult detect = DetectorUtil.circleDetect(ball, blackhole, null, null);
                    //球和黑洞相交，说明发生了碰撞，将一个空的碰撞流形和碰撞物体对作为一个元组添加到列表中
                    if (detect.isHasCollision()) {
                        manifolds.add(new Pair<>(null, new Pair<>(body1, body2)));
                    }
                }
            }
            return manifolds;
        }
        /*计算球受到黑洞的引力作用，并将引力作为一个力添加到球的力列表*/
        private void gravityAccumulation(PhysicsBody body1, PhysicsBody body2) {
            //球和黑洞的位置，创建一个从球到黑洞的向量，表示引力的方向
            Vector2 bc1 = new Vector2(body1.getShape().getTransform().x, body1.getShape().getTransform().y);
            Vector2 bc2 = new Vector2(body2.getShape().getTransform().x, body2.getShape().getTransform().y);
            Vector2 force = bc1.to(bc2);
            //计算球到黑洞的距离，根据万有引力公式，计算引力的大小，与引力的方向相乘，得到引力的向量
            //获取黑洞的半径，将引力的大小乘以一个常数，表示黑洞的引力强度
            double r = force.getMagnitude();
            force.normalize();
            BlackHole blackhole = (BlackHole) body2.getShape();
            //获取球的质量，将引力的大小乘以球的质量，表示引力对球的作用力
            force.multiply(body1.getMass().getMass() * blackhole.getRadius() * 10000 / r / r);
            //将引力作为一个力添加到球的力列表中
            body1.getForces().add(force);

        }
    };
    /*在每一帧中监听球和黑洞之间的碰撞事件*/
    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        //遍历所有的球，清除它们的所有力，只保留重力的作用
        for(PhysicsBody ball:balls){
            ball.getForces().clear();
        }
        //调用基本碰撞检测器，检测所有的球和黑洞之间是否发生了碰撞，并返回一个包含碰撞流形和碰撞物体对的列表
        List<Pair<Manifold,Pair<PhysicsBody,PhysicsBody>>> detect=basicCollisionDetector.detect(balls,blackHoles,null);
        //对于每一对发生碰撞的球和黑洞，将球从球的列表和所有物体的列表中移除，表示球被黑洞吞噬
        for(Pair<Manifold,Pair<PhysicsBody,PhysicsBody>> pair:detect){
            PhysicsBody ball=pair.getValue().getKey();
            balls.remove(ball);
            allBodies.remove(ball);
        }

        return new ArrayList<>();
    }
}

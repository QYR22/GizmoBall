package gizmoball.game.listener;

import gizmoball.engine.collision.CollisionFilter;
import gizmoball.engine.collision.Penetration;
import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.detector.DetectorResult;
import gizmoball.engine.collision.detector.DetectorUtil;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.collision.manifold.ManifoldSolver;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.Ball;
import javafx.util.Pair;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class BallListener implements TickListener {
    private final List<PhysicsBody> balls;
    private final BasicCollisionDetector basicCollisionDetector=new BasicCollisionDetector(){
        private Manifold processDetect(ManifoldSolver manifoldSolver, Ball ball1, Ball ball2) {
            if (!DetectorUtil.AABBDetect(ball1, ball2)) {
                return null;
            }

            Penetration penetration = new Penetration();
            DetectorResult detect = DetectorUtil.circleDetect(ball1, ball2, null, penetration);
            if (!detect.isHasCollision()) {
                return null;
            }
            Manifold manifold = new Manifold();
            if (!manifoldSolver.getManifold(penetration, ball1, ball2, detect.getApproximateShape(), manifold)) {
                return null;
            }
            return manifold;
        }
        @Override
        public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect(List<PhysicsBody> bodies1, List<PhysicsBody> bodies2, List<CollisionFilter> listeners) {
            //创建一个空的列表，用于存储碰撞流形和碰撞物体对
            List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds = new ArrayList<>();
            //碰撞流形求解器，用于计算碰撞流形的信息
            ManifoldSolver manifoldSolver = new ManifoldSolver();
            //对每一对球进行碰撞检测，对processDetect传入碰撞流形求解器和两个球对象作为参数。
            for (int i = 0; i < bodies1.size() - 1; i++) {
                for (int j = i + 1; j < bodies1.size(); j++) {
                    PhysicsBody physicsBody1 = bodies1.get(i);
                    PhysicsBody physicsBody2 = bodies1.get(j);
                    Ball ball1 = (Ball) physicsBody1.getShape();
                    Ball ball2 = (Ball) physicsBody2.getShape();
                    Manifold manifold = this.processDetect(manifoldSolver, ball1, ball2);
                    //如果processDetect方法返回了一个非空的碰撞流形，说明两个球发生了碰撞，将碰撞流形和碰撞物体对作为一个元组添加到列表中
                    if (manifold != null) {
                        Pair<PhysicsBody, PhysicsBody> physicsBodyPhysicsBodyPair = new Pair<>(physicsBody1, physicsBody2);
                        manifolds.add(new Pair<>(manifold, physicsBodyPhysicsBodyPair));
                    }
                }
            }
            return manifolds;
        }
    };
    /*用于在每一帧中调用碰撞检测器，检测所有球之间的碰撞，并返回一个包含碰撞流形和碰撞物体对的列表*/
    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        return basicCollisionDetector.detect(balls,null,null);
    }

}

package gizmoball.engine.collision.detector;

import gizmoball.engine.Settings;
import gizmoball.engine.collision.CollisionFilter;
import gizmoball.engine.collision.Penetration;
import gizmoball.engine.collision.contact.ContactConstraint;
import gizmoball.engine.collision.contact.SequentialImpulses;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.collision.manifold.ManifoldSolver;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

//基础碰撞检测器
public class BasicCollisionDetector implements CollisionDetector {

    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect(List<PhysicsBody> bodies1, List<PhysicsBody> bodies2, List<CollisionFilter> filters) {
        List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds = new ArrayList<>();
        ManifoldSolver manifoldSolver = new ManifoldSolver();
        for (PhysicsBody body1 : bodies1) {
            for (PhysicsBody body2 : bodies2) {
                Manifold manifold = this.processDetect(manifoldSolver, body1, body2, filters);
                if (manifold != null) {
                    Pair<PhysicsBody, PhysicsBody> physicsBodyPhysicsBodyPair = new Pair<>(body1, body2);
                    manifolds.add(new Pair<>(manifold, physicsBodyPhysicsBodyPair));
                }
            }
        }
        return manifolds;
    }
//    碰撞检测

    private Manifold processDetect(ManifoldSolver manifoldSolver, PhysicsBody body1, PhysicsBody body2, List<CollisionFilter> filters) {
        AbstractShape shape1 = body1.getShape();
        AbstractShape shape2 = body2.getShape();
        // BroadPhase
        for (CollisionFilter filter : filters) {
            if (!filter.isAllowedBroadPhase(body1, body2)) return null;
        }
        if (!DetectorUtil.XXYYDetect(shape1, shape2)) {
            return null;
        }
        // NarrowPhase
        for (CollisionFilter filter : filters) {
            if (!filter.isAllowedNarrowPhase(body1, body2)) return null;
        }
        Penetration penetration = new Penetration();
        DetectorResult detect = DetectorUtil.satDetect(shape1, shape2, null, penetration);
        if (!detect.isHasCollision()) {
            return null;
        }
        // ManifoldSolver
        for (CollisionFilter filter : filters) {
            if (!filter.isAllowedManifold(body1, body2, detect.getApproximateShape(), penetration)) return null;
        }
        Manifold manifold = new Manifold();
        if (!manifoldSolver.getManifold(penetration, shape1, shape2, detect.getApproximateShape(), manifold)) {
            return null;
        }
        return manifold;
    }

    @Override
    public List<ContactConstraint> preLocalSolve(List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds) {
        List<ContactConstraint> contactConstraints = new ArrayList<>();
        for (Pair<Manifold, Pair<PhysicsBody, PhysicsBody>> manifold : manifolds) {
            ContactConstraint contactConstraint = new ContactConstraint(manifold.getValue());
            contactConstraint.update(manifold.getKey());
            contactConstraints.add(contactConstraint);
        }
        return contactConstraints;
    }
//    碰撞解析
    @Override
    public void LocalSolve(SequentialImpulses solver, Vector2 gravity, List<ContactConstraint> constraints, List<PhysicsBody> bodies) {
        for (PhysicsBody body : bodies) {
            body.integrateVelocity(gravity);
        }
        solver.initialize(constraints);
        for (int i = 0; i < Settings.DEFAULT_SOLVER_ITERATIONS; i++) {
            solver.solveVelocityConstraints(constraints);
        }
        for (PhysicsBody body : bodies) {
            body.integratePosition();
        }
        for (int i = 0; i < Settings.DEFAULT_SOLVER_ITERATIONS; i++) {
            solver.solvePositionConstraints(constraints);
        }
    }

}

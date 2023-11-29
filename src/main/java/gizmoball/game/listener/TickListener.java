package gizmoball.game.listener;

import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.physics.PhysicsBody;
import javafx.util.Pair;
import java.util.List;


public interface TickListener {
    List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() ;
}

package gizmoball.game.listener;

import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.Flipper;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static gizmoball.game.GizmoSettings.DEFAULT_FLIPPER_ANGULAR;
import static gizmoball.game.GizmoSettings.DEFAULT_FLIPPER_ROTATION;

@Getter
@Setter
public class FlipperListener implements TickListener{
    private final List<PhysicsBody> balls;
    private final List<PhysicsBody> flippers;
    private final BasicCollisionDetector basicCollisionDetector=new BasicCollisionDetector();
    public FlipperListener(List<PhysicsBody> balls, List<PhysicsBody> flippers) {
        this.balls = balls;
        this.flippers=flippers;
    }

    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        updatePosition(flippers);
        return basicCollisionDetector.detect(balls,flippers,new ArrayList<>());
    }

    private void updatePosition(List<PhysicsBody> flippers) {
        for(PhysicsBody body:flippers){
            Flipper flipper=(Flipper)body.getShape();
            double angular=flipper.getAngular();
            if(flipper.getIsUp()){
                if(angular<30){
                    setUpVelocity(body);
                    flipper.flip(DEFAULT_FLIPPER_ROTATION);
                    continue;
                }
                body.setAngularVelocity(0);
            }else{
                if(flipper.getAngular()>0){
                    setDownVelocity(body);
                    flipper.flip(-DEFAULT_FLIPPER_ROTATION);
                }else if(flipper.getAngular()==0){
                    body.setAngularVelocity(0);
                }
            }
        }
    }

    private void setDownVelocity(PhysicsBody body) {
        Flipper flipper=(Flipper) body.getShape();
        if(flipper.getDirection()==Flipper.Direction.LEFT){
            body.setAngularVelocity(-DEFAULT_FLIPPER_ANGULAR);
        }else{
            body.setAngularVelocity(DEFAULT_FLIPPER_ANGULAR);
        }
    }

    private void setUpVelocity(PhysicsBody body) {
        Flipper flipper=(Flipper) body.getShape();
        if(flipper.getDirection()==Flipper.Direction.LEFT){
            body.setAngularVelocity(DEFAULT_FLIPPER_ANGULAR);
        }else{
            body.setAngularVelocity(-DEFAULT_FLIPPER_ANGULAR);
        }
    }
}

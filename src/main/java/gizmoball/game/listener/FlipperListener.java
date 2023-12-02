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
        //更新挡板位置
        updatePosition(flippers);
        //碰撞检测
        return basicCollisionDetector.detect(balls,flippers,new ArrayList<>());
    }
    /*更新挡板位置*/
    private void updatePosition(List<PhysicsBody> flippers) {
        //获取所有挡板的形状角度信息
        for(PhysicsBody body:flippers){
            Flipper flipper=(Flipper)body.getShape();
            double angular=flipper.getAngular();
            //判断挡板是否处于上升态，
            if(flipper.getIsUp()){
                //判断角度是否小于30度
                if(angular<30){
                    //调用setUpVelocity设置角速度
                    setUpVelocity(body);
                    //用于旋转挡板一个固定的角度
                    flipper.flip(DEFAULT_FLIPPER_ROTATION);
                    continue;
                }
                //不是，则设置角速度为0
                body.setAngularVelocity(0);
            }else{
                //弹簧不处于上升状态
                //判断角度是否大于零
                if(flipper.getAngular()>0){
                    //调用setDownVelocity设置角速度
                    setDownVelocity(body);
                    //用于旋转弹簧一个负的固定的角度
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

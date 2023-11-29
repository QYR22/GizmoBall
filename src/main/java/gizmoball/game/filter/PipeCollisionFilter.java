package gizmoball.game.filter;

import gizmoball.engine.collision.CollisionFilter;
import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.CurvedPipe;
import gizmoball.game.entity.Ball;
import gizmoball.game.entity.Pipe;
import lombok.AllArgsConstructor;

import static gizmoball.game.GizmoSettings.PIPE_PIERCE_BIAS;

@AllArgsConstructor
public class PipeCollisionFilter implements CollisionFilter {
    private final Vector2 gravity;
    private double ballX,ballY;
    private double minX,maxX,minY,maxY;
    private double radius;
    private Pipe.PipeDirection pipeDirection;
    private Vector2 linearVelocity;
    public PipeCollisionFilter(Vector2 gravity){
        this.gravity=gravity;
    }
    @Override
    public boolean isAllowedBroadPhase(PhysicsBody body1,PhysicsBody body2){
        return true;
    }
    @Override
    public boolean isAllowedNarrowPhase(PhysicsBody body1,PhysicsBody body2){
        return true;
    }
    @Override
    public boolean isAllowedManifold(PhysicsBody body1,PhysicsBody body2,AbstractShape shape,Penetration penetration){
        AbstractShape ballShape=body1.getShape();
        AbstractShape pipeShape=body2.getShape();
        if(!(pipeShape instanceof Pipe)){
            return true;
        }
        Ball ball=(Ball)ballShape;
        Pipe pipe=(Pipe)pipeShape;
        double pipeX= pipe.getTransform().getX();
        double pipeY=pipe.getTransform().getY();
        ballX=ball.getTransform().getX();
        ballY=ball.getTransform().getY();
        radius=ball.getRadius();
        pipeDirection=pipe.getPipeDirection();
        minX=pipeX- pipe.getHalfWidth();
        maxX=pipeX+pipe.getHalfWidth();
        minY=pipeY-pipe.getHalfHeight();
        maxY=pipeY+pipe.getHalfHeight();
        linearVelocity=body1.getLinearVelocity();
        if(radius>pipe.getHalfHeight()){
            return true;
        }
        if(isOutPipe()){
            return true;
        }
        if(!isCollision()){
            if(isInPipe()){
                maintainPipeProperty(body1,body2);
            }
        }
        if(!isInPipe()){
            if(penetration.getNormal().dot(pipe.getNormals()[0])<1e5* Epsilon.E||penetration.getNormal().dot(pipe.getNormals()[1])<1e5*Epsilon.E){
                return false;
            }
            return true;
        }
        maintainPipeProperty(body1, body2);
        fixCollision(ball,pipe,penetration);
        return true;

    }

    private void fixCollision(Ball ball, Pipe pipe, Penetration penetration) {
        Vector2 normal=penetration.getNormal();
        if(pipeDirection==Pipe.PipeDirection.TRANSVERSE){
            boolean isHigh=ball.getTransform().y>pipe.getTransform().y;
            if(isHigh){
                penetration.setDepth(radius-(maxY-ball.getTransform().y));
                normal.x=0;
                normal.y=1;
            }else{
                penetration.setDepth(radius-(ball.getTransform().y-minY));
                normal.x=0;
                normal.y=-1;
            }
        }else{
            boolean isRight=ball.getTransform().x>pipe.getTransform().x;
            if (isRight) {
                penetration.setDepth(radius - (maxX - ball.getTransform().x));
                normal.x = 1;
                normal.y = 0;
            } else {
                penetration.setDepth(radius - (ball.getTransform().x - minX));
                normal.x = -1;
                normal.y = 0;
            }
        }
    }

    private void maintainPipeProperty(PhysicsBody body1, PhysicsBody body2) {
        body1.getForces().clear();
        body1.integrateVelocity(gravity.getNegative());
        if(body1.getShape().getRate()==body2.getShape().getRate()){
            if(pipeDirection==Pipe.PipeDirection.TRANSVERSE){
                linearVelocity.y=0;
            }else if(pipeDirection==Pipe.PipeDirection.VERTICAL){
                linearVelocity.x=0;
            }
        }
        if(linearVelocity.getMagnitude()<30){
            linearVelocity.multiply(30/linearVelocity.getMagnitude());
        }
    }

    private boolean isCollision() {
        if(pipeDirection==Pipe.PipeDirection.TRANSVERSE){
            return ballY+radius>maxY||ballY-radius<minY;
        }else{
            return ballX+radius>maxX||ballX-radius<minX;
        }
    }

    private boolean isInPipe() {
        return ballX>minX&&ballX<maxX&&ballY<maxY&&ballY>minY;
    }

    private boolean isOutPipe() {
        if(pipeDirection== Pipe.PipeDirection.TRANSVERSE){
            if(ballY>maxY||ballY<minY){
                return true;
            }
            if(ballX<minX){
                Vector2 upX=new Vector2(minX-ballX,maxY-ballY);
                Vector2 downX=new Vector2(minX-ballX,minY-ballY);
                if(upX.getMagnitude()+PIPE_PIERCE_BIAS<radius||downX.getMagnitude()+PIPE_PIERCE_BIAS<radius){
                    return true;
                }
            }else if(ballX>maxX){
                Vector2 upX=new Vector2(maxX-ballX,maxY-ballY);
                Vector2 downX=new Vector2(maxX - ballX, minY - ballY);
                if(upX.getMagnitude()+PIPE_PIERCE_BIAS<radius||downX.getMagnitude()+PIPE_PIERCE_BIAS<radius){
                    return true;
                }
            }
        }else{
            if(ballX>maxX||ballX<minX){
                return true;
            }
            if(ballY<minY){
                Vector2 upX=new Vector2(minX-ballX,maxY-ballY);
                Vector2 downX=new Vector2(minX-ballX,minY-ballY);
                if(upX.getMagnitude()+PIPE_PIERCE_BIAS<radius||downX.getMagnitude()+PIPE_PIERCE_BIAS<radius){
                    return true;
                }
            }else if(ballY>maxY){
                Vector2 upX=new Vector2(maxX-ballX,maxY-ballY);
                Vector2 downX=new Vector2(maxX - ballX, minY - ballY);
                if(upX.getMagnitude()+PIPE_PIERCE_BIAS<radius||downX.getMagnitude()+PIPE_PIERCE_BIAS<radius){
                    return true;
                }
            }
        }
        return false;
    }

}

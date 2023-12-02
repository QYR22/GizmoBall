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
    /*根据球和管道的形状、位置、方向信息判断球和弯管的相对位置关系（内部/外部/边缘），返回布尔值表示是否允许碰撞发生*/
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
    /*修正球和管道之间碰撞的结果*/
    private void fixCollision(Ball ball, Pipe pipe, Penetration penetration) {
        //获取穿透的法线方向和深度，以及球和管道的位置、方向等信息
        Vector2 normal=penetration.getNormal();
        //判断管道的方向是横向/纵向，以及球的位置是在管道的上方/下方/左边/右边
        //重新计算穿透的深度，并将法线方向设为与管道的边缘垂直的方向
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
    /*维持弯管特性*/
    private void maintainPipeProperty(PhysicsBody body1, PhysicsBody body2) {
        body1.getForces().clear();//清除球的所有力，只保留重力作用
        body1.integrateVelocity(gravity.getNegative());
        //判断球和弯管的旋转速率是否相同，如果相同，根据弯管的方向，将球的线速度在相应的轴上设为零，使球沿着弯管的弧线运动。
        if(body1.getShape().getRate()==body2.getShape().getRate()){
            if(pipeDirection==Pipe.PipeDirection.TRANSVERSE){
                linearVelocity.y=0;
            }else if(pipeDirection==Pipe.PipeDirection.VERTICAL){
                linearVelocity.x=0;
            }
        }
        //如果球的线速度小于30，将球的线速度放大到30，使球不会因为速度过小而停留在弯管内。
        if(linearVelocity.getMagnitude()<30){
            linearVelocity.multiply(30/linearVelocity.getMagnitude());
        }
    }
    /*判断是否发生碰撞*/
    private boolean isCollision() {
        if(pipeDirection==Pipe.PipeDirection.TRANSVERSE){
            return ballY+radius>maxY||ballY-radius<minY;
        }else{
            return ballX+radius>maxX||ballX-radius<minX;
        }
    }
    /*判断是否在管内*/
    private boolean isInPipe() {
        return ballX>minX&&ballX<maxX&&ballY<maxY&&ballY>minY;
    }
    /*判断是否在管外*/
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

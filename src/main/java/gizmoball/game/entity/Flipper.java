package gizmoball.game.entity;

import com.sun.javafx.scene.traversal.Direction;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Triangle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*挡板*/
public class Flipper extends Triangle {
    private Direction direction;
    public enum Direction{
        LEFT,RIGHT
    }
    private Boolean isUp;

    private double angular;
    private void init(){
        this.isUp=false;
        this.angular=0;
    }
    /*反序列化*/
    @Deprecated
    public Flipper(){}
    /*只有方向信息*/
    public Flipper(Direction direction){
        this.direction=direction;
    }
    /*無位移旋轉*/
    public Flipper(Vector2[] vertices, Direction direction){
        super(vertices,new Transform());
        this.direction=direction;
        init();
    }
    /*有位移旋轉*/
    public Flipper(Vector2[] vertices,Transform transform,Direction direction){
        super(vertices,transform);
        this.direction=direction;
        init();
    }
    public void rise(){this.isUp=true;}
    public void down(){this.isUp=false;}
    public void flip(double beta){
        angular+=beta;
        if(this.direction==Direction.LEFT){
            Vector2 transformed=getTransform().getTransformed(vertices[0]);
            rotate(beta/180*Math.PI,transformed.x,transformed.y);
        }else{
            Vector2 transformed=getTransform().getTransformed(vertices[1]);
            rotate(-beta/180*Math.PI,transformed.x,transformed.y);
        }
    }
}

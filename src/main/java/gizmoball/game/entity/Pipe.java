package gizmoball.game.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

/*管道*/
@Getter
@Setter
public class Pipe extends Rectangle {
    /*無位移旋轉*/
    public Pipe(double midWidth,double midHeight) {
        super(midWidth,midHeight);
        this.pipeDirection=PipeDirection.TRANSVERSE;
    }
    /*有位移旋轉*/
    public Pipe(double midWidth,double midHeight,Transform transform){
        super(midWidth,midHeight,transform);
        this.pipeDirection=PipeDirection.TRANSVERSE;

    }
    public enum PipeDirection {
        TRANSVERSE,VERTICAL
    }
    private PipeDirection pipeDirection;
    @Deprecated
    public Pipe(){this(0,0);}
    @Override
    public void rotate(double beta,double x,double y){
        transform.rotate(beta,x,y);
        if(Math.abs(this.getTransform().sint)>1/2){
            this.pipeDirection=PipeDirection.VERTICAL;
        }
        else{
            this.pipeDirection=PipeDirection.TRANSVERSE;
        }
    }
}

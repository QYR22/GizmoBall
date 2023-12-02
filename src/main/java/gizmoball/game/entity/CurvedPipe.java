package gizmoball.game.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.QuarterCircle;
/*弯管*/
public class CurvedPipe extends QuarterCircle {
    @Deprecated
    public CurvedPipe(){};
    public CurvedPipe(double radius){
        super(radius);
    }
    public CurvedPipe(Transform transform,double radius){
        super(transform,radius);
    }
}

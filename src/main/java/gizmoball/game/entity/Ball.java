package gizmoball.game.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Circle;
/*球*/
public class Ball extends Circle {
    @Deprecated
    public Ball(){}
    public Ball(double radius){
        super(radius);
    }
    public Ball(double radius, Transform transform){
        super(radius, transform);
    }
}

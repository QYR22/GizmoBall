package gizmoball.game.entity;

import gizmoball.engine.geometry.shape.Circle;

import gizmoball.engine.geometry.Transform;
/*黑洞*/
public class BlackHole extends Circle {
    @Deprecated
    public BlackHole(){}
    public BlackHole(double radius){
        super(radius);
    }
    public BlackHole(double radius, Transform transform){
        super(radius,transform);
    }
}

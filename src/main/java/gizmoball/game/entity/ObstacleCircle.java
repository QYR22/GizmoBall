package gizmoball.game.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Circle;


/*障碍物圆*/
public class ObstacleCircle extends Circle {
    public ObstacleCircle(){}
    public ObstacleCircle(double radius){super(radius);}
    public ObstacleCircle(double radius, Transform transform){
        super(radius,transform);
    }

}

package gizmoball.game.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Rectangle;
/*障碍物矩形*/
public class ObstacleRectangle extends Rectangle {
    public ObstacleRectangle(double midWidth, double midHeight, Transform transform){
        super(midWidth,midHeight,transform);
    }
    public ObstacleRectangle(double midWidth,double midHeight){
        super(midWidth,midHeight);
    }
    public ObstacleRectangle(){}
}

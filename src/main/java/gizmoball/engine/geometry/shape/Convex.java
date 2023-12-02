package gizmoball.engine.geometry.shape;

import gizmoball.engine.geometry.Vector2;

// 表示形状的接口
public interface Convex {
//    获取分离轴
    Vector2[] getAxes(Vector2[] foci);

//    获取焦点
    Vector2[] getFoci();

//    获取最远的碰撞特征
    Vector2 getFarthestFeature(Vector2 vector);

//    获取顶点
    Vector2 getFarthestPoint(Vector2 vector);

}

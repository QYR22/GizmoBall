package gizmoball.engine.geometry.shape;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.XXYY;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.Mass;
import lombok.Getter;
import lombok.Setter;

// 抽象的形状（一个基类，所有形状都需要继承这个基类）
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class", visible = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractShape implements Convex {


    protected Transform transform;

    protected int rate = 1;

    protected AbstractShape() {
        this(new Transform());
    }

    protected AbstractShape(Transform transform) {
        this.transform = transform;
    }

    public abstract void zoom(int rate);


    public abstract XXYY createXXYY();


    public abstract Interval project(Vector2 axis);

    public abstract Mass createMass(double density);


    public void rotate(double c, double s, double x, double y) {
        transform.rotate(c, s, x, y);
    }

    public void rotate(double theta, double x, double y) {
        transform.rotate(theta, x, y);
    }

    public void translate(double x, double y) {
        transform.translate(x, y);
    }

    public void translate(Vector2 vector2) {
        transform.translate(vector2);
    }

    public Vector2 getLocalPoint(Vector2 worldPoint) {
        return this.transform.getInverseTransformed(worldPoint);
    }
}

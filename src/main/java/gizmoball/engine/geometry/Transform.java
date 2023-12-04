package gizmoball.engine.geometry;

import gizmoball.engine.collision.Interval;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


//表示物体的旋转或位移
@Getter
@Setter
@ToString
public class Transform {
//    cos(t)的值
    public double cost;
//    sin(t)的值
    public double sint;
//    x轴坐标
    public double x;
//    y轴坐标
    public double y;

    public Transform() {
        this(1.0, 0.0, 0.0, 0.0);
    }

    public Transform(double cost, double sint, double x, double y) {
        this.cost = cost;
        this.sint = sint;
        this.x = x;
        this.y = y;
    }

    public Transform copy() {
        return new Transform(this.cost, this.sint, this.x, this.y);
    }
//    旋转
    public void rotate(double c, double s, double x, double y) {
        double cosT = this.cost;
        double sinT = this.sint;
        this.cost = Interval.sandwich(c * cosT - s * sinT, -1.0, 1.0);
        this.sint = Interval.sandwich(s * cosT + c * sinT, -1.0, 1.0);
        if (Math.abs(this.cost) < Epsilon.E) {
            this.cost = 0;
        }
        if (Math.abs(this.sint) < Epsilon.E) {
            this.sint = 0;
        }
        double cx = this.x - x;
        double cy = this.y - y;
        this.x = c * cx - s * cy + x;
        this.y = s * cx + c * cy + y;
    }

    public void rotate(double theta, double x, double y) {
        this.rotate(Math.cos(theta), Math.sin(theta), x, y);
    }
//    平移
    public void translate(double x, double y) {
        this.x += x;
        this.y += y;
    }

    public void translate(Vector2 vector2) {
        this.translate(vector2.x, vector2.y);
    }

//    变换x坐标
    public double getTransformedX(Vector2 vector) {
        return this.cost * vector.x - this.sint * vector.y + this.x;
    }

//    变换y坐标
    public double getTransformedY(Vector2 vector) {
        return this.sint * vector.x + this.cost * vector.y + this.y;
    }


//    旋转+平移
    public void transform(Vector2 vector) {
        double x = vector.x;
        double y = vector.y;
        vector.x = this.cost * x - this.sint * y + this.x;
        vector.y = this.sint * x + this.cost * y + this.y;
    }

//    进行旋转/平移变换，返回变换后的新向量
    public Vector2 getTransformed(Vector2 vector) {
        Vector2 tv = new Vector2();
        double x = vector.x;
        double y = vector.y;
        tv.x = this.cost * x - this.sint * y + this.x;
        tv.y = this.sint * x + this.cost * y + this.y;
        return tv;
    }

    public Vector2 getInverseTransformed(Vector2 vector) {
        Vector2 tv = new Vector2();
        double tx = vector.x - this.x;
        double ty = vector.y - this.y;
        tv.x = this.cost * tx + this.sint * ty;
        tv.y = -this.sint * tx + this.cost * ty;
        return tv;
    }

    public Vector2 getTransformedR(Vector2 vector) {
        Vector2 v = new Vector2();
        double x = vector.x;
        double y = vector.y;
        v.x = this.cost * x - this.sint * y;
        v.y = this.sint * x + this.cost * y;
        return v;
    }

    public void transformR(Vector2 vector) {
        double x = vector.x;
        double y = vector.y;
        vector.x = this.cost * x - this.sint * y;
        vector.y = this.sint * x + this.cost * y;
    }

    public Vector2 getInverseTransformedR(Vector2 vector) {
        Vector2 v = new Vector2();
        double x = vector.x;
        double y = vector.y;
        // sin(-a) = -sin(a)
        v.x = this.cost * x + this.sint * y;
        v.y = -this.sint * x + this.cost * y;
        return v;
    }

//    根据cos和sin数值获取角度
    public double getAngle() {
        double angle = 0;
        if (cost > 0 && sint > 0) {
            angle = Math.toDegrees(Math.asin(sint));
        } else if (cost > 0 && sint < 0) {
            angle = Math.toDegrees(Math.asin(sint));
        } else if (cost < 0 && sint > 0) {
            angle = 180 - Math.toDegrees(Math.asin(sint));
        } else if (cost < 0 && sint < 0) {
            angle = -180 - Math.toDegrees(Math.asin(sint));
        } else if (cost == 0 && sint > 0) {
            angle = 90;
        } else if (cost == 0 && sint < 0) {
            angle = -90;
        } else if (cost > 0 && sint == 0) {
            angle = 0;
        } else if (cost < 0 && sint == 0) {
            angle = 180;
        }
        return angle;
    }

}

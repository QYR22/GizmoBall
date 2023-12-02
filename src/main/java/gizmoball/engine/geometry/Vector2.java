package gizmoball.engine.geometry;

import lombok.ToString;

//向量
@ToString
public class Vector2 {

    public double x;

    public double y;

    public Vector2() {
        this(0, 0);
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        Vector2 v = (Vector2) obj;
        return Math.abs(this.x - v.x) <= Epsilon.E && Math.abs(this.y - v.y) <= Epsilon.E;
    }
    public Vector2 copy() {
        return new Vector2(this.x, this.y);
    }
//    距离的平方
    public double distanceSquared(Vector2 point) {
        double dx = this.x - point.x;
        double dy = this.y - point.y;
        return dx * dx + dy * dy;
    }
//    模长
    public double getMagnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

//    模长平方
    public double getMagnitudeSquared() {
        return this.x * this.x + this.y * this.y;
    }
//    向量加法
    public Vector2 add(Vector2 vector) {
        this.x += vector.x;
        this.y += vector.y;
        return this;
    }

    public Vector2 sum(Vector2 vector) {
        return new Vector2(this.x + vector.x, this.y + vector.y);
    }
//    向量减法
    public Vector2 subtract(Vector2 vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        return this;
    }

    public Vector2 difference(Vector2 vector) {
        return new Vector2(this.x - vector.x, this.y - vector.y);
    }


    public Vector2 to(Vector2 vector) {
        return new Vector2(vector.x - this.x, vector.y - this.y);
    }

//    向量乘法（乘以一个常熟）
    public Vector2 multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }
//    向量除法（除以一个常数）
    public Vector2 divide(double scalar) {
        this.x /= scalar;
        this.y /= scalar;
        return this;
    }

    public Vector2 product(double scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

//    向量点乘
    public double dot(Vector2 vector) {
        return this.x * vector.x + this.y * vector.y;
    }

//    向量叉乘
    public double cross(Vector2 vector) {
        return this.x * vector.y - this.y * vector.x;
    }

    public Vector2 cross(double z) {
        return new Vector2(-this.y * z, this.x * z);
    }

//    判断是否是零向量
    public boolean isZero() {
        return Math.abs(this.x) <= Epsilon.E && Math.abs(this.y) <= Epsilon.E;
    }

//    向量取反
    public Vector2 negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public Vector2 getNegative() {
        return new Vector2(-this.x, -this.y);
    }

//  置为左手系的法线
    public Vector2 left() {
        double temp = this.x;
        this.x = this.y;
        this.y = -temp;
        return this;
    }

//    置为右手系的法线
    public Vector2 right() {
        left();
        return negate();
    }

//    置零
    public Vector2 zero() {
        this.x = 0.0;
        this.y = 0.0;
        return this;
    }

//    规范化
    public Vector2 getNormalized() {
        double magnitude = this.getMagnitude();
        if (magnitude <= Epsilon.E) return new Vector2();
        magnitude = 1.0 / magnitude;
        return new Vector2(this.x * magnitude, this.y * magnitude);
    }

    public double normalize() {
        double magnitude = Math.sqrt(this.x * this.x + this.y * this.y);
        if (magnitude <= Epsilon.E) return 0;
        double m = 1.0 / magnitude;
        this.x *= m;
        this.y *= m;
        return magnitude;
    }

//    投影
    public Vector2 project(Vector2 vector) {
        double dotProd = this.dot(vector);
        double denominator = vector.dot(vector);
        if (denominator <= Epsilon.E) return new Vector2();
        denominator = dotProd / denominator;
        return new Vector2(denominator * vector.x, denominator * vector.y);
    }

}

package gizmoball.engine.geometry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 坐标轴
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XXYY {

    public double minX;

    public double minY;

    public double maxX;

    public double maxY;

    public XXYY(XXYY xxyy) {
        this.minX = xxyy.minX;
        this.minY = xxyy.minY;
        this.maxX = xxyy.maxX;
        this.maxY = xxyy.maxY;
    }

    public boolean overlaps(XXYY xxyy) {
        return this.minX <= xxyy.maxX &&
                this.maxX >= xxyy.minX &&
                this.minY <= xxyy.maxY &&
                this.maxY >= xxyy.minY;
    }

    public void translate(double x, double y) {
        this.minX += x;
        this.minY += y;
        this.maxX += x;
        this.maxY += y;
    }

    public void translate(Vector2 vector2) {
        this.translate(vector2.x, vector2.y);
    }
}

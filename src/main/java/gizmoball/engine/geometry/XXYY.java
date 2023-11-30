package gizmoball.engine.geometry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /**
     * 判断XXYY是否重叠
     *
     * @param xxyy XXYY
     * @return boolean
     */
    public boolean overlaps(XXYY xxyy) {
        return this.minX <= xxyy.maxX &&
                this.maxX >= xxyy.minX &&
                this.minY <= xxyy.maxY &&
                this.maxY >= xxyy.minY;
    }

    /**
     * 按给定坐标平移
     *
     * @param x x轴平移距离
     * @param y y轴平移距离
     */
    public void translate(double x, double y) {
        this.minX += x;
        this.minY += y;
        this.maxX += x;
        this.maxY += y;
    }

    /**
     * 按给定{@link Vector2}平移
     *
     * @param vector2 给定平移{@link Vector2}
     */
    public void translate(Vector2 vector2) {
        this.translate(vector2.x, vector2.y);
    }
}

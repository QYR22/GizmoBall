package gizmoball.ui;

import gizmoball.engine.geometry.XXYY;
import gizmoball.engine.geometry.Vector2;

public class GeometryUtil {

    /**
     * 计算XXYY到边界的偏移，返回能将XXYY移动到边界内的最小向量
     *
     * @param xxyy     目标xxyy
     * @param boundary 边界
     * @return 返回能将XXYY移动到边界内的最小向量
     */
    public static Vector2 offsetToBoundary(XXYY xxyy, XXYY boundary) {
        double offsetX = 0;
        double offsetY = 0;
        if (xxyy.minX < boundary.minX) {
            offsetX = -xxyy.minX;
        } else if (xxyy.maxX > boundary.maxX) {
            offsetX = boundary.maxX - xxyy.maxX;
        }
        if (xxyy.minY < boundary.minY) {
            offsetY = -xxyy.minY;
        } else if (xxyy.maxY > boundary.maxY) {
            offsetY = boundary.maxY - xxyy.maxY;
        }
        return new Vector2(offsetX, offsetY);
    }

    /**
     * 计算XXYY对齐到网格的偏移，返回能将XXYY对齐到网格的最小向量
     *
     * @param xxyy       目标xxyy
     * @param gridWidth  网格宽度
     * @param gridHeight 网格高度
     * @return 到对其网格需要应用的偏移
     */
    public static Vector2 snapToGrid(XXYY xxyy, int gridWidth, int gridHeight) {
        // 以左下角对齐
        return snapToGrid(new Vector2(xxyy.minX, xxyy.minY), gridWidth, gridHeight);
    }

    /**
     * 计算点对齐到网格的偏移，返回能将点对齐到网格的最小向量
     *
     * @param vector     点
     * @param gridWidth  网格宽度
     * @param gridHeight 网格高度
     * @return 到对其网格需要应用的偏移
     */
    public static Vector2 snapToGrid(Vector2 vector, int gridWidth, int gridHeight) {
        double x = vector.x;
        double y = vector.y;
        double offsetX = x % gridWidth;
        double offsetY = y % gridHeight;
        offsetX = offsetX > gridWidth / 2.0 ? gridWidth - offsetX : -offsetX;
        offsetY = offsetY > gridHeight / 2.0 ? gridHeight - offsetY : -offsetY;
        return new Vector2(offsetX, offsetY);
    }

    /**
     * 将XXYY补成一个正方形
     *
     * @param xxyy /
     */
    public static void padToSquare(XXYY xxyy) {
        double width = xxyy.maxX - xxyy.minX;
        double height = xxyy.maxY - xxyy.minY;
        double delta = Math.abs(width - height);
        if (width > height) {
            xxyy.minY -= delta / 2.0;
            xxyy.maxY += delta / 2.0;
        } else {
            xxyy.minX -= delta / 2.0;
            xxyy.maxX += delta / 2.0;
        }
    }
}

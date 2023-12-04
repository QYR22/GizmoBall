package gizmoball.engine.collision;

import lombok.AllArgsConstructor;
import lombok.Data;

//  投影间隔（用于宽碰撞检测）
@Data
@AllArgsConstructor
public class Interval {

    protected double min;

    protected double max;
//    判断是否发生重叠
    public boolean overlaps(Interval interval) {
        return !(this.min > interval.max || interval.min > this.max);
    }

    public double getOverlap(Interval interval) {
        if (this.overlaps(interval)) {
            return Math.min(this.max, interval.max) - Math.max(this.min, interval.min);
        }
        return 0;
    }

    public boolean containsExclusive(Interval interval) {
        return interval.min > this.min && interval.max < this.max;
    }

    public static double sandwich(double value, double left, double right) {
        return (value <= right && value >= left) ? value : (value < left ? left : right);
    }

}

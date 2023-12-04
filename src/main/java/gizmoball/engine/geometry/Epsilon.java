package gizmoball.engine.geometry;

public class Epsilon {

    public static final double E = Epsilon.compute();
//    返回最小精度
    public static double compute() {
        double e = 0.5;
        while (1.0 + e > 1.0) {
            e *= 0.5;
        }
        return e;
    }
}

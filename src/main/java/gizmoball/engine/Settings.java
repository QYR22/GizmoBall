package gizmoball.engine;


//该类定义与物理引擎和游戏逻辑相关的参数以及他们的默认数值
public class Settings {
//    每秒钟角速度减小的比例
    public static final double DEFAULT_ANGULAR_DAMPING = 0.01;
//    每秒更新次数
    public static final int TICKS_PER_SECOND = 60;
//    每个Tick的时常
    public static final double DEFAULT_TICK_FREQUENCY = 1.0 / TICKS_PER_SECOND;
//    最大平移距离
    public static final double DEFAULT_MAXIMUM_TRANSLATION = 30.0;
//    最大旋转角度
    public static final double DEFAULT_MAXIMUM_ROTATION = 0.5 * Math.PI;
//    迭代次数
    public static final int DEFAULT_SOLVER_ITERATIONS = 25;
//    线性容差
    public static final double DEFAULT_LINEAR_TOLERANCE = 0.05;
//    最大线性校正值
    public static final double DEFAULT_MAXIMUM_LINEAR_CORRECTION = 1;
//    比例因子
    public static final double DEFAULT_BAUMGARTE = 1;

}

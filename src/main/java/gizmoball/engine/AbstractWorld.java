package gizmoball.engine;

import gizmoball.engine.collision.contact.SequentialImpulses;
import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.detector.CollisionDetector;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;

import java.util.ArrayList;
import java.util.List;

// 抽象的世界类，用于描述整个弹球游戏中的物体所处的虚拟环境
public abstract class AbstractWorld<T extends PhysicsBody> {
//    重力
    public static final Vector2 EARTH_GRAVITY = new Vector2(0, -9.8);

    protected Vector2 gravity;

    protected final List<T> bodies;
//    碰撞检测器
    protected final CollisionDetector collisionDetector;
//    碰撞解析器
    protected final SequentialImpulses solver;


    public AbstractWorld(Vector2 gravity) {
        this.gravity = gravity;
        this.bodies = new ArrayList<>();

        this.collisionDetector = new BasicCollisionDetector();
        this.solver = new SequentialImpulses();
    }
//    对物体进行增加、删除操作
    public void addBody(T body) {
        this.bodies.add(body);
    }

    public void removeBody(T body) {
        this.bodies.remove(body);
    }

    public void removeAllBodies() {
        bodies.clear();
    }
//    获取所有物体
    public List<T> getBodies() {
        return bodies;
    }

//    每个tick更新一次游戏
    public abstract void tick();


}

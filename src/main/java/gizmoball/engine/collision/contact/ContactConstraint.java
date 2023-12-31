package gizmoball.engine.collision.contact;

import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.collision.manifold.ManifoldPoint;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//碰撞相关的约束信息
@Data
@AllArgsConstructor
public class ContactConstraint {
    private Pair<PhysicsBody, PhysicsBody> pair;
    private List<SolvableContact> contacts;
//    碰撞法线
    private Vector2 normal;
//    碰撞切线
    private Vector2 tangent;
//    阻力系数
    private double friction;
//    回弹系数
    private double restitution;
//    回弹启动速度
    private double restitutionVelocity;
//    切线速度
    private double tangentSpeed;
//    列表大小
    private int size;

    public ContactConstraint(Pair<PhysicsBody, PhysicsBody> pair) {
        this.pair = pair;
        this.contacts = new ArrayList<>(2);
        this.normal = new Vector2();
        this.tangent = new Vector2();
        this.tangentSpeed = 0;
        this.size = 0;
    }


    public void update(Manifold manifold) {
        PhysicsBody body1 = getBody1();
        PhysicsBody body2 = getBody2();

        Vector2 normal = manifold.getNormal();
        this.normal.x = normal.x;
        this.normal.y = normal.y;

        this.tangent.x = normal.y;
        this.tangent.y = -normal.x;

        this.friction = this.getMixedFriction(body1, body2);
        this.restitution = this.getMixedRestitution(body1, body2);
        this.restitutionVelocity = this.getMixedRestitutionVelocity(body1, body2);

        this.tangentSpeed = 0;

        List<ManifoldPoint> points = manifold.getPoints();
        List<SolvableContact> contacts = new ArrayList<>(points.size());
        for (ManifoldPoint point : points) {
            SolvableContact newContact = new SolvableContact(
                    point.getPoint(),
                    point.getDepth(),
                    body1.getShape().getLocalPoint(point.getPoint()),
                    body2.getShape().getLocalPoint(point.getPoint()));
            contacts.add(newContact);
        }
        this.contacts.clear();
        this.contacts.addAll(contacts);
        this.size = this.contacts.size();
    }
//    碰撞对象1
    public PhysicsBody getBody1() {
        return pair.getKey();
    }

//    碰撞对象2
    public PhysicsBody getBody2() {
        return pair.getValue();
    }


    private double getMixedRestitutionVelocity(PhysicsBody body1, PhysicsBody body2) {
        return Math.max(body1.getRestitutionVelocity(), body2.getRestitutionVelocity());
    }

    private double getMixedRestitution(PhysicsBody body1, PhysicsBody body2) {
        return Math.max(body1.getRestitution(), body2.getRestitution());
    }

    private double getMixedFriction(PhysicsBody body1, PhysicsBody body2) {
        return Math.min(body1.getRestitution(), body2.getRestitution());
    }

}

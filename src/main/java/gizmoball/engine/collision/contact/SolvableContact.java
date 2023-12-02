package gizmoball.engine.collision.contact;


import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import lombok.AllArgsConstructor;
import lombok.Data;

//接触信息
@Data
@AllArgsConstructor
final class SolvableContact {
    private Vector2 p;
    private double depth;
    private Vector2 p1;
    private Vector2 p2;
    private Vector2 r1;
    private Vector2 r2;
    double jn;
    double jt;
    double jp;
    private double massN;
    private double massT;
    double vb;

    public SolvableContact(Vector2 point, double depth, Vector2 p1, Vector2 p2) {
        this.p = point;
        this.depth = depth;
        this.p1 = p1;
        this.p2 = p2;
    }
}

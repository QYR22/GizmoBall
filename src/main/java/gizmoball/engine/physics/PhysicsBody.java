package gizmoball.engine.physics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gizmoball.engine.Settings;
import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
// 代表一个物体
@Data
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class", visible = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhysicsBody {
    protected Mass mass;
    protected final Vector2 linearVelocity;
    protected double angularVelocity;
    protected double linearDamping;
    protected final Vector2 force;
    protected final List<Vector2> forces;
    protected final AbstractShape shape;
    protected double gravityScale;
    protected double friction;
    protected double restitution;
    protected double restitutionVelocity;
    public PhysicsBody() {
        this(null);
    }

    public PhysicsBody(AbstractShape shape) {
        this.mass = new Mass();
        this.linearVelocity = new Vector2();
        this.linearDamping = 0.0;
        this.force = new Vector2();
        this.forces = new ArrayList<>();
        this.shape = shape;
        this.gravityScale = 10.0;
    }

    private void accumulate() {
        this.force.zero();
        int size = this.forces.size();
        if (size > 0) {
            Iterator<Vector2> it = this.forces.iterator();
            while (it.hasNext()) {
                Vector2 force = it.next();
                this.force.add(force);
                it.remove();
            }
        }
    }
    public void integrateVelocity(Vector2 gravity) {
        if (this.mass.getType() == MassType.INFINITE || this.mass.getType() == null) {
            return;
        }
        double elapsedTime = Settings.DEFAULT_TICK_FREQUENCY;
        this.accumulate();

        double mass = this.mass.getMass();
        double inverseMass = this.mass.getInverseMass();
        if (inverseMass > Epsilon.E) {
            this.linearVelocity.x += elapsedTime * inverseMass * (gravity.x * this.gravityScale * mass + this.force.x);
            this.linearVelocity.y += elapsedTime * inverseMass * (gravity.y * this.gravityScale * mass + this.force.y);
        }

        if (this.linearDamping != 0.0) {
            double linear = 1.0 - elapsedTime * this.linearDamping;
            linear = Interval.sandwich(linear, 0.0, 1.0);

            this.linearVelocity.x *= linear;
            this.linearVelocity.y *= linear;
        }

        double angular = 1.0 - elapsedTime * Settings.DEFAULT_ANGULAR_DAMPING;
        angular = Interval.sandwich(angular, 0.0, 1.0);

        this.angularVelocity *= angular;
    }

    private boolean isStatic() {
        return this.mass.getType() == MassType.INFINITE &&
                Math.abs(this.linearVelocity.x) <= Epsilon.E &&
                Math.abs(this.linearVelocity.y) <= Epsilon.E &&
                Math.abs(this.angularVelocity) <= Epsilon.E;
    }

    public void integratePosition() {
        double elapsedTime = Settings.DEFAULT_TICK_FREQUENCY;
        double maxTranslation = Settings.DEFAULT_MAXIMUM_TRANSLATION;
        double maxTranslationSquared = maxTranslation * maxTranslation;
        double maxRotation = Settings.DEFAULT_MAXIMUM_ROTATION;

        if (this.isStatic()) {
            return;
        }
        double translationX = this.linearVelocity.x * elapsedTime;
        double translationY = this.linearVelocity.y * elapsedTime;
        double translationMagnitudeSquared = translationX * translationX + translationY * translationY;

        if (translationMagnitudeSquared > maxTranslationSquared) {
            double translationMagnitude = Math.sqrt(translationMagnitudeSquared);
            double ratio = maxTranslation / translationMagnitude;

            this.linearVelocity.multiply(ratio);
            translationX *= ratio;
            translationY *= ratio;
        }

        double rotation = this.angularVelocity * elapsedTime;

        if (rotation > maxRotation) {
            double ratio = maxRotation / Math.abs(rotation);
            this.angularVelocity *= ratio;
            rotation *= ratio;
        }

        this.getShape().translate(translationX, translationY);
        Vector2 center = this.getShape().getTransform().getTransformed(this.getMass().getCenter());
        this.getShape().rotate(rotation, center.x, center.y);
    }

}



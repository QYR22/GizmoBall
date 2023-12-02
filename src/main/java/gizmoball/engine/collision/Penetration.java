package gizmoball.engine.collision;

import gizmoball.engine.geometry.Vector2;
import lombok.Data;

//  穿透信息（用于窄碰撞检测）
@Data
public class Penetration {

    protected Vector2 normal;

    protected double depth;

    public Penetration() {
        this.normal = new Vector2();
    }

}

package com.icarus.project;

import com.badlogic.gdx.math.Vector2;

abstract class AirplaneMoving extends AirplaneVisible {
    public Vector2 velocity;

    public AirplaneMoving(Vector2 position, Vector2 velocity) {
        super(position);
        this.velocity = velocity;
    }

    public Vector2 getVelocity() {
        return velocity;
    }
    
    public Vector2 getHeading() {
        return velocity;
    }

    public void step(Airplane airplane, float dt) {
        position.add(velocity.cpy().scl(dt));
    }
}

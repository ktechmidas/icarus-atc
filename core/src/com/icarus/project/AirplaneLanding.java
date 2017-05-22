package com.icarus.project;

import com.badlogic.gdx.math.Vector2;

class AirplaneLanding extends AirplaneMoving {
    public Vector2 heading;
    private Runway runway;
    public float decelRate = 0.12f;

    public AirplaneLanding(Vector2 position, Vector2 velocity, Runway runway) {
        super(position, velocity);
        heading = velocity.cpy().nor();
        this.runway = runway;
    }

    public void step(Airplane airplane, float dt) {
        //Move airplane
        super.step(airplane, dt);

        velocity.sub(velocity.cpy().nor().scl(dt * decelRate));
    }

    public Vector2 getHeading() {
        return heading;
    }

    public String getLabel() {
        return "";
    }
}

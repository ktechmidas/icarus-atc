package com.icarus.project;

import com.badlogic.gdx.math.Vector2;

public class AirplaneTakingOff extends AirplaneAltitude {
    public Vector2 heading;
    public float accelRate = 0.12f;

    public AirplaneFlying transitionToFlying(float altitude) {
        return new AirplaneFlying(position, velocity, altitude);
    }

    public AirplaneTakingOff(Vector2 position, Vector2 velocity) {
        super(position, velocity, 0.0f);
        this.heading = velocity.cpy().nor();
    }

    public String getLabel() {
        return (int) altitude + "m";
    }

    public Vector2 getHeading() {
        return heading;
    }

    public void step(Airplane airplane, float dt) {
        //Move airplane
        super.step(airplane, dt);

        velocity.add(velocity.cpy().nor().scl(dt * accelRate));

        if(PIScreen.toMeters(velocity.len()) > 150) {
            airplane.transitionToFlying(1);
            airplane.setTargetAltitude(10000);
        }
    }
}

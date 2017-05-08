package com.icarus.project;

import com.badlogic.gdx.math.Vector2;

class AirplaneAltitude extends AirplaneMoving {
    public float altitude;

    public AirplaneAltitude(Vector2 position, Vector2 velocity, float altitude) {
        super(position, velocity);

        this.altitude = altitude;
    }

    public float getAltitude() {
        return altitude;
    }
}

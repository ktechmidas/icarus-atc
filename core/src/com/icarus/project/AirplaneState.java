package com.icarus.project;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;

abstract class AirplaneState {
    abstract public void draw(Airplane airplane, BitmapFont font, SpriteBatch batch, Camera camera);

    abstract public void step(Airplane airplane, float dt);

    public AirplaneLanding transitionToLanding(Runway runway) {
        return null;
    }

    public AirplaneFlying transitionToFlying(float altitude) {
        return null;
    }

    public AirplaneTakingOff transitionToTakingOff(Vector2 position, Vector2 velocity) {
        return null;
    }

    public Vector2 getPosition() {
        return null;
    }

    public Vector2 getVelocity() {
        return null;
    }
    
    public float getAltitude() {
        return 0.0f;
    }
}

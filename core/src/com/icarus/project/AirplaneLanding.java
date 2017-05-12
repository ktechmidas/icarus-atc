package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

class AirplaneLanding extends AirplaneMoving {
    public Vector2 heading;

    private Runway runway;

    public float maxVelocity = 250; //meters per second
    public float decelRate = 0.065f;

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

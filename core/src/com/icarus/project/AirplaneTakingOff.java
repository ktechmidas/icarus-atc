package com.icarus.project;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class AirplaneTakingOff extends AirplaneState {
    public Vector2 position;
    public Vector2 velocity;
    public Vector2 heading;

    private Runway runway;

    public float maxVelocity = 250; //meters per second
    public float accelRate = 0.05f;

    public AirplaneFlying transitionToFlying(int altitude) {
        return new AirplaneFlying(position, velocity, altitude);
    }

    public AirplaneTakingOff(Vector2 position, Vector2 velocity) {
        this.position = position;
        this.velocity = velocity;
        this.heading = velocity.cpy().nor();
    }

    public void draw(Airplane airplane, BitmapFont font, SpriteBatch batch, Camera camera) {
        Vector3 pos = camera.project(new Vector3(position.x, position.y, 0));
        airplane.sprite.setColor(Colors.colors[4]);
        airplane.sprite.setPosition(
                pos.x - airplane.sprite.getWidth() / 2,
                pos.y - airplane.sprite.getHeight() / 2);
        airplane.sprite.draw(batch);
    }

    public void step(Airplane airplane, float dt) {
        //Move airplane
        position.add(velocity.cpy().scl(dt));

        //Point airplane in direction of travel
        airplane.sprite.setRotation(heading.angle());

        velocity.add(velocity.cpy().nor().scl(dt * accelRate));

        if(PIScreen.toMeters(velocity.len()) > 150) {
            airplane.transitionToFlying(0);
            airplane.setTargetAltitude(10000);
        }
    }
}

package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

class AirplaneLanding extends AirplaneState {
    public Vector2 position;
    public Vector2 velocity;
    public Vector2 heading;

    private Runway runway;

    public float maxVelocity = 250; //meters per second
    public float decelRate = 0.1f;

    public AirplaneLanding(Vector2 position, Vector2 velocity, Runway runway) {
        this.position = position;
        this.velocity = velocity;
        this.heading = velocity.cpy().nor();
        this.runway = runway;
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

        velocity.sub(velocity.cpy().nor().scl(dt * decelRate));
    }
}

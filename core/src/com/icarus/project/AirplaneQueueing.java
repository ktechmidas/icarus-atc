package com.icarus.project;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class AirplaneQueueing extends AirplaneState {

    @Override
    public void draw(Airplane airplane, BitmapFont font, SpriteBatch batch, Camera camera) {

    }

    @Override
    public void step(Airplane airplane, float dt) {

    }

    public AirplaneTakingOff transitionToTakingOff(Vector2 position, Vector2 velocity) {
        return new AirplaneTakingOff(position, velocity);
    }

    public AirplaneQueueing() {
        super(); // Do I need this?
    }
}

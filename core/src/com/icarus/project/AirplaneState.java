package com.icarus.project;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Camera;

abstract class AirplaneState {
    abstract public void draw(Airplane airplane, BitmapFont font, SpriteBatch batch, Camera camera);

    abstract public void step(Airplane airplane);
}

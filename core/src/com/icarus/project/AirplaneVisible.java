package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

abstract class AirplaneVisible extends AirplaneState {
    public Vector2 position;

    public AirplaneVisible(Vector2 position) {
        this.position = position;
    }

    public String getLabel() {
        return "";
    }

    public Vector2 getPosition() {
        return position;
    }

    abstract public Vector2 getHeading();

    public void draw(Airplane airplane, BitmapFont font, SpriteBatch batch, Camera camera) {
        Vector3 pos = camera.project(new Vector3(position.x, position.y, 0));
        airplane.sprite.setRotation(getHeading().angle());
        airplane.sprite.setColor(Colors.colors[4]);
        airplane.sprite.setPosition(
                pos.x - airplane.sprite.getWidth() / 2,
                pos.y - airplane.sprite.getHeight() / 2);
        airplane.sprite.draw(batch);
        font.setColor(Colors.colors[4]);
        font.draw(batch, getLabel(),
                pos.x - 100, // Position left edge of text box
                pos.y - 26 * Gdx.graphics.getDensity(),
                200,
                Align.center,
                false
        );
    }
}

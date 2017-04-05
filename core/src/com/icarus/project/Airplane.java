package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Align;

class Airplane {
    //The global airplane image
    public static Texture texture;
    //The name of this airplane
    public String name;
    //The sprite used by this airplane for display. It references the global texture.
    public Sprite sprite;

    public AirplaneState state;

    public boolean isSelected;

    public FlightType flightType;

    public Airplane(
            String name, FlightType flightType, Vector2 position, Vector2 velocity, float altitude)
    {
        this.state = new AirplaneFlying(position, velocity, altitude);

        sprite = new Sprite(texture);
        sprite.setScale(0.25f * Gdx.graphics.getDensity());
        sprite.setOrigin(
                sprite.getScaleX() * sprite.getWidth() / 2,
                sprite.getScaleY() * sprite.getHeight() / 2);
        sprite = new Sprite(texture);
        sprite.setOriginCenter();
        sprite.setScale(0.2f * Gdx.graphics.getDensity());
    }

    //Draw the airplane image. This assumes that the camera has already been set up.
    public void draw(BitmapFont font, SpriteBatch batch, Camera camera) {
        state.draw(this, font, batch, camera);
    }

    //Move the airplane image at evey render
    public void step() {
        state.step(this);
    }

    public void setTargetWaypoint(Waypoint waypoint) {
        ((AirplaneFlying) state).setTargetWaypoint(waypoint);
    }

    public void setTargetHeading(Vector2 targetHeading){
        ((AirplaneFlying) state).setTargetHeading(targetHeading);
    }

    public void setTargetRunway(Runway targetRunway, int point) {
        ((AirplaneFlying) state).setTargetRunway(targetRunway, point);
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Vector2 getPosition() {
        return ((AirplaneFlying) state).position;
    }

    public float getAltitude() {
        return ((AirplaneFlying) state).altitude;
    }

    public enum FlightType {
        ARRIVAL, DEPARTURE, FLYOVER
    }

}

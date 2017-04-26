package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Camera;

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

    public StateType stateType;

    public Airplane(
            String name, FlightType flightType, Vector2 position, Vector2 velocity, float altitude)
    {
        this.name = name;
        this.state = new AirplaneFlying(position, velocity, altitude);
        this.stateType = StateType.FLYING;
        this.flightType = flightType;

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
    public void step(float dt) {
        state.step(this, dt);
    }

    public void setTargetWaypoint(Waypoint waypoint) {
        if(stateType == StateType.FLYING) {
            ((AirplaneFlying) state).setTargetWaypoint(waypoint);
        }
    }

    public void setTargetHeading(Vector2 targetHeading){
        if(stateType == StateType.FLYING) {
            ((AirplaneFlying) state).setTargetHeading(targetHeading);
        }
    }

    public void setTargetRunway(Runway targetRunway, int point) {
        if(stateType == StateType.FLYING) {
            ((AirplaneFlying) state).setTargetRunway(targetRunway, point);
            setTargetAltitude(0);
        }
    }

    public void setTargetAltitude(float targetAltitude) {
        if(stateType == StateType.FLYING) {
            ((AirplaneFlying) state).targetAltitude = targetAltitude;
        }
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Vector2 getPosition() {
        if(stateType == StateType.FLYING) {
            return ((AirplaneFlying) state).position;
        }
        else if(stateType == StateType.LANDING) {
            return ((AirplaneLanding) state).position;
        }
        else {
            return null;
        }
    }

    public float getAltitude() {
        if(stateType == StateType.FLYING) {
            return ((AirplaneFlying) state).altitude;
        }
        else {
            return 0.0f;
        }
    }

    public Vector2 getVelocity() {
        if(stateType == StateType.FLYING) {
            return ((AirplaneFlying) state).velocity;
        }
        else {
            return ((AirplaneLanding) state).velocity;
        }
    }

    public void transitionToLanding(Runway runway) {
        state = state.transitionToLanding(runway);
        stateType = StateType.LANDING;
    }

    public enum FlightType {
        ARRIVAL, DEPARTURE, FLYOVER
    }

    public enum StateType {
        FLYING, LANDING
    }
}

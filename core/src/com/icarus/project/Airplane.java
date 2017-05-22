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

    public boolean colliding;

    public Airplane(
            String name, FlightType flightType, Vector2 position, Vector2 velocity, float altitude)
    {
        this.name = name;
        this.flightType = flightType;
        if(flightType == FlightType.ARRIVAL || flightType == FlightType.FLYOVER) {
            this.stateType = StateType.FLYING;
            this.state = new AirplaneFlying(position, velocity, altitude);
        }
        else {
            this.stateType = StateType.QUEUEING;
            this.state = new AirplaneQueueing();
        }

        colliding = false;

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

    public void setTargetAirport(OtherAirport targetAirport) {
        if(stateType == StateType.FLYING) {
            ((AirplaneFlying) state).setTargetAirport(targetAirport);
            setTargetAltitude(10000);
        }
    }

    public void setNoTarget() {
        if(stateType == StateType.FLYING) {
            ((AirplaneFlying) state).setNoTarget();
        }
    }

    public void setTargetAltitude(float targetAltitude) {
        Gdx.app.log("Airplane", state + ", " + stateType);
        if(stateType == StateType.FLYING) {
            ((AirplaneFlying) state).targetAltitude = targetAltitude;
        }
    }

    public Vector2 getVelocity() {
        if(stateType == StateType.FLYING) {
            return ((AirplaneFlying) state).velocity;
        }
        else if(stateType == StateType.LANDING) {
            return ((AirplaneLanding) state).velocity;
        }
        else if(stateType == StateType.TAKINGOFF) {
            return ((AirplaneTakingOff) state).velocity;
        }
        else {
            return null;
        }
    }

    public enum FlightType {
        ARRIVAL, DEPARTURE, FLYOVER
    }

    public enum StateType {
        FLYING, LANDING, TAKINGOFF, QUEUEING
    }

    public AirplaneFlying.TargetType getTargetType() {
        if(stateType == StateType.FLYING) {
            return ((AirplaneFlying) state).targetType;
        }
        return null;
    }

    public void transitionToLanding(Runway runway) {
        state = state.transitionToLanding(runway);
        stateType = StateType.LANDING;
    }

    public void transitionToFlying(int altitude) {
        state = state.transitionToFlying(altitude);
        stateType = StateType.FLYING;
    }

    public void transitionToTakingOff(Vector2 position, Vector2 velocity) {
        state = state.transitionToTakingOff(position, velocity);
        stateType = StateType.TAKINGOFF;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

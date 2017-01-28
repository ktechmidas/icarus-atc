package com.icarus.project;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Timer;

class Airplane {
    //The global airplane image
    public static Texture texture;
    //The name of this airplane
    public String name;
    //The position of this airplane
    public Vector2 position;
    //The velocity of this airplane
    public Vector2 velocity;
    //The altitude of this airplane in meters
    public float altitude;
    //The sprite used by this airplane for display. It references the global texture.
    public Sprite sprite;

    public Vector2 targetHeading;

    public Waypoint targetWaypoint;

    public boolean isSelected;

    public float turnRate = 6;

    public TargetState targetState;

    public Airplane(String name, Vector2 position, Vector2 velocity, float altitude) {
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        this.altitude = altitude;
        sprite = new Sprite(texture);
        sprite.setScale(0.25f * Gdx.graphics.getDensity());
        this.targetHeading = null;
        this.targetWaypoint = null;
        this.targetState = TargetState.NO_TARGET;
    }

    //Draw the airplane image. This assumes that the camera has already been set up.
    public void draw(SpriteBatch batch, Camera camera) {
        Vector3 pos = camera.project(new Vector3(position.x, position.y, 0));
        sprite.setPosition(pos.x, pos.y);
        sprite.draw(batch);
    }

    //Move the airplane image at evey render
    public void step() {
        //Move airplane
        position.add(velocity.cpy().scl(Gdx.graphics.getDeltaTime()));

        switch(targetState) {
            case TARGET_HEADING:
                this.targetWaypoint = null;
                //If the airplane is off of its target by more than 0.1 degrees
//                if(Math.abs(targetHeading.angle() - velocity.angle()) > 0.1) {
//                    if(Math.abs(targetHeading.angle() - velocity.angle()) < 180) {
//                        turn(turnRate);
////                        velocity.rotate(turnRate * Gdx.graphics.getDeltaTime());
//                    }
//                    else {
//                        turn(-turnRate);
////                        velocity.rotate(-turnRate * Gdx.graphics.getDeltaTime());
//                    }
//                }
//                else {
//                    ProjectIcarus.getInstance().ui.setStatus(name + ": turn complete");
//                    targetHeading = null;
//                }
                turnToHeading(targetHeading);
                break;
            case TARGET_WAYPOINT:
                this.targetHeading = null;
                Vector2 waypointHeading = targetWaypoint.position.cpy().sub(this.position);
                Gdx.app.log("Airplane", "" + waypointHeading);
                turnToHeading(waypointHeading);
                break;
            case TARGET_RUNWAY:
                break;
            default:
                break;
        }

        //Point airplane in direction of travel
        sprite.setRotation(velocity.angle());
    }

    public enum TargetState {
        TARGET_HEADING, TARGET_WAYPOINT, TARGET_RUNWAY, NO_TARGET
    }

    public void setTargetWaypoint(Waypoint waypoint) {
        targetState = TargetState.TARGET_WAYPOINT;
        this.targetWaypoint = waypoint;
    }

    public void setTargetHeading(Vector2 targetHeading){
        targetState = TargetState.TARGET_HEADING;
        this.targetHeading = targetHeading;
    }

    public void turnToHeading(Vector2 targetHeading) {
        if(Math.abs(targetHeading.angle() - velocity.angle()) > 0.1) {
            if(Math.abs(targetHeading.angle() - velocity.angle()) < 180) {
                turn(turnRate);
            }
            else {
                turn(-turnRate);
            }
        }
    }

    public void turn(float turnRate){
        velocity.rotate(turnRate * Gdx.graphics.getDeltaTime());
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}

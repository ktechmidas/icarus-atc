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

    public float turnRate = 3;

    public Airplane(String name, Vector2 position, Vector2 velocity, float altitude) {
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        this.altitude = altitude;
        sprite = new Sprite(texture);
        sprite.setScale(0.2f * Gdx.graphics.getDensity());
        this.targetHeading = null;
        this.targetWaypoint = null;
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

        if(targetHeading != null) {
            turnToHeading(targetHeading);
            if(Math.abs(targetHeading.angle() - velocity.angle()) < 0.1) {
                ProjectIcarus.getInstance().ui.setStatus(name + ": turn complete");
                removeTarget();
            }
        }
        else if(targetWaypoint != null) {
//            Vector2 waypointHeading = targetWaypoint.position.cpy().sub(this.position);
            Vector2 waypointHeading = targetWaypoint.position.cpy().sub(this.position);

            turnToHeading(waypointHeading);
            if(Math.abs(waypointHeading.angle() - velocity.angle()) < 0.1) {
                ProjectIcarus.getInstance().ui.setStatus(name + ": turn complete");
                setTargetHeading(waypointHeading);
            }
        }

        //Point airplane in direction of travel
        sprite.setRotation(velocity.angle());
    }

    public void removeTarget() {
        this.targetWaypoint = null;
        this.targetHeading = null;
    }

    public void setTargetWaypoint(Waypoint waypoint) {
        this.targetWaypoint = waypoint;
        this.targetHeading = null;
    }

    public void setTargetHeading(Vector2 targetHeading){
        this.targetHeading = targetHeading;
        this.targetWaypoint = null;
    }

    public void turnToHeading(Vector2 targetHeading) {
        float angle = targetHeading.angle(velocity);
        if(angle < 0) {
            velocity.rotate(turnRate * Gdx.graphics.getDeltaTime());
        }
        else {
            velocity.rotate(-turnRate * Gdx.graphics.getDeltaTime());
        }
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}

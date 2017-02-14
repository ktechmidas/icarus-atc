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
    //The position of this airplane
    public Vector2 position;
    //The velocity of this airplane
    public Vector2 velocity;
    //The altitude of this airplane in meters
    public float altitude;
    //The sprite used by this airplane for display. It references the global texture.
    public Sprite sprite;

    private Vector2 targetHeading;
    private Waypoint targetWaypoint;

    public boolean isSelected;

    public float turnRate = 3;

    public Airplane(String name, Vector2 position, Vector2 velocity, float altitude) {
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        this.altitude = altitude;
        sprite = new Sprite(texture);
        sprite.setScale(0.25f * Gdx.graphics.getDensity());
        sprite.setOrigin(
                sprite.getScaleX() * sprite.getWidth() / 2,
                sprite.getScaleY() * sprite.getHeight() / 2);
        this.targetHeading = null;
        this.targetWaypoint = null;

        sprite = new Sprite(texture);
        sprite.setOriginCenter();
        sprite.setScale(0.2f * Gdx.graphics.getDensity());
    }

    //Draw the airplane image. This assumes that the camera has already been set up.
    public void draw(BitmapFont font, SpriteBatch batch, Camera camera) {
        Vector3 pos = camera.project(new Vector3(position.x, position.y, 0));
        sprite.setPosition(pos.x - sprite.getWidth() / 2, pos.y - sprite.getHeight() / 2);
        sprite.draw(batch);
        font.setColor(new Color(1, 1, 1, 1));
        font.draw(batch, (int) altitude + "m", pos.x - 100, pos.y - 40, 200, Align.center, false);
    }

    //Move the airplane image at evey render
    public void step() {
        //Move airplane
        position.add(velocity.cpy().scl(Gdx.graphics.getDeltaTime()));

        if(targetHeading != null) {
            turnToHeading(targetHeading);
        }
        else if(targetWaypoint != null) {
            turnToHeading(targetWaypoint.position.cpy().sub(this.position));
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
        if(Math.abs(targetHeading.angle(velocity)) < 0.01) {
            ProjectIcarus.getInstance().ui.setStatus(name + ": turn complete");
            removeTarget();
        }
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}

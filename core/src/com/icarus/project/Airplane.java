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
    private Runway targetRunway;
    private int targetRunwayPoint;
    private int targetRunwayStage;

    public boolean isSelected;

    public float turnRate = 3; //degree per second
    public float maxVelocity = 250; //meters per second

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
            if(turnToHeading(targetHeading, turnRate)) {
                PIScreen.getInstance().ui.setStatus(name + ": turn complete");
                targetWaypoint = null;
            }
        }
        else if(targetWaypoint != null) {
            if(turnToHeading(targetWaypoint.position.cpy().sub(this.position), turnRate)) {
                PIScreen.getInstance().ui.setStatus(name + ": turn complete");
                targetWaypoint = null;
            }
        }
        else if(targetRunway != null) {
            Vector2 target = targetRunway.points[1 - targetRunwayPoint].cpy()
                .sub(targetRunway.points[targetRunwayPoint]).nor();
            if(targetRunwayStage == 0) {
                System.out.println("stage 0");
                Vector2 pos = position.cpy().sub(target.scl(velocity.len() / turnRate));
                Vector2 line = pos.cpy().sub(targetRunway.points[targetRunwayPoint]).nor();
                if(Math.abs(velocity.cpy().scl(-1).angle(target)) < Math.abs(line.angle(target))) {
                    targetRunwayStage = 1;
                }
                else {
                    float angle = line.angle(velocity);
                    if(angle > 0) {
                        velocity.rotate(turnRate * Gdx.graphics.getDeltaTime());
                    }
                    else {
                        velocity.rotate(-turnRate * Gdx.graphics.getDeltaTime());
                    }
                }
            }
            else if(targetRunwayStage == 1) {
                System.out.println("stage 1");
                Vector2 targetVector = targetRunway.points[targetRunwayPoint].cpy()
                        .sub(targetRunway.points[1 - targetRunwayPoint]);
                Vector2 targetPoint = targetRunway.points[targetRunwayPoint];
                float t = (targetVector.x * (position.y - targetPoint.y) +
                        targetVector.y * (position.x - targetPoint.x)) / 
                    (velocity.x * targetVector.y - velocity.y * targetVector.y);
                Vector2 isect = position.cpy().add(velocity.cpy().scl(t));
                System.out.println(isect);
                float alpha = Math.abs(velocity.angle(target.cpy().scl(-1.0f)));
                float radius = velocity.len() / turnRate;
                float d = (float) Math.sin(Math.PI / 2f - alpha / 2f) /
                    ((float) Math.sin(alpha / 2f) / radius);
                if(position.dst(isect) < d) {
                    targetRunwayStage = 2;
                }
            }
            else {
                System.out.println("stage 2");
                if(turnToHeading(target, turnRate)) {
                    PIScreen.getInstance().ui.setStatus(name + ": turn complete");
                    targetRunway = null;
                }
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
        this.targetRunway = null;
    }

    public void setTargetHeading(Vector2 targetHeading){
        this.targetHeading = targetHeading;
        this.targetWaypoint = null;
        this.targetRunway = null;
    }

    public void setTargetRunway(Runway targetRunway, int point) {
        this.targetRunway = targetRunway;
        this.targetRunwayPoint = point;
        this.targetRunwayStage = 0;
        this.targetHeading = null;
        this.targetWaypoint = null;
    }

    public boolean turnToHeading(Vector2 targetHeading, float turnRate) {
        float angle = targetHeading.angle(velocity);
        if(angle < 0) {
            velocity.rotate(turnRate * Gdx.graphics.getDeltaTime());
        }
        else {
            velocity.rotate(-turnRate * Gdx.graphics.getDeltaTime());
        }
        return Math.abs(targetHeading.angle(velocity)) < 0.001;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}

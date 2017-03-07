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

import static com.icarus.project.Airplane.TargetType.HEADING;
import static com.icarus.project.Airplane.TargetType.NONE;
import static com.icarus.project.Airplane.TargetType.RUNWAY;
import static com.icarus.project.Airplane.TargetType.WAYPOINT;

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

    public FlightType flightType;
    public TargetType targetType;

    public Airplane(String name, FlightType flightType, Vector2 position, Vector2 velocity, float altitude) {
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        this.altitude = altitude;
        this.flightType = flightType;

        sprite = new Sprite(texture);
        sprite.setScale(0.25f * Gdx.graphics.getDensity());
        sprite.setOrigin(
                sprite.getScaleX() * sprite.getWidth() / 2,
                sprite.getScaleY() * sprite.getHeight() / 2);

        targetType = NONE;

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

        switch(targetType) {
            case WAYPOINT:
                if(turnToHeading(targetWaypoint.position.cpy().sub(this.position), turnRate)) {
                    PIScreen.getInstance().ui.setStatus(name + ": turn complete");
//                    targetWaypoint = null;
                    targetType = NONE;
                }
                break;
            case HEADING:
                if(turnToHeading(targetHeading, turnRate)) {
                    PIScreen.getInstance().ui.setStatus(name + ": turn complete");
//                    targetWaypoint = null;
                    targetType = NONE;
                }
                break;
            case RUNWAY:
                Vector2 target = targetRunway.points[1 - targetRunwayPoint].cpy()
                        .sub(targetRunway.points[targetRunwayPoint]).nor();
                if(targetRunwayStage == 0) {
                    Vector2 line = position.cpy().sub(targetRunway.points[targetRunwayPoint]).nor();
                    if(velocity.dot(target) > line.dot(target)) {
                        targetRunwayStage = 1;
                    }
                    else {
                        float angle = target.angle(velocity);
                        if(angle < 0) {
                            velocity.rotate(turnRate * Gdx.graphics.getDeltaTime());
                        }
                        else {
                            velocity.rotate(-turnRate * Gdx.graphics.getDeltaTime());
                        }
                    }
                }
                else if(targetRunwayStage == 1) {
                    Vector2 targetVector = targetRunway.points[targetRunwayPoint].cpy()
                            .sub(targetRunway.points[1 - targetRunwayPoint]);
                    Vector2 targetPoint = targetRunway.points[targetRunwayPoint];
                    float t = (targetVector.x * (position.y - targetPoint.y) +
                            targetVector.y * (position.x - targetPoint.x)) /
                            (velocity.x * targetVector.y - velocity.y * targetVector.y);
                    Vector2 isect = position.cpy().add(velocity.cpy().scl(t));
                    float beta = (float) Math.acos(
                            targetVector.cpy().nor().dot(velocity.cpy().nor()));
                    float alpha = beta / 2.0f;
                    float x = position.dst(targetPoint) / (float) Math.cos(alpha);
                    Vector2 center = isect.cpy().add((targetVector.cpy().nor().scl(-1.0f))
                            .add(velocity.cpy().nor()).scl(0.5f).nor());
                    float radius = center.dst(position);
                    float turn = velocity.len() / radius;
                    if(turn < turnRate) {
                        targetRunwayStage = 2;
                    }
                }
                else {
                    if(turnToHeading(target, turnRate)) {
                        PIScreen.getInstance().ui.setStatus(name + ": turn complete");
//                        targetRunway = null;
                        targetType = NONE;
                    }
                }
                break;
            default:
                break;
        }

        //Point airplane in direction of travel
        sprite.setRotation(velocity.angle());
    }

    public void setTargetWaypoint(Waypoint waypoint) {
        this.targetWaypoint = waypoint;
        targetType = WAYPOINT;
    }

    public void setTargetHeading(Vector2 targetHeading){
        this.targetHeading = targetHeading;
        targetType = HEADING;
    }

    public void setTargetRunway(Runway targetRunway, int point) {
        this.targetRunway = targetRunway;
        this.targetRunwayPoint = point;
        this.targetRunwayStage = 0;
        targetType = RUNWAY;
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

    public enum FlightType {
        ARRIVAL, DEPARTURE, FLYOVER
    }

    public enum TargetType {
        WAYPOINT, HEADING, RUNWAY, NONE
    }
}

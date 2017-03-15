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

    Vector2 intersect;

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

        intersect = position;
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
                float radius = velocity.len() / (turnRate * Gdx.graphics.getDeltaTime());
                // Calculate heading of target runway
                Vector2 target = targetRunway.points[1 - targetRunwayPoint].cpy()
                        .sub(targetRunway.points[targetRunwayPoint]).nor();
                // If airplane is pointing away from runway line
                if(targetRunwayStage == 0) {
                    System.out.println("stage 0");
                    // Calculate target point offset by turning radius
                    Vector2 pos = targetRunway.points[targetRunwayPoint].cpy()
                            .sub(target.scl(radius));
                    // Heading from airplane to pos
                    Vector2 line = pos.cpy().sub(position).nor();
                    if(Math.abs(velocity.cpy().scl(-1).angle(target)) < Math.abs(line.cpy().scl(-1).angle(target)) + 0.05) {
                        targetRunwayStage = 1;
                    }
                    else {
                        float angle = line.angle(velocity);
                        if(angle < 0) {
                            velocity.rotate(turnRate * Gdx.graphics.getDeltaTime());
                        }
                        else {
                            velocity.rotate(-turnRate * Gdx.graphics.getDeltaTime());
                        }
                    }
                }
                // If airplane is pointing towards runway line
                else if(targetRunwayStage == 1) {
                    System.out.println("stage 1");
                    // Runway heading vector
                    Vector2 targetVector = targetRunway.points[targetRunwayPoint].cpy()
                            .sub(targetRunway.points[1 - targetRunwayPoint]);
                    Vector2 targetPoint = targetRunway.points[targetRunwayPoint];
                    float toIntersect = (targetVector.x * (position.y - targetPoint.y) -
                            targetVector.y * (position.x - targetPoint.x)) /
                            (velocity.x * targetVector.y - velocity.y * targetVector.x);
                    intersect = position.cpy().add(velocity.cpy().scl(toIntersect));
                    float alpha = Math.abs(velocity.angleRad(targetVector.cpy()));
                    float d = (float) Math.sin(Math.PI / 2f - alpha / 2f) /
                            ((float) Math.sin(alpha / 2f) / radius);
                    if(position.dst(intersect) < d) {
                        targetRunwayStage = 2;
                        intersect = position;
                    }
                }
                // If turn to runway has completed
                else {
                    System.out.println("stage 2");
                    if(turnToHeading(target, turnRate)) {
                        PIScreen.getInstance().ui.setStatus(name + ": turn complete");
                        targetRunway = null;
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
        ARRIVAL, DEPARTURE, FLYOVER, LANDED
    }

    public enum TargetType {
        WAYPOINT, HEADING, RUNWAY, NONE
    }
}

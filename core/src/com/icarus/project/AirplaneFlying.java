package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

import static com.icarus.project.AirplaneFlying.TargetType.HEADING;
import static com.icarus.project.AirplaneFlying.TargetType.NONE;
import static com.icarus.project.AirplaneFlying.TargetType.RUNWAY;
import static com.icarus.project.AirplaneFlying.TargetType.WAYPOINT;

class AirplaneFlying extends AirplaneState {
    public Vector2 position;
    public Vector2 velocity;
    public float altitude;

    private Vector2 targetHeading;
    private Waypoint targetWaypoint;
    private Runway targetRunway;
    private int targetRunwayPoint;
    private int targetRunwayStage;
    public float targetAltitude;

    public float turnRate = 3; //degree per second
    public float maxVelocity = 250; //meters per second
    public float altitudeChangeRate = 0.0625f;

    public TargetType targetType;

    public enum TargetType {
        WAYPOINT, HEADING, RUNWAY, NONE
    }

    public AirplaneFlying(Vector2 position, Vector2 velocity, float altitude) {
        this.position = position;
        this.velocity = velocity;
        this.altitude = altitude;
        targetType = NONE;
        targetAltitude = this.altitude;
    }

    public AirplaneLanding transitionToLanding(Runway runway) {
        return new AirplaneLanding(position, velocity, runway);
    }

    public void draw(Airplane airplane, BitmapFont font, SpriteBatch batch, Camera camera) {
        Vector3 pos = camera.project(new Vector3(position.x, position.y, 0));
        airplane.sprite.setColor(Colors.colors[4]);
        airplane.sprite.setPosition(
                pos.x - airplane.sprite.getWidth() / 2,
                pos.y - airplane.sprite.getHeight() / 2);
        airplane.sprite.draw(batch);
        font.setColor(Colors.colors[4]);
        font.draw(batch, (int) altitude + "m",
                pos.x - 100, // Position left edge of text box
                pos.y - 26 * Gdx.graphics.getDensity(),
                200,
                Align.center,
                false
        );
    }

    public void step(Airplane airplane, float dt) {
        //Move airplane
        position.add(velocity.cpy().scl(dt));

        switch(targetType) {
            case WAYPOINT:
                if(turnToHeading(targetWaypoint.position.cpy().sub(this.position), turnRate, dt)) {
                    PIScreen.getInstance().ui.setStatus(airplane.name + ": turn complete");
                    targetType = NONE;
                }
                break;
            case HEADING:
                if(turnToHeading(targetHeading, turnRate, dt)) {
                    PIScreen.getInstance().ui.setStatus(airplane.name + ": turn complete");
                    targetType = NONE;
                }
                break;
            case RUNWAY:
                float radius = velocity.len() / (turnRate * dt);
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
                    if(Math.abs(velocity.cpy().scl(-1).angle(target)) <
                            Math.abs(line.cpy().scl(-1).angle(target)) + 0.05)
                    {
                        targetRunwayStage = 1;
                    }
                    else {
                        float angle = line.angle(velocity);
                        if(angle < 0) {
                            velocity.rotate(turnRate * dt);
                        }
                        else {
                            velocity.rotate(-turnRate * dt);
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
                    Vector2 intersect = position.cpy().add(velocity.cpy().scl(toIntersect));
                    float alpha = Math.abs(velocity.angleRad(targetVector.cpy()));
                    float d = (float) Math.sin(Math.PI / 2f - alpha / 2f) /
                            ((float) Math.sin(alpha / 2f) / radius);
                    if(position.dst(intersect) < d) {
                        targetRunwayStage = 2;
                    }
                }
                // If turn to runway has completed
                else if(targetRunwayStage == 2) {
                    System.out.println("stage 2");
                    if(turnToHeading(target, turnRate, dt)) {
                        PIScreen.getInstance().ui.setStatus(airplane.name + ": turn complete");
                        targetRunwayStage = 3;
                    }
                }
                else {
                    System.out.println("stage 3");
                    Vector2 targetPoint = targetRunway.points[targetRunwayPoint];
                    if(targetPoint.dst(position) < 3) {
                        airplane.transitionToLanding(targetRunway);
                    }
                }
                break;
            default:
                break;
        }

        //Point airplane in direction of travel
        airplane.sprite.setRotation(velocity.angle());

        // Change altitude
        if(altitude > targetAltitude && Math.abs(altitude - targetAltitude) > 1) {
            altitude -= velocity.len() * altitudeChangeRate;
        }
        else if(altitude < targetAltitude && Math.abs(altitude - targetAltitude) > 1) {
            altitude += velocity.len() * altitudeChangeRate;
        }
    }

    public void setTargetWaypoint(Waypoint waypoint) {
        this.targetWaypoint = waypoint;
        targetType = WAYPOINT;
    }

    public void setTargetHeading(Vector2 targetHeading) {
        this.targetHeading = targetHeading;
        targetType = HEADING;
    }

    public void setTargetRunway(Runway targetRunway, int point) {
        this.targetRunway = targetRunway;
        this.targetRunwayPoint = point;
        this.targetRunwayStage = 0;
        targetType = RUNWAY;
    }

    public boolean turnToHeading(Vector2 targetHeading, float turnRate, float dt) {
        float angle = targetHeading.angle(velocity);
        if(angle < 0) {
            velocity.rotate(turnRate * dt);
        }
        else {
            velocity.rotate(-turnRate * dt);
        }
        return Math.abs(targetHeading.angle(velocity)) < 0.001;
    }
}

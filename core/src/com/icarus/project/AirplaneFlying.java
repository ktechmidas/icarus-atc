package com.icarus.project;

import com.badlogic.gdx.math.Vector2;

import static com.icarus.project.AirplaneFlying.TargetType.AIRPORT;
import static com.icarus.project.AirplaneFlying.TargetType.HEADING;
import static com.icarus.project.AirplaneFlying.TargetType.NONE;
import static com.icarus.project.AirplaneFlying.TargetType.RUNWAY;
import static com.icarus.project.AirplaneFlying.TargetType.WAYPOINT;

class AirplaneFlying extends AirplaneAltitude {
    private Vector2 targetHeading;
    private Waypoint targetWaypoint;
    private Runway targetRunway;
    private int targetRunwayPoint;
    private int targetRunwayStage;
    public float targetAltitude;
    public OtherAirport targetAirport;

    public float turnRate = 3 * (float) Math.PI / 180.0f; //radians per second

    public TargetType targetType;

    public enum TargetType {
        WAYPOINT, HEADING, RUNWAY, AIRPORT, NONE
    }

    public AirplaneFlying(Vector2 position, Vector2 velocity, float altitude) {
        super(position, velocity, altitude);

        targetType = NONE;
        targetAltitude = this.altitude;
    }

    public AirplaneLanding transitionToLanding(Runway runway) {
        return new AirplaneLanding(position, velocity, runway);
    }

    public AirplaneFlying transitionToFlying(float altitude) {
        this.altitude = altitude;
        return this;
    }

    public String getLabel() {
        return (int) altitude + "m";
    }

    public void step(Airplane airplane, float dt) {
        super.step(airplane, dt);

        switch(targetType) {
            case WAYPOINT:
                if(turnToHeading(targetWaypoint.position.cpy().sub(this.position), turnRate, dt)) {
                    targetType = NONE;
                }
                break;

            case HEADING:
                if(turnToHeading(targetHeading, turnRate, dt)) {
                    targetType = NONE;
                }
                break;

            case RUNWAY:
                // Descend
                float distance = Math.abs(getPosition().cpy()
                        .sub(targetRunway.points[targetRunwayPoint]).len()
                );
                float time = distance / velocity.len();
                float descentRate = (altitude - 1) / time; // meters per second; -1 to avoid crashing
                altitude -= descentRate * dt;

                float radius = velocity.len() / turnRate;
                // Calculate heading of target runway
                Vector2 target = targetRunway.points[1 - targetRunwayPoint].cpy()
                        .sub(targetRunway.points[targetRunwayPoint]).nor();
                // If airplane is pointing away from runway line
                if(targetRunwayStage == 0) {
                    // Calculate target point offset by turning radius
                    Vector2 pos = targetRunway.points[targetRunwayPoint].cpy()
                            .sub(target.scl(radius * 2.0f));
                    if(turnToHeading(pos.sub(position).nor(), turnRate, dt)) {
                        targetRunwayStage = 1;
                    }
                }
                // If airplane is pointing towards runway line
                else if(targetRunwayStage == 1) {
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
                    if(turnToHeading(target, turnRate, dt)) {
                        targetRunwayStage = 3;
                    }

                    Vector2 targetPoint = targetRunway.points[targetRunwayPoint];
                    System.out.println(targetPoint.dst(position));
                    if(targetPoint.dst(position) < 3) {
                        airplane.transitionToLanding(targetRunway);
                    }
                }
                else {
                    Vector2 targetPoint = targetRunway.points[targetRunwayPoint];
                    System.out.println(targetPoint.dst(position));
                    if(targetPoint.dst(position) < 3) {
                        airplane.transitionToLanding(targetRunway);
                    }
                }
                break;
            case AIRPORT:
                turnToHeading(targetAirport.position.cpy().sub(this.position), turnRate, dt);
                break;
            default:
                break;
        }

        //Point airplane in direction of travel
        airplane.sprite.setRotation(velocity.angle());

        if(targetType != RUNWAY) {
            // Change altitude
            if(altitude > targetAltitude && Math.abs(altitude - targetAltitude) > 1) {
                altitude -= PIScreen.getInstance().altitudeChangeRate * dt;
            }
            else if(altitude < targetAltitude && Math.abs(altitude - targetAltitude) > 1) {
                altitude += PIScreen.getInstance().altitudeChangeRate * dt;
            }
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

    public void setTargetAirport(OtherAirport targetAirport) {
        this.targetAirport = targetAirport;
        targetType = AIRPORT;
    }

    public void setNoTarget() {
        targetType = NONE;
    }

    public boolean turnToHeading(Vector2 targetHeading, float turnRate, float dt) {
        float angle = targetHeading.angle(velocity);
        if(angle < 0) {
            velocity.rotateRad(Math.min(turnRate * dt, -angle));
        }
        else {
            velocity.rotateRad(Math.max(-turnRate * dt, -angle));
        }
        return Math.abs(targetHeading.angle(velocity)) < 0.01;
    }
}

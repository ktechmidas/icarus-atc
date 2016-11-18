package com.icarus.project;

class Airport {
    public Waypoint[] waypoints;
    public float width;
    public float height;

    public Airport(Waypoint[] waypoints, float width, float height) {
        this.waypoints = waypoints;
        this.width = width;
        this.height = height;
    }
}

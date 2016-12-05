package com.icarus.project;

class Airport {
    //A list of waypoints that are in this waypoint
    public Waypoint[] waypoints;
    //Bondaries of the airport
    public float width;
    public float height;

    //Constructs an airport given the data pulled from the JSON airport file
    public Airport(Waypoint[] waypoints, float width, float height) {
        this.waypoints = waypoints;
        this.width = width;
        this.height = height;
    }
}

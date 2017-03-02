package com.icarus.project;

class Airport {
    //A list of waypoints that are in this airport
    public Waypoint[] waypoints;
    //A list of runways that are in this airport
    public Runway[] runways;
    //Bondaries of the airport
    public float width;
    public float height;

    //Constructs an airport given the data pulled from the JSON airport file
    public Airport(Waypoint[] waypoints, Runway[] runways, float width, float height) {
        this.waypoints = waypoints;
        this.runways = runways;
        this.width = width;
        this.height = height;
    }
}

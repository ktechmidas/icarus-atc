package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Align;
import com.google.gson.JsonObject;

class Waypoint {
    //The displayed name for the waypoint
    public String name;
    //The position in world coordinates
    public Vector2 position;
    //The display radius of the triangles at current zoom in pixels
    public static float waypointSize = 10.0f * Gdx.graphics.getDensity();

    //Constructs a waypoint directly
    public Waypoint(String name, Vector2 position) {
        this.name = name;
        this.position = position;
    }

    //Constructs a waypoint based on JSON. This is used when loading a level.
    public Waypoint(JsonObject json) {
        this(json.get("name").getAsString(),
                new Vector2(json.get("x").getAsFloat(), json.get("y").getAsFloat()));
    }

    //Draws the triangle in a given ShapeRender. This assumes that the zoom camera has already been
    //set up.
    public void draw(ShapeRenderer shapes, Camera camera) {
        Vector3 pos = camera.project(new Vector3(position.x, position.y, 0f));
        shapes.setColor(Colors.colors[3]);
        shapes.triangle(
                -0.7f * waypointSize + pos.x,
                -0.4041f * waypointSize + pos.y,
                0.7f * waypointSize + pos.x,
                -0.4041f * waypointSize + pos.y,
                0.0f * waypointSize + pos.x,
                0.8083f * waypointSize + pos.y);
    }

    //Draws the label for the waypoint. This assumes that the zoom camera has already been set up.
    //This is separate from draw because the labels should go on top of all of the triangles.
    public void drawLabel(BitmapFont font, SpriteBatch batch, Camera camera) {
        Vector3 pos = camera.project(new Vector3(position.x, position.y, 0f));
        font.setColor(Colors.colors[3]);
        font.draw(batch, name, pos.x - 100, pos.y - 20, 200, Align.center, false);
    }
}

package com.icarus.prototype;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.google.gson.JsonObject;

class Waypoint {
    public String name;
    public Vector2 position;
    private float size = 10.0f;

    public Waypoint(String name, Vector2 position) {
        this.name = name;
        this.position = position;
   }

    public Waypoint(JsonObject json) {
        this(json.get("name").getAsString(),
                new Vector2(json.get("x").getAsFloat(), json.get("y").getAsFloat()));
    }

    public void draw(ShapeRenderer shapes) {
       shapes.triangle(
          -0.7f * size + position.x,
          -0.4041f * size + position.y,
          0.7f * size + position.x,
          -0.4041f * size + position.y,
          0.0f * size + position.x,
          0.8083f * size + position.y);
    }
}

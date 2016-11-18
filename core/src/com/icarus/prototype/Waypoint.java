package com.icarus.prototype;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.google.gson.JsonObject;
import com.icarus.prototype.Colors;

class Waypoint {
    public String name;
    public Vector2 position;
    private static float waypointSize = 20.0f;

    public Waypoint(String name, Vector2 position) {
        this.name = name;
        this.position = position;
   }

    public Waypoint(JsonObject json) {
        this(json.get("name").getAsString(),
                new Vector2(json.get("x").getAsFloat(), json.get("y").getAsFloat()));
    }

    public void draw(ShapeRenderer shapes) {
        shapes.setColor(Colors.colors[3]);
        shapes.triangle(
          -0.7f * waypointSize + position.x,
          -0.4041f * waypointSize + position.y,
          0.7f * waypointSize + position.x,
          -0.4041f * waypointSize + position.y,
          0.0f * waypointSize + position.x,
          0.8083f * waypointSize + position.y);
    }

    public void drawLabel(BitmapFont font, SpriteBatch batch) {
        font.setColor(Colors.colors[3]);
//        font.getData().setScale(scale); //TODO Liam
        font.draw(batch, name, position.x - 100, position.y - 15, 200, Align.center, false);
    }

    public static void scaleWaypoint(float factor){
        waypointSize *= factor;
        Gdx.app.log("Waypoint", "" + waypointSize);
    }
}

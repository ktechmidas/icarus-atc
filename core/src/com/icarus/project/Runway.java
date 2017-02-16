package com.icarus.project;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Camera;
import com.google.gson.JsonObject;

class Runway {
    //The points in the runway
    Vector2[] points;

    //Constructs a runway directly.
    public Runway(Vector2[] points) {
        this.points = points;
    }

    //Constructs a runway based on JSON. This is used when loading a level.
    public Runway(JsonObject json) {
        this(new Vector2[] {
            new Vector2(
                    json.get("points").getAsJsonArray().get(0).getAsJsonObject().get("x")
                        .getAsFloat(),
                    json.get("points").getAsJsonArray().get(0).getAsJsonObject().get("y")
                        .getAsFloat()
            ),
            new Vector2(
                    json.get("points").getAsJsonArray().get(1).getAsJsonObject().get("x")
                        .getAsFloat(),
                    json.get("points").getAsJsonArray().get(1).getAsJsonObject().get("y")
                        .getAsFloat()
            )
        });
    }

    //Draws the runway
    public void draw(ShapeRenderer shapes, Camera camera) {
        Vector3 pos0 = camera.project(new Vector3(points[0].x, points[0].y, 0f));
        Vector3 pos1 = camera.project(new Vector3(points[1].x, points[1].y, 0f));
        shapes.setColor(Colors.colors[3]);
        shapes.rectLine(pos0.x, pos0.y, pos1.x, pos1.y, 8f);
    }
}

package com.icarus.project;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Align;
import com.google.gson.JsonObject;

class Runway {
    //The points in the runway
    Vector2[] points;

    Vector2[] nameOffsets;

    String[] names;

    private GlyphLayout glyphLayout;

    //Constructs a runway directly.
    public Runway(Vector2[] points, String[] names, Vector2[] nameOffsets) {
        glyphLayout = new GlyphLayout();
        this.points = points;
//        for(Vector2 point: this.points) {
//            point.add(0, MainUi.statusBarHeight);
//        }
        this.names = names;
        this.nameOffsets = nameOffsets;
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
            ),
        }, new String[] {
            json.get("names").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString(),
            json.get("names").getAsJsonArray().get(1).getAsJsonObject().get("name").getAsString(),
        }, new Vector2[] {
            new Vector2(
                    json.get("names").getAsJsonArray().get(0).getAsJsonObject().get("offset")
                        .getAsJsonObject().get("x")
                        .getAsFloat(),
                    json.get("names").getAsJsonArray().get(0).getAsJsonObject().get("offset")
                        .getAsJsonObject().get("y")
                        .getAsFloat()
            ),
            new Vector2(
                    json.get("names").getAsJsonArray().get(1).getAsJsonObject().get("offset")
                        .getAsJsonObject().get("x")
                        .getAsFloat(),
                    json.get("names").getAsJsonArray().get(1).getAsJsonObject().get("offset")
                        .getAsJsonObject().get("y")
                        .getAsFloat()
            ),

        });
    }

    //Draws the runway
    public void draw(ShapeRenderer shapes, Camera camera) {
        Vector3 pos0 = camera.project(new Vector3(points[0].x, points[0].y, 0f));
        Vector3 pos1 = camera.project(new Vector3(points[1].x, points[1].y, 0f));
        shapes.setColor(Colors.colors[3]);
        shapes.rectLine(pos0.x, pos0.y, pos1.x, pos1.y, 8f);
    }

    ///Draws the label for the runway.
    public void drawLabel(BitmapFont font, SpriteBatch batch, Camera camera) {
        for(int i = 0; i < 2; i++) {
            glyphLayout.setText(font, names[i]);
            Vector3 pos = camera.project(new Vector3(points[i].x, points[i].y, 0f));
            font.setColor(Colors.colors[3]);
            font.draw(batch, names[i],
                    pos.x + nameOffsets[i].x - 100,
                    pos.y + nameOffsets[i].y + glyphLayout.height / 2,
                    200, Align.center, false);
        }
    }
}

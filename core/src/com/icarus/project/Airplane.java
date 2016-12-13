package com.icarus.project;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;

class Airplane {
    //The global airplane image
    public static Texture texture;
    //The name of this airplane
    public String name;
    //The position of this airplane in meters
    public Vector2 position;
    //The velocity of this airplane in meters/second
    public Vector2 velocity;
    //The altitude of this airplane in meters
    public float altitude;
    //The sprite used by this airplane for display. It references the global texture.
    private Sprite sprite;

    public Airplane(String name, Vector2 position, Vector2 velocity, float altitude) {
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        this.altitude = altitude;
        sprite = new Sprite(texture);
    }

    //Draw the airplane image. This assumes that the camera has already been set up.
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    //Move the airplane image at evey render
    public void step() {
        position.add(velocity.cpy().scl(Gdx.graphics.getDeltaTime()));
        sprite.setPosition(position.x, position.y);

        //Point airplane in direction of travel
        double rotation = -Math.atan(velocity.x / velocity.y) * (180 / Math.PI);
        if (velocity.y < 0 && velocity.x < 0){
            sprite.setRotation(90 - (float) rotation);
//            Gdx.app.log("Airplane", "" + (90-rotation));
        }
        else if (velocity.y < 0){
            sprite.setRotation(-90 - (float) rotation);
//            Gdx.app.log("Airplane", "" + (-90 - rotation));
        }
        else {
            sprite.setRotation((float) rotation);
//            Gdx.app.log("Airplane", "" + rotation);
        }

    }
}

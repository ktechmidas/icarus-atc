package com.icarus.project;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;

class Airplane {
    public static Texture texture;
    public String name;
    public Vector2 position;
    public Vector2 velocity;
    public float altitude;
    private Sprite sprite;

    public Airplane(String name, Vector2 position, Vector2 velocity, float altitude) {
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        this.altitude = altitude;
        sprite = new Sprite(texture);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }
}

package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class MainUi {
    private BitmapFont font;
    private ShapeRenderer shapes;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ImageButton headingButton;

    public MainUi(AssetManager assets, BitmapFont font) {
        this.font = font;
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        Drawable headingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/heading_button.png")));
        headingButton = new ImageButton(headingDrawable);
    }

    public void draw() {
        shapes.setProjectionMatrix(camera.combined);
        //draw a rectangle for the status bar
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Colors.colors[0]);
        shapes.rect(0, 0, Gdx.graphics.getWidth(),  50);
        shapes.end();
        batch.begin();
        headingButton.draw(batch, 1);
        batch.end();
    }
}

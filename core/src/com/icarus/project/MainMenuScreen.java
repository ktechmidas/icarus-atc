package com.icarus.project;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.assets.AssetManager;

/**
 * Created by gmcfeeters3345 on 2/14/2017.
 */

public class MainMenuScreen implements Screen {
    final ProjectIcarus game;
    public Stage stage;
    AssetManager menuManager = new AssetManager();
    FileHandleResolver menuResolver = new InternalFileHandleResolver();


    OrthographicCamera camera;
    private BitmapFont font = new BitmapFont();
    private SpriteBatch batch;
    private Skin skin = new Skin();
    private ImageButton menuButton;
    private int buttonSize = (int) (100 * Gdx.graphics.getDensity());
    private int buttonGap = (int) (5 * Gdx.graphics.getDensity());
    public int statusBarHeight = (int) (25 * Gdx.graphics.getDensity());


    public MainMenuScreen(final ProjectIcarus game) {
        this.game = game;

        stage = new Stage();
        BitmapFont font = new BitmapFont();


        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();

        menuManager.load("buttons/landing_button.png", Texture.class);
        menuManager.finishLoading();
        Drawable landingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) menuManager.get("buttons/landing_button.png"))
        );
        menuButton = new ImageButton(landingDrawable);
        menuButton.setSize(buttonSize, buttonSize);
        menuButton.setPosition(4 * buttonGap + 3 * buttonSize, statusBarHeight + buttonGap);
        menuButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
               /* game.setScreen(new PIScreen(game));
                dispose();*/
               Gdx.app.log("MainMenuScreen","Main menu button");
            }
        });
        stage.addActor(menuButton);
        menuButton.setSize(buttonSize, buttonSize);

    }


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
            Gdx.gl.glClearColor(1, 1, 1, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            stage.act();
            stage.draw();
}


    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }


    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        menuManager.dispose();

    }

}

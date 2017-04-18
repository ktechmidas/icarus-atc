package com.icarus.project;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;

/**
 * Created by gmcfeeters3345 on 2/14/2017.
 */

public class MainMenuScreen implements Screen {
    final ProjectIcarus game;
    public Stage stage;
    private String status;


    OrthographicCamera camera;
    private BitmapFont font = new BitmapFont();
    private SpriteBatch batch;
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

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}

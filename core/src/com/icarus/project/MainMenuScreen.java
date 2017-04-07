package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Input;
import com.sun.glass.ui.EventLoop;

import java.util.Random;
/**
 * Created by gmcfeeters3345 on 2/14/2017.
 */

public class MainMenuScreen implements Screen, GestureDetector.GestureListener{
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
    private Texture playerPortrait1;

    private ImageButton playerBtn1;
    private Skin playerBtnSkin1;
    private ImageButton.ImageButtonStyle playerBtnStyle1;

    private ImageButton.ImageButtonStyle backBtnStyle;
    int[] positions = {1240, 150, 235, 500, 600, 500, 975, 500, 440, 200, 785, 200, 1280, 485};
    int drawCounter = 0;
    private Texture playerSelected;
    int selected = -1;
    public MainMenuScreen(final ProjectIcarus game) {

        this.game = game;
        stage = new Stage();
        BitmapFont font = new BitmapFont();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        playerBtnSkin1 = new Skin();   //create button skin
        playerBtnSkin1.add("playerBtn1", new Texture("buttons/landing_button.png"));    //add the image to the skin
        playerBtnStyle1 = new ImageButton.ImageButtonStyle();  //create button style
        playerBtnStyle1.imageUp = playerBtnSkin1.getDrawable("playerBtn1");  //sets the button appearance when it is not pressed
        playerBtnStyle1.imageDown = playerBtnSkin1.getDrawable("playerBtn1");    //sets the button appearance when it is pressed
        playerBtn1 = new ImageButton(playerBtnStyle1);    //initializes the ImageButton with the created style as a parameter
        playerBtn1.setBounds(positions[2], positions[3], playerBtn1.getWidth(), playerBtn1.getHeight());  //tells the button where to go
        stage.addActor(playerBtn1);
        while(1==1) {
            if (playerBtn1.isPressed()) {
                Gdx.app.log("MainMenuScreen", "Main menu button");
                playerBtn1.setDisabled(true);
            } else {

            }
        }
        //While loop is causeing black screen and am only chekcing once without it
        /*menuManager.load("buttons/landing_button.png", Texture.class);
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
                Gdx.app.log("MainMenuScreen","Main menu button");
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
               *//*game.setScreen(new PIScreen(game));
                dispose();*//*

            }
        });
        stage.addActor(menuButton);
        menuButton.setSize(buttonSize, buttonSize);*/

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
            Gdx.gl.glClearColor(1, 1, 1, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            playerBtn1.draw(batch, 1);
            batch.end();
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

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
}

package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.assets.AssetManager;

public class MainMenuScreen implements Screen, GestureDetector.GestureListener {
    final ProjectIcarus game;
    public Stage stage;
    int width = 0;
    int height = 0;
    AssetManager menuManager = new AssetManager();
    OrthographicCamera camera;
    private BitmapFont font = new BitmapFont();
    private SpriteBatch batch;
    private ImageButton playButton;
    private Skin playBtnSkin;
    private ImageButton.ImageButtonStyle playBtnStyle;
    int[] positions = {1240, 150, 235, 500, 600, 500, 975, 500, 440, 200, 785, 200, 1280, 485};
    private int buttonSize = (int) (100 * Gdx.graphics.getDensity());
    private Texture logo;

    public MainMenuScreen(final ProjectIcarus game) {
        this.game = game;
        stage = new Stage();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        playBtnSkin = new Skin();   //create button skin
        playBtnSkin.add("playButton", new Texture("buttons/play_button_pause.png"));//add the image to the skin
        playBtnStyle = new ImageButton.ImageButtonStyle();  //create button style
        playBtnStyle.imageUp = playBtnSkin.getDrawable("playButton");  //sets the button appearance when it is not pressed
        playBtnStyle.imageDown = playBtnSkin.getDrawable("playButton");    //sets the button appearance when it is pressed
        playButton = new ImageButton(playBtnStyle);    //initializes the ImageButton with the created style as a parameter
        playButton.setSize(buttonSize, buttonSize); //set button size
        int width = (int) ((Gdx.graphics.getWidth() - playButton.getWidth())/2); //set button orientation horizontally
        int height = (int) ((Gdx.graphics.getHeight() - playButton.getHeight())/4); //set button orientation vertically
        playButton.setBounds(width, height, playButton.getWidth(), playButton.getHeight());  //tells the button where to go
        playButton.addListener(new InputListener() {//adds listener to check for touch

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new PIScreen(game));//switch screen to game state
                playButton.setDisabled(true);

            }
        });
        stage.addActor(playButton);//adds the button to the stage
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new GestureDetector(this)));

        logo = new Texture(Gdx.files.internal("buttons/Icarus_Logo.png"));
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        game.assets.update();

        Gdx.gl.glClearColor(Colors.colors[0].r, Colors.colors[0].g, Colors.colors[2].b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        playButton.draw(batch, 1);
        float width = logo.getWidth() * Gdx.graphics.getDensity() / 3.0f;
        float height = logo.getHeight() * Gdx.graphics.getDensity() / 3.0f;
        batch.draw(logo,
                (Gdx.graphics.getWidth() - width)/2,
                2 * (Gdx.graphics.getHeight() - height)/3,
                width, height);
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
        stage.dispose();
        game.dispose();
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

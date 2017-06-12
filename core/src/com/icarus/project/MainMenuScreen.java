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
    private final ProjectIcarus game;
    private Stage stage;
    private AssetManager menuManager = new AssetManager();
    private BitmapFont font = new BitmapFont();
    private SpriteBatch batch;
    private ImageButton playButton;
    private ImageButton tutorialButton;
    private Texture logo;

    public MainMenuScreen(final ProjectIcarus game) {
        this.game = game;
        stage = new Stage();
        batch = new SpriteBatch();

        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // Button skin
        Skin buttonSkin = new Skin();
        buttonSkin.add("playButton", new Texture("buttons/play_button.png"));
        buttonSkin.add("tutorialButton", new Texture("buttons/tutorial_button.png"));

        // Create button style
        ImageButton.ImageButtonStyle playButtonStyle = new ImageButton.ImageButtonStyle();
        playButtonStyle.imageUp = buttonSkin.getDrawable("playButton"); // Unpressed
        playButtonStyle.imageDown = buttonSkin.getDrawable("playButton"); // Pressed

        ImageButton.ImageButtonStyle tutorialButtonStyle = new ImageButton.ImageButtonStyle();
        tutorialButtonStyle.imageUp = buttonSkin.getDrawable("tutorialButton"); // Unpressed
        tutorialButtonStyle.imageDown = buttonSkin.getDrawable("tutorialButton"); // Pressed

        int buttonSize = (int) (100 * Gdx.graphics.getDensity());

        // Play button
        playButton = new ImageButton(playButtonStyle);
        playButton.setSize(buttonSize, buttonSize);
        int x = (int) (Gdx.graphics.getWidth() / 3f - buttonSize / 2f);
        int y = (int) (Gdx.graphics.getHeight() / 4f - buttonSize / 2f);
        playButton.setBounds(x, y, playButton.getWidth(), playButton.getHeight());
        playButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new PlayScreen(game, false)); // Switch to play screen
                playButton.setDisabled(true);
                tutorialButton.setDisabled(true);
            }
        });
        stage.addActor(playButton);

        // Tutorial button
        tutorialButton = new ImageButton(tutorialButtonStyle);
        tutorialButton.setSize(buttonSize, buttonSize);
        x = (int) (2 * (x + buttonSize / 2f) - buttonSize / 2f);
        tutorialButton.setBounds(x, y, buttonSize, buttonSize);
        tutorialButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new PlayScreen(game, true)); // Switch to tutorial mode
                playButton.setDisabled(true);
                tutorialButton.setDisabled(true);
            }
        });
        stage.addActor(tutorialButton);

        // Game logo
        logo = new Texture(Gdx.files.internal("buttons/Icarus_Logo.png"));

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new GestureDetector(this)));
    }

    @Override
    public void render(float delta) {
        game.assets.update();

        // Set background color
        Gdx.gl.glClearColor(Colors.colors[0].r, Colors.colors[0].g, Colors.colors[1].b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        // Draw play button
        playButton.getImage().setColor(Colors.colors[4]);
        playButton.draw(batch, 1);

        // Draw tutorial button
        tutorialButton.getImage().setColor(Colors.colors[4]);
        tutorialButton.draw(batch, 1);

        // Draw logo
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
    public void dispose() {
        batch.dispose();
        font.dispose();
        menuManager.dispose();
        stage.dispose();
        game.dispose();
    }

    @Override
    public void show() {
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

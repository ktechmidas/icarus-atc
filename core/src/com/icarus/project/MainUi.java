package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class MainUi {
    private BitmapFont font;
    private ShapeRenderer shapes;
    private String status;
    private GlyphLayout layout;
    private SpriteBatch batch;
    public Stage stage;

    private ImageButton headingButton;
    private ImageButton altitudeButton;

    public static final String TAG = "MainUi";

    public int buttonSize = (int) (100 * Gdx.graphics.getDensity());
    public int buttonGap = (int) (5 * Gdx.graphics.getDensity());
    public int statusBarHeight = (int) (25 * Gdx.graphics.getDensity());

    public MainUi(AssetManager assets, BitmapFont font) {
        this.font = font;

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        stage = new Stage();
        layout = new GlyphLayout();

        status = "Hello, World!";

        Drawable headingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/heading_button.png"))
        );
        headingButton = new ImageButton(headingDrawable);
        headingButton.setPosition(0, statusBarHeight + buttonGap);
        headingButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                ProjectIcarus.getInstance().getSelectedAirplane().turn(10);
                setStatus("begin turning 10 degrees");
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
//                setStatus("headingButton up");
            }
        });
        stage.addActor(headingButton);
        headingButton.setSize(buttonSize, buttonSize);

        Drawable altitudeDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/altitude_button.png"))
        );
        altitudeButton = new ImageButton(altitudeDrawable);
        altitudeButton.setPosition(buttonSize, statusBarHeight + buttonGap);
        altitudeButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                setStatus("altitudeButton down");
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                setStatus("altitudeButton up");
            }
        });
        stage.addActor(altitudeButton);
        altitudeButton.setSize(buttonSize, buttonSize);

        showAirplaneButtons(false);
    }

    public void draw() {
        stage.draw();
        //draw a rectangle for the status bar
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, 1);
        shapes.rect(0, 0, Gdx.graphics.getWidth(), statusBarHeight);
        shapes.end();

        batch.begin();
        layout.setText(font, status);
        shapes.setColor(1, 1, 1, 1);
        font.draw(batch, status, Gdx.graphics.getWidth() / 2 - layout.width / 2,
                20 * Gdx.graphics.getDensity()
        );
        batch.end();

        //show airplane-specific buttons if an airplane is selected
        showAirplaneButtons(ProjectIcarus.getInstance().getSelectedAirplane() != null);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void showAirplaneButtons(boolean isVisible){
        headingButton.setVisible(isVisible);
        altitudeButton.setVisible(isVisible);
    }
}

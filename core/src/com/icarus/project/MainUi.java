package com.icarus.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
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
    private ImageButton circleButton;
    private ImageButton headingWheel;
    private ImageButton landingButton;

    public static final String TAG = "MainUi";

    private int buttonSize = (int) (100 * Gdx.graphics.getDensity());
    private int buttonGap = (int) (5 * Gdx.graphics.getDensity());
    private int statusBarHeight = (int) (25 * Gdx.graphics.getDensity());

    public MainUi(AssetManager assets, BitmapFont font) {
        this.font = font;

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        stage = new Stage();
        layout = new GlyphLayout();

        status = "Welcome to Icarus Air Traffic Control";

        Drawable headingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/heading_button.png"))
        );
        headingButton = new ImageButton(headingDrawable);
        headingButton.setPosition(0, statusBarHeight + buttonGap);
        headingButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                setStatus("Select a target waypoint");
                PIScreen.getInstance().followingPlane = false;
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_WAYPOINT;
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
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                setStatus("altitudeButton up");
            }
        });
        stage.addActor(altitudeButton);
        altitudeButton.setSize(buttonSize, buttonSize);

        Drawable circleDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/circle_button.png"))
        );
        circleButton = new ImageButton(circleDrawable);
        circleButton.setPosition(2 * buttonSize, statusBarHeight + buttonGap);
        circleButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                showHeadingSelector(true);
                showAirplaneButtons(false);
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_HEADING;
            }
        });
        stage.addActor(circleButton);
        circleButton.setSize(buttonSize, buttonSize);

        Drawable headingWheelDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/selection_wheel.png"))
        );
        headingWheel = new ImageButton(headingWheelDrawable);
        int wheelSize = Gdx.graphics.getHeight() - statusBarHeight;
        headingWheel.setSize(wheelSize, wheelSize);
        headingWheel.setPosition(Gdx.graphics.getWidth()/2 - headingWheel.getWidth()/2,
                                 statusBarHeight
        );
        headingWheel.addListener(new DragListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                Vector2 heading = new Vector2(x, y).sub(headingWheel.getWidth()/2, headingWheel.getHeight()/2);
                setStatus((int) heading.rotate(-90).angle() + "");
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                Vector2 heading = new Vector2(x, y).sub(headingWheel.getWidth()/2, headingWheel.getHeight()/2);
                showHeadingSelector(false);
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                setStatus((int) heading.angle() + "");
                PIScreen.getInstance().getSelectedAirplane().setTargetHeading(heading);
            }
        });
        stage.addActor(headingWheel);

        Drawable landingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/landing_button.png"))
        );
        landingButton = new ImageButton(landingDrawable);
        landingButton.setPosition(3 * buttonSize, statusBarHeight + buttonGap);
        landingButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                setStatus("landingButton");
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_RUNWAY;
                PIScreen.getInstance().followingPlane = false;
            }
        });
        stage.addActor(landingButton);
        landingButton.setSize(buttonSize, buttonSize);

        showAirplaneButtons(false);
        showHeadingSelector(false);
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
        font.setColor(Colors.colors[3]);
        font.draw(batch, status, Gdx.graphics.getWidth() / 2 - layout.width / 2,
                20 * Gdx.graphics.getDensity()
        );
        batch.end();

        //show airplane-specific buttons if an airplane is selected
        showAirplaneButtons(PIScreen.getInstance().getSelectedAirplane() != null
                && PIScreen.getInstance().uiState != ProjectIcarus.UiState.SELECT_HEADING);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void showAirplaneButtons(boolean isVisible){
        headingButton.setVisible(isVisible);
        altitudeButton.setVisible(isVisible);
        circleButton.setVisible(isVisible);
        landingButton.setVisible(isVisible);
    }

    public void showHeadingSelector(boolean isVisible){
        headingWheel.setVisible(isVisible);
    }
}

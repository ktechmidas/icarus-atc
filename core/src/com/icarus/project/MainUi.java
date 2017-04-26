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
    private ImageButton waypointButton;
    private ImageButton altitudeButton;
    private ImageButton circleButton;
    private ImageButton headingWheel;
    private ImageButton landingButton;
    private ImageButton warpUpButton;
    private ImageButton warpDownButton;

    public static final String TAG = "MainUi";

    private int buttonSize = (int) (100 * Gdx.graphics.getDensity());
    private int buttonGap = (int) (5 * Gdx.graphics.getDensity());
    public int statusBarHeight = (int) (25 * Gdx.graphics.getDensity());
    public MainUi(AssetManager assets, BitmapFont font) {
        this.font = font;

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        stage = new Stage();
        layout = new GlyphLayout();

        status = "Welcome to Icarus Air Traffic Control";

        // Initialize heading selection button
        Drawable headingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/heading_button.png"))
        );
        headingButton = new ImageButton(headingDrawable);
        headingButton.setSize(buttonSize, buttonSize);
        headingButton.setPosition(buttonGap, statusBarHeight + buttonGap);
        headingButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
//                showHeadingSelector(true);
//                showAirplaneButtons(false);
//                hideAirplaneButtons();
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_HEADING;
            }
        });
        stage.addActor(headingButton);

        // Initialize waypoint selection button
        Drawable waypointDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/waypoint_button.png"))
        );
        waypointButton = new ImageButton(waypointDrawable);
        waypointButton.setSize(buttonSize, buttonSize);
        waypointButton.setPosition(2 * buttonGap + buttonSize, statusBarHeight + buttonGap);
        waypointButton.addListener(new InputListener(){
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
        stage.addActor(waypointButton);

        // Initialize altitude button
        Drawable altitudeDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/altitude_button.png"))
        );
        altitudeButton = new ImageButton(altitudeDrawable);
        altitudeButton.setSize(buttonSize, buttonSize);
        altitudeButton.setPosition(3 * buttonGap + 2 * buttonSize, statusBarHeight + buttonGap);
        altitudeButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.CHANGE_ALTITUDE;
                PIScreen.getInstance().altitudeTarget =
                    PIScreen.getInstance().selectedAirplane.getAltitude();
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            }
        });
        stage.addActor(altitudeButton);

        // Initialize circling button
        Drawable circleDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/circle_button.png"))
        );
        circleButton = new ImageButton(circleDrawable);
        circleButton.setSize(buttonSize, buttonSize);
        circleButton.setPosition(4 * buttonGap + 3 * buttonSize, statusBarHeight + buttonGap);
        circleButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                // Circle airport
            }
        });
        stage.addActor(circleButton);

        // Initialize landing button
        Drawable landingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/landing_button.png"))
        );
        landingButton = new ImageButton(landingDrawable);
        landingButton.setSize(buttonSize, buttonSize);
        landingButton.setPosition(5 * buttonGap + 4 * buttonSize, statusBarHeight + buttonGap);
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

        // Initialize heading selection wheel
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

        Drawable warpUpDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/warpup.png"))
        );
        warpUpButton = new ImageButton(warpUpDrawable);
        warpUpButton.setSize(buttonSize / 2, buttonSize / 2);
        warpUpButton.setPosition(3 * buttonGap + buttonSize,
                Gdx.graphics.getHeight() - buttonGap - buttonSize / 2);
        warpUpButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                PIScreen.getInstance().warpSpeed *= 2.0;
            }
        });
        stage.addActor(warpUpButton);

        Drawable warpDownDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/warpdown.png"))
        );
        warpDownButton = new ImageButton(warpDownDrawable);
        warpDownButton.setSize(buttonSize / 2, buttonSize / 2);
        warpDownButton.setPosition(1 * buttonGap,
                Gdx.graphics.getHeight() - buttonGap - buttonSize / 2);
        warpDownButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(PIScreen.getInstance().warpSpeed > 1.0) {
                    PIScreen.getInstance().warpSpeed *= 0.5;
                }
            }
        });
        stage.addActor(warpDownButton);

        //showAirplaneButtons(false);
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

        font.setColor(Colors.colors[4]);
        String warp = "x" + (int)(PIScreen.getInstance().warpSpeed);
        layout.setText(font, warp);
        font.draw(batch, warp, 2 * buttonGap + 3 * buttonSize / 4 - layout.width / 2,
                Gdx.graphics.getHeight() - buttonGap - buttonSize / 4);
        batch.end();

        //show airplane-specific buttons if an airplane is selected
        if(PIScreen.getInstance().uiState == ProjectIcarus.UiState.SELECT_HEADING) {
            showHeadingSelector(true);
            hideAirplaneButtons();
        }
        else if(PIScreen.getInstance().getSelectedAirplane() != null) {
            Airplane airplane = PIScreen.getInstance().getSelectedAirplane();
            showAirplaneButtons(true, airplane.flightType);
            showHeadingSelector(false);

            //draw a rectangle for airplane status
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(Colors.colors[4]);
            int statusWidth = (int)(200.0 * Gdx.graphics.getDensity());
            shapes.rect(
                    Gdx.graphics.getWidth() - statusWidth, Gdx.graphics.getHeight() - 100,
                    statusWidth, 100);
            shapes.end();

            batch.begin();
            font.setColor(Colors.colors[0]);
            font.draw(batch, airplane.name,
                    Gdx.graphics.getWidth() - statusWidth + 10, Gdx.graphics.getHeight() - 20);

            String type = null;
            if(PIScreen.getInstance().getSelectedAirplane().flightType ==
                    Airplane.FlightType.ARRIVAL)
            {
                type = "Arrival";
            }
            else if(PIScreen.getInstance().getSelectedAirplane().flightType ==
                    Airplane.FlightType.FLYOVER)
            {
                type = "Flyover";
            }
            else if(PIScreen.getInstance().getSelectedAirplane().flightType ==
                    Airplane.FlightType.DEPARTURE)
            {
                type = "Departure";
            }
            font.draw(batch, type,
                    Gdx.graphics.getWidth() - statusWidth + 10, Gdx.graphics.getHeight() - 50);

            if(airplane.stateType == Airplane.StateType.FLYING ||
                    airplane.stateType == Airplane.StateType.LANDING)
            {
                String alt = (int)airplane.getAltitude() + "m";
                font.draw(batch, alt,
                        Gdx.graphics.getWidth() - statusWidth / 2 + 10,
                        Gdx.graphics.getHeight() - 20);
            }

            batch.end();
        }
        else {
            hideAirplaneButtons();
        }
//        showAirplaneButtons(PIScreen.getInstance().getSelectedAirplane() != null
//                && PIScreen.getInstance().uiState != ProjectIcarus.UiState.SELECT_HEADING);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void showAirplaneButtons(boolean isVisible, Airplane.FlightType flightType){
        if(flightType == Airplane.FlightType.ARRIVAL) {
            circleButton.setVisible(isVisible);
            landingButton.setVisible(isVisible);
        }
        headingButton.setVisible(isVisible);
        waypointButton.setVisible(isVisible);
        altitudeButton.setVisible(isVisible);
    }

    public void hideAirplaneButtons() {
        headingButton.setVisible(false);
        waypointButton.setVisible(false);
        altitudeButton.setVisible(false);
        circleButton.setVisible(false);
        landingButton.setVisible(false);
    }

    public void showHeadingSelector(boolean isVisible){
        headingWheel.setVisible(isVisible);
        //showAirplaneButtons(!isVisible);
    }
}

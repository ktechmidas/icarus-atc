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

import static com.icarus.project.Airplane.StateType.FLYING;
import static com.icarus.project.Airplane.StateType.LANDING;

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
    private ImageButton headingWheel;
    private ImageButton landingButton;
    private ImageButton takeoffButton;
    private ImageButton handoffButton;
    private ImageButton cancelButton;
    private ImageButton warpUpButton;
    private ImageButton warpDownButton;
    private ImageButton pauseButton;
    private ImageButton playPauseButton;
    float warpPause = 0;
    public int points;


    public static final String TAG = "MainUi";

    public int statusBarHeight = (int) (25 * Gdx.graphics.getDensity());
    private int buttonGap = (int) (5 * Gdx.graphics.getDensity());
    private int buttonSize = (Gdx.graphics.getHeight() - 5 * buttonGap - statusBarHeight) / 4;
    private int warpButtonSize = buttonSize / 2;
//    public int buttonBarWidth = buttonSize + 2 * buttonGap;

    private Airplane selectedAirplane;

    public MainUi(AssetManager assets, BitmapFont font) {
        this.font = font;
        points = points+0;

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        stage = new Stage();
        layout = new GlyphLayout();

        status = "Welcome to Icarus Air Traffic Control";

        Vector2 buttonPosition = new Vector2(buttonGap,
                Gdx.graphics.getHeight() - buttonGap - buttonSize
        );

        Drawable warpDownDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/warpdown.png"))
        );
        warpDownButton = new ImageButton(warpDownDrawable);
        warpDownButton.setSize(warpButtonSize, warpButtonSize);
        warpDownButton.setPosition(Gdx.graphics.getWidth() - 3 * buttonGap - 3 * warpButtonSize,
                statusBarHeight + buttonGap
        );
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

        Drawable pauseDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/pause_button.png"))
        );
        pauseButton = new ImageButton(pauseDrawable);
        pauseButton.setSize(warpButtonSize, warpButtonSize);
        pauseButton.setPosition(Gdx.graphics.getWidth() - 2 * buttonGap - 2 * warpButtonSize,
                statusBarHeight + buttonGap
        );
        pauseButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(PIScreen.getInstance().warpSpeed >= 1.0 ) {
                    warpPause = PIScreen.getInstance().warpSpeed;
                    PIScreen.getInstance().warpSpeed = 0;
                    playPauseButton.setVisible(true);
                    pauseButton.setVisible(false);
                }
               /* else if(PIScreen.getInstance().warpSpeed == 0) {
                   PIScreen.getInstance().warpSpeed = warpPause;
                }*/
            }
        });
        stage.addActor(pauseButton);

        Drawable playPauseDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/play_button_pause.png"))
        );
        playPauseButton = new ImageButton(playPauseDrawable);
        playPauseButton.setSize(buttonSize / 2, buttonSize / 2);
        playPauseButton.setPosition(Gdx.graphics.getWidth() - 2 * buttonGap - 2 * warpButtonSize,
                statusBarHeight + buttonGap
        );
        playPauseButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(PIScreen.getInstance().warpSpeed == 0) {
                    PIScreen.getInstance().warpSpeed = warpPause;
                    playPauseButton.setVisible(false);
                    pauseButton.setVisible(true);

                }
            }
        });
        stage.addActor(playPauseButton);
        playPauseButton.setVisible(false);

        Drawable warpUpDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/warpup.png"))
        );
        warpUpButton = new ImageButton(warpUpDrawable);
        warpUpButton.setSize(warpButtonSize, warpButtonSize);
        warpUpButton.setPosition(Gdx.graphics.getWidth() - buttonGap - warpButtonSize,
                statusBarHeight + buttonGap
        );
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

        // Initialize heading selection button
        Drawable headingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/heading_button.png"))
        );
        headingButton = new ImageButton(headingDrawable);
        headingButton.setSize(buttonSize, buttonSize);
        headingButton.setPosition(buttonPosition.x, buttonPosition.y);
        headingButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_HEADING;
            }
        });
        stage.addActor(headingButton);

        // Set next button position
        buttonPosition.y -= buttonGap + buttonSize;

        // Initialize waypoint selection button
        Drawable waypointDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/waypoint_button.png"))
        );
        waypointButton = new ImageButton(waypointDrawable);
        waypointButton.setSize(buttonSize, buttonSize);
        waypointButton.setPosition(buttonPosition.x, buttonPosition.y);
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

        // Set next button position
        buttonPosition.y -= buttonGap + buttonSize;

        // Initialize altitude button
        Drawable altitudeDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/altitude_button.png"))
        );
        altitudeButton = new ImageButton(altitudeDrawable);
        altitudeButton.setSize(buttonSize, buttonSize);
        altitudeButton.setPosition(buttonPosition.x, buttonPosition.y);
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

        // Set next button position
        buttonPosition.y -= buttonGap + buttonSize;

        // Initialize handoff button
        Drawable handoffDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/handoff_button.png"))
        );
        handoffButton = new ImageButton(handoffDrawable);
        handoffButton.setSize(buttonSize, buttonSize);
        handoffButton.setPosition(buttonPosition.x, buttonPosition.y);
        handoffButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                setStatus("handoffButton");
            }
        });
        stage.addActor(handoffButton);

        // Initialize landing button
        Drawable landingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/landing_button.png"))
        );
        landingButton = new ImageButton(landingDrawable);
        landingButton.setSize(buttonSize, buttonSize);
        landingButton.setPosition(buttonPosition.x, buttonPosition.y);
        landingButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                setStatus("Please select one end of a runway");
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_RUNWAY;
                PIScreen.getInstance().followingPlane = false;
            }
        });
        stage.addActor(landingButton);

        // Initialize takeoff button
        Drawable takeoffDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/takeoff_button.png"))
        );
        takeoffButton = new ImageButton(takeoffDrawable);
        takeoffButton.setSize(buttonSize, buttonSize);
        takeoffButton.setPosition(5 * buttonGap + 4 * buttonSize, statusBarHeight + buttonGap);
        takeoffButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                setStatus("takeoffButton");
            }
        });
        stage.addActor(takeoffButton);

        // Initialize cancel button
        Drawable cancelDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/cancel_button.png"))
        );
        cancelButton = new ImageButton(cancelDrawable);
        cancelButton.setSize(buttonSize, buttonSize);
        cancelButton.setPosition(buttonGap, statusBarHeight + buttonGap);
        cancelButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                selectedAirplane.setNoTarget();
                selectedAirplane.setTargetAltitude(5000);
                setStatus("Cancelled landing");
            }
        });
        stage.addActor(cancelButton);

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
                setStatus((int) heading.cpy().rotate(-90).angle() + "°");
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                Vector2 heading = new Vector2(x, y).sub(headingWheel.getWidth()/2, headingWheel.getHeight()/2);
                toggleHeadingSelector(false);
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                setStatus(selectedAirplane.name + ": turning to "
                        + (int) heading.cpy().rotate(-90).angle() + "°"
                );
                selectedAirplane.setTargetHeading(heading);
            }
        });
        stage.addActor(headingWheel);

        toggleHeadingSelector(false);
        hideAirplaneButtons();
    }

    public void draw() {
        selectedAirplane = PIScreen.getInstance().getSelectedAirplane();

//        stage.draw();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Draw rectangle for the status bar
        shapes.setColor(0, 0, 0, 1);
        shapes.rect(0, 0, Gdx.graphics.getWidth(), statusBarHeight);
        shapes.end();

        stage.draw();

        batch.begin();
        // Draw status text
        layout.setText(font, status);
        font.setColor(Colors.colors[3]);
        font.draw(batch,
                status,
                Gdx.graphics.getWidth() / 2 - layout.width / 2,
                15 * Gdx.graphics.getDensity()
        );

        //draw the warp speed
        font.setColor(Colors.colors[4]);
        String warp = "x" + (int)(PIScreen.getInstance().warpSpeed);
        layout.setText(font, warp);
        font.draw(batch,
                warp,
                Gdx.graphics.getWidth() - (4 * buttonGap + 3 * warpButtonSize) / 2 - layout.width / 2,
                15 * Gdx.graphics.getDensity()
        );

        //draw points
        font.setColor(Colors.colors[4]);
        String point = "Points: " + PIScreen.getInstance().points;
        layout.setText(font, point);
        font.draw(batch,
                point,
                buttonGap,
                15 * Gdx.graphics.getDensity()
        );

        //String
        batch.end();

        //show airplane-specific buttons if an airplane is selected
        if(PIScreen.getInstance().uiState == ProjectIcarus.UiState.SELECT_HEADING) {
            toggleHeadingSelector(true);
            hideAirplaneButtons();
        }
        else if(PIScreen.getInstance().uiState == ProjectIcarus.UiState.SELECT_WAYPOINT) {
            hideAirplaneButtons();
        }
        else if(selectedAirplane != null) {
            showAirplaneButtons(selectedAirplane.flightType);
            toggleHeadingSelector(false);

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
            font.draw(batch, selectedAirplane.name,
                    Gdx.graphics.getWidth() - statusWidth + 10, Gdx.graphics.getHeight() - 20);

            String type = null;
            if(selectedAirplane.flightType ==
                    Airplane.FlightType.ARRIVAL)
            {
                type = "Arrival";
            }
            else if(selectedAirplane.flightType ==
                    Airplane.FlightType.FLYOVER)
            {
                type = "Flyover";
            }
            else if(selectedAirplane.flightType ==
                    Airplane.FlightType.DEPARTURE)
            {
                type = "Departure";
            }
            font.draw(batch, type,
                    Gdx.graphics.getWidth() - statusWidth + 10, Gdx.graphics.getHeight() - 50);

            if(selectedAirplane.stateType == FLYING || selectedAirplane.stateType == LANDING) {
                String alt = (int) selectedAirplane.getAltitude() + "m";
                font.draw(batch, alt,
                        Gdx.graphics.getWidth() - statusWidth / 2 + 10,
                        Gdx.graphics.getHeight() - 20);
            }

            font.draw(batch,
                    (int) PIScreen.toMeters(selectedAirplane.getVelocity().len()) + "m/s",
                    Gdx.graphics.getWidth() - statusWidth / 2 + 10,
                    Gdx.graphics.getHeight() - 50
            );

            batch.end();
        }
        else {
            hideAirplaneButtons();
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void showAirplaneButtons(Airplane.FlightType flightType){
        hideAirplaneButtons();
        if(selectedAirplane.stateType == FLYING) {
            if(selectedAirplane.getTargetType() == AirplaneFlying.TargetType.RUNWAY) {
                cancelButton.setVisible(true);
            }
            else {
                if (flightType == Airplane.FlightType.ARRIVAL) {
                    landingButton.setVisible(true);
                } else if (flightType == Airplane.FlightType.DEPARTURE
                        || flightType == Airplane.FlightType.FLYOVER) {
                    handoffButton.setVisible(true);
                }
                headingButton.setVisible(true);
                waypointButton.setVisible(true);
                altitudeButton.setVisible(true);
            }
        }
    }

    public void hideAirplaneButtons() {
        headingButton.setVisible(false);
        waypointButton.setVisible(false);
        altitudeButton.setVisible(false);
        landingButton.setVisible(false);
        takeoffButton.setVisible(false);
        handoffButton.setVisible(false);
        cancelButton.setVisible(false);
    }

    public void toggleHeadingSelector(boolean isVisible){
        headingWheel.setVisible(isVisible);
    }
}

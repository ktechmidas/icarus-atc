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

import java.util.ArrayList;

import static com.icarus.project.Airplane.StateType.FLYING;
import static com.icarus.project.Airplane.StateType.LANDING;
import static com.icarus.project.Airplane.StateType.QUEUEING;

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

    private ArrayList<ImageButton> airportButtons;
    float warpPause = 0;
//    public int points;

    public static final String TAG = "MainUi";

    public int statusBarHeight;
    private int buttonGap;
    private int buttonSize;
    private int airportButtonSize;
    private int warpButtonSize;

    private int textGap = (int) (10 * Gdx.graphics.getDensity());

    private Airplane selectedAirplane;

    public MainUi(AssetManager assets, BitmapFont font) {
        this.font = font;
//        points = points+0;

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        stage = new Stage();
        layout = new GlyphLayout();

        statusBarHeight = (int) font.getLineHeight();
        buttonGap = (int) (5 * Gdx.graphics.getDensity());
        buttonSize = (Gdx.graphics.getHeight() - 5 * buttonGap - statusBarHeight) / 4;
        airportButtonSize = (int) ((2f / 3f) * buttonSize);
        warpButtonSize = buttonSize / 2;

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
                if(PIScreen.getInstance().warpSpeed < 8) {
                    PIScreen.getInstance().warpSpeed *= 2.0;
                }
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
                    PIScreen.getInstance().selectedAirplane.state.getAltitude();
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
                selectedAirplane.setTargetAltitude(10000);
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_AIRPORT;
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
        takeoffButton.setPosition(Gdx.graphics.getWidth() - buttonSize - buttonGap,
                statusBarHeight + 2 * buttonGap + warpButtonSize

        );
        takeoffButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(PIScreen.getInstance().queueingAirplanes.size() > 0) {
                    setStatus("Please select one end of a runway");
                    PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_RUNWAY;
                    PIScreen.getInstance().followingPlane = false;
                    PIScreen.getInstance().setSelectedAirplane(
                            PIScreen.getInstance().queueingAirplanes.get(0)
                    );

                    PIScreen.getInstance().toggleOverview(false);
                }
                else {
                    setStatus("Queue is empty");
                }
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
                if(selectedAirplane.getTargetType() == AirplaneFlying.TargetType.RUNWAY) {
                    setStatus(selectedAirplane.name + ": Aborted landing");
                    selectedAirplane.setTargetAltitude(2000);
                }
                if(PIScreen.getInstance().uiState != ProjectIcarus.UiState.CHANGE_ALTITUDE) {
                    selectedAirplane.setNoTarget();
                }
                if(selectedAirplane.stateType == QUEUEING) {
                    PIScreen.getInstance().setSelectedAirplane(null);
                }
                PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
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

        // Create airport buttons
        airportButtons = new ArrayList<>();
        for(final OtherAirport otherAirport: PIScreen.getInstance().otherAirports) {
            // Determine scale factor to position airport on screen
            float scaleFactor = ((Gdx.graphics.getHeight()
                        - airportButtonSize
                        - statusBarHeight) / 2 - font.getLineHeight())
                    / (float) PIScreen.getInstance().farthestAirportDistance;
            Vector2 airportPos = otherAirport.position.cpy().scl(scaleFactor)
                    .add(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
            // Initialize airport button
            Drawable airportDrawable = new TextureRegionDrawable(
                    new TextureRegion((Texture) assets.get("buttons/airport.png"))
            );
            ImageButton airportButton = new ImageButton(airportDrawable);
            airportButton.setSize(airportButtonSize, airportButtonSize);
            airportButton.setPosition(
                    airportPos.x - airportButtonSize / 2,
                    airportPos.y - airportButtonSize / 2
            );
            airportButton.getImage().setColor(Colors.colors[3]); // Same as waypoint color
            airportButton.addListener(new InputListener() {
                @Override
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                @Override
                public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                    PIScreen.getInstance().uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                    setStatus("Selected airport " + otherAirport.name);
                    selectedAirplane.setTargetAirport(otherAirport);
                }
            });
            stage.addActor(airportButton);
            airportButton.setVisible(false);
            airportButtons.add(airportButton);
        }

        toggleHeadingSelector(false);
        hideAirplaneButtons();
        takeoffButton.setVisible(true);
    }

    public void draw() {
        selectedAirplane = PIScreen.getInstance().getSelectedAirplane();

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
                font.getLineHeight() / 2 + (statusBarHeight - font.getCapHeight()) / 2
        );

        //draw the warp speed
        String warp = "x" + (int)(PIScreen.getInstance().warpSpeed);
        layout.setText(font, warp);
        font.draw(batch,
                warp,
                Gdx.graphics.getWidth() - (4 * buttonGap + 3 * warpButtonSize) / 2 - layout.width / 2,
                font.getLineHeight() / 2 + (statusBarHeight - font.getCapHeight()) / 2
        );

        //draw points
        String point = "Points: " + PIScreen.getInstance().points;
        layout.setText(font, point);
        font.draw(batch,
                point,
                textGap,
                font.getLineHeight() / 2 + (statusBarHeight - font.getCapHeight()) / 2
        );

        // Draw queueingAirplanes size
        String size = "" + PIScreen.getInstance().queueingAirplanes.size();
        layout.setText(font, size);
        font.draw(batch,
                size,
                Gdx.graphics.getWidth() - (2 * buttonGap + warpButtonSize) / 2 - layout.width / 2,
                font.getLineHeight() / 2 + (statusBarHeight - font.getCapHeight()) / 2
        );
        batch.end();

        toggleOtherAirports(false);
        toggleHeadingSelector(false);
        hideAirplaneButtons();
        takeoffButton.setVisible(false);
        //show airplane-specific buttons if an airplane is selected
        if(selectedAirplane != null) {
            switch(PIScreen.getInstance().uiState) {
                case CHANGE_ALTITUDE:
                    cancelButton.setVisible(true);
                    break;
                case SELECT_AIRPLANE:
                    showAirplaneButtons(selectedAirplane.flightType);
                    takeoffButton.setVisible(true);

                    //draw a rectangle for airplane status
                    shapes.begin(ShapeRenderer.ShapeType.Filled);
                    shapes.setColor(Colors.colors[4]);
                    int statusWidth = (int)(200.0 * Gdx.graphics.getDensity());
                    shapes.rect(
                            Gdx.graphics.getWidth() - statusWidth,
                            Gdx.graphics.getHeight() - 5 * font.getLineHeight() / 2,
                            statusWidth,
                            5 * font.getLineHeight() / 2
                    );
                    shapes.end();

                    batch.begin();
                    font.setColor(Colors.colors[0]);
                    font.draw(batch,
                            selectedAirplane.name,
                            Gdx.graphics.getWidth() - statusWidth + textGap,
                            Gdx.graphics.getHeight() - font.getLineHeight() / 2
                    );

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
                    font.draw(batch,
                            type,
                            Gdx.graphics.getWidth() - statusWidth + textGap,
                            Gdx.graphics.getHeight() - 3 * font.getLineHeight() / 2
                    );

                    if(selectedAirplane.stateType == FLYING
                            || selectedAirplane.stateType == LANDING)
                    {
                        String alt = (int) selectedAirplane.state.getAltitude() + "m";
                        font.draw(batch,
                                alt,
                                Gdx.graphics.getWidth() - (3f / 8f) * statusWidth,
                                Gdx.graphics.getHeight() - font.getLineHeight() / 2
                        );
                    }

                    if(selectedAirplane.getVelocity() != null) {
                        font.draw(batch,
                                (int) PIScreen.toMeters(selectedAirplane.getVelocity().len()) + "m/s",
                                Gdx.graphics.getWidth() - (3f / 8f) * statusWidth,
                                Gdx.graphics.getHeight() - 3 * font.getLineHeight() / 2
                        );
                    }

                    batch.end();
                    break;
                case SELECT_AIRPORT:
                    toggleOtherAirports(true);
                    cancelButton.setVisible(true);
                    break;
                case SELECT_HEADING:
                    toggleHeadingSelector(true);
                    cancelButton.setVisible(true);
                    break;
                case SELECT_RUNWAY:
                    cancelButton.setVisible(true);
                    break;
                case SELECT_WAYPOINT:
                    cancelButton.setVisible(true);
                    break;
                default:
                    break;
            }
        }
        else {
            takeoffButton.setVisible(true);
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private void showAirplaneButtons(Airplane.FlightType flightType){
        hideAirplaneButtons();
        if(selectedAirplane.stateType == FLYING) {
            if(selectedAirplane.getTargetType() == AirplaneFlying.TargetType.NONE) {
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
            else {
                if(selectedAirplane.getTargetType() == AirplaneFlying.TargetType.HEADING
                        || selectedAirplane.getTargetType() == AirplaneFlying.TargetType.WAYPOINT) {
                    altitudeButton.setVisible(true);
                }
                cancelButton.setVisible(true);
            }
        }
    }

    private void hideAirplaneButtons() {
        headingButton.setVisible(false);
        waypointButton.setVisible(false);
        altitudeButton.setVisible(false);
        landingButton.setVisible(false);
        handoffButton.setVisible(false);
        cancelButton.setVisible(false);
    }

    private void toggleOtherAirports(boolean isVisible) {
        batch.begin();
        for(ImageButton airportButton: airportButtons) {
            OtherAirport otherAirport = PIScreen.getInstance().otherAirports
                    .get(airportButtons.indexOf(airportButton));
            airportButton.setVisible(isVisible);
            if(isVisible) {
                font.setColor(Colors.colors[3]); // Same as waypoint text color
                font.draw(batch,
                        otherAirport.name,
                        airportButton.getX() + (airportButtonSize - 3 * font.getSpaceWidth()) / 2,
                        airportButton.getY() - textGap
                );
            }
        }
        batch.end();
    }

    public void toggleHeadingSelector(boolean isVisible){
        headingWheel.setVisible(isVisible);
    }
}

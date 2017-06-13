package com.icarus.project;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static com.icarus.project.Airplane.FlightType.ARRIVAL;
import static com.icarus.project.Airplane.FlightType.DEPARTURE;
import static com.icarus.project.Airplane.FlightType.FLYOVER;

public class PlayScreen extends Game implements Screen, GestureDetector.GestureListener {
    // This class
    public static PlayScreen self;
    public static final String TAG = "PIState";

    // Is this a tutorial game?
    public final boolean isTutorial;
    public TutorialState tutorialState;
    public int tutorialIndex;
    public float tutorialTimer;
    public int tutorialMessagePause = 5; // seconds

    // The currently loaded Airport
    private Airport airport;

    // Other airports
    public int farthestAirportDistance;
    public ArrayList<OtherAirport> otherAirports;

    // UI
    private MainUi ui;
    public ProjectIcarus.UiState uiState;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont labelFont, smallLabelFont, titleFont, airplaneFont;

    // Camera
    private OrthographicCamera camera;
    private float maxZoomIn; // Maximum possible zoomed in distance
    private float maxZoomOut; // Maximum possible zoomed out distance
    public boolean followingPlane;
    private Vector2 oldInitialFirstPointer=null, oldInitialSecondPointer=null;
    private float oldScale;
    private boolean zoomedOut;

    // Pan boundaries
    private float toBoundaryRight, toBoundaryLeft, toBoundaryTop, toBoundaryBottom;

    // Airplanes
    public ArrayList<Airplane> airplanes, queueingAirplanes;
    public Airplane selectedAirplane;
    public float altitudeTarget;
    private float cruiseAlt = 10000; // meters
    private float arrivalAlt = 2000; // meters
    public float altitudeChangeRate = 20f; // meters per second
    private float cruiseSpeed = 250; // meters per second
    private float arrivalSpeed = 150; // meters per second

    // Airplane spawning
    private float minAirplaneInterval, maxAirplaneInterval, timeElapsed, airplaneInterval;

    // Collisions
    private ArrayList<CollisionAnimation> collisions = new ArrayList<>();
    private float collisionRadius = toPixels(150); // pixels
    private float collisionWarningHSep = toPixels(5500); // pixels
    private float collisionWarningVSep = 305; // meters
    private float collisionWarningVSepCruise = 610; // meters

    public float warpSpeed;

    public float points;

    private Random r = new Random();

    private class CollisionAnimation {
        Airplane a;
        Airplane b;
        float time;
        float startWarp;
        public int stage;
        Vector3 origin;

        float nextShake;

        public CollisionAnimation(Airplane a, Airplane b) {
            this.a = a;
            this.b = b;
            time = 0.0f;
            stage = 0;
            startWarp = warpSpeed;
            zoomedOut = false;
            camera.zoom = maxZoomOut;
            // Temporary origin
            Vector2 o = a.state.getPosition().cpy();//.add(b.state.getPosition()).scl(0.5f);
            if(b != null) {
                o.add(b.state.getPosition()).scl(0.5f);
            }
            origin = new Vector3(o.x, o.y, 0.0f);
        }

        public void step() {
            float dt = Gdx.graphics.getDeltaTime();
            time += dt;
            followingPlane = false;
            selectedAirplane = null;
            if(stage == 1) {
                float alpha;
                warpSpeed = 0.0f;

                alpha = 1.5f * dt;
                camera.zoom = camera.zoom * (1.0f - alpha) + 0.25f * alpha;
                setCameraPosition(origin);

                if(Math.abs(camera.zoom - 0.25f) < 0.05f) {
                    Gdx.input.vibrate(2000);
                    stage = 2;
                    time = 0.0f;
                    nextShake = 0.0f;
                }
            }
            if(stage == 0) {
                float alpha;
                warpSpeed = 0.0f;

                alpha = 1.5f * dt;
                zoomCamera(camera.position, camera.zoom * (1.0f - alpha) + maxZoomOut * alpha);
                alpha *= 5.0;
                setCameraPosition(camera.position.cpy().scl(1.0f - alpha).add(origin.cpy().scl(alpha)));

                if(Math.abs(camera.zoom - maxZoomOut) < 0.05f) {
                    stage = 1;
                    time = 0.0f;
                    nextShake = 0.0f;
                }
            }
            else if(stage == 3) {
                airplanes.remove(a);
                if(b != null) {
                    airplanes.remove(b);
                    ui.setStatus(a.name + " collided with " + b.name + "!");
                }
                else {
                    ui.setStatus(a.name + ": crashed into ground!");
                }
                stage = 4;
                time = 0.0f;
            }
            else if(stage == 2) {
                warpSpeed = 1.0f;
                // screenshake
                nextShake -= dt;
                if(nextShake < 0.0f) {
                    nextShake += r.nextFloat() * 0.05;
                    setCameraPosition(
                            origin.cpy().add(
                                new Vector3(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, 0.0f)
                            .scl(10.0f)));
                }
                if(time > 2.0) {
                    stage = 3;
                }
            }
        }
    }

    public PlayScreen(ProjectIcarus game, boolean isTutorial) {
        self = this;
        points = 0;

        // If this is a tutorial
        this.isTutorial = isTutorial;
        tutorialState = TutorialState.WELCOME;
        tutorialIndex = 0;
        tutorialTimer = 0;

        zoomedOut = false;

        // Initialize the AssetManager
        AssetManager manager = game.assets;
        manager.finishLoading();

        airport = manager.get("airports/airport.json");
        labelFont = manager.get("fonts/3270Medium.ttf");
        smallLabelFont = manager.get("fonts/3270Medium_small.ttf");
        titleFont = manager.get("fonts/3270Medium_title.ttf");
        airplaneFont = manager.get("fonts/3270Medium_airplane.ttf");
        Airplane.texture = manager.get("sprites/airplane.png");

        // Create 5 other airports
        addOtherAirports(5);

        // UI
        ui = new MainUi(manager, labelFont);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;

        // Airplanes
        airplanes = new ArrayList<>();
        queueingAirplanes = new ArrayList<>();
        selectedAirplane = null;
        minAirplaneInterval = 20; // seconds
        maxAirplaneInterval = 120; // seconds
        timeElapsed = 0.0f;
        airplaneInterval = minAirplaneInterval;

        // Camera
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // The maximum zoom level is the smallest dimension compared to the viewer
        maxZoomOut = Math.min(airport.width / Gdx.graphics.getWidth(),
                airport.height / (Gdx.graphics.getHeight() - ui.statusBarHeight)
        );
        maxZoomIn = maxZoomOut / 100;

        // Start the app in maximum zoomed out state
        camera.zoom = maxZoomOut;
        camera.position.set(airport.width / 2,
                (airport.height - ui.statusBarHeight) / 2, 0
        );
        camera.update();

        Gdx.input.setInputProcessor(new InputMultiplexer(ui.stage, new GestureDetector(this)));

        warpSpeed = 1.0f;

        if(isTutorial) {
            ui.setStatus(tutorialStrings[tutorialIndex]);
        }
        else {
            ui.setStatus("Welcome to Icarus Air Traffic Control");
            addAirplane();
        }
    }

    @Override
    public void render(float delta) {
        if(points < 0) {
            points = 0;
        }

        super.render();

        // Set background color
        Gdx.gl.glClearColor(Colors.colors[0].r, Colors.colors[0].g, Colors.colors[1].b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Time elapsed this frame, taking time warp into account
        float dt = Gdx.graphics.getDeltaTime() * warpSpeed;

        if(isTutorial) {
            tutorialRender(dt);
        }

        ArrayList<Airplane> toRemove = new ArrayList<>();
        BoundingBox airportBoundary = new BoundingBox(new Vector3(0, 0, 0),
                new Vector3(airport.width, airport.height, 0)
        );
        if(queueingAirplanes.size() > 10){
            points -= 0.1f * dt;
            ui.setStatus("Too many queued airplanes!");
        }
        // Check every airplane
        for(Airplane airplane: airplanes) {
            // If the plane is stopped on the runway
            if(airplane.state.getVelocity().len() < 0.01 &&
                    airplane.stateType == Airplane.StateType.LANDING)
            {
                toRemove.add(airplane);
                ui.setStatus(airplane.name + " landed successfully!");
                points += 20;
            }
            // If the plane has exited the airport
            else if(!airportBoundary.contains(new Vector3(airplane.state.getPosition(), 0))) {
                toRemove.add(airplane);
                // If the plane was handed off to another airport
                if(airplane.getTargetType() == AirplaneFlying.TargetType.AIRPORT) {
                    ui.setStatus(airplane.name + " handed off successfully");
                    points += 5;
                }
                // If the plane wasn't handed off
                else {
                    ui.setStatus(airplane.name + " left the airport improperly!");
                    points -= 5;
                }
            }

            // Check for collisions and near misses with other planes
            for(Airplane other: airplanes) {
                if(other != airplane) {
                    Vector2 pos1 = airplane.state.getPosition();
                    float alt1 = airplane.state.getAltitude();
                    Vector2 pos2 = other.state.getPosition();
                    float alt2 = other.state.getAltitude();
                    if(pos1 != null && pos2 != null) {
                        // If two airplanes are within collision warning distance
                        if(pos1.dst(pos2) < collisionWarningHSep
                                && ((Math.abs(alt1 - alt2) < collisionWarningVSep
                                    && alt1 < cruiseAlt)
                                || (Math.abs(alt1 - alt2) < collisionWarningVSepCruise
                                    && alt1 > cruiseAlt))) {
                            ui.setStatus(airplane.name + " and " + other.name + " are too close!");
                            points -= 0.5f * dt;
                        }
                        // If two airplanes have collided
                        if(pos1.dst(pos2) < collisionRadius
                                && Math.abs(alt1 - alt2) < collisionRadius &&
                                !airplane.colliding) {
                            airplane.colliding = true;
                            other.colliding = true;
                            System.out.println("Collision!");
                            collisions.add(new CollisionAnimation(airplane, other));
                            points = 0;
                        }
                    }
                }
            }

            // Check for collisions with the ground
            if(airplane.state.getAltitude() < 1
                    && airplane.stateType != Airplane.StateType.LANDING
                    && airplane.stateType != Airplane.StateType.TAKINGOFF &&
                    !airplane.colliding) {
                airplane.colliding = true;
                collisions.add(new CollisionAnimation(airplane, null));
                points = 0;
            }
        }

        // Remove airplanes from game
        for(Airplane airplane: toRemove) {
            if(airplanes.contains(airplane)) {
                if(airplane == selectedAirplane) {
                    setSelectedAirplane(null);
                }
                airplanes.remove(airplane);
            }
        }

        // Collision animation
        ArrayList<CollisionAnimation> collisionsToRemove = new ArrayList<>();
        for(CollisionAnimation collision: collisions) {
            collision.step();
            if(collision.stage == 4) {
                collisionsToRemove.add(collision);
            }
        }
        for(CollisionAnimation collision: collisionsToRemove) {
            collisions.remove(collision);
        }

        // Draw map, if the player isn't trying to select another airport
        if(uiState != ProjectIcarus.UiState.SELECT_AIRPORT) {
            // Draw waypoint triangles
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            for(Waypoint waypoint: airport.waypoints) {
                waypoint.draw(shapes, camera);
            }

            // Draw runways
            for(Runway runway: airport.runways) {
                runway.draw(shapes, camera);
            }
            shapes.end();

            batch.begin();
            // Draw waypoint labels
            for(Waypoint waypoint: airport.waypoints) {
                if(zoomedOut) {
                    waypoint.drawLabel(smallLabelFont, batch, camera);
                }
                else {
                    waypoint.drawLabel(labelFont, batch, camera);
                }
            }

            // Draw runway labels
            for(Runway runway: airport.runways) {
                if(zoomedOut) {
                    runway.drawLabel(smallLabelFont, batch, camera);
                }
                else {
                    runway.drawLabel(labelFont, batch, camera);
                }
            }

            // Draw airplanes
            for(Airplane airplane: airplanes) {
                airplane.step(dt); // Move airplanes
                airplane.draw(airplaneFont, batch, camera);
            }

            // Draw altitude selection text
            if(uiState == ProjectIcarus.UiState.CHANGE_ALTITUDE) {
                titleFont.setColor(Colors.colors[4]);
                titleFont.draw(batch, (int) altitudeTarget + "m",
                        (float) Gdx.graphics.getWidth() / 2 - 100,
                        (float) Gdx.graphics.getHeight() / 2,
                        200,
                        Align.center, false
                );
            }
            batch.end();
        }

        // Draw the UI (buttons, status bar, etc.)
        ui.draw();

        // Follow selected airplane with camera
        if(selectedAirplane != null && followingPlane && !zoomedOut) {
            setCameraPosition(new Vector3(selectedAirplane.state.getPosition(), 0));
        }

        if(!isTutorial) {
            // Generate a new airplane after a random amount of time
            if(timeElapsed > airplaneInterval) {
                Random r = new Random();
                airplaneInterval = r.nextInt((int) (maxAirplaneInterval - minAirplaneInterval) + 1)
                        + minAirplaneInterval;
                airplaneInterval *= Math.exp(-0.008f * points);
                timeElapsed = 0.0f;
                addAirplane();
            }
            else {
                timeElapsed += dt;
            }
        }
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        labelFont.dispose();
        airplaneFont.dispose();
        titleFont.dispose();
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        Vector3 position = new Vector3(x, Gdx.graphics.getHeight() - y, 0);
        switch (uiState) {
            case SELECT_AIRPLANE:
                setSelectedAirplane(null);
                for(Airplane airplane: airplanes) {
                    if(airplane.sprite.getBoundingRectangle().contains(position.x, position.y)) {
                        if(zoomedOut) {
                            zoomedOut = false;
                            camera.zoom = maxZoomOut;
                            camera.update();
                        }
                        setSelectedAirplane(airplane);
                        ui.setStatus("Selected " + getSelectedAirplane().name);
                        return true;
                    }
                }
                break;
            case SELECT_WAYPOINT:
                for(Waypoint waypoint: airport.waypoints) {
                    Vector3 pos = camera.project(new Vector3(waypoint.position, 0));
                    Circle circle = new Circle(pos.x, pos.y, 4 * Waypoint.waypointSize);
                    if(circle.contains(position.x, position.y)) {
                        selectedAirplane.setTargetWaypoint(waypoint);
                        ui.setStatus(selectedAirplane.name + ": targeting waypoint " + waypoint.name);
                        uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                        followingPlane = true;
                        return true;
                    }
                }
                break;
            case SELECT_HEADING:
                uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                ui.toggleHeadingSelector(false);
                break;
            case SELECT_RUNWAY:
                for(Runway runway: airport.runways) {
                    for(int end = 0; end < 2; end++) {
                        Vector3 pos = camera.project(new Vector3(runway.points[end], 0));
                        Vector2 pos2 = new Vector2(
                                pos.x + runway.nameOffsets[end].x * Gdx.graphics.getDensity(),
                                pos.y - runway.nameOffsets[end].y * Gdx.graphics.getDensity());
                        Circle circle = new Circle(pos2.x, pos2.y, 20 * Gdx.graphics.getDensity());
                        if(circle.contains(position.x, position.y)) {
                            if(selectedAirplane.flightType == DEPARTURE) {
                                takeOff(runway, end);
                            }
                            else {
                                land(runway, end);
                            }
                            break;
                        }
                    }
                }
                break;
            case CHANGE_ALTITUDE:
                uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                ui.setStatus("Set target altitude to: " + (int) altitudeTarget + "m");
                ((AirplaneFlying) selectedAirplane.state).targetAltitude = altitudeTarget;
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean longPress(float x, float y) {
        toggleOverview(!zoomedOut);
        Gdx.input.vibrate(20);
        return true;
    }

    public void toggleOverview(boolean show) {
        if(show) {
            zoomedOut = true;
            camera.zoom = airport.height / (Gdx.graphics.getHeight() - ui.statusBarHeight);
            camera.position.set(airport.width / 2,
                    (airport.height - ui.statusBarHeight) / 2, 0
            );
        }
        else {
            zoomedOut = false;
            camera.zoom = maxZoomOut;
        }
        camera.update();
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if(!zoomedOut) {
            if(uiState == ProjectIcarus.UiState.CHANGE_ALTITUDE) {
                altitudeTarget -= deltaY * 3;
                if(altitudeTarget < 0f) {
                    altitudeTarget = 0f;
                }
            }
            else {
                followingPlane = false;
                setCameraPosition(camera.position.add(
                        camera.unproject(new Vector3(0, 0, 0))
                                .add(camera.unproject(new Vector3(deltaX, deltaY, 0)).scl(-1f))
                ));
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1,
                         Vector2 pointer2)
    {
        if(!zoomedOut) {
            if (!(initialPointer1.equals(oldInitialFirstPointer)
                    && initialPointer2.equals(oldInitialSecondPointer)))
            {
                oldInitialFirstPointer = initialPointer1.cpy();
                oldInitialSecondPointer = initialPointer2.cpy();
                oldScale = camera.zoom;
            }
            Vector3 center = new Vector3(
                    (pointer1.x + initialPointer2.x) / 2,
                    (pointer2.y + initialPointer1.y) / 2,
                    0
            );
            zoomCamera(center,
                    oldScale * initialPointer1.dst(initialPointer2) / pointer1.dst(pointer2));
            return true;
        }
        else {
            return false;
        }
    }

    private void zoomCamera(Vector3 origin, float scale) {
        if(followingPlane && !zoomedOut) {
            camera.zoom = scale;
            camera.zoom = Math.min(maxZoomOut, Math.max(camera.zoom, maxZoomIn));
        }
        else {
            Vector3 oldUnprojection = camera.unproject(origin.cpy()).cpy();
            camera.zoom = scale; // Larger value of zoom = small images, border view
            camera.zoom = Math.min(maxZoomOut, Math.max(camera.zoom, maxZoomIn));
            camera.update();
            Vector3 newUnprojection = camera.unproject(origin.cpy()).cpy();
            camera.position.add(oldUnprojection.cpy().add(newUnprojection.cpy().scl(-1f)));
        }

        setToBoundary(); // Calculate distances to boundaries

        // Shift the view when zooming to keep view within map
        if (toBoundaryRight < 0 || toBoundaryTop < 0){
            camera.position.add(
                    camera.unproject(new Vector3(0, 0, 0))
                            .add(camera.unproject(new Vector3(Math.max(0, -toBoundaryRight),
                                    Math.min(0, toBoundaryTop),
                                    0)).scl(-1f))
            );
        }
        if (toBoundaryLeft > 0 || toBoundaryBottom > 0){
            camera.position.add(
                    camera.unproject(new Vector3(0, 0, 0))
                            .add(camera.unproject(new Vector3(Math.min(0, -toBoundaryLeft),
                                    Math.max(0, toBoundaryBottom),
                                    0)).scl(-1f))
            );
        }

        camera.update();
    }

    private void setToBoundary() {
        // Calculates the distance from the edge of the camera to the specified boundary
        toBoundaryRight = airport.width
                - camera.position.x - Gdx.graphics.getWidth()/2 * camera.zoom;
        toBoundaryLeft = -camera.position.x
                + (Gdx.graphics.getWidth()/2) * camera.zoom;
        toBoundaryTop = airport.height
                - camera.position.y - Gdx.graphics.getHeight()/2 * camera.zoom;
        toBoundaryBottom = -camera.position.y
                + (Gdx.graphics.getHeight()/2 - ui.statusBarHeight) * camera.zoom;
    }

    private void setCameraPosition(Vector3 position) {
        camera.position.set(position);

        Vector2 camMin = new Vector2(camera.viewportWidth,
                camera.viewportHeight - 2 * ui.statusBarHeight
        );
        camMin.scl(camera.zoom / 2);
        Vector2 camMax = new Vector2(airport.width,
                airport.height - camera.zoom * ui.statusBarHeight
        );
        camMax.sub(camMin);

        camera.position.x = Math.min(camMax.x, Math.max(camera.position.x, camMin.x));
        camera.position.y = Math.min(camMax.y, Math.max(camera.position.y, camMin.y));

        camera.update();
    }

    public static void setupAssetManager(AssetManager manager) {
        float fontSize = 20.0f * Gdx.graphics.getDensity();

        FreetypeFontLoader.FreeTypeFontLoaderParameter labelFontParams;
        FreetypeFontLoader.FreeTypeFontLoaderParameter titleFontParams;
        FreetypeFontLoader.FreeTypeFontLoaderParameter airplaneFontParams;
        FreetypeFontLoader.FreeTypeFontLoaderParameter smallLabelFontParams;

        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(Airport.class, new AirportLoader(resolver));
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        String airportFile = "airports/airport.json";

        // Load the airport
        manager.load(airportFile, Airport.class);

        // Load the label font
        labelFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        labelFontParams.fontFileName = "fonts/3270Medium.ttf";
        labelFontParams.fontParameters.size = Math.round(fontSize);
        manager.load("fonts/3270Medium.ttf", BitmapFont.class, labelFontParams);

        smallLabelFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        smallLabelFontParams.fontFileName = "fonts/3270Medium.ttf";
        smallLabelFontParams.fontParameters.size = Math.round(fontSize * 0.7f);
        manager.load("fonts/3270Medium_small.ttf", BitmapFont.class, smallLabelFontParams);

        titleFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        titleFontParams.fontFileName = "fonts/3270Medium.ttf";
        titleFontParams.fontParameters.size = Math.round(fontSize * 4);
        titleFontParams.fontFileName = "fonts/3270Medium.ttf";
        manager.load("fonts/3270Medium_title.ttf", BitmapFont.class, titleFontParams);

        airplaneFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        airplaneFontParams.fontFileName = "fonts/3270Medium.ttf";
        airplaneFontParams.fontParameters.size = Math.round(fontSize * 0.8f);
        airplaneFontParams.fontFileName = "fonts/3270Medium.ttf";
        manager.load("fonts/3270Medium_airplane.ttf", BitmapFont.class, airplaneFontParams);

        // Load the airplane sprite
        manager.load("sprites/airplane.png", Texture.class);

        manager.load("buttons/altitude_button.png", Texture.class);
        manager.load("buttons/waypoint_button.png", Texture.class);
        manager.load("buttons/heading_button.png", Texture.class);
        manager.load("buttons/takeoff_button.png", Texture.class);
        manager.load("buttons/landing_button.png", Texture.class);
        manager.load("buttons/handoff_button.png", Texture.class);
        manager.load("buttons/cancel_button.png", Texture.class);
        manager.load("buttons/selection_wheel.png", Texture.class);
        manager.load("buttons/warpup.png", Texture.class);
        manager.load("buttons/warpdown.png", Texture.class);
        manager.load("buttons/pause_button.png", Texture.class);
        manager.load("buttons/play_button_pause.png", Texture.class);
        manager.load("buttons/airport.png", Texture.class);
    }

    public void setSelectedAirplane(Airplane selectedAirplane){
        // deselect old selectedAirplane if not null
        if(this.selectedAirplane != null){
            this.selectedAirplane.setSelected(false);
        }
        this.selectedAirplane = selectedAirplane;
        // select new selectedAirplane if not null
        if(selectedAirplane != null){
            if(selectedAirplane.stateType != Airplane.StateType.QUEUEING) {
                followingPlane = true;
            }
            selectedAirplane.setSelected(true);
        }
        else {
            followingPlane = false;
        }
    }

    private void addAirplane() {
        // Randomly choose between ARRIVAL, FLYOVER, and DEPARTURE
        // Also determine altitude and speed based on flight type
        float altitude;
        Airplane.FlightType flightType;
        float speed;
        int randFlightType = r.nextInt(10);
        if(randFlightType < 2) {
            flightType = Airplane.FlightType.FLYOVER;
            altitude = cruiseAlt; // Meters
            speed = toPixels(cruiseSpeed);
        }
        else if(randFlightType < 6) {
            flightType = Airplane.FlightType.ARRIVAL;
            altitude = arrivalAlt;
            speed = toPixels(arrivalSpeed);
        }
        else {
            flightType = Airplane.FlightType.DEPARTURE;
            altitude = 0;
            speed = 0.01f;
        }

        // Generate a random flight name
        int flightNum = r.nextInt(9999) + 1;
        String flightName = "";
        for (int i = 0; i < 3; i++) {
            char c = (char) (r.nextInt(26) + 'A');
            flightName += c;
        }
        flightName += flightNum;

        Vector2 position;
        Vector2 velocity;

        if (flightType == FLYOVER || flightType == ARRIVAL) {
            // Generate a random heading
            int heading = r.nextInt(359);
            float theta = (float) (heading * Math.PI / 180);
            velocity = new Vector2(1, 0).setLength(speed).rotate(heading);

            // Calculate vectors from the center to the corners
            Vector2 center = new Vector2(airport.width / 2, airport.height / 2);
            Vector2 upperRight = new Vector2(airport.width, airport.height).sub(center);
            Vector2 upperLeft = new Vector2(0, airport.height).sub(center);
            Vector2 lowerLeft = new Vector2(0, 0).sub(center);
            Vector2 lowerRight = new Vector2(airport.width, 0).sub(center);

            // Generate a random position along the edge of the map
            position = new Vector2();
            if (heading < upperRight.angle() || heading > lowerRight.angle()) {
                position.add(0, (float) (airport.height / 2 - airport.width / 2 * Math.tan(theta)));
            }
            else if (heading < upperLeft.angle()) {
                position.add((float) (airport.width / 2 - (airport.height / 2) / Math.tan(theta)), 0);
            }
            else if (heading < lowerLeft.angle()) {
                position.add(airport.width,
                        (float) ((airport.width / 2) * Math.tan(theta) + airport.height / 2)
                );
            }
            else {
                position.add((float) ((airport.height / 2) / Math.tan(theta) + airport.width / 2),
                        airport.height
                );
            }

            // Add a new airplane
            airplanes.add(new Airplane(flightName, flightType, position, velocity, altitude));
        }
        else {
            ui.setStatus(flightName + ": added to queue");

            // Add a new airplane
            queueingAirplanes.add(new Airplane(flightName, flightType, null, null, 0f));

        }
    }

    private void takeOff(Runway runway, int end) {
        float speed = 0.01f;
        Vector2 position = runway.points[end].cpy();
        Vector2 heading = runway.points[1-end].cpy().sub(position).nor();
        Vector2 velocity = heading.scl(speed);

        uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
        followingPlane = true;
        ui.setStatus(queueingAirplanes.get(0).name + ": taking off");

        queueingAirplanes.get(0).transitionToTakingOff(position, velocity);
        airplanes.add(queueingAirplanes.get(0));
        queueingAirplanes.remove(0);
    }

    private void land(Runway runway, int end) {
        if(canLand(runway, end)) {
            // If the airplane is in the correct place
            selectedAirplane.setTargetRunway(runway, end);
            followingPlane = true;
            ui.setStatus("Selected runway " + runway.names[end]);
            Gdx.app.log(TAG, "Selected runway " + runway.names[end]);
        }
        else {
            ui.setStatus(selectedAirplane.name
                    + " cannot land at runway "
                    + runway.names[end]
            );
        }
        uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
    }

    private boolean canLand(Runway runway, int end) {
        // Landing constraints
        float headingVariance = 20; // Maximum heading deviation from runway
        float positionVariance = 20; // Maximum position deviation from runway
        Vector2 targetRunway = runway.points[1-end].cpy()
                .sub(runway.points[end]);
        // Calculate distance
        float distance = Math.abs(selectedAirplane.state.getPosition().cpy()
                .sub(runway.points[end]).len()
        );
        float time = distance / selectedAirplane.state.getVelocity().len();
        float descentRate = selectedAirplane.state.getAltitude() / time; // meters per second
        // Calculate difference between airplane heading and runway heading
        float angleDifference = selectedAirplane.state.getVelocity()
                .angle(targetRunway);
        // Calculate radial distance of airplane from runway
        // with respect to the runway's heading
        Vector2 relativePosition = runway.points[end].cpy()
                .sub(selectedAirplane.state.getPosition());
        float positionDifference = relativePosition.angle(targetRunway);
        return (descentRate < altitudeChangeRate
                && Math.abs(angleDifference) < headingVariance
                && Math.abs(positionDifference) < positionVariance);
    }

    private void addOtherAirports(int airports) {
        int airportMinDistance = 50000;
        int airportMaxDistance = 100000;
        otherAirports = new ArrayList<>();
        int minHeading = 0;
        int minDifference = 20;
        int sector = 359 / airports;
        farthestAirportDistance = airportMinDistance;
        for(int i = 0; i < airports; i++) {
            // Generate random three-letter airport name
            String name = "";
            for (int n = 0; n < 3; n++) {
                char c = (char) (r.nextInt(26) + 'A');
                name += c;
            }
            // Generate semi-random position
            int relativeHeading = r.nextInt(sector - minDifference) + minHeading;
            int distance = r.nextInt(airportMaxDistance - airportMinDistance) + airportMinDistance;
            Gdx.app.log(TAG, "Airport distance = " + distance);
            otherAirports.add(new OtherAirport(
                    name,
                    new Vector2(1, 0).setAngle(relativeHeading).setLength(distance))
            );
            minHeading += sector;
            farthestAirportDistance = Math.max(farthestAirportDistance, distance);
        }
    }

    public void tutorialRender(float dt) {
        switch(tutorialState) {
            case WELCOME:
                if(tutorialTimer < 5) {
                    ui.setStatus("Welcome to the Icarus ATC Tutorial.");
                }
                else if(tutorialTimer < 10) {
                    ui.setStatus("This is the map screen. You can pan and zoom.");
                }
                else {
                    if(!zoomedOut) {
                        ui.setStatus("Press and hold to see an overview.");
                    }
                    else {
                        ui.setStatus("This is an arrival. It needs to land at a runway.");
                        airplanes.add(
                                new Airplane(
                                        "TUT0001",
                                        Airplane.FlightType.ARRIVAL,
                                        new Vector2(0, 80),
                                        new Vector2(toPixels(arrivalSpeed), 0),
                                        arrivalSpeed
                                )
                        );
                        tutorialState = TutorialState.ARRIVAL;
                        tutorialTimer = 0;
                        tutorialIndex = 0;
                    }

                }
//                if(tutorialIndex > 2) {
//                    tutorialState = TutorialState.ARRIVAL;
//                }
                break;
            case ARRIVAL:
                if(tutorialIndex == 0) {
                    if(tutorialTimer < tutorialMessagePause) {
                        ui.setStatus("This is an arrival. It needs to land at a runway.");
                    }
                    else if(selectedAirplane == null) {
                        ui.setStatus("Tap the airplane to see commands.");
                    }
                    else if(uiState == ProjectIcarus.UiState.SELECT_AIRPLANE) {
                        ui.setStatus("Press the top button on the left.");
                    }
                    else {
                        tutorialIndex = 1;
                        tutorialTimer = 0;
                    }
                }
                else if(tutorialIndex == 1) {
                    if(uiState == ProjectIcarus.UiState.SELECT_HEADING){
                        ui.setStatus("Drag your finger around the circle to 90 degrees.");
                    }
                    else {
                        tutorialIndex = 2;
                        tutorialTimer = 0;
                    }
                }
                else if(tutorialIndex == 2) {
                    float ewokPos = 0;
                    for(Waypoint waypoint: airport.waypoints) {
                        if(waypoint.name.equals("EWOK")) {
                            ewokPos = camera.project(new Vector3(waypoint.position.x,
                                    waypoint.position.y, 0
                            )).x;
                        }
                    }
                    if(tutorialTimer < tutorialMessagePause) {
                        ui.setStatus("Wait until the plane passes EWOK.");
                    }
                    else if(selectedAirplane.state.getPosition().x < ewokPos) {
                        ui.setStatus("Use the buttons on the right to speed up time.");
                        Gdx.app.log(TAG, selectedAirplane.state.getPosition().x + ", " + ewokPos);
                    }
                    else {
                        ui.setStatus("Now, press the second button on the left.");
                        tutorialIndex = 2;
                    }
                }
                else if(tutorialIndex == 3) {
                    float flcnPos = 0;
                    for(Waypoint waypoint: airport.waypoints) {
                        if(waypoint.name.equals("EWOK")) {
                            flcnPos = camera.project(new Vector3(waypoint.position.x,
                                    waypoint.position.y, 0
                            )).x;
                        }
                    }
                    if(uiState == ProjectIcarus.UiState.SELECT_WAYPOINT) {
                        ui.setStatus("Tap waypoint FLCN.");
                    }
                    else if(selectedAirplane.getTargetType() == AirplaneFlying.TargetType.WAYPOINT
                            && selectedAirplane.state.getAltitude() >= arrivalAlt
                            && uiState == ProjectIcarus.UiState.SELECT_AIRPLANE) {
                        ui.setStatus("Press the third button on the left.");
                    }
                    else if(uiState == ProjectIcarus.UiState.CHANGE_ALTITUDE) {
                        ui.setStatus("Drag down to 1000m. Tap when finished.");
                    }
                    else if(selectedAirplane.state.getPosition().x < flcnPos) {
                        ui.setStatus("Wait until it nears FLCN.");
                    }
                    else {
                        tutorialIndex = 3;
                    }
                }
                else if(tutorialIndex == 4) {
                    if(uiState == ProjectIcarus.UiState.SELECT_AIRPLANE) {
                        ui.setStatus("Now target waypoint EMPR.");
                    }
                    else {
                        tutorialIndex = 4;
                    }
                }
                else if(tutorialIndex == 5) {
                    if(!canLand(airport.runways[0], 0)) {
                        ui.setStatus("Wait for the airplane to line up.");
                    }
                    else if(uiState == ProjectIcarus.UiState.SELECT_AIRPLANE) {
                        ui.setStatus("Press the bottom button on the left.");
                    }
                    else {
                        ui.setStatus("Tap runway 14.");
                        tutorialIndex = 5;
                    }
                }
                else if(tutorialIndex == 6) {
                    if(selectedAirplane.stateType == Airplane.StateType.FLYING) {
                        ui.setStatus("Good job! The plane will land by itself.");
                    }
                    else {
                        tutorialState = TutorialState.FLYOVER;
                        tutorialTimer = 0;
                    }
                }
                break;
            case FLYOVER:
                break;
            case DEPARTURE:
                break;
            default:
                break;
        }
        tutorialTimer += Gdx.graphics.getDeltaTime();
//        ui.setStatus(tutorialStrings[tutorialIndex]);
//        if(tutorialTimer > tutorialMessagePause && tutorialIndex < tutorialStrings.length - 1) {
//            tutorialIndex++;
//            tutorialTimer = 0;
//        }
    }

    public static String tutorialStrings[] = new String[]{
//            "Welcome to the Icarus ATC Tutorial.",
//            "This is the map screen. You can pan and zoom.",
//            // Wait few seconds
//            "Press and hold to see an overview.",
//            // Wait until they do that
//            // Arrival appears on screen
//            "This is an arrival. It needs to land at a runway.",
//            // Wait a few seconds
//            "Tap the airplane to see commands.",
//            "Press the top button on the left.",
//            "Drag your finger around the circle to 90 degrees.",
//            "Wait until the plane passes EWOK.",
//            "Use the buttons on the right to speed up time.",
//            "Now, press the second button on the left.",
//            "Tap waypoint FLCN.",
//            "Press the third button on the left.",
//            "Drag down to 1000m. Tap when finished.",
//            "Now target waypoint EMPR.",
            "Press the bottom button on the left.",
            "Tap runway 14.",
            "Good job! The plane will land by itself.",
            // Warp up here, then warp down after the landing
            "Press the button on the right.",
            "Tap the plane TUT0002",
            "This is a flyover.",
            "Press the bottom button on the left.",
            "Tap an airport to handoff the plane.",
            "Good job! You're all done with this one.",
            "Press the button that just appeared.",
            "Select any runway.",
            "This is a departure. It also must be handed off.",
            "But first, wait for it to take off.",
            // Departure takes off
            "Select the airplane.",
            "Press the handoff button.",
            "Again, tap any airport.",
            "Good job! You're ready to play!"
    };

    public enum TutorialState {
        WELCOME,
        ARRIVAL,
        FLYOVER,
        DEPARTURE
    }

    public static float toMeters(float pixels) {
        return 50 * pixels;
    }

    public static float toPixels(float meters) {
        return meters / 50;
    }

    public Airplane getSelectedAirplane(){
        return this.selectedAirplane;
    }

    public static PlayScreen getInstance() {
        return self;
    }

    @Override
    public void pinchStop() {}

    @Override
    public void hide() {}

    @Override
    public void create() {}

    @Override
    public void show() {}

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }
}

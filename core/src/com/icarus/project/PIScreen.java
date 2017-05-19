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
import java.util.Random;

import static com.icarus.project.Airplane.FlightType.ARRIVAL;
import static com.icarus.project.Airplane.FlightType.DEPARTURE;
import static com.icarus.project.Airplane.FlightType.FLYOVER;

public class PIScreen extends Game implements Screen, GestureDetector.GestureListener {
    // This class
    public static PIScreen self;
    public static final String TAG = "PIState";

    // The currently loaded Airport
    private Airport airport;

    // Other airports
    public int farthestAirportDistance;
    public ArrayList<OtherAirport> otherAirports;

    // UI
    public MainUi ui;
    public ProjectIcarus.UiState uiState;
    public ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont labelFont, titleFont, airplaneFont;

    // Camera
    private OrthographicCamera camera;
    private float maxZoomIn; // Maximum possible zoomed in distance
    private float maxZoomOut; // Maximum possible zoomed out distance
    public boolean followingPlane;
    private Vector2 oldInitialFirstPointer=null, oldInitialSecondPointer=null;
    private float oldScale;

    // Pan boundaries
    private float toBoundaryRight;
    private float toBoundaryLeft;
    private float toBoundaryTop;
    private float toBoundaryBottom;

    // Airplanes
    private ArrayList<Airplane> airplanes;
    public ArrayList<Airplane> queueingAirplanes;
    public Airplane selectedAirplane;
    public float altitudeTarget;
    private float cruiseAlt = 10000; // meters
    public float altitudeChangeRate = 14f; // meters per second

    // Airplane spawning
    private float minAirplaneInterval;
    private float maxAirplaneInterval;
    private float timeElapsed;
    private float airplaneInterval;

    // Collisions
    private ArrayList<CollisionAnimation> collisions = new ArrayList<>();
    private float collisionRadius = toPixels(150); // pixels
    private float collisionWarningHSep = toPixels(5500); // pixels
    private float collisionWarningVSep = toPixels(305); // pixels
    private float collisionWarningVSepCruise = toPixels(610); // pixels

    public float warpSpeed;

    public int points;

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
            camera.zoom = maxZoomOut;
            // Temporary origin
            Vector2 o = a.state.getPosition().cpy().add(b.state.getPosition()).scl(0.5f);
            origin = new Vector3(o.x, o.y, 0.0f);
        }

        public void step() {
            float dt = Gdx.graphics.getDeltaTime();
            time += dt;
            followingPlane = false;
            selectedAirplane = null;
            if(stage == 0) {
                float alpha;
                warpSpeed = 0.0f;

                alpha = 1.5f * dt;
                camera.zoom = camera.zoom * (1.0f - alpha) + 0.25f * alpha;
                setCameraPosition(origin);

                if(Math.abs(camera.zoom - 0.25f) < 0.05f) {
                    Gdx.input.vibrate(2000);
                    stage = 1;
                    time = 0.0f;
                    nextShake = 0.0f;
                }
            }
            else if(stage == 2) {
                airplanes.remove(a);
                airplanes.remove(b);
                ui.setStatus(a.name + " collided with " + b.name + "!");
                stage = 3;
                time = 0.0f;
            }
            else if(stage == 1) {
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
                    stage = 2;
                }
            }
        }
    }

    public PIScreen(ProjectIcarus game) {
        self = this;
        points = 0;

        // Initialize the AssetManager
        AssetManager manager = game.assets;
        manager.finishLoading();

        airport = manager.get("airports/airport.json");
        labelFont = manager.get("fonts/3270Medium.ttf");
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
        addAirplane();

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
    }

    @Override
    public void render(float delta) {
        super.render();

        // Set background color
        Gdx.gl.glClearColor(Colors.colors[0].r, Colors.colors[0].g, Colors.colors[2].b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Time elapsed this frame, taking time warp into account
        float dt = Gdx.graphics.getDeltaTime() * warpSpeed;

        ArrayList<Airplane> toRemove = new ArrayList<>();
        BoundingBox airportBoundary = new BoundingBox(new Vector3(0, 0, 0),
                new Vector3(airport.width, airport.height, 0)
        );
        // Check every airplane
        for(Airplane airplane: airplanes) {
            // If the plane is stopped on the runway
            if(airplane.state.getVelocity().len() < 0.01 &&
                    airplane.stateType == Airplane.StateType.LANDING)
            {
                toRemove.add(airplane);
                ui.setStatus(airplane.name + " landed successfully!");
                points += 50;
            }
            // If the plane has exited the airport
            else if(!airportBoundary.contains(new Vector3(airplane.state.getPosition(), 0))) {
                toRemove.add(airplane);
                // If the plane was handed off to another airport
                if(airplane.getTargetType() == AirplaneFlying.TargetType.AIRPORT) {
                    ui.setStatus(airplane.name + " handed off successfully");
                    points += 10;
                }
                // If the plane wasn't handed off
                else {
                    ui.setStatus(airplane.name + " left the airport improperly!");
                    points -= 10;
                }
            }

            // Check for collisions and near misses
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
                            Gdx.app.log(TAG, airplane.name + " and " + other.name + " are too close!");
                            points -= 1f * dt;
                        }
                        // If two airplanes have collided
                        if(pos1.dst(pos2) < collisionRadius
                                && Math.abs(alt1 - alt2) < collisionRadius &&
                                !airplane.colliding) {
                            airplane.colliding = true;
                            other.colliding = true;
                            collisions.add(new CollisionAnimation(airplane, other));
                            points = 0;
                        }
                    }
                }
            }
        }

        // Remove airplanes from game
        for(Airplane airplane: toRemove) {
            if(airplanes.contains(airplane)) {
                airplanes.remove(airplane);
            }
        }

        // Collision animation
        ArrayList<CollisionAnimation> collisionsToRemove = new ArrayList<>();
        for(CollisionAnimation collision: collisions) {
            collision.step();
            if(collision.stage == 3) {
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

            // Draw waypoint labels
            batch.begin();
            for(Waypoint waypoint: airport.waypoints) {
                waypoint.drawLabel(labelFont, batch, camera);
            }

            // Draw runway labels
            for(Runway runway: airport.runways) {
                runway.drawLabel(labelFont, batch, camera);
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
        if(selectedAirplane != null && followingPlane) {
            setCameraPosition(new Vector3(selectedAirplane.state.getPosition(), 0));
        }

        // Generate a new airplane after a random amount of time
        if(timeElapsed > airplaneInterval) {
            Random r = new Random();
            airplaneInterval = r.nextInt((int) (maxAirplaneInterval - minAirplaneInterval) + 1)
                    + minAirplaneInterval;
            timeElapsed = 0.0f;
            addAirplane();
        }
        else {
            timeElapsed += dt;
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
    public boolean pan(float x, float y, float deltaX, float deltaY) {
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

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1,
                         Vector2 pointer2)
    {
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

    private void zoomCamera(Vector3 origin, float scale) {
        if(followingPlane) {
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

    public void addAirplane() {
        // Randomly choose between ARRIVAL, FLYOVER, and DEPARTURE
        // Also determine altitude and speed based on flight type
        float altitude;
        Airplane.FlightType flightType;
        float speed;
        int randFlightType = r.nextInt(10);
        if(randFlightType < 2) {
            flightType = Airplane.FlightType.FLYOVER;
            altitude = cruiseAlt; // Meters
            speed = toPixels(250);
        }
        else if(randFlightType < 6) {
            flightType = Airplane.FlightType.ARRIVAL;
            altitude = 2000;
            speed = toPixels(150);
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

    public void takeOff(Runway runway, int end) {
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

    public void land(Runway runway, int end) {
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
        if(descentRate < altitudeChangeRate
                && Math.abs(angleDifference) < headingVariance
                && Math.abs(positionDifference) < positionVariance) {
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

    public static float toMeters(float pixels) {
        return 50 * pixels;
    }

    public static float toPixels(float meters) {
        return meters / 50;
    }

    public Airplane getSelectedAirplane(){
        return this.selectedAirplane;
    }

    public static PIScreen getInstance() {
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
    public boolean longPress(float x, float y) {
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

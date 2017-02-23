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
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;

public class PIScreen extends Game implements GestureDetector.GestureListener, Screen {
    private Game game;
    private Vector2 oldInitialFirstPointer=null, oldInitialSecondPointer=null;
    private float oldScale;
    //Used for drawing waypoints
    public ShapeRenderer shapes;
    //The currently loaded Airport
    private Airport airport;
    //The airplanes in the current game
    private ArrayList<Airplane> airplanes;
    //The font used for labels
    private BitmapFont labelFont;
    //Used for drawing airplanes
    private SpriteBatch batch;
    private Utils utils;

    public MainUi ui;

    private OrthographicCamera camera;
    private float maxZoomIn; // Maximum possible zoomed in distance
    private float maxZoomOut; // Maximum possible zoomed out distance

    private float fontSize;

    // Pan boundaries
    private float toBoundaryRight;
    private float toBoundaryLeft;
    private float toBoundaryTop;
    private float toBoundaryBottom;

    public boolean followingPlane;

    private Airplane selectedAirplane;

    public static final String TAG = "PIState";

    public static PIScreen self;

    public ProjectIcarus.UiState uiState;

    public FreetypeFontLoader.FreeTypeFontLoaderParameter labelFontParams;

    public PIScreen(Game game) {
        this.game = game;
        self = this;
        fontSize = 20.0f * Gdx.graphics.getDensity();
        //initialize the AssetManager
        AssetManager manager = new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(Airport.class, new AirportLoader(resolver));
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        //load the airport
        manager.load("airports/test.json", Airport.class);
        //load the label font
        labelFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        labelFontParams.fontFileName = "fonts/3270Medium.ttf";
        labelFontParams.fontParameters.size = Math.round(fontSize);
        manager.load("fonts/3270Medium.ttf", BitmapFont.class, labelFontParams);
        //load the airplane sprite
        manager.load("sprites/airplane.png", Texture.class);

        manager.load("buttons/altitude_button.png", Texture.class);
        manager.load("buttons/heading_button.png", Texture.class);
        manager.load("buttons/takeoff_button.png", Texture.class);
        manager.load("buttons/circle_button.png", Texture.class);
        manager.load("buttons/landing_button.png", Texture.class);
        manager.load("buttons/more_button.png", Texture.class);
        manager.load("buttons/selection_wheel.png", Texture.class);

        manager.finishLoading();
        airport = manager.get("airports/test.json");
        labelFont = manager.get("fonts/3270Medium.ttf");
        Airplane.texture = manager.get("sprites/airplane.png");

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();

        //add test airplanes
        airplanes = new ArrayList<Airplane>();
        airplanes.add(new Airplane("airplane1", new Vector2(100, 100), new Vector2(4, 0), 10000));
        airplanes.add(new Airplane("airplane2", new Vector2(100, 500), new Vector2(4, 0), 10000));
        airplanes.add(new Airplane("airplane3", new Vector2(500, 100), new Vector2(4, 0), 10000));
        airplanes.add(new Airplane("airplane4", new Vector2(500, 500), new Vector2(4, 0), 10000));

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        utils = new Utils();
        // The maximum zoom level is the smallest dimension compared to the viewer
        maxZoomOut = Math.min(airport.width / Gdx.graphics.getWidth(),
                airport.height / Gdx.graphics.getHeight());
        maxZoomIn = maxZoomOut / 100;

        // Start the app in maximum zoomed out state
        camera.zoom = maxZoomOut;
        camera.position.set(airport.width/2, airport.height/2, 0);
        camera.update();

        ui = new MainUi(manager, labelFont);

        selectedAirplane = null;
        uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;

        Gdx.input.setInputProcessor(new InputMultiplexer(ui.stage, new GestureDetector(this)));
    }

    @Override
    public void render(float delta) {
        super.render();
        Gdx.gl.glClearColor(Colors.colors[0].r, Colors.colors[0].g, Colors.colors[2].b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //draw waypoint triangles
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for(Waypoint waypoint: airport.waypoints) {
            waypoint.draw(shapes, camera);
        }
        shapes.end();

        //draw runways
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for(Runway runway: airport.runways) {
            runway.draw(shapes, camera);
        }
        shapes.end();

        //draw waypoint labels
        batch.begin();
        for(Waypoint waypoint: airport.waypoints) {
            waypoint.drawLabel(labelFont, batch, camera);
        }
        batch.end();

        //draw runway labels
        batch.begin();
        for(Runway runway: airport.runways) {
            runway.drawLabel(labelFont, batch, camera);
        }
        batch.end();

        //draw airplanes
        batch.begin();
        for(Airplane airplane: airplanes) {
            airplane.step(); //Move airplanes
            airplane.draw(labelFont, batch, camera);
        }
        batch.end();

        ui.draw();

        // follow selected airplane
        if(selectedAirplane != null && followingPlane) {
            setCameraPosition(new Vector3(selectedAirplane.position, 0));
        }
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        labelFont.dispose();
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
                        ui.setStatus("selected " + getSelectedAirplane().name);
                        return true;
                    }
                }
                break;
            case SELECT_WAYPOINT:
                for(Waypoint waypoint: airport.waypoints) {
                    Vector3 pos = camera.project(new Vector3(waypoint.position, 0));
                    Circle circle = new Circle(pos.x, pos.y, Waypoint.waypointSize);
                    if(circle.contains(position.x, position.y)) {
                        selectedAirplane.setTargetWaypoint(waypoint);
                        ui.setStatus("Selected waypoint " + waypoint.name);
                        uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                        followingPlane = true;
                        return true;
                    }
                }
                break;
            case SELECT_HEADING:
                uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                ui.showHeadingSelector(false);
                break;
            case SELECT_RUNWAY:
                for(Runway runway: airport.runways) {
                    Vector3 pos1 = camera.project(new Vector3(runway.points[0], 0));
                    Circle circle1 = new Circle(pos1.x, pos1.y, 20 * Gdx.graphics.getDensity());
                    Vector3 pos2 = camera.project(new Vector3(runway.points[1], 0));
                    Circle circle2 = new Circle(pos2.x, pos2.y, 20 * Gdx.graphics.getDensity());
                    if (circle1.contains(position.x, position.y)) {
                        Vector2 runwayHeading = runway.points[0].cpy().sub(runway.points[1]);
                        if (Math.abs(selectedAirplane.position.cpy().sub(runway.points[0]).angle(runwayHeading)) < 10){
                            ui.setStatus("Within path " + selectedAirplane.position.cpy().sub(runway.points[0]).angle(runwayHeading));
                        }
                        else {
                            ui.setStatus("Outside path " + selectedAirplane.position.cpy().sub(runway.points[0]).angle(runwayHeading));
                        }
//                        ui.setStatus("selected runway end 1 " + runway.names[0]);
                        uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                        followingPlane = true;
                        break;
                    }
                    else if (circle2.contains(position.x, position.y)) {
                        Vector2 runwayHeading = runway.points[1].cpy().sub(runway.points[0]);
                        if (Math.abs(selectedAirplane.position.cpy().sub(runway.points[1]).angle(runwayHeading)) < 10){
                            ui.setStatus("Within path " + selectedAirplane.position.cpy().sub(runway.points[1]).angle(runwayHeading));
                        }
                        else {
                            ui.setStatus("Outside path " + selectedAirplane.position.cpy().sub(runway.points[1]).angle(runwayHeading));
                        }
//                        ui.setStatus("selected runway end 2 " + runway.names[1]);
                        uiState = ProjectIcarus.UiState.SELECT_AIRPLANE;
                        followingPlane = true;
                        break;
                    }
                }
                break;
            default:
                break;
        }

        if(selectedAirplane == null){
            ui.setStatus("deselected airplane");
        }
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        followingPlane = false;
        setCameraPosition(camera.position.add(
                camera.unproject(new Vector3(0, 0, 0))
                        .add(camera.unproject(new Vector3(deltaX, deltaY, 0)).scl(-1f))
        ));
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
            camera.zoom = scale; //Larger value of zoom = small images, border view
            camera.zoom = Math.min(maxZoomOut, Math.max(camera.zoom, maxZoomIn));
            camera.update();
            Vector3 newUnprojection = camera.unproject(origin.cpy()).cpy();
            camera.position.add(oldUnprojection.cpy().add(newUnprojection.cpy().scl(-1f)));
        }

        setToBoundary(); //Calculate distances to boundaries

        //Shift the view when zooming to keep view within map
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
        toBoundaryRight = (airport.width - camera.position.x
                - Gdx.graphics.getWidth()/2 * camera.zoom);
        toBoundaryLeft = (-camera.position.x + Gdx.graphics.getWidth()/2 * camera.zoom);
        toBoundaryTop = (airport.height - camera.position.y
                - Gdx.graphics.getHeight()/2 * camera.zoom);
        toBoundaryBottom = (-camera.position.y + Gdx.graphics.getHeight()/2 * camera.zoom);
    }

    private void setCameraPosition(Vector3 position) {
        camera.position.set(position);

        Vector2 camMin = new Vector2(camera.viewportWidth, camera.viewportHeight);
        camMin.scl(camera.zoom / 2);
        Vector2 camMax = new Vector2(airport.width, airport.height);
        camMax.sub(camMin);

        camera.position.x = Math.min(camMax.x, Math.max(camera.position.x, camMin.x));
        camera.position.y = Math.min(camMax.y, Math.max(camera.position.y, camMin.y));

        camera.update();
    }

    private void setSelectedAirplane(Airplane selectedAirplane){
        // deselect old selectedAirplane if not null
        if(this.selectedAirplane != null){
            this.selectedAirplane.setSelected(false);
        }
        this.selectedAirplane = selectedAirplane;
        // select new selectedAirplane if not null
        if(selectedAirplane != null){
            followingPlane = true;
            selectedAirplane.setSelected(true);
        }
        else {
            followingPlane = false;
        }
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

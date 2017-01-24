package com.icarus.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.ArrayList;

public class ProjectIcarus extends ApplicationAdapter implements GestureDetector.GestureListener {
    private Vector2 oldInitialFirstPointer=null, oldInitialSecondPointer=null;
    private float oldScale;
    //Used for drawing waypoints
    private ShapeRenderer shapes;
    //The currently loaded Airport
    private Airport airport;
    //The airplanes in the current game
    private ArrayList<Airplane> airplanes;
    //The font used for labels
    private BitmapFont labelFont;
    //Used for drawing airplanes
    private SpriteBatch batch;
    private Utils utils;

    private MainUi ui;

    private OrthographicCamera camera;
//    private float currentZoom;
    private float maxZoomIn; // Maximum possible zoomed in distance
    private float maxZoomOut; // Maximum possible zoomed out distance
    private float fontSize = 40;

    // Pan boundaries
    private float toBoundaryRight;
    private float toBoundaryLeft;
    private float toBoundaryTop;
    private float toBoundaryBottom;

    private Airplane selectedAirplane = null;

    public static final String TAG = "ProjectIcarus";

    @Override
    public void create () {
        //initialize the AssetManager
        AssetManager manager = new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(Airport.class, new AirportLoader(resolver));
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        //load the airport
        manager.load("airports/test.json", Airport.class);
        //load the label font
        FreeTypeFontLoaderParameter labelFontParams = new FreeTypeFontLoaderParameter();
        labelFontParams.fontFileName = "fonts/ShareTechMono-Regular.ttf";
        labelFontParams.fontParameters.size = Math.round(20.0f * Gdx.graphics.getDensity());
        manager.load("fonts/ShareTechMono-Regular.ttf", BitmapFont.class, labelFontParams);
        //load the airplane sprite
        manager.load("sprites/airplane.png", Texture.class);

        manager.load("buttons/altitude_button.png", Texture.class);
        manager.load("buttons/heading_button.png", Texture.class);
        manager.load("buttons/takeoff_button.png", Texture.class);
        manager.load("buttons/circle_button.png", Texture.class);
        manager.load("buttons/landing_button.png", Texture.class);
        manager.load("buttons/more_button.png", Texture.class);

        manager.finishLoading();
        airport = manager.get("airports/test.json");
        labelFont = manager.get("fonts/ShareTechMono-Regular.ttf");
        Airplane.texture = manager.get("sprites/airplane.png");

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();

        //add a dummy airplane
        airplanes = new ArrayList();
        airplanes.add(new Airplane("TEST", new Vector2(200, 200), new Vector2(-5, -5), 100, new Vector2(200, 200)));

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

        Gdx.input.setInputProcessor(new InputMultiplexer(ui.stage, new GestureDetector(this)));
    }

    private void setToBoundary(){
        // Calculates the distance from the edge of the camera to the specified boundary
        toBoundaryRight = (airport.width - camera.position.x
                - Gdx.graphics.getWidth()/2 * camera.zoom);
        toBoundaryLeft = (-camera.position.x + Gdx.graphics.getWidth()/2 * camera.zoom);
        toBoundaryTop = (airport.height - camera.position.y
                - Gdx.graphics.getHeight()/2 * camera.zoom);
        toBoundaryBottom = (-camera.position.y + Gdx.graphics.getHeight()/2 * camera.zoom);
    }

    @Override
    public void render () {
        super.render();
        Gdx.gl.glClearColor(Colors.colors[0].r, Colors.colors[0].g, Colors.colors[2].b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //draw waypoint triangles
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for(Waypoint waypoint: airport.waypoints) {
            waypoint.draw(shapes, camera);
        }
        shapes.end();

        //draw waypoint labels
        batch.begin();
        for(Waypoint waypoint: airport.waypoints) {
            waypoint.drawLabel(labelFont, batch, camera);
        }
        batch.end();

        //draw airplanes
        batch.begin();
        for(Airplane airplane: airplanes) {
            airplane.step(); //Move airplanes
            airplane.draw(batch, camera);
        }
        batch.end();

        ui.draw();
    }

    @Override
    public void dispose () {
        shapes.dispose();
        batch.dispose();
        labelFont.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    public void setSelectedAirplane(Airplane airplane){
        if(selectedAirplane != null){
            selectedAirplane.isSelected = false;
        }
        selectedAirplane = airplane;
        selectedAirplane.isSelected = true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        Vector3 position = camera.unproject(new Vector3(x, y, 0));
        Gdx.app.log(TAG, "" + position);
        for(Airplane airplane: airplanes) {
            if(airplane.sprite.getBoundingRectangle().contains(position.x, position.y)){
                setSelectedAirplane(airplane);
                Gdx.app.log("ProjectIcarus", "selected airplane");
                return true;
            }
        }
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
        setToBoundary(); // Calculate distances to boundaries
        float translateX;
        float translateY;

        if (deltaX > 0){ // If user pans to the left
            translateX = utils.absMin(deltaX, toBoundaryLeft);
        }
        else { // If user pans to the right
            translateX = utils.absMin(deltaX, toBoundaryRight);
        }
        if (deltaY > 0){ // If user pans up
            translateY = utils.absMin(deltaY, toBoundaryTop);
        }
        else { // If user pans down
            translateY = utils.absMin(deltaY, toBoundaryBottom);
        }

        //Shift camera by delta or by distance to boundary, whichever is closer
        camera.position.add(
                camera.unproject(new Vector3(0, 0, 0))
                        .add(camera.unproject(new Vector3(deltaX, deltaY, 0)).scl(-1f))
        );

        Vector2 camMin = new Vector2(camera.viewportWidth, camera.viewportHeight);
        camMin.scl(camera.zoom / 2);
        Vector2 camMax = new Vector2(airport.width, airport.height);
        camMax.sub(camMin);

        camera.position.x = Math.min(camMax.x, Math.max(camera.position.x, camMin.x));
        camera.position.y = Math.min(camMax.y, Math.max(camera.position.y, camMin.y));

        camera.update();
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
//        currentZoom = camera.zoom;
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
	    return false;
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

    @Override
    public void pinchStop() {

    }

    private void zoomCamera(Vector3 origin, float scale) {

        Vector3 oldUnprojection = camera.unproject(origin.cpy()).cpy();
        camera.zoom = scale; //Larger value of zoom = small images, border view
        camera.zoom = Math.min(maxZoomOut, Math.max(camera.zoom, maxZoomIn));
        camera.update();
        Vector3 newUnprojection = camera.unproject(origin.cpy()).cpy();
        camera.position.add(oldUnprojection.cpy().add(newUnprojection.cpy().scl(-1f)));

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
}

package com.icarus.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

public class ProjectIcarus extends ApplicationAdapter implements GestureDetector.GestureListener {
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

    private OrthographicCamera camera;
    private float currentZoom;
    private float maxZoomIn = 0.1f; // Maximum possible zoomed in distance
    private float maxZoomOut = 2.0f; // Maximum possible zoomed out distance
    private float fontSize = 40;

    // Pan boundaries
    private float toBoundaryRight;
    private float toBoundaryLeft;
    private float toBoundaryTop;
    private float toBoundaryBottom;

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

        manager.finishLoading();
        airport = manager.get("airports/test.json");
        labelFont = manager.get("fonts/ShareTechMono-Regular.ttf");
        Airplane.texture = manager.get("sprites/airplane.png");

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();

        //add a dummy airplane
        airplanes = new ArrayList();
        airplanes.add(new Airplane("TEST", new Vector2(10, 10), new Vector2(5, 2), 100));

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(new GestureDetector(this));

        utils = new Utils();
        // The maximum zoom level is the smallest dimension compared to the viewer
        maxZoomOut = Math.min(airport.width / Gdx.graphics.getWidth(),
                airport.height / Gdx.graphics.getHeight());

        // Start the app in maximum zoomed out state
        camera.zoom = maxZoomOut;
        camera.position.set(airport.width/2, airport.height/2, 0);
		camera.update();
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
		Gdx.gl.glClearColor(Colors.colors[0].r, Colors.colors[0].g, Colors.colors[2].b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        //draw waypoint triangles
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for(Waypoint waypoint: airport.waypoints) {
            waypoint.draw(shapes);
        }
        shapes.end();

        //draw airplanes
        batch.begin();
        for(Airplane airplane: airplanes) {
            airplane.draw(batch);
        }
        batch.end();

        //draw waypoint labels
        batch.begin();
        for(Waypoint waypoint: airport.waypoints) {
            waypoint.drawLabel(labelFont, batch);
        }
        batch.end();
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
        setToBoundary(); // Calculate distances to boundaries
        float translateX;
        float translateY;

        if (-deltaX * currentZoom > 0){ // If user pans to the right
            translateX = utils.absMin(-deltaX * currentZoom, toBoundaryRight);
        }
        else { // If user pans to the left
            translateX = utils.absMin(-deltaX * currentZoom, toBoundaryLeft);
        }
        if (deltaY * currentZoom > 0){ // If user pans up
            translateY = utils.absMin(deltaY * currentZoom, toBoundaryTop);
        }
        else { // If user pans down
            translateY = utils.absMin(deltaY * currentZoom, toBoundaryBottom);
        }

        camera.translate(translateX, translateY);
        camera.update();
		return true;
	}

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        currentZoom = camera.zoom;
        return false;
    }

	@Override
	public boolean zoom(float initialDistance, float distance) {
		float tempZoom = camera.zoom;
        camera.zoom = Math.max(Math.min((initialDistance / distance) * currentZoom, maxZoomOut),
				maxZoomIn);
		Waypoint.scaleWaypoint(camera.zoom / tempZoom); // Scale waypoint to retain apparent size

        setToBoundary(); // Calculate distances to boundaries

        // Shift the view when zooming to keep view within map
        if (toBoundaryRight < 0 || toBoundaryTop < 0){
            camera.translate(Math.min(0, toBoundaryRight),
                    Math.min(0, toBoundaryTop));
        }
        if (toBoundaryLeft > 0 || toBoundaryBottom > 0){
            camera.translate(Math.max(0, toBoundaryLeft),
                    Math.max(0, toBoundaryBottom));
        }

        camera.update();
		return true;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1,
                         Vector2 pointer2) {
		return false;
	}

    @Override
    public void pinchStop() {

    }
}

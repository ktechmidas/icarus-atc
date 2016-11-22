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

import java.util.ArrayList;

public class ProjectIcarus extends ApplicationAdapter implements GestureDetector.GestureListener {
	private ShapeRenderer shapes;
	private Airport airport;
	private ArrayList<Airplane> airplanes;
	private BitmapFont labelFont;
	private SpriteBatch batch;
    private Utils utils;

    private OrthographicCamera camera;
    private float currentZoom;
    private float maxZoomIn = 0.1f; //Maximum possible zoomed in distance
    private float maxZoomOut = 2.0f; //Maximum possible zoomed out distance
    private float fontSize = 40;

    private float toBoundaryXPositive;
    private float toBoundaryXNegative;
    private float toBoundaryYPositive;
    private float toBoundaryYNegative;

	@Override
	public void create () {
		AssetManager manager = new AssetManager();
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(Airport.class, new AirportLoader(resolver));
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		manager.load("airports/test.json", Airport.class);
		FreeTypeFontLoaderParameter labelFontParams = new FreeTypeFontLoaderParameter();
		labelFontParams.fontFileName = "fonts/ShareTechMono-Regular.ttf";
		labelFontParams.fontParameters.size = 40;
		manager.load("fonts/ShareTechMono-Regular.ttf", BitmapFont.class, labelFontParams);

		manager.load("sprites/airplane.png", Texture.class);

		manager.finishLoading();
		airport = manager.get("airports/test.json");
		labelFont = manager.get("fonts/ShareTechMono-Regular.ttf");
		Airplane.texture = manager.get("sprites/airplane.png");
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();
		airplanes = new ArrayList();
		airplanes.add(new Airplane("TEST", new Vector2(10, 10), new Vector2(5, 2), 100));

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(new GestureDetector(this));

        utils = new Utils();
        maxZoomOut = Math.min(airport.width / Gdx.graphics.getWidth(),
                airport.height / Gdx.graphics.getHeight());

        camera.zoom = maxZoomOut;
        camera.position.set(airport.width/2, airport.height/2, 0);
		camera.update();
	}

    private void setToSpriteEdge(){
        toBoundaryXPositive = (airport.width - camera.position.x
                - Gdx.graphics.getWidth()/2 * camera.zoom);
        toBoundaryXNegative = (-camera.position.x + Gdx.graphics.getWidth()/2 * camera.zoom);
        toBoundaryYPositive = (airport.height - camera.position.y
                - Gdx.graphics.getHeight()/2 * camera.zoom);
        toBoundaryYNegative = (-camera.position.y + Gdx.graphics.getHeight()/2 * camera.zoom);
    }

//    @Override
//    public void resize(float width, float height) {
//        camera.viewportWidth = width;
//        camera.viewportHeight = height;
//        camera.position.set(width/2f, height/2f, 0); //by default camera position on (0,0,0)
//    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(Colors.colors[0].r, Colors.colors[0].g, Colors.colors[2].b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

		shapes.begin(ShapeRenderer.ShapeType.Filled);
		for(Waypoint waypoint: airport.waypoints) {
			waypoint.draw(shapes);
		}
		shapes.end();
		batch.begin();
		for(Airplane airplane: airplanes) {
			airplane.draw(batch);
		}
		batch.end();
		batch.begin();
		for(Waypoint waypoint: airport.waypoints) {
			waypoint.drawLabel(labelFont, batch);
		}
		batch.end();
	}

//	private float setScale(){ //TODO Liam
//
//	}

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
        setToSpriteEdge();
        float translateX;
        float translateY;
        if (-deltaX * currentZoom > 0){
            translateX = utils.absMin(-deltaX * currentZoom, toBoundaryXPositive);
        }
        else {
            translateX = utils.absMin(-deltaX * currentZoom, toBoundaryXNegative);
        }
        if (deltaY * currentZoom > 0){
            translateY = utils.absMin(deltaY * currentZoom, toBoundaryYPositive);
        }
        else {
            translateY = utils.absMin(deltaY * currentZoom, toBoundaryYNegative);
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
		Waypoint.scaleWaypoint(camera.zoom / tempZoom);

        setToSpriteEdge();

        if (toBoundaryXPositive < 0 || toBoundaryYPositive < 0){
            camera.translate(Math.min(0, toBoundaryXPositive),
                    Math.min(0, toBoundaryYPositive));
        }

        camera.update();

        if (toBoundaryXNegative > 0 || toBoundaryYNegative > 0){
            camera.translate(Math.max(0, toBoundaryXNegative),
                    Math.max(0, toBoundaryYNegative));
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

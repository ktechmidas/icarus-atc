package com.icarus.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

public class ProjectIcarus extends ApplicationAdapter implements GestureDetector.GestureListener {
	private ShapeRenderer shapes;
	private com.icarus.project.Airport airport;
	private BitmapFont labelFont;
	private SpriteBatch batch;

    private OrthographicCamera camera;
    private float currentZoom;
    private float maxZoom; //Maximum possible zoomed in distance
    private float minZoom; //Maximum possible zoomed out distance
//    private float fontSize = 40;
	
	@Override
	public void create () {
		AssetManager manager = new AssetManager();
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(com.icarus.project.Airport.class, new AirportLoader(resolver));
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		manager.load("airports/test.json", com.icarus.project.Airport.class);
		FreeTypeFontLoaderParameter labelFontParams = new FreeTypeFontLoaderParameter();
		labelFontParams.fontFileName = "ShareTechMono-Regular.ttf";
		labelFontParams.fontParameters.size = 40;
		manager.load("ShareTechMono-Regular.ttf", BitmapFont.class, labelFontParams);

		manager.finishLoading();


		airport = manager.get("airports/test.json");
		labelFont = manager.get("ShareTechMono-Regular.ttf");
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(new GestureDetector(this));

        minZoom = 2.0f;
        maxZoom = 0.1f;
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(com.icarus.project.Colors.colors[0].r, com.icarus.project.Colors.colors[0].g, com.icarus.project.Colors.colors[2].b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

		shapes.begin(ShapeRenderer.ShapeType.Filled);
		for(com.icarus.project.Waypoint waypoint: airport.waypoints) {
			waypoint.draw(shapes);
		}
		shapes.end();
		batch.begin();
		for(com.icarus.project.Waypoint waypoint: airport.waypoints) {
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
		camera.translate(-deltaX * currentZoom, deltaY * currentZoom);
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
        camera.zoom = Math.max(Math.min((initialDistance / distance) * currentZoom, minZoom), maxZoom);
		com.icarus.project.Waypoint.scaleSize(camera.zoom / tempZoom);
//        labelFont.getData().setScale(camera.zoom / tempZoom);
        camera.update();
		return true;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		return false;
	}

	@Override
	public void pinchStop() {

	}
}

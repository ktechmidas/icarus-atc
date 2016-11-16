package com.icarus.prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class IcarusPrototype extends ApplicationAdapter implements GestureDetector.GestureListener {
	private ShapeRenderer shapes;
	private Airport airport;
	private OrthographicCamera camera;
	private float currentzoom;

	@Override
	public void create () {
		AssetManager manager = new AssetManager();
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(Airport.class, new AirportLoader(resolver));
		manager.load("airports/test.json", Airport.class);
		manager.finishLoading();
		airport = manager.get("airports/test.json");
		shapes = new ShapeRenderer();
		Gdx.input.setInputProcessor(new GestureDetector(this));
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shapes.setProjectionMatrix(camera.combined);
		shapes.begin(ShapeRenderer.ShapeType.Filled);
		shapes.setColor(1, 1, 1, 1);
		for(Waypoint waypoint: airport.waypoints) {
			waypoint.draw(shapes);
		}
		shapes.end();
	}

	@Override
	public void dispose () {
		shapes.dispose();
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
		camera.translate(-deltaX*currentzoom, deltaY*currentzoom);
		camera.update();
		return true;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		currentzoom = camera.zoom;
		return true;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		camera.zoom = (initialDistance/distance)/camera.zoom;
		System.out.println(camera.zoom);
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

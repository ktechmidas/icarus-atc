package com.icarus.prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.icarus.prototype.Airport;
import com.icarus.prototype.AirportLoader;
import com.icarus.prototype.Waypoint;

public class IcarusPrototype extends ApplicationAdapter {
	private ShapeRenderer shapes;
	private Airport airport;
	
	@Override
	public void create () {
		AssetManager manager = new AssetManager();
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(Airport.class, new AirportLoader(resolver));
		manager.load("airports/test.json", Airport.class);
		manager.finishLoading();
		airport = manager.get("airports/test.json");
		shapes = new ShapeRenderer();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
}

package com.icarus.prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import java.util.ArrayList;

import com.icarus.prototype.Airport;
import com.icarus.prototype.AirportLoader;
import com.icarus.prototype.Waypoint;
import com.icarus.prototype.Airplane;
import com.icarus.prototype.Colors;

public class IcarusPrototype extends ApplicationAdapter {
	private ShapeRenderer shapes;
	private Airport airport;
	private ArrayList<Airplane> airplanes;
	private BitmapFont labelFont;
	private SpriteBatch batch;
	
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
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(Colors.colors[0].r, Colors.colors[0].g, Colors.colors[2].b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
	
	@Override
	public void dispose () {
		shapes.dispose();
		batch.dispose();
		labelFont.dispose();
	}
}	

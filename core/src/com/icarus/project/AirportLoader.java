package com.icarus.project;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

//Used to load Airports from JSON stored by the level editor
class AirportLoader
    extends SynchronousAssetLoader<Airport, AirportLoader.AirportParameters>
{
    private static Gson gson = new Gson();

    public class AirportParameters extends AssetLoaderParameters<Airport> {
        public AirportParameters() {
        }
    }

    public AirportLoader(FileHandleResolver resolver) {
        super(resolver);
    }
    
    public Array<AssetDescriptor> getDependencies(String fname, FileHandle file, AirportParameters params) {
        return new Array();
    }

    public Airport load(
            AssetManager manager,
            String filename,
            FileHandle file,
            AirportParameters parameter)
    { 
        //parse the json and return the root object
        JsonObject json = gson.fromJson(file.readString(), JsonObject.class);
        //get the dimensions
        JsonPrimitive width = json.getAsJsonPrimitive("width");
        JsonPrimitive height = json.getAsJsonPrimitive("height");
        //get all waypoints
        Waypoint[] waypoints;
        {
            JsonArray array = json.getAsJsonArray("waypoints");
            waypoints = new Waypoint[array.size()];
            for(int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                waypoints[i] = new Waypoint(obj);
            }
        }
        //get all runways
        Runway[] runways;
        {
            JsonArray array = json.getAsJsonArray("runways");
            runways = new Runway[array.size()];
            for(int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                runways[i] = new Runway(obj);
            }
        }
        //construct an Airport
        return new Airport(waypoints, runways, width.getAsFloat(), height.getAsFloat());
    }
}

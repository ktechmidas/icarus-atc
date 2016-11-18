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

class AirportLoader
    extends SynchronousAssetLoader<com.icarus.project.Airport, AirportLoader.AirportParameters>
{
    private static Gson gson = new Gson();

    public class AirportParameters extends AssetLoaderParameters<com.icarus.project.Airport> {
        public AirportParameters() {
        }
    }

    public AirportLoader(FileHandleResolver resolver) {
        super(resolver);
    }
    
    public Array<AssetDescriptor> getDependencies(String fname, FileHandle file, AirportParameters params) {
        return new Array();
    }

    public com.icarus.project.Airport load(
            AssetManager manager,
            String filename,
            FileHandle file,
            AirportParameters parameter)
    { 
        JsonObject json = gson.fromJson(file.readString(), JsonObject.class);
        JsonArray array = json.getAsJsonArray("waypoints");
        Waypoint[] waypoints = new Waypoint[array.size()];
        for(int i = 0; i < array.size(); i++) {
            JsonObject obj = array.get(i).getAsJsonObject();
            waypoints[i] = new Waypoint(obj);
        }
        return new com.icarus.project.Airport(waypoints);
    }
}

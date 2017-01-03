package com.icarus.project;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class MainUi {
    public MainUi(AssetManager assets) {

        Drawable headingDrawable = new TextureRegionDrawable(
                new TextureRegion((Texture) assets.get("buttons/heading_button.png")));
        ImageButton headingButton = new ImageButton(headingDrawable);
    }
}

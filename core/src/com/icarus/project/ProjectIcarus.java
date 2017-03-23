package com.icarus.project;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ProjectIcarus extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    private Game game;


    public ProjectIcarus(){
        game = this;
    }

    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        this.setScreen(new MainMenuScreen(this));
    }

    public enum UiState {
        SELECT_WAYPOINT, SELECT_AIRPLANE, SELECT_HEADING, SELECT_RUNWAY
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
        screen.dispose();
    }
}

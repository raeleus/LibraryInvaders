/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.libraryinvaders.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.libraryinvaders.Core;
import static com.ray3k.libraryinvaders.Core.DATA_PATH;
import com.ray3k.libraryinvaders.State;

public class MenuState extends State {
    private Stage stage;
    private Skin skin;
    private Table root;

    public MenuState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/skin/skin.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        Gdx.input.setInputProcessor(stage);
        
        Image bg = new Image(skin, "bg");
        bg.setFillParent(true);
        stage.addActor(bg);
        
        createMenu();
    }
    
    private void createMenu() {
        FileHandle fileHandle = Gdx.files.local(Core.DATA_PATH + "/data.json");
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(fileHandle);
        
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Label label = new Label(val.getString("title"), skin, "title");
        label.setAlignment(Align.center);
        root.add(label).padBottom(50.0f);
        
        root.row();
        Image image = new Image(getEnemy());
        image.setScaling(Scaling.fit);
        root.add(image).grow();
        
        root.defaults().space(30.0f).padLeft(25.0f);
        root.row();
        ImageTextButton imageTextButton = new ImageTextButton("Play", skin);
        root.add(imageTextButton).expandX().left();
        
        imageTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/menu.wav", Sound.class).play();
                showCharacterDialog();
            }
        });
        
        root.row();
        imageTextButton = new ImageTextButton("Quit", skin);
        root.add(imageTextButton).expandX().left().padBottom(50.0f);
        
        imageTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/menu.wav", Sound.class).play();
                Gdx.app.exit();
            }
        });
    }
    
    private Drawable getEnemy() {
        Array<String> names = getCore().getImagePacks().get(Core.DATA_PATH + "/enemies");
        
        Drawable drawable = new TextureRegionDrawable(getCore().getAtlas().findRegion(names.random()));
        return drawable;
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        stage.draw();
    }

    @Override
    public void act(float delta) {
        stage.act(delta);
    }

    @Override
    public void dispose() {
        
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    private void showCharacterDialog() {
        Dialog dialog = new Dialog("", skin);
        
        Label label = new Label("Choose a character...", skin);
        dialog.getContentTable().add(label);
        
        dialog.getContentTable().row();
        Table table = new Table();
        ScrollPane scrollPane = new ScrollPane(table, skin);
        scrollPane.setFadeScrollBars(false);
        dialog.getContentTable().add(scrollPane).grow();
        
        final ButtonGroup<ImageTextButton> buttons = new ButtonGroup<ImageTextButton>();
        for (String name : getCore().getImagePacks().get(DATA_PATH + "/characters")) {
            Drawable drawable = new TextureRegionDrawable(getCore().getAtlas().findRegion(name));
            Image image = new Image(drawable);
            ImageTextButton imageTextButton = new ImageTextButton(name, skin, "list");
            imageTextButton.getImageCell().setActor(image);
            imageTextButton.getLabelCell().left().expandX();
            table.add(imageTextButton).growX();
            buttons.add(imageTextButton);
            
            table.row();
        }
        
        dialog.getContentTable().row();
        TextButton textButton = new TextButton("OK", skin);
        dialog.getContentTable().add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/menu.wav", Sound.class).play();
                ((GameState)getCore().getStateManager().getState("game")).setSelectedCharacter(buttons.getChecked().getText().toString());
                
                Gdx.input.setInputProcessor(null);
                Action changeStateAction = new Action() {
                    @Override
                    public boolean act(float delta) {
                        getCore().getStateManager().loadState("game");
                        return true;
                    }
                };
                root.addAction(new SequenceAction(new DelayAction(.5f), changeStateAction));
            }
        });
        
        dialog.show(stage);
        dialog.setSize(600.0f, 500.0f);
        dialog.setPosition(stage.getWidth() / 2.0f, stage.getHeight() / 2.0f, Align.center);
        stage.setScrollFocus(scrollPane);
    }
}
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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.libraryinvaders.Core;
import com.ray3k.libraryinvaders.Entity;
import com.ray3k.libraryinvaders.EntityManager;
import com.ray3k.libraryinvaders.InputManager;
import com.ray3k.libraryinvaders.State;
import com.ray3k.libraryinvaders.entities.BarricadeEntity;
import com.ray3k.libraryinvaders.entities.EnemyEntity;
import com.ray3k.libraryinvaders.entities.PlayerEntity;
import com.ray3k.libraryinvaders.entities.UfoEntity;

public class GameState extends State {
    private String selectedCharacter;
    private int score;
    private static int highscore = 0;
    private OrthographicCamera camera;
    private Viewport viewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private Label scoreLabel;
    private EntityManager entityManager;
    private float respawnTimer;
    private float ufoTimer;
    private static final float UFO_MAX_TIME = 20.0f;
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        EnemyEntity.speedMultiplier = 1.0f;
        respawnTimer = -1;
        score = 0;
        
        ufoTimer = UFO_MAX_TIME;
        
        inputManager = new InputManager();
        
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.apply();
        
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/skin/skin.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        PlayerEntity player = new PlayerEntity(this);
        
        spawnEntities();
        
        Array<BarricadeEntity> barricades = new Array<BarricadeEntity>();
        float barricadesWidth = 0.0f;
        final int BARRICADE_COUNT = 4;
        for (int i = 0; i < BARRICADE_COUNT; i++) {
            BarricadeEntity barricade = new BarricadeEntity(this);
            Array<String> names = getCore().getImagePacks().get(Core.DATA_PATH + "/barricades");
            barricade.setTextureRegion(getCore().getAtlas().findRegion(names.random()));
            
            barricade.setX(barricadesWidth);
            barricade.setY(30.0f + player.getTextureRegion().getRegionHeight() + 35.0f);

            barricadesWidth += barricade.getTextureRegion().getRegionWidth();
            
            barricades.add(barricade);
        }
        
        final float GAP = (Gdx.graphics.getWidth() - barricadesWidth - 100.0f) / (BARRICADE_COUNT - 1);
        
        for (int i = 1; i < barricades.size; i++) {
            BarricadeEntity barricade = barricades.get(i);
            barricade.addX(GAP * i);
            barricadesWidth += GAP;
        }
        
        final float addX = (Gdx.graphics.getWidth() - barricadesWidth) / 2.0f;
        for (BarricadeEntity barricade : barricades) {
            barricade.addX(addX);
            subdivideBarricade(barricade);
        }
        
        createStageElements();
    }
    
    private void spawnEntities() {
        float y = Gdx.graphics.getHeight() - 70.0f;
        for (int row = 0; row < 4; row++) {
            float rowHeight = 0.0f;
            float x = 0.0f;
            for (int column = 0; column < 7; column++) {
                EnemyEntity enemy = new EnemyEntity(this);
                enemy.setX(x);
                enemy.setY(y - enemy.getTextureRegion().getRegionHeight());
                x += enemy.getTextureRegion().getRegionWidth() + 30.0f;
                if (enemy.getTextureRegion().getRegionHeight() > rowHeight) {
                    rowHeight = enemy.getTextureRegion().getRegionHeight();
                }
            }
            y -= rowHeight + 30.0f;
        }
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        scoreLabel = new Label("0", skin);
        root.add(scoreLabel).expandY().padTop(25.0f).top();
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        stage.act(delta);
        
        if (respawnTimer > 0) {
            respawnTimer -= delta;
            
            if (respawnTimer <= 0) {
                EnemyEntity.speedMultiplier += 1.0f;
                spawnEntities();
                respawnTimer = -1;
            }
        } else {
            boolean setTimer = true;
            for (Entity entity : entityManager.getEntities()) {
                if (entity instanceof EnemyEntity) {
                    setTimer = false;
                    break;
                }
            }
            
            if (setTimer) {
                respawnTimer = 2.0f;
            }
        }
        
        ufoTimer -= delta;
        if (ufoTimer <= 0) {
            UfoEntity ufo = new UfoEntity(this);
            ufo.setPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - ufo.getTextureRegion().getRegionHeight() - 25.0f);
            ufoTimer = UFO_MAX_TIME;
        }
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
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(String selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreLabel.setText(Integer.toString(score));
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText(Integer.toString(this.score));
        if (this.score > highscore) {
            highscore = this.score;
        }
    }

    private void subdivideBarricade(BarricadeEntity barricade) {
        final int ROWS = 3;
        final int COLUMNS = 5;
        final int WIDTH = barricade.getTextureRegion().getRegionWidth() / COLUMNS;
        final int HEIGHT = barricade.getTextureRegion().getRegionHeight() / ROWS;
        
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                BarricadeEntity piece = new BarricadeEntity(this);
                piece.setPosition(x * WIDTH + barricade.getX(), y * HEIGHT + barricade.getY());
                piece.getCollisionBox().width = WIDTH;
                piece.getCollisionBox().height = HEIGHT;
                TextureRegion tex = new TextureRegion(barricade.getTextureRegion(), x * WIDTH, (ROWS - y - 1) * HEIGHT, WIDTH, HEIGHT);
                piece.setTextureRegion(tex);
            }
        }
        
        barricade.dispose();
    }
}
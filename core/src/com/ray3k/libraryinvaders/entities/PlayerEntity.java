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

package com.ray3k.libraryinvaders.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.ray3k.libraryinvaders.Core;
import com.ray3k.libraryinvaders.Entity;
import com.ray3k.libraryinvaders.InputManager;
import com.ray3k.libraryinvaders.states.GameState;
import java.util.Iterator;

public class PlayerEntity extends Entity implements InputManager.KeyActionListener {
    private Sound damageSound;
    private Sound shotSound;
    private GameState gameState;
    private Array<BulletEntity> myBullets;
    private static final int MAX_BULLETS = 1;
    
    public PlayerEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
    }

    @Override
    public void create() {
        myBullets = new Array<BulletEntity>();
        setCheckingCollisions(true);
        setTextureRegion(getCore().getAtlas().findRegion(((GameState) getCore().getStateManager().getState("game")).getSelectedCharacter()));
        
        damageSound = getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/player-hit.wav", Sound.class);
        shotSound = getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/player-shot.wav", Sound.class);
        
        setX(Gdx.graphics.getWidth() / 2.0f - getTextureRegion().getRegionWidth() / 2.0f);
        setY(30.0f);
        
        getCollisionBox().setSize(getTextureRegion().getRegionWidth(), getTextureRegion().getRegionHeight());
        
        ((GameState)getCore().getStateManager().getState("game")).getInputManager().addKeyActionListener(this);
    }

    @Override
    public void act(float delta) {
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            setMotion(200.0f, 180.0f);
        } else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            setMotion(200.0f, 0.0f);
        } else {
            setMotion(0.0f, 0.0f);
        }
    }

    @Override
    public void act_end(float delta) {
        
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        
    }

    @Override
    public void destroy() {
        damageSound.play();
        new WhiteFlashEntity(gameState);
        new GameOverTimerEntity(gameState, 1.0f);
    }

    @Override
    public void collision(Entity other) {
        if (other instanceof BulletEntity) {
            BulletEntity bullet = (BulletEntity) other;
            if (bullet.getParent() != this) {
                bullet.dispose();
                dispose();
            }
        } else if (other instanceof EnemyEntity) {
            EnemyEntity enemy = (EnemyEntity) other;
            enemy.dispose();
            dispose();
        }
    }

    @Override
    public void keyPressed(int key) {
        if (!isDestroyed() && key == Keys.SPACE) {
            Iterator<BulletEntity> iter = myBullets.iterator();
            while (iter.hasNext()) {
                BulletEntity bullet = iter.next();
                if (bullet.isDestroyed()) {
                    iter.remove();
                }
            }
            
            if (myBullets.size < MAX_BULLETS) {
                BulletEntity bullet = new BulletEntity(gameState);
                bullet.setParent(this);
                bullet.setPosition(getX() + getTextureRegion().getRegionWidth() / 2.0f - bullet.getTextureRegion().getRegionWidth() / 2.0f, getY() + getTextureRegion().getRegionHeight() / 2.0f - bullet.getTextureRegion().getRegionHeight() / 2.0f);
                bullet.setMotion(500.0f, 90.0f);
                shotSound.play();
                myBullets.add(bullet);
            }
        }
    }
}

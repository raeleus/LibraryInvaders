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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ray3k.libraryinvaders.Core;
import com.ray3k.libraryinvaders.Entity;
import com.ray3k.libraryinvaders.states.GameState;

public class PlayerEntity extends Entity {
    private Sound damageSound;
    private Sound shotSound;
    
    public PlayerEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
    }

    @Override
    public void create() {
        setTextureRegion(getCore().getAtlas().findRegion(((GameState) getCore().getStateManager().getState("game")).getSelectedCharacter()));
        
        damageSound = getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/player-hit.wav", Sound.class);
        shotSound = getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/player-shot.wav", Sound.class);
        
        setX(Gdx.graphics.getWidth() / 2.0f - getTextureRegion().getRegionWidth() / 2.0f);
        setY(30.0f);
        
        setOffsetX(getTextureRegion().getRegionWidth() / 2.0f);
        setOffsetY(getTextureRegion().getRegionHeight() / 2.0f);
        getCollisionBox().setSize(getTextureRegion().getRegionWidth() / 2.0f, getTextureRegion().getRegionHeight() / 2.0f);
    }

    @Override
    public void act(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
        
        if (other instanceof BulletEntity) {
            BulletEntity bullet = (BulletEntity) other;
            if (bullet.getParent() != this) {
                damageSound.play();
                dispose();
            }
        }
    }

}

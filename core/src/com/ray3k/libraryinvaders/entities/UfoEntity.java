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

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.ray3k.libraryinvaders.Core;
import com.ray3k.libraryinvaders.Entity;
import com.ray3k.libraryinvaders.states.GameState;

public class UfoEntity extends Entity {
    private static final float BULLET_TIMER_MIN = 1.0f;
    private static final float BULLET_TIMER_MAX = 5.0f;
    private float bulletTimer;
    private Sound bulletSound;
    private Sound hitSound;
    private GameState gameState;
    private static final int POINT_WORTH = 70;
    
    public UfoEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
    }

    @Override
    public void create() {
        setCheckingCollisions(true);
        setTextureRegion(getUfo());
        setMotion(90.0f, 180.0f);
        
        getCollisionBox().setSize(getTextureRegion().getRegionWidth(), getTextureRegion().getRegionHeight());
        
        bulletTimer = MathUtils.random(0.0f, BULLET_TIMER_MAX);
        
        bulletSound = getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/enemy-shot.wav", Sound.class);
        hitSound = getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/enemy-hit.wav", Sound.class);
    }

    @Override
    public void act(float delta) {
        if (getX() + getTextureRegion().getRegionWidth() < 0) {
            dispose();
        }
        
        bulletTimer -= delta;
        if (bulletTimer < 0) {
            bulletTimer = MathUtils.random(BULLET_TIMER_MIN, BULLET_TIMER_MAX);
            
            bulletSound.play();
            
            BulletEntity bullet = new BulletEntity(gameState);
            bullet.setParent(this);
            bullet.setPosition(getX() + getTextureRegion().getRegionWidth() / 2.0f, getY() + getTextureRegion().getRegionHeight() / 2.0f);
            
            bullet.setMotion(100.0f, 270.0f);
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
    }

    @Override
    public void collision(Entity other) {
        if (other instanceof BulletEntity) {
            if (((BulletEntity) other).getParent() instanceof PlayerEntity) {
                other.dispose();
                dispose();
                hitSound.play();
                gameState.addScore(POINT_WORTH);
            }
        }
    }

    private TextureRegion getUfo() {
        Array<String> names = getCore().getImagePacks().get(Core.DATA_PATH + "/ufos");
        
        return getCore().getAtlas().findRegion(names.random());
    }
}

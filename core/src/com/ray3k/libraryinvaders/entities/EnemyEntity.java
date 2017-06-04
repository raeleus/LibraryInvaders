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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.ray3k.libraryinvaders.Core;
import com.ray3k.libraryinvaders.Entity;
import com.ray3k.libraryinvaders.states.GameState;

public class EnemyEntity extends Entity {
    private static final float CREEP_AMOUNT = 15.0f;
    private static final float BULLET_TIMER_MIN = 20.0f;
    private static final float BULLET_TIMER_MAX = 40.0f;
    private float bulletTimer;
    private boolean queueBounce;
    private GameState gameState;
    private Sound bulletSound;
    private Sound hitSound;

    public EnemyEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
    }
    
    @Override
    public void create() {
        setCheckingCollisions(true);
        setTextureRegion(getEnemy());
        setMotion(60.0f, 0.0f);
        
        getCollisionBox().setSize(getTextureRegion().getRegionWidth(), getTextureRegion().getRegionHeight());
        
        bulletTimer = MathUtils.random(0.0f, BULLET_TIMER_MAX);
        
        bulletSound = getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/enemy-shot.wav", Sound.class);
        hitSound = getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/enemy-hit.wav", Sound.class);
    }

    @Override
    public void act(float delta) {
        if (getX() + getTextureRegion().getRegionWidth() > Gdx.graphics.getWidth() || getX() < 0.0f) {
            for (Entity ent : gameState.getEntityManager().getEntities()) {
                if (ent instanceof EnemyEntity) {
                    ((EnemyEntity) ent).queueBounce = true;
                }
            }
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
        
        if (getY() > Gdx.graphics.getHeight()) {
            dispose();
        }
    }

    @Override
    public void act_end(float delta) {
        if (queueBounce) {
            queueBounce = false;
            setXspeed(-getXspeed());
            addY(-CREEP_AMOUNT);
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
        if (other instanceof BarricadeEntity) {
            other.dispose();
        } else if (other instanceof BulletEntity) {
            if (((BulletEntity) other).getParent() instanceof PlayerEntity) {
                other.dispose();
                dispose();
                hitSound.play();
            }
        }
    }
    
    private TextureRegion getEnemy() {
        Array<String> names = getCore().getImagePacks().get(Core.DATA_PATH + "/enemies");
        
        return getCore().getAtlas().findRegion(names.random());
    }
}

package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Interface for all "renderable" objects.
 */
public interface CanBeRendered {

	/**
	 * Draws the object.
	 * 
	 * @param camera
	 *            {@link Camera} which may be used for rendering
	 * @param spriteBatch
	 *            {@link SpriteBatch} which may be used for rendering
	 * @param deltaTime
	 *            the time span between the current frame and the last frame in seconds. Might be smoothed over n
	 *            frames. See also {@link Graphics#getDeltaTime()}.
	 */
	void render(Camera camera, SpriteBatch spriteBatch, float deltaTime);
}

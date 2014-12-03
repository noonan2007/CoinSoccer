package com.samsung.android.sample.coinsoccer.settings;

import com.badlogic.gdx.graphics.Color;

/**
 * Player settings contract.
 */
public class PlayerSettings {

	/**
	 * index of player -> should be 0 or 1. See also {@link Which}.
	 */
	public final Which which;

	/**
	 * color tint to mark user icon
	 */
	public final int color;

	/**
	 * name of the user
	 */
	public final String name;

	/**
	 * The constructor.
	 * 
	 * @param which
	 *            see {@link #index} description
	 * @param color
	 *            see {@link #color} description
	 * @param name
	 *            see {@link #name} description
	 */
	public PlayerSettings(Which which, int color, String name) {
		this.which = which;
		this.color = color;
		this.name = name;
	}

	public static Color createGdxColor(int color) {
		return createGdxColor(color, 1);
	}

	public static Color createGdxColor(int color, float alpha) {
		return new Color(
				(float) android.graphics.Color.red(color) / 255,
				(float) android.graphics.Color.green(color) / 255,
				(float) android.graphics.Color.blue(color) / 255,
				alpha);
	}
}
package com.samsung.android.sample.coinsoccer.game;

/**
 * Common interface for circle-shaped objects.
 * 
 * This is used to make calculations of positions of pawns
 * taking into account also "virtual" future positions some 
 * other pawns.
 */
public interface ICircle {

	/**
	 * It returns x coordinate of center of the circle.
	 * 
	 * @return x coordinate of center of the circle.
	 */
	float getCenterX();

	/**
	 * It returns y coordinate of center of the circle.
	 * 
	 * @return y coordinate of center of the circle.
	 */
	float getCenterY();

	/**
	 * It returns radius of the circle.
	 * 
	 * @return radius of the circle.
	 */
	float getRadius();
}

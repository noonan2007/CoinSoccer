package com.samsung.android.sample.coinsoccer.game;

public class RectangularArea {

	private final float mLeftX;
	private final float mBottomY;
	private final float mWidth;
	private final float mHeight;

	public RectangularArea(float leftX, float bottomY, float width, float height) {
		mLeftX = leftX;
		mBottomY = bottomY;
		mWidth = width;
		mHeight = height;
	}

	public float getLeftX() {
		return mLeftX;
	}

	public float getBottomY() {
		return mBottomY;
	}

	public float getWidth() {
		return mWidth;
	}

	public float getHeight() {
		return mHeight;
	}
	
	public float getCenterX() {
		return getLeftX() + getWidth() / 2;
	}

	public float getCenterY() {
		return getBottomY() + getHeight() / 2;
	}

	public float getRightX() {
		return getLeftX() + getWidth();
	}

	public float getTopY() {
		return getBottomY() + getHeight();
	}

	public boolean contains(float x, float y) {
		return x >= getLeftX() && x <= getRightX() && y >= getBottomY() && y <= getTopY();
	}
}
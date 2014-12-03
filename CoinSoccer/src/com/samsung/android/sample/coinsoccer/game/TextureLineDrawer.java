package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class TextureLineDrawer {

	private final float mThickness;
	private Texture mTexture;
	private final float[] mVertices = new float[4 * 5];
	private final float mColor = Color.WHITE.toFloatBits();
	private final float mX;
	private final float mY;
	private final float mLength;
	private final float mDegrees;

	public TextureLineDrawer(float thickness, float x, float y, float length, float degrees) {
		mThickness = thickness;
		mX = x;
		mY = y;
		mLength = length;
		mDegrees = degrees;
	}

	public void setTexture(Texture texture) {
		mTexture = texture;
		prepareVertices(
				mX, mY - mThickness / 2, 0, mThickness / 2, mLength, mThickness,
				1, 1, mDegrees, 0, 0,
				(int) (mLength * mTexture.getHeight() / mThickness), mTexture.getHeight(),
				false, false);
	}

	public void draw(SpriteBatch spriteBatch) {
		spriteBatch.draw(mTexture, mVertices, 0, mVertices.length);
	}
	
	private void prepareVertices(float x, float y, float originX, float originY,
			float width, float height, float scaleX,
			float scaleY, float rotation, int srcX, int srcY,
			int srcWidth, int srcHeight, boolean flipX, boolean flipY) {

		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

		float x1;
		float y1;
		float x2;
		float y2;
		float x3;
		float y3;
		float x4;
		float y4;

		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;

			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;

			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;

			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = p1x;
			y1 = p1y;

			x2 = p2x;
			y2 = p2y;

			x3 = p3x;
			y3 = p3y;

			x4 = p4x;
			y4 = p4y;
		}

		x1 += worldOriginX;
		y1 += worldOriginY;
		x2 += worldOriginX;
		y2 += worldOriginY;
		x3 += worldOriginX;
		y3 += worldOriginY;
		x4 += worldOriginX;
		y4 += worldOriginY;

		float invTexHeight = 1.0f / mTexture.getHeight();
		float invTexWidth = 1.0f / mTexture.getWidth();

		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;

		if (flipX) {
			float tmp = u;
			u = u2;
			u2 = tmp;
		}

		if (flipY) {
			float tmp = v;
			v = v2;
			v2 = tmp;
		}

		int i = 0;
		mVertices[i++] = x1;
		mVertices[i++] = y1;
		mVertices[i++] = mColor;
		mVertices[i++] = u;
		mVertices[i++] = v;

		mVertices[i++] = x2;
		mVertices[i++] = y2;
		mVertices[i++] = mColor;
		mVertices[i++] = u;
		mVertices[i++] = v2;

		mVertices[i++] = x3;
		mVertices[i++] = y3;
		mVertices[i++] = mColor;
		mVertices[i++] = u2;
		mVertices[i++] = v2;

		mVertices[i++] = x4;
		mVertices[i++] = y4;
		mVertices[i++] = mColor;
		mVertices[i++] = u2;
		mVertices[i++] = v;
	}
}
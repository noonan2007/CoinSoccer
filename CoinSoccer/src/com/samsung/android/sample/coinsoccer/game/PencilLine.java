package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

public class PencilLine {

	private class InnerActor extends Actor {

		private TextureRegion mTextureRegion;

		public void setRegion(TextureRegion textureRegion) {
			mTextureRegion = textureRegion;
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.draw(mTextureRegion, getX(), getY(), getWidth(), getHeight());
		}
	}

	private final Group mGroup;
	private final InnerActor mStart;
	private final InnerActor mStrechable;
	private final InnerActor mEnd;

	public PencilLine(float thickness, float x, float y, float length, float degrees) {
		mGroup = new Group();
		mStart = new InnerActor();
		mGroup.addActor(mStart);
		mStrechable = new InnerActor();
		mGroup.addActor(mStrechable);
		mEnd = new InnerActor();
		mGroup.addActor(mEnd);
		mGroup.setBounds(x, y - thickness / 2, length, thickness);
		mGroup.setOrigin(0, thickness / 2);
		mGroup.setRotation(degrees);
	}

	public void draw(SpriteBatch spriteBatch) {
		mGroup.draw(spriteBatch, 1);
	}

	public void setTextures(TextureRegion start, TextureRegion stretchable, TextureRegion end) {
		mStart.setRegion(start);
		mStrechable.setRegion(stretchable);
		mEnd.setRegion(end);
		float 
			height = mGroup.getHeight(),
			startWidth = start.getRegionWidth() * height / start.getRegionHeight(),
			endWidth = end.getRegionWidth() * height / end.getRegionHeight(),
			stretchableWidth = mGroup.getWidth() - startWidth - endWidth;
		if (stretchableWidth < 0) {
			stretchableWidth = 0;
		}
		mStart.setBounds(0, 0, startWidth, height);
		mStrechable.setBounds(startWidth, 0, stretchableWidth, height);
		mEnd.setBounds(startWidth + stretchableWidth, 0, endWidth, height);
	}
}

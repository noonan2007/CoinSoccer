package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.samsung.android.sample.coinsoccer.game.AssetsProvider.AssetsProviderClient;

public class ShotInputWidget implements CanBeRendered, AssetsProviderClient {

	private static final float SHOT_MULTIPLIER = 3;
	private static final float MAX_DISTORTION_ANGLE = 30;
	private static final float MAX_ALLOWED_SHOT_SIZE = 7.5f;
	private static final float CANCEL_SHOT_DISTANCE = 1.75f;

	private static class TextureActor extends Actor {

		TextureRegion mRegion;

		public TextureActor(float width, float height) {
			setSize(width, height);
			setOrigin(width / 2, height / 2);
		}

		public TextureActor(float width, float height, float originX, float originY) {
			setSize(width, height);
			setOrigin(originX, originY);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.draw(mRegion, getX() - getOriginX(), getY() - getOriginY(), getWidth(), getHeight());
		}
	}

	private static class ShotDistortionWidget extends Group {

		private final TextureActor mBackground;
		private final TextureActor mGreenZone;
		private final TextureActor mIndicator;
		private int mDirection = 1;
		private float mDistanceRatio;
		private float mDistortionRatio;

		public ShotDistortionWidget(float width, float height) {
			setSize(width, height);
			setOrigin(width / 2, height / 2);
			mBackground = new TextureActor(getWidth(), getHeight());
			addActor(mBackground);
			mGreenZone = new TextureActor(getHeight(), getHeight());
			addActor(mGreenZone);
			mIndicator = new TextureActor(getHeight(), getHeight());
			addActor(mIndicator);
		}

		public void resetDistortion() {
			mDistortionRatio = 0;
			mIndicator.setX(0);
		}

		/**
		 * @return float in range [-1, 1]
		 */
		public float getDistortionRatio() {
			return -mDistortionRatio;
		}

		@Override
		public void act(float delta) {
			if (!isVisible()) {
				return;
			}
			mDistortionRatio = mDistortionRatio + mDirection * getDistanceMultiplier(delta);
			switch(mDirection) {
				case 1:
					if(mDistortionRatio >= 1) {
						mDistortionRatio = 1;
						mDirection = -1;
					}
					break;
				case -1:
					if (mDistortionRatio <= -1) {
						mDistortionRatio = -1;
						mDirection = 1;
					}
					break;
			}
			mIndicator.setX(mDistortionRatio * (getWidth() - mIndicator.getWidth()) / 2);
		}

		public void setDistanceRatio(float distanceRatio) {
			mDistanceRatio = distanceRatio;
		}

		public float getDistanceMultiplier(float delta) {
			return 6 * delta * (float) Math.pow(mDistanceRatio, 1.4);
		}
	}

	private final InputProcessor mInputProcessor = new InputAdapter() {

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			if (pointer == 0) {
				onPawnTouchUp(screenX, screenY);
			}
			return true;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			if (pointer == 0) {
				onPawnTouchDragged(screenX, screenY);
			}
			return true;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			return true;
		}
	};
	private final CoinSoccerGame mGame;
	private final Group mGroup;
	private PlayerPawn mPlayerPawn;
	private final Vector2 mHelperVector;
	private final TextureActor mShotHandleImage;
	private final ShotDistortionWidget mShotDistortionWidget;
	private boolean mHasLeavedCancelShotAreaForTheFirstTimeSinceBeingShown;

	public ShotInputWidget(CoinSoccerGame game) {
		mHelperVector = new Vector2();
		mGame = game;
		mGroup = new Group();
		mShotHandleImage = new TextureActor(2, 2, 1, 0);
		mGroup.addActor(mShotHandleImage);
		mShotDistortionWidget = new ShotDistortionWidget(3.5f, 0.75f);
		mShotDistortionWidget.setPosition(0, 1.25f);
		mGroup.addActor(mShotDistortionWidget);
		mGame.getAssetsProvider().addClient(this);
	}

	@Override
	public void render(Camera camera, SpriteBatch spriteBatch, float deltaTime) {
		if (isActive()) {
			mGroup.act(deltaTime);
			mGroup.draw(spriteBatch, 1);
		}
	}

	@Override
	public void requestAssetsRefresh(AssetsProvider assetsProvider) {
		mShotHandleImage.mRegion = assetsProvider.getIndicatorTexture();
		mShotDistortionWidget.mBackground.mRegion = assetsProvider.getAccuracyTestBgTexture();
		mShotDistortionWidget.mGreenZone.mRegion = assetsProvider.getAccuracyTestGreenZoneTexture();
		mShotDistortionWidget.mIndicator.mRegion = assetsProvider.getAccuracyTestAimTexture();
	}

	public void start(PlayerPawn playerPawn, int screenX, int screenY) {
		mPlayerPawn = playerPawn;
		mHasLeavedCancelShotAreaForTheFirstTimeSinceBeingShown = false;
		mGroup.setX(mPlayerPawn.getCenterX());
		mGroup.setY(mPlayerPawn.getCenterY());
		mShotDistortionWidget.resetDistortion();
		mGroup.setVisible(true);
		onPawnTouchDragged(screenX, screenY);
		mGame.addProcessor(0, mInputProcessor);
	}

	public void stop() {
		if (mPlayerPawn != null) {
			mPlayerPawn.setDrawShotRange(-1);
			mPlayerPawn = null;
		}
		mGroup.setVisible(false);
		mGroup.setRotation(0);
		mShotHandleImage.setPosition(0, 0);
		mGame.removeProcessor(mInputProcessor);
		mGame.mExposedDialogs.shotCancelInfo(false);
	}

	public boolean isActive() {
		return mPlayerPawn != null;
	}

	protected boolean onPawnTouchDragged(int screenX, int screenY) {
		mGame.getGameCameraManager().screenToWorld(mHelperVector.set(screenX, screenY));
		float distance = Math.max(0, mHelperVector.dst(mGroup.getX(), mGroup.getY()));
		boolean shouldCancelShotIfUpNow = false;
		if (distance <= CANCEL_SHOT_DISTANCE) {
			if (mHasLeavedCancelShotAreaForTheFirstTimeSinceBeingShown) {
				shouldCancelShotIfUpNow = true;
			}
		}
		else {
			if (!mHasLeavedCancelShotAreaForTheFirstTimeSinceBeingShown) {
				mHasLeavedCancelShotAreaForTheFirstTimeSinceBeingShown = true;
			}
		}
		if (shouldCancelShotIfUpNow) {
			mShotDistortionWidget.setVisible(false);
			mGame.mExposedDialogs.shotCancelInfo(true, screenX, screenY);
		}
		else {
			if (distance > MAX_ALLOWED_SHOT_SIZE) {
				float correction = MAX_ALLOWED_SHOT_SIZE / distance;
				mHelperVector.set(
						mGroup.getX() + (mHelperVector.x - mGroup.getX()) * correction,
						mGroup.getY() + (mHelperVector.y - mGroup.getY()) * correction);
				distance = MAX_ALLOWED_SHOT_SIZE;
			}
			mGame.mExposedDialogs.shotCancelInfo(false);
			mShotDistortionWidget.setVisible(true);
		}
		mPlayerPawn.setDrawShotRange(Math.abs(distance) - mShotHandleImage.getHeight() / 2);
		mShotDistortionWidget.setDistanceRatio(distance / MAX_ALLOWED_SHOT_SIZE);
		mShotHandleImage.setY(-Math.abs(Math.max(distance, mShotHandleImage.getHeight())));
		mGroup.setRotation(90f + (float) Math.toDegrees(Math.atan2(
				mHelperVector.y - mGroup.getY(), mHelperVector.x - mGroup.getX())));
		return shouldCancelShotIfUpNow;
	}

	protected void onPawnTouchUp(int screenX, int screenY) {
		if (!isActive()) {
			return;
		}
		if (onPawnTouchDragged(screenX, screenY)) {
			mGame.onShotVectorPreparationCancelled(mPlayerPawn);
		}
		else {
			Vector2 pos = mPlayerPawn.getBody().getPosition();
			float distortion = mShotDistortionWidget.getDistortionRatio();
			if (distortion != 0) {
				rotatePointByDegrees(mHelperVector, pos, distortion * MAX_DISTORTION_ANGLE);
			}
			mHelperVector
					.set(pos.x - mHelperVector.x, pos.y - mHelperVector.y)
					.scl(SHOT_MULTIPLIER);
			mGame.onShotVectorPrepared(mPlayerPawn, mHelperVector.x, mHelperVector.y);
		}
		stop();
	}
	
	private static void rotatePointByDegrees(Vector2 point, Vector2 center, float degrees) {
		rotatePointByRadians(point, center, (float) Math.toRadians(degrees));
	}

	private static void rotatePointByRadians(Vector2 point, Vector2 center, float radians) {
		float 
				diffX = point.x - center.x, 
				diffY = point.y - center.y, 
				sin = (float) Math.sin(radians),
				cos = (float) Math.cos(radians);
		point.set(center.x + diffX * cos - diffY * sin, center.y + diffX * sin + diffY * cos);
	}
}

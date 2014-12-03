package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.samsung.android.sample.coinsoccer.game.AssetsProvider.AssetsProviderClient;

public class BaseCoinPawn implements AssetsProviderClient, CanBeRendered,
		ICircle {

	private static final int SAVED_SNAPSHOTS_MAX_COUNT = 3;

	private final Body mBody;
	private final Sprite mSprite;
	private final float mRadius;
	private final TextureRegion mShadesTexture;
	private ChainableStateSnapshot mSavedSnapshot;

	protected BaseCoinPawn(Body body, float centerX, float centerY, float radius) {
		mBody = body;
		mBody.setUserData(this);
		mRadius = radius;
		mSprite = new Sprite();
		mShadesTexture = new TextureRegion();
		mSprite.setBounds(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
		mSprite.setOrigin(radius, radius);
	}

	public void initSnapshotsChain() {
		initSnapshotsChain(ChainableStateSnapshot.obtain(this, true));
	}

	public void initSnapshotsChain(float centerX, float centerY, float angle) {
		initSnapshotsChain(ChainableStateSnapshot.obtain(this, centerX, centerY, angle));
	}

	public void initSnapshotsChain(ChainableStateSnapshot initialSnapshot) {
		if (mSavedSnapshot != null) {
			throw new IllegalStateException(
					"Snapshot chain must be inited once and only once before the pawn object is used!!!");
		}
		initialSnapshot.initChain();
		mSavedSnapshot = initialSnapshot;
	}

	@Override
	public void render(Camera camera, SpriteBatch spriteBatch, float deltaTime) {
		mSprite.draw(spriteBatch);
		spriteBatch.draw(mShadesTexture, mSprite.getX(), mSprite.getY(),
				mSprite.getWidth(), mSprite.getHeight());
	}

	@Override
	public void requestAssetsRefresh(AssetsProvider assetsProvider) {
		mSprite.setRegion(assetsProvider.getCoinInnerTexture());
		mShadesTexture.setRegion(assetsProvider.getCoinOuterTexture());
	}

	public void updateSpriteToBody() {
		Vector2 position = mBody.getPosition();
		mSprite.setPosition(position.x - mSprite.getWidth() / 2, position.y - mSprite.getHeight() / 2);
		mSprite.setRotation((float) Math.toDegrees(mBody.getAngle()));
	}

	public void updateBodyToSprite() {
		mBody.setTransform(getCenterX(), getCenterY(), getAngle());
	}

	public Body getBody() {
		return mBody;
	}

	public Sprite getSprite() {
		return mSprite;
	}

	@Override
	public float getRadius() {
		return mRadius;
	}

	public boolean hitTest(float worldX, float worldY, boolean useBoundingBox) {
		if (useBoundingBox) { // spriteTest
			float left = mSprite.getX();
			float right = left + mSprite.getWidth();
			float bottom = mSprite.getY();
			float top = bottom + mSprite.getHeight();
			return worldX >= left && worldX < right && worldY >= bottom && worldY < top;
		}
		else { // shape test
			// assumption the fixture shape is circle and its center is placed
			// at (0, 0) of the body, which is required from AbstractPawn body
			return mBody.getFixtureList().get(0).testPoint(worldX, worldY);
		}
	}

	public void saveSnapshot() {
		if (mSavedSnapshot == null) {
			throw new IllegalStateException(
					"Snapshot chain should be inited before calling this method!!!");
		}
		ChainableStateSnapshot newestNode = ChainableStateSnapshot.obtain(this, true);
		mSavedSnapshot.append(newestNode);
		mSavedSnapshot = newestNode;
		ChainableStateSnapshot cutOffBeforeSnapshot =
				mSavedSnapshot.getPrevious(SAVED_SNAPSHOTS_MAX_COUNT);
		if (cutOffBeforeSnapshot != null) {
			cutOffBeforeSnapshot.cutOffPreviousUntilTheInitialNode();
		}
	}

	public ChainableStateSnapshot getSnapshot() {
		if (mSavedSnapshot == null) {
			throw new IllegalStateException(
					"Snapshot chain should be inited before calling this method!!!");
		}
		return mSavedSnapshot;
	}

	public ChainableStateSnapshot popLastSnapshot() {
		if (mSavedSnapshot == null) {
			throw new IllegalStateException(
					"Snapshot chain should be inited before calling this method!!!");
		}
		ChainableStateSnapshot lastSnapshot = mSavedSnapshot;
		if (lastSnapshot.hasPrevious()) {
			mSavedSnapshot = lastSnapshot.getPrevious();
			lastSnapshot.detachFromPrevious();
		}
		return lastSnapshot;
	}

	@Override
	public float getCenterX() {
		return mSprite.getX() + mSprite.getWidth() / 2;
	}

	@Override
	public float getCenterY() {
		return mSprite.getY() + mSprite.getHeight() / 2;
	}

	public float getAngle() {
		return (float) Math.toRadians(mSprite.getRotation());
	}

	public void applyBodyState(float centerX, float centerY, float angle) {
		getBody().setTransform(centerX, centerY, angle);
		updateSpriteToBody();
	}

	public boolean isMoving() {
		if (mBody.isAwake()) {
			if (mBody.getLinearVelocity().len() < 1 && mBody.getAngularVelocity() < 1) {
				mBody.setAwake(false);
				return false;
			}
			return true;
		}
		return false;
	}

	public void setActive(boolean active) {
		mBody.setActive(active);
	}

	public void stopMoving() {
		mBody.setAwake(false);
	}
}

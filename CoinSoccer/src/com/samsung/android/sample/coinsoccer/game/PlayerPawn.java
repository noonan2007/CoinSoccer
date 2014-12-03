package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

public class PlayerPawn extends BaseCoinPawn {

	public static final short BOX2D_CATEGORY = 0x0008;

	public static class BodyBuilder implements Disposable {

		private static final float PLAYER_PAWN_BODY_ANGULAR_DAMPING = 3;
		private static final float PLAYER_PAWN_BODY_LINEAR_DAMPING = 3;
		private static final float PLAYER_PAWN_BODY_FRICTION = 0.6f;
		private static final float PLAYER_PAWN_BODY_DENSITY = 1.5f;
		private static final float PLAYER_PAWN_BODY_RESTITUTION = 0.5f;

		private final World mWorld;
		private final BodyDef mBodyDef;
		private final CircleShape mCircleShape;
		private final FixtureDef mFixtureDef;

		public BodyBuilder(World world, float radius) {
			mWorld = world;
			mBodyDef = new BodyDef();
			mBodyDef.type = BodyType.DynamicBody;
			mBodyDef.bullet = true;
			mBodyDef.allowSleep = true;
			mBodyDef.angularDamping = PLAYER_PAWN_BODY_ANGULAR_DAMPING;
			mBodyDef.linearDamping = PLAYER_PAWN_BODY_LINEAR_DAMPING;
			mCircleShape = new CircleShape();
			mCircleShape.setRadius(radius);
			mFixtureDef = new FixtureDef();
			mFixtureDef.shape = mCircleShape;
			mFixtureDef.friction = PLAYER_PAWN_BODY_FRICTION;
			mFixtureDef.density = PLAYER_PAWN_BODY_DENSITY;
			mFixtureDef.restitution = PLAYER_PAWN_BODY_RESTITUTION;
			mFixtureDef.filter.categoryBits = BOX2D_CATEGORY;
		}

		public Body newBody(float worldX, float worldY) {
			mBodyDef.position.set(worldX, worldY);
			Body body = mWorld.createBody(mBodyDef);
			body.createFixture(mFixtureDef);
			return body;
		}

		public float getRadius() {
			return mCircleShape.getRadius();
		}

		@Override
		public void dispose() {
			mCircleShape.dispose();
		}
	}

	private boolean mIsPawnSelectable;
	private final Player mPlayer;
	private final int mIndex;
	private TextureRegion mSelectedTextureRegion;
	private float mSelectableDeltaSum;
	private int mSelectableDeltaSumDir = 1;
	private float mShotRadius = -1;

	public PlayerPawn(AssetsProvider assetsProvider, Player player, int index, 
			BodyBuilder bodyBuilder, float centerX, float centerY, Color pawnTint) {
		super(bodyBuilder.newBody(centerX, centerY), centerX, centerY,
				bodyBuilder.getRadius());
		getSprite().setColor(pawnTint);
		mPlayer = player;
		mIndex = index;
		assetsProvider.addClient(this);
	}

	public void setPawnSelectable(boolean isPawnSelectable) {
		if (mIsPawnSelectable = isPawnSelectable) {
			mSelectableDeltaSum = 1;
		}
	}

	public boolean isPawnSelectable() {
		return mIsPawnSelectable;
	}

	public Player getPlayer() {
		return mPlayer;
	}

	public int getIndex() {
		return mIndex;
	}

	@Override
	public void render(Camera camera, SpriteBatch spriteBatch, float deltaTime) {
		if (mIsPawnSelectable) {
			float borderSize = 0.4f;
			borderSize = borderSize - 0.6f * borderSize * calculateDeltaFactor(deltaTime);
			spriteBatch.setColor(1, 1, 0.65f, 0.45f);
			spriteBatch.draw(mSelectedTextureRegion,
					getSprite().getX() - borderSize, getSprite().getY() - borderSize,
					getSprite().getWidth() + 2 * borderSize, getSprite().getHeight() + 2 * borderSize);
			spriteBatch.setColor(1, 1, 1, 1);
		}
		else if (mShotRadius > 0) {
			spriteBatch.setColor(1, 1, 1, 0.25f);
			spriteBatch.draw(mSelectedTextureRegion,
					getCenterX() - mShotRadius, getCenterY() - mShotRadius,
					2 * mShotRadius, 2 * mShotRadius);
			spriteBatch.setColor(1, 1, 1, 1);
		}
		super.render(camera, spriteBatch, deltaTime);
	}

	public void setDrawShotRange(float shotRadius) {
		mShotRadius = shotRadius;
	}

	@Override
	public void requestAssetsRefresh(AssetsProvider assetsProvider) {
		super.requestAssetsRefresh(assetsProvider);
		mSelectedTextureRegion = assetsProvider.getGradientCircleTexture();
	}

	private float calculateDeltaFactor(float deltaTime) {
		mSelectableDeltaSum += mSelectableDeltaSumDir * deltaTime * 0.65f;
		if (mSelectableDeltaSum >= 1) {
			mSelectableDeltaSum = 1;
			mSelectableDeltaSumDir = -1;
			return 1;
		}
		else if (mSelectableDeltaSum <= 0) {
			mSelectableDeltaSum = 0;
			mSelectableDeltaSumDir = 1;
			return 0;
		}
		return /* Interpolation.fade.apply( */ mSelectableDeltaSum - (int) mSelectableDeltaSum /* ) */;
	}
}

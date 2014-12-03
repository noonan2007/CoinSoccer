package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.samsung.android.sample.coinsoccer.game.Playground.PlaygroundSide;

public class GoalSensor extends RectangularArea {

	public static final short BOX2D_CATEGORY = 0x0002;

	public static final class BodyBuilder implements Disposable {

		private final World mWorld;
		private final BodyDef mBodyDef;
		private final PolygonShape mPolygonShape;
		private final FixtureDef mFixtureDef;

		public BodyBuilder(World world) {
			mWorld = world;
			mBodyDef = new BodyDef();
			mBodyDef.type = BodyType.StaticBody;
			mPolygonShape = new PolygonShape();
			mFixtureDef = new FixtureDef();
			mFixtureDef.shape = mPolygonShape;
			mFixtureDef.isSensor = true;
			mFixtureDef.filter.categoryBits = BOX2D_CATEGORY;
			mFixtureDef.filter.maskBits = BallPawn.BOX2D_CATEGORY;
		}

		public Body newBody(float leftX, float bottomY, float halfWidth, float halfHeight) {
			mBodyDef.position.set(leftX + halfWidth, bottomY + halfHeight);
			mPolygonShape.setAsBox(halfWidth, halfHeight);
			Body body = mWorld.createBody(mBodyDef);
			body.createFixture(mFixtureDef);
			return body;
		}

		@Override
		public void dispose() {
			mPolygonShape.dispose();
		}
	}

	private final PlaygroundHalf mPlaygroundHalf;
	private Body mBody;

	public GoalSensor(PlaygroundHalf playgroundHalf, float x, float y, float width,
			float height) {
		super(x, y, width, height);
		mPlaygroundHalf = playgroundHalf;
	}

	public void initBody(BodyBuilder bodyBuilder) {
		mBody = bodyBuilder.newBody(getLeftX(), getBottomY(), getWidth() / 2, getHeight() / 2);
		mBody.setUserData(this);
	}

	public Body getBody() {
		return mBody;
	}

	public PlaygroundHalf getPlaygroundHalf() {
		return mPlaygroundHalf;
	}

	public PlaygroundSide getPlaygroundSide() {
		return mPlaygroundHalf.getPlaygroundSide();
	}
}

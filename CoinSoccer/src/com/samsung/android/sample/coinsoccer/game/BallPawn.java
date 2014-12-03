package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class BallPawn extends BaseCoinPawn {

	public static final short BOX2D_CATEGORY = 0x0004;

	private static final float BALL_PAWN_BODY_ANGULAR_DAMPING = 3;
	private static final float BALL_PAWN_BODY_LINEAR_DAMPING = 3;
	private static final float BALL_PAWN_BODY_FRICTION = 0.6f;
	private static final float BALL_PAWN_BODY_DENSITY = 1.0f;
	private static final float BALL_PAWN_BODY_RESTITUTION = 1f;

	private static Body createBody(World world, float worldX, float worldY, float radius) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.bullet = true;
		bodyDef.allowSleep = true;
		bodyDef.angularDamping = BALL_PAWN_BODY_ANGULAR_DAMPING;
		bodyDef.linearDamping = BALL_PAWN_BODY_LINEAR_DAMPING;
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(radius);
		FixtureDef mFixtureDef = new FixtureDef();
		mFixtureDef.shape = circleShape;
		mFixtureDef.friction = BALL_PAWN_BODY_FRICTION;
		mFixtureDef.density = BALL_PAWN_BODY_DENSITY;
		mFixtureDef.restitution = BALL_PAWN_BODY_RESTITUTION;
		mFixtureDef.filter.categoryBits = BOX2D_CATEGORY;
		bodyDef.position.set(worldX, worldY);
		Body body = world.createBody(bodyDef);
		body.createFixture(mFixtureDef);
		circleShape.dispose();
		return body;
	}

	public BallPawn(AssetsProvider assetsProvider, World world,
			float centerX, float centerY, float radius) {
		super(createBody(world, centerX, centerY, radius), centerX, centerY, radius);
		initSnapshotsChain();
		assetsProvider.addClient(this);
	}
}

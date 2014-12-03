package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.samsung.android.sample.coinsoccer.game.AssetsProvider.AssetsProviderClient;

/**
 * Playground "root" class. It is responsible both for rendering the playground 
 * texture and keeping physical objects like goal sensors and playground borders.
 */
public class Playground extends RectangularArea implements CanBeRendered, AssetsProviderClient {

	public static final float BORDER_BODY_RESTITUTION = 0.55f;

	/**
	 * Identifies playground halves.
	 */
	public enum PlaygroundSide {

		NORTH {

			@Override
			public PlaygroundSide getOppositeSide() {
				return SOUTH;
			}
		},
		SOUTH {

			@Override
			public PlaygroundSide getOppositeSide() {
				return NORTH;
			}
		};

		public abstract PlaygroundSide getOppositeSide();
	}

	/**
	 * A helper class for creating playground border physical bodies.
	 */
	private static class BorderWallBodyBuilder {

		public Body newBody(World world, float centerX, float centerY, float leftEdgeX, float rightEdgeX,
				float bottomEdgeY, float topEdgeY, float goalLeftX, float goalRightX, float northGoalTopY,
				float southGoalBottomY, float cornerRadius) {

			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyType.StaticBody;
			ChainShape chainShape = new ChainShape();
			FixtureDef fixtureDef = new FixtureDef();

			bodyDef.position.set(centerX, centerY);
			Body body = world.createBody(bodyDef);

			float[] vertices = new float[] {

					// left border
					leftEdgeX, bottomEdgeY + cornerRadius,
					// top left cut corner
					leftEdgeX, topEdgeY - cornerRadius,
					// top left edge
					leftEdgeX + cornerRadius, topEdgeY,

					// outer borders of the north (top) goal
					goalLeftX, topEdgeY,
					goalLeftX, northGoalTopY,
					goalRightX, northGoalTopY,

					// top right edge
					goalRightX, topEdgeY,
					// top right cut corner
					rightEdgeX - cornerRadius, topEdgeY,
					// right border
					rightEdgeX, topEdgeY - cornerRadius,
					// bottom right cut corner
					rightEdgeX, bottomEdgeY + cornerRadius,
					// bottom right edge
					rightEdgeX - cornerRadius, bottomEdgeY,

					// outer borders of the south (bottom) goal
					goalRightX, bottomEdgeY,
					goalRightX, southGoalBottomY,
					goalLeftX, southGoalBottomY,

					// bottom left edge
					goalLeftX, bottomEdgeY,
					// bottom left cut corner
					leftEdgeX + cornerRadius, bottomEdgeY,
					leftEdgeX, bottomEdgeY + cornerRadius
			};
			for (int i = 0; i < vertices.length; i++) {
				vertices[i] -= i % 2 == 0 ? centerX : centerY;
			}
			chainShape.createChain(vertices);
			fixtureDef.shape = chainShape;
			fixtureDef.restitution = BORDER_BODY_RESTITUTION;
			body.createFixture(fixtureDef);
			chainShape.dispose();
			return body;
		}
	}

	private final RectangularArea mPlayableArea;
	private final PlaygroundHalf mNorthHalf;
	private final PlaygroundHalf mSouthHalf;
	private final float mHorizontalPadding;
	private final float mVerticalPadding;
	private final Body mBorderBody;
	private final TextureLineDrawer[] mTextureLineDrawers;
	private Texture mPlaygroundTexture;
	private TextureRegion mCircleLinesTexture;

	public Playground(float width, float height,
			float horizontalPadding, float verticalPadding,
			float goalWidth, float goalHeight,
			float goalAreaWidth, float goalAreaHeight, float ballRadius,
			AssetsProvider assetsProvider, World world) {
		super(-width / 2, -height / 2, width, height);
		final float ballDiameter = 2 * ballRadius;
		if (ballDiameter >= goalHeight) {
			throw new IllegalStateException("Ball diameter cannot be higher than goal height!!!");
		}
		mHorizontalPadding = horizontalPadding;
		mVerticalPadding = verticalPadding;
		mPlayableArea = new RectangularArea(getLeftX() + horizontalPadding,
				getBottomY() + verticalPadding, width - 2 * horizontalPadding,
				height - 2 * verticalPadding);
		mNorthHalf = new PlaygroundHalf(PlaygroundSide.NORTH, this,
				goalWidth, goalHeight, goalAreaWidth, goalAreaHeight,
				ballDiameter);
		mSouthHalf = new PlaygroundHalf(PlaygroundSide.SOUTH, this,
				goalWidth, goalHeight, goalAreaWidth, goalAreaHeight,
				ballDiameter);

		final GoalSensor.BodyBuilder goalSensorBuilder = new GoalSensor.BodyBuilder(world);
		mNorthHalf.initSensorBody(goalSensorBuilder);
		mSouthHalf.initSensorBody(goalSensorBuilder);
		goalSensorBuilder.dispose();

		// create border walls - for the time being it seems that the reference
		// to it is not needed
		final BorderWallBodyBuilder borderWallBodyBuilder = new BorderWallBodyBuilder();
		mBorderBody = borderWallBodyBuilder.newBody(world,
				getCenterX(), getCenterY(),
				mPlayableArea.getLeftX(), mPlayableArea.getRightX(),
				mPlayableArea.getBottomY(), mPlayableArea.getTopY(),
				mNorthHalf.getGoalInnerArea().getLeftX(),
				mNorthHalf.getGoalInnerArea().getRightX(),
				mNorthHalf.getGoalInnerArea().getTopY(),
				mSouthHalf.getGoalInnerArea().getBottomY(),
				ballDiameter);

		float lineThickness = 0.75f;
		mTextureLineDrawers = new TextureLineDrawer[] {
				new TextureLineDrawer(lineThickness, mPlayableArea.getLeftX(),
						getCenterY(), mPlayableArea.getWidth(), 0),
				new TextureLineDrawer(lineThickness, mPlayableArea.getLeftX(),
						mPlayableArea.getBottomY(), mPlayableArea.getHeight(), 90),
				new TextureLineDrawer(lineThickness, mPlayableArea.getLeftX(),
						mPlayableArea.getBottomY(), mPlayableArea.getWidth(), 0),
				new TextureLineDrawer(lineThickness, mPlayableArea.getRightX(),
						mPlayableArea.getTopY(), mPlayableArea.getHeight(), 270),
				new TextureLineDrawer(lineThickness, mPlayableArea.getRightX(),
						mPlayableArea.getTopY(), mPlayableArea.getWidth(), 180)
		};

		assetsProvider.addClient(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(Camera camera, SpriteBatch spriteBatch, float deltaTime) {
		// not needed to check if mPlaygroundTexture is null!!!
		// render is never called when AssetsProvider is not ready
		spriteBatch.draw(mPlaygroundTexture, getLeftX(), getBottomY(),
				getWidth(), getHeight());
		for (int i = 0; i < mTextureLineDrawers.length; i++) {
			mTextureLineDrawers[i].draw(spriteBatch);
		}
		float radius = mPlayableArea.getWidth() / 4;
		spriteBatch.draw(mCircleLinesTexture,
				getCenterX() - radius, getCenterY() - radius,
				2 * radius, 2 * radius);
		mNorthHalf.render(camera, spriteBatch, deltaTime);
		mSouthHalf.render(camera, spriteBatch, deltaTime);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestAssetsRefresh(AssetsProvider assetsProvider) {
		mPlaygroundTexture = assetsProvider.getPlaygroundBgTexture();
		mCircleLinesTexture = assetsProvider.getCircleLinesTexture();
		Texture longLineTexture = assetsProvider.getLongLineTexture();
		for (int i = 0; i < mTextureLineDrawers.length; i++) {
			mTextureLineDrawers[i].setTexture(longLineTexture);
		}
		TextureRegion pencilStartTexture = assetsProvider.getPencilStartTexture();
		TextureRegion pencilStretchableTexture = assetsProvider.getPencilStretchableTexture();
		TextureRegion pencilEndTexture = assetsProvider.getPencilEndTexture();
		mNorthHalf.setLineTexture(longLineTexture);
		mNorthHalf.setPencilTextures(pencilStartTexture, pencilStretchableTexture, pencilEndTexture);
		mSouthHalf.setLineTexture(longLineTexture);
		mSouthHalf.setPencilTextures(pencilStartTexture, pencilStretchableTexture, pencilEndTexture);
	}

	public PlaygroundHalf getPlaygroundHalf(PlaygroundSide side) {
		switch (side) {
			case NORTH:
				return mNorthHalf;
			case SOUTH:
				return mSouthHalf;
		}
		throw new IllegalArgumentException();
	}

	public RectangularArea getPlayableArea() {
		return mPlayableArea;
	}

	public float getHorizontalPadding() {
		return mHorizontalPadding;
	}

	public float getVerticalPadding() {
		return mVerticalPadding;
	}

	public Body getBorderBody() {
		return mBorderBody;
	}
}

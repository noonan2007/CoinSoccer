package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.samsung.android.sample.coinsoccer.game.GoalSensor.BodyBuilder;
import com.samsung.android.sample.coinsoccer.game.Playground.PlaygroundSide;

public class PlaygroundHalf extends RectangularArea implements CanBeRendered {

	private static final float[] PAWN_DEPLOYMENT_RATIOS_ON_ROUND_START = new float[] {
			0.5f, 0.1f, // goal keeper
			0.2f, 0.6f, 0.5f, 0.6f, 0.8f, 0.6f,
			0.6f, 0.8f, 0.4f, 0.9f
	};
	private static final float[] PAWN_DEPLOYMENT_RATIOS_ON_GAME_START = new float[] {
			0.25f, 0.5f, // goal keeper
			0.35f, 0.5f, 0.45f, 0.5f, 0.55f, 0.5f,
			0.65f, 0.5f, 0.75f, 0.5f
	};

	private final PlaygroundSide mPlaygroundSide;
	private final GoalSensor mGoalSensor;
	private final RectangularArea mGoalArea;
	private final RectangularArea mGoalInnerArea;
	private final Playground mPlayground;
	private final TextureLineDrawer[] mTextureLineDrawers;
	private final PencilLine[] mPencilLineDrawers;

	public PlaygroundHalf(PlaygroundSide playgroundSide, Playground playground,
			float goalWidth, float goalHeight, float goalAreaWidth, float goalAreaHeight,
			float ballDiameter) {
		super(playground.getPlayableArea().getLeftX(),
				playgroundSide == PlaygroundSide.SOUTH ?
						playground.getPlayableArea().getBottomY() :
						playground.getPlayableArea().getCenterY(),
				playground.getPlayableArea().getWidth(),
				playground.getPlayableArea().getHeight() / 2);
		mPlayground = playground;
		mPlaygroundSide = playgroundSide;
		final float goalSensorBottomY, goalInnerAreaBottomY, goalAreaBottomY;
		switch (playgroundSide) {
			case SOUTH:
				goalInnerAreaBottomY = getBottomY() - goalHeight;
				goalSensorBottomY = goalInnerAreaBottomY;
				goalAreaBottomY = getBottomY();
				break;
			case NORTH:
			default:
				goalInnerAreaBottomY = getTopY();
				goalSensorBottomY = goalInnerAreaBottomY + ballDiameter;
				goalAreaBottomY = getTopY() - goalAreaHeight;
				break;
		}
		
		mGoalInnerArea = new RectangularArea(getCenterX() - goalWidth / 2,
				goalInnerAreaBottomY, goalWidth, goalHeight);
		mGoalSensor = new GoalSensor(this, mGoalInnerArea.getLeftX(),
				goalSensorBottomY, goalWidth, goalHeight - ballDiameter);
		mGoalArea = new RectangularArea(getCenterX() - goalAreaWidth / 2,
				goalAreaBottomY, goalAreaWidth, goalAreaHeight);
		
		float lineThickness = 0.75f;
		float pencilLineThickness = 1.35f;
		float halfPencilLineThickness = pencilLineThickness / 2;
		float horizontalPencilLength = (getWidth() - mGoalInnerArea.getWidth()) / 2;
		switch (playgroundSide) {
			case SOUTH:
				mTextureLineDrawers = new TextureLineDrawer[] {
						new TextureLineDrawer(lineThickness, mGoalArea.getLeftX(), mGoalArea.getBottomY(),
								mGoalArea.getHeight(), 90),
						new TextureLineDrawer(lineThickness, mGoalArea.getRightX(), mGoalArea.getTopY(),
								mGoalArea.getWidth(), 180),
						new TextureLineDrawer(lineThickness, mGoalArea.getRightX(), mGoalArea.getTopY(),
								mGoalArea.getHeight(), 270)
				};
				mPencilLineDrawers = new PencilLine[] {
						new PencilLine(pencilLineThickness, getLeftX() - halfPencilLineThickness,
								getBottomY(), getHeight(), 90),
						new PencilLine(pencilLineThickness, getRightX() + halfPencilLineThickness,
								getBottomY(), getHeight(), 90),
						new PencilLine(pencilLineThickness, mGoalInnerArea.getLeftX(),
								mGoalInnerArea.getBottomY() - halfPencilLineThickness,
								mGoalInnerArea.getWidth(), 0),
						new PencilLine(pencilLineThickness,
								mGoalInnerArea.getLeftX() - halfPencilLineThickness,
								mGoalInnerArea.getTopY(),
								mGoalInnerArea.getHeight() + pencilLineThickness, 270),
						new PencilLine(pencilLineThickness,
								mGoalInnerArea.getRightX() + halfPencilLineThickness,
								mGoalInnerArea.getTopY(),
								mGoalInnerArea.getHeight() + pencilLineThickness, 270),
						new PencilLine(pencilLineThickness, getLeftX() - pencilLineThickness,
								getBottomY() - halfPencilLineThickness, horizontalPencilLength, 0),
						new PencilLine(pencilLineThickness, getRightX() + pencilLineThickness,
								getBottomY() - halfPencilLineThickness, horizontalPencilLength, 180)
				};
				break;

			case NORTH:
			default:
				mTextureLineDrawers = new TextureLineDrawer[] {
						new TextureLineDrawer(lineThickness, mGoalArea.getLeftX(), mGoalArea.getBottomY(),
								mGoalArea.getWidth(), 0),
						new TextureLineDrawer(lineThickness, mGoalArea.getLeftX(), mGoalArea.getBottomY(),
								mGoalArea.getHeight(), 90),
						new TextureLineDrawer(lineThickness, mGoalArea.getRightX(), mGoalArea.getTopY(),
								mGoalArea.getHeight(), 270)
				};
				mPencilLineDrawers = new PencilLine[] {
						new PencilLine(pencilLineThickness, getLeftX() - halfPencilLineThickness,
								getTopY(), getHeight(), 270),
						new PencilLine(pencilLineThickness, getRightX() + halfPencilLineThickness,
								getTopY(), getHeight(), 270),
						new PencilLine(pencilLineThickness, mGoalInnerArea.getLeftX(),
								mGoalInnerArea.getTopY() + halfPencilLineThickness,
								mGoalInnerArea.getWidth(), 0),
						new PencilLine(pencilLineThickness,
								mGoalInnerArea.getLeftX() - halfPencilLineThickness,
								mGoalInnerArea.getBottomY(),
								mGoalInnerArea.getHeight() + pencilLineThickness, 90),
						new PencilLine(pencilLineThickness,
								mGoalInnerArea.getRightX() + halfPencilLineThickness,
								mGoalInnerArea.getBottomY(),
								mGoalInnerArea.getHeight() + pencilLineThickness, 90),
						new PencilLine(pencilLineThickness, getLeftX() - pencilLineThickness,
								getTopY() + halfPencilLineThickness, horizontalPencilLength, 0),
						new PencilLine(pencilLineThickness, getRightX() + pencilLineThickness,
								getTopY() + halfPencilLineThickness, horizontalPencilLength, 180)
				};
				break;
		}
	}

	public PlaygroundSide getPlaygroundSide() {
		return mPlaygroundSide;
	}

	public void initSensorBody(BodyBuilder goalSensorBuilder) {
		mGoalSensor.initBody(goalSensorBuilder);
	}

	public GoalSensor getGoalSensor() {
		return mGoalSensor;
	}

	public RectangularArea getGoalInnerArea() {
		return mGoalInnerArea;
	}

	public RectangularArea getGoalArea() {
		return mGoalArea;
	}

	public boolean isFlippedX() {
		return mPlaygroundSide == PlaygroundSide.SOUTH;
	}

	public boolean isFlippedY() {
		return mPlaygroundSide == PlaygroundSide.SOUTH;
	}

	public Playground getPlayground() {
		return mPlayground;
	}

	@Override
	public void render(Camera camera, SpriteBatch spriteBatch, float deltaTime) {
		for (int i = 0; i < mTextureLineDrawers.length; i++) {
			mTextureLineDrawers[i].draw(spriteBatch);
		}
		for (int i = 0; i < mPencilLineDrawers.length; i++) {
			mPencilLineDrawers[i].draw(spriteBatch);
		}
	}

	public void setLineTexture(Texture lineTexture) {
		for (int i = 0; i < mTextureLineDrawers.length; i++) {
			mTextureLineDrawers[i].setTexture(lineTexture);
		}
	}

	public void setPencilTextures(TextureRegion start,
			TextureRegion stretchable, TextureRegion end) {
		for (int i = 0; i < mPencilLineDrawers.length; i++) {
			mPencilLineDrawers[i].setTextures(start, stretchable, end);
		}
	}

	public void readStartPlayerPawnPosition(int pawnIndex, Vector2 target) {
		readPawnPositionFromRatios(PAWN_DEPLOYMENT_RATIOS_ON_GAME_START,
				pawnIndex, target);
	}

	public void readDefaultPlayerPawnPosition(int pawnIndex, Vector2 target) {
		readPawnPositionFromRatios(PAWN_DEPLOYMENT_RATIOS_ON_ROUND_START,
				pawnIndex, target);
	}

	public int getPlayerPawnPositionsCount() {
		return PAWN_DEPLOYMENT_RATIOS_ON_GAME_START.length / 2;
	}

	private void readPawnPositionFromRatios(float[] ratios, int pawnIndex,
			Vector2 target) {
		int i = 2 * pawnIndex;
		target.x = isFlippedX() ?
				getLeftX() + ratios[i] * getWidth() : 
					getRightX() - ratios[i] * getWidth();
		target.y = isFlippedY() ? 
				getBottomY() + ratios[i + 1] * getHeight() : 
					getTopY() - ratios[i + 1] * getHeight();
	}
}

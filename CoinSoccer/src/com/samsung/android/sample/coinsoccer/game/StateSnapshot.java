package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Describes the position and rotation of {@link BaseCoinPawn} in box2d {@link World}.
 * This class is used to track the important points in history of paws positions.
 * It can also be easily re-applied to any {@link BaseCoinPawn}.
 */
public class StateSnapshot implements Poolable {

	public static StateSnapshot obtain(BaseCoinPawn pawn) {
		return obtain(pawn, true);
	}

	public static StateSnapshot obtain(BaseCoinPawn pawn, boolean setToCurrentPawnState) {
		StateSnapshot object = sPool.obtain();
		if (setToCurrentPawnState) {
			object.setToCurrentPawnState(pawn);
		}
		return object;
	}

	public static StateSnapshot obtainCopy(StateSnapshot stateSnapshot) {
		StateSnapshot object = sPool.obtain();
		object.set(stateSnapshot.getCenterX(), stateSnapshot.getCenterY(),
				stateSnapshot.getAngle());
		return object;
	}

	private static final Pool<StateSnapshot> sPool = new Pool<StateSnapshot>() {

		@Override
		protected StateSnapshot newObject() {
			return new StateSnapshot();
		}
	};

	float mCenterX;
	float mCenterY;
	float mAngle;

	protected StateSnapshot() {}

	/**
	 * 
	 */
	public float getCenterX() {
		return mCenterX;
	}

	/**
	 * 
	 */
	public float getCenterY() {
		return mCenterY;
	}

	/**
	 * See {@link Body#getAngle()}
	 * 
	 * @return
	 */
	public float getAngle() {
		return mAngle;
	}

	public void free() {
		sPool.free(this);
	}

	@Override
	public void reset() {
		mAngle = mCenterX = mCenterY = 0;
	}
	
	protected void set(float centerX, float centerY, float angle) {
		mCenterX = centerX;
		mCenterY = centerY;
		mAngle = angle;
	}

	protected void setToCurrentPawnState(BaseCoinPawn pawn) {
		Body body = pawn.getBody();
		set(body.getPosition().x, body.getPosition().y, body.getAngle());
	}
}
package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class FuturePawnPosition extends PawnStateSnapshot implements Poolable {

	public static FuturePawnPosition obtain(BaseCoinPawn pawn) {
		return obtain(pawn, true);
	}

	public static FuturePawnPosition obtain(BaseCoinPawn pawn, boolean setToCurrentPawnState) {
		FuturePawnPosition object = sPool.obtain();
		object.mPawn = pawn;
		if (setToCurrentPawnState) {
			object.setToCurrentPawnState(pawn);
		}
		return object;
	}

	public static FuturePawnPosition obtainCopy(PawnStateSnapshot source) {
		FuturePawnPosition object = sPool.obtain();
		object.mPawn = source.mPawn;
		object.mAngle = source.mAngle;
		object.mCenterX = source.mCenterX;
		object.mCenterY = source.mCenterY;
		return object;
	}
	
	private static final Pool<FuturePawnPosition> sPool = new Pool<FuturePawnPosition>() {

		@Override
		protected FuturePawnPosition newObject() {
			return new FuturePawnPosition();
		}
	};

	protected FuturePawnPosition() {}

	public void setCenterX(float centerX) {
		mCenterX = centerX;
	}

	public void setCenterY(float centerY) {
		mCenterY = centerY;
	}

	public void setAngle(float angle) {
		mAngle = angle;
	}

	@Override
	public void free() {
		sPool.free(this);
	}
}
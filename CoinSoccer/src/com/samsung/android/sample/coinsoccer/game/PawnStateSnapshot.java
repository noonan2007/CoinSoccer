package com.samsung.android.sample.coinsoccer.game;

public abstract class PawnStateSnapshot extends StateSnapshot implements ICircle {

	protected BaseCoinPawn mPawn;

	@Override
	public float getRadius() {
		return mPawn.getRadius();
	}
	
	public BaseCoinPawn getPawn() {
		return mPawn;
	}

	@Override
	public void reset() {
		mPawn = null;
		super.reset();
	}

	@Override
	public void free() {
		throw new IllegalStateException("PawnStateSnapshot is not Poolable");
	}
}

package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.utils.Pool;

/**
 * A {@link StateSnapshot} whose instance can be linked together with other 
 * instances into a one-way chain. Each node in chain is aware only of its 
 * predecessors but not successors.
 * This class uses {@link Pool} to manage its instances. 
 */
public class ChainableStateSnapshot extends PawnStateSnapshot {

	public static ChainableStateSnapshot obtain(BaseCoinPawn pawn) {
		return obtain(pawn, true);
	}

	/**
	 * 
	 * @param pawn
	 * @return
	 */
	public static ChainableStateSnapshot obtain(BaseCoinPawn pawn, boolean setToCurrentPawnState) {
		ChainableStateSnapshot object = sPool.obtain();
		object.mPawn = pawn;
		if (setToCurrentPawnState) {
			object.setToCurrentPawnState(pawn);
		}
		return object;
	}

	public static ChainableStateSnapshot obtain(BaseCoinPawn pawn,
			float centerX, float centerY, float angle) {
		ChainableStateSnapshot object = sPool.obtain();
		object.mPawn = pawn;
		object.set(centerX, centerY, angle);
		return object;
	}

	private static final Pool<ChainableStateSnapshot> sPool = new Pool<ChainableStateSnapshot>() {

		@Override
		protected ChainableStateSnapshot newObject() {
			return new ChainableStateSnapshot();
		}
	};

	private ChainableStateSnapshot mPrevious;
	private boolean mIsIninitalNode;
	
	ChainableStateSnapshot() {}

	/**
	 * 
	 */
	public void initChain() {
		if (mPrevious != null) {
			throw new IllegalStateException(
					"Node who has predecessors cannot be marked as chain start!!!");
		}
		mIsIninitalNode = true;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isInitialNode() {
		return mIsIninitalNode;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isInChain() {
		return hasPrevious() || isInitialNode();
	}

	/**
	 * It returns previous {@link ChainableStateSnapshot} in a chain or null if there is none.
	 * 
	 * @return previous {@link ChainableStateSnapshot} in a chain or null if there is none
	 */
	public ChainableStateSnapshot getPrevious() {
		return mPrevious;
	}

	/**
	 * 
	 * @param stepsBack
	 * @return
	 */
	public ChainableStateSnapshot getPrevious(int stepsBack) {
		return stepsBack == 1 ? mPrevious : stepsBack > 1 ? getPrevious(stepsBack - 1) : null;
	}

	/**
	 * 
	 * @return
	 */
	public ChainableStateSnapshot getInitialSnapshot() {
		return mPrevious == null ? this : mPrevious.getInitialSnapshot();
	}

	/**
	 * It appends an instance
	 * 
	 * @param next
	 */
	public void append(ChainableStateSnapshot next) {
		if (mPrevious == null && !mIsIninitalNode) {
			throw new IllegalStateException("Current node seems not be a part of chain!!!");
		}
		if (next.mIsIninitalNode) {
			next.mIsIninitalNode = false;
		}
		next.mPrevious = this;
	}

	/**
	 * It returns count of previous nodes in the chain starting from this instance. 
	 * It returns 0 if given node is the first node in chain.
	 * 
	 * @return count of previous nodes in the chain or 0 if this is the first node in chain.
	 */
	public int getPreviousCount() {
		return mPrevious == null ? 0 : 1 + mPrevious.getPreviousCount();
	}

	/**
	 * Returns true if given node has a previous node
	 * 
	 * @return true if the node has a previous node.
	 */
	public boolean hasPrevious() {
		return mPrevious != null;
	}

	/**
	 * It detaches given node from the preceding nodes.
	 * The detached node does not get the "initial node" status.
	 * You may turn it on manually if you need it form another chain.
	 */
	public void detachFromPrevious() {
		if (mPrevious == null) {
			if (mIsIninitalNode) {
				mIsIninitalNode = false;
			}
		}
		else {
			mPrevious = null;
		}
	}

	/**
	 * It cuts of all previous {@link ChainableStateSnapshot} nodes in chain except for the first one. 
	 * As this also make the removed instance to be recycled into the pool {@link #sPool} so they must 
	 * not be used in any other part of code after they have been removed from chain.
	 */
	public void cutOffPreviousUntilTheInitialNode() {
		// do not allow to remove the first snapshot in chain - it will be the initial state of our pawns
		if (mPrevious != null && !mPrevious.mIsIninitalNode) {
			ChainableStateSnapshot free, prev = mPrevious;
			mPrevious = getInitialSnapshot();
			while (prev != null && prev != mPrevious) {
				free = prev;
				prev = prev.mPrevious;
				sPool.free(free);
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public void free() {
		sPool.free(this);
	}

	@Override
	public void reset() {
		mPrevious = null;
		mIsIninitalNode = false;
		super.reset();
	}
}
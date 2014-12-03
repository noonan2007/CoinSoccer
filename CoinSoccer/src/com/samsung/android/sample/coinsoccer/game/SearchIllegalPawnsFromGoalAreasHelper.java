package com.samsung.android.sample.coinsoccer.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Helper class to calculate future positions o pawns in illegal positions.
 */
public class SearchIllegalPawnsFromGoalAreasHelper implements IHasElements<PawnStateSnapshot> {

	private interface PositionChecker {

		boolean isLegalPosition(ICircle pawnPosition);
	}

	private static final class NotInGoalAreasChecker implements PositionChecker {

		PlaygroundHalf mHalf;

		@Override
		public boolean isLegalPosition(ICircle pawnPosition) {
			if (isPawnPositionInside(pawnPosition, mHalf.getPlayground().getPlayableArea())) {
				if (isPawnPositionInside(pawnPosition, mHalf.getGoalArea())) {
					return false;
				}
				if (isPawnPositionInside(pawnPosition, mHalf.getGoalInnerArea())) {
					return false;
				}
				return true;
			}
			return false;
		}
	};

	private final NotInGoalAreasChecker mNotInGoalAreasChecker = new NotInGoalAreasChecker();
	private final Comparator<ICircle> sComparator = new Comparator<ICircle>() {

		@Override
		public int compare(ICircle lhs, ICircle rhs) {
			float diff = lhs.getCenterX() - rhs.getCenterX();
			return diff > 0 ? 1 : diff < 0 ? -1 : 0;
		}
	};
	
	private final List<FuturePawnPosition> mFuturePositions = new ArrayList<FuturePawnPosition>();
	private final List<BaseCoinPawn> mPawnsToMove = new ArrayList<BaseCoinPawn>();
	private final List<BaseCoinPawn> mPawnsKeepingPosition = new ArrayList<BaseCoinPawn>();
	private final List<ICircle> mFiltered = new ArrayList<ICircle>();
	private final List<ICircle> mFilteredReverseOrder = new ArrayList<ICircle>();
	private final List<ChainableStateSnapshot> mHistorySnapshots = new ArrayList<ChainableStateSnapshot>();

	/**
	 * It clears cached results if there are any. 
	 * After calling this method {@link #getCount()} should return 0.
	 * However it is not needed to call this method before calling
	 * {@link #searchPawnsToBeRemovedFromGoalAreas()} method, as that 
	 * calls {@link #freeAll()} internally.
	 */
	public void freeAll() {
		mPawnsToMove.clear();
		mHistorySnapshots.clear();
		mPawnsKeepingPosition.clear();
		mFiltered.clear();
		mFilteredReverseOrder.clear();
		for (int i = 0; i < mFuturePositions.size(); i++) {
			mFuturePositions.get(i).free();
		}
		mFuturePositions.clear();
	}

	/**
	 * Main "worker" method. It searches for all {@link PlayerPawn} of the current 
	 * {@link Player} of the {@link CoinSoccerGame} passed in the constructor, which 
	 * are in the goal area or in the goal inner area. For those of them which are considered
	 * to be there "illegaly" the future legal {@link FuturePawnPosition} is calculated.
	 * The calculated "future positions" of pawn can be then accessed with {@link #getCount()}
	 * and {@link #get(int)} methods.
	 */
	public void recalculate(CoinSoccerGame game) {

		freeAll();

		final Player currentPlayer = game.getPlayers().getActivePlayer();

		// find pawns inside goal area and goal inner area
		final PlaygroundHalf half = game.getPlayground().getPlaygroundHalf(
				currentPlayer.getPlaygroundSide());
		final RectangularArea goalInnerArea = half.getGoalInnerArea();
		final RectangularArea goalArea = half.getGoalArea();

		for (int i = 0; i < currentPlayer.getCount(); i++) {
			PlayerPawn pawn = currentPlayer.get(i);
			if (isPawnPositionInside(pawn, goalArea) || isPawnPositionInside(pawn, goalInnerArea)) {
				mPawnsToMove.add(pawn);
			}
			else {
				mPawnsKeepingPosition.add(pawn);
			}
		}

		if (mPawnsToMove.size() > 0) {
			final boolean yDown = half.isFlippedY();
			mNotInGoalAreasChecker.mHalf = half;

			// exclude the oldest pawn inside goal area
			BaseCoinPawn oldestPawnInGoalArea = getPawnRemainingInGoalArea(
					mPawnsToMove, goalArea, goalInnerArea);
			mPawnsToMove.remove(oldestPawnInGoalArea);

			Player oppositePlayer = game.getPlayers().getOppositePlayer(currentPlayer);
			for (int i = 0; i < oppositePlayer.getCount(); i++) {
				mPawnsKeepingPosition.add(oppositePlayer.get(i));
			}

			BallPawn ballPawn = game.getBallPawn();
			mPawnsKeepingPosition.add(ballPawn);

			// negotiate pawns future positions for found pawns
			for (BaseCoinPawn pawn : mPawnsToMove) {
				FuturePawnPosition futurePos = getFuturePos(pawn, goalArea, yDown);
				adjustPosition(futurePos, mPawnsKeepingPosition, mFuturePositions,
						yDown, mNotInGoalAreasChecker);
				mFuturePositions.add(futurePos);
			}

			// if oldest pawn is inside inner goal area propose position inside goal area
			// and ball is not overlapping goal area nor goal inner area
			if (isPawnPositionInside(oldestPawnInGoalArea, goalInnerArea)
					&& !CoordsUtils.overlaps(ballPawn, goalInnerArea)
					&& !CoordsUtils.overlaps(ballPawn, goalArea)) {
				FuturePawnPosition futurePos = getFuturePos(oldestPawnInGoalArea,
						goalInnerArea, yDown);
				adjustPosition(futurePos, mPawnsKeepingPosition, mFuturePositions,
						yDown, null);
				mFuturePositions.add(futurePos);
			}
		}
	}

	/**
	 * Returns the size of results produced by the last of 
	 * {@link #searchPawnsToBeRemovedFromGoalAreas()}.
	 * You can then use {@link #get(int)} method to get a 
	 * result item for a particular index. 
	 * 
	 * @return count of suggested {@link FuturePawnPosition} produced by the last call of
	 *         {@link #searchPawnsToBeRemovedFromGoalAreas()}
	 */
	@Override
	public int getCount() {
		return mFuturePositions.size();
	}

	/**
	 * This returns a particular {@link StateSnapshot} at given index. It should be called after
	 * {@link #searchPawnsToBeRemovedFromGoalAreas()} method has been called and prior to calling {@link #freeAll()}
	 * 
	 * @param index
	 *            index of {@link StateSnapshot} result item to get
	 * @return {@link StateSnapshot} at given index produced by last search with use of
	 *         {@link #searchPawnsToBeRemovedFromGoalAreas()}
	 */
	@Override
	public PawnStateSnapshot get(int index) {
		return mFuturePositions.get(index);
	}

	private BaseCoinPawn getPawnRemainingInGoalArea(List<BaseCoinPawn> pawnsToMove,
			RectangularArea goalArea, RectangularArea goalInnerArea) {
		int count = pawnsToMove.size();
		if (count == 1) {
			return pawnsToMove.get(0);
		}
		if (count > 0) {
			for (BaseCoinPawn pawn : pawnsToMove) {
				mHistorySnapshots.add(pawn.getSnapshot());
			}
			ListIterator<ChainableStateSnapshot> li = mHistorySnapshots.listIterator();
			ChainableStateSnapshot snapshot;
			while (mHistorySnapshots.size() > 1) {
				while (li.hasNext()) {
					snapshot = li.next();
					if (!isPawnPositionInside(snapshot, goalArea) &&
							!isPawnPositionInside(snapshot, goalInnerArea)) {
						li.remove();
						if (mHistorySnapshots.size() == 1) {
							return mHistorySnapshots.get(0).getPawn();
						}
					}
				}
				while (li.hasPrevious()) {
					snapshot = li.previous();
					if (snapshot.hasPrevious()) {
						li.set(snapshot.getPrevious());
					}
					else {
						li.remove();
						if (mHistorySnapshots.size() == 1) {
							return mHistorySnapshots.get(0).getPawn();
						}
					}
				}
			}
		}
		throw new IllegalStateException("List of pawns should not be empty!!!");
	}

	private FuturePawnPosition getFuturePos(BaseCoinPawn pawn,
			RectangularArea leavingArea, boolean yDown) {
		FuturePawnPosition pos = FuturePawnPosition.obtain(pawn, false);
		pos.setAngle(pawn.getAngle());
		pos.setCenterX(pawn.getCenterX());
		pos.setCenterY(yDown ?
				leavingArea.getTopY() + pawn.getRadius() :
				leavingArea.getBottomY() - pawn.getRadius());
		return pos;
	}

	private boolean adjustPosition(FuturePawnPosition testedPosition,
			Iterable<? extends ICircle> otherPawns,
			Iterable<? extends ICircle> confirmedFuturePositions,
			boolean flipY, PositionChecker positionChecker) {
		float yStep = 2 * testedPosition.getRadius();
		if (flipY) {
			yStep = -yStep;
		}
		for (int i = 0; i < 5; i++) {
			if (adjustPositionInLine(testedPosition, otherPawns,
					confirmedFuturePositions, positionChecker)) {
				return true;
			}
			else {
				testedPosition.setCenterY(testedPosition.getCenterY() + yStep);
			}
		}
		return false;
	}

	private boolean adjustPositionInLine(FuturePawnPosition pos,
			Iterable<? extends ICircle> otherPawns,
			Iterable<? extends ICircle> confirmedFuturePositions,
			PositionChecker positionChecker) {

		// filter out pawns which cannot overlap
		float yDown = pos.getCenterY() - pos.getRadius(), yUp = pos.getCenterY() + pos.getRadius();
		for (ICircle pawnPosition : otherPawns) {
			if (isBetweenY(pawnPosition, yDown, yUp)) {
				mFiltered.add(pawnPosition);
			}
		}
		for (ICircle pawnPosition : confirmedFuturePositions) {
			if(isBetweenY(pawnPosition, yDown, yUp)) {
				mFiltered.add(pawnPosition);
			}
		}

		// sort filtered from in growing order
		Collections.sort(mFiltered, sComparator);

		// sort in reverse order for the second check
		for (int i = mFiltered.size() - 1; i >= 0; i--) {
			mFilteredReverseOrder.add(mFiltered.get(i));
		}

		FuturePawnPosition rightPos = FuturePawnPosition.obtainCopy(pos);
		boolean isRightPosValid = negotiatePositionRecursive(
				rightPos, mFiltered, 1, positionChecker);

		if (isRightPosValid && rightPos.getCenterX() == pos.getCenterX()) {
			// if the position is the same as the testedPosition - no need to test "on the other side"
			rightPos.free();
			return true;
		}

		FuturePawnPosition leftPos = FuturePawnPosition.obtainCopy(pos);
		boolean isLeftPosValid = negotiatePositionRecursive(
				leftPos, mFilteredReverseOrder, -1, positionChecker);

		if(isRightPosValid) {
			if(isLeftPosValid) {
				// both found positions are valid - select the one which is closer
				float leftDiff = pos.getCenterX() - leftPos.getCenterX();
				float rightDiff = rightPos.getCenterX() - pos.getCenterX();
				pos.setCenterX(leftDiff < rightDiff ? 
						leftPos.getCenterX() : rightPos.getCenterX());
			}
			else {
				pos.setCenterX(rightPos.getCenterX());
			}
		}
		else if (isLeftPosValid) {
			pos.setCenterX(leftPos.getCenterX());
		}
		rightPos.free();
		leftPos.free();
		return isRightPosValid || isLeftPosValid;
	}

	private boolean negotiatePositionRecursive(FuturePawnPosition pos,
			List<ICircle> list, int dirFactor, PositionChecker positionChecker) {
		if (list.size() == 0) {
			return true;
		}
		ICircle potentialObstacle, actualObstacle = null;
		Iterator<ICircle> it = list.iterator();
		while (it.hasNext()) {
			potentialObstacle = it.next();
			it.remove();
			if (CoordsUtils.overlaps(pos, potentialObstacle)) {
				actualObstacle = potentialObstacle;
				break;
			}
		}
		if (actualObstacle == null) {
			return true;
		}
		float xMove = actualObstacle.getRadius() + pos.getRadius();
		pos.setCenterX(actualObstacle.getCenterX() + dirFactor * xMove);
		if (positionChecker != null && !positionChecker.isLegalPosition(pos)) {
			return false;
		}
		return negotiatePositionRecursive(pos, list, dirFactor, positionChecker);
	}

	private static boolean isBetweenY(ICircle pawnPosition, float yDown, float yUp) {
		float cYMin = pawnPosition.getCenterY() - pawnPosition.getRadius();
		if (cYMin >= yDown && cYMin <= yUp) {
			return true;
		}
		else {
			float cYMax = pawnPosition.getCenterY() + pawnPosition.getRadius();
			if (cYMax >= yDown && cYMax <= yUp) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPawnPositionInside(ICircle pawnPosition, RectangularArea area) {
		return area.contains(pawnPosition.getCenterX(), pawnPosition.getCenterY());
	}
}

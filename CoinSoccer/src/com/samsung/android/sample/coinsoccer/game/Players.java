package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.samsung.android.sample.coinsoccer.game.Playground.PlaygroundSide;
import com.samsung.android.sample.coinsoccer.settings.Which;

public class Players implements CanBeRendered {

	private final Player mFirstPlayer;
	private final Player mSecondPlayer;
	private Which mWhichIsActive;

	public Players(CoinSoccerGame game, float playerPawnRadius) {
		final PlayerPawn.BodyBuilder playerPawnBuilder = new PlayerPawn.BodyBuilder(
				game.getWorld(), playerPawnRadius);
		mFirstPlayer = new Player(game, game.getFirstPlayerSettings(),
				PlaygroundSide.NORTH, playerPawnBuilder);
		mSecondPlayer = new Player(game, game.getSecondPlayerSettings(),
				PlaygroundSide.SOUTH, playerPawnBuilder);
		playerPawnBuilder.dispose();
	}

	/**
	 * Changes current player to the opposite one.
	 */
	public void swapActivePlayer() {
		mWhichIsActive = getWhichIsInactivePlayer();
	}

	/**
	 * Returns reference to current player.
	 * 
	 * @return reference to current player
	 */
	public Player getActivePlayer() {
		return getPlayer(mWhichIsActive);
	}

	/**
	 * 
	 * @return
	 */
	public Player getInactivePlayer() {
		return getOppositePlayer(getActivePlayer());
	}

	/**
	 * 
	 * @return
	 */
	public Which getWhichIsActivePlayer() {
		return mWhichIsActive;
	}

	/**
	 * 
	 * @return
	 */
	public Which getWhichIsInactivePlayer() {
		return Which.getTheOppositeOne(mWhichIsActive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(Camera camera, SpriteBatch spriteBatch, float deltaTime) {
		mFirstPlayer.render(null, spriteBatch, deltaTime);
		mSecondPlayer.render(null, spriteBatch, deltaTime);
	}

	/**
	 * 
	 * @return if at least one
	 */
	public boolean isAtLeastOnePlayerPawnMoving() {
		return mFirstPlayer.isAtLeastOnePlayerMoving() || mSecondPlayer.isAtLeastOnePlayerMoving();
	}

	/**
	 * Updates sprites of all player pawns to its models in box2d. 
	 * See also {@link Player#updatePawnSpritesToPhysics()}.
	 */
	public void updatePawnSpritesToPhysics() {
		mFirstPlayer.updatePawnSpritesToPhysics();
		mSecondPlayer.updatePawnSpritesToPhysics();
	}

	/**
	 * Gets reference to player which is defending goal on given {@link PlaygroundSide}.
	 * 
	 * @param playgroundSide
	 *            {@link PlaygroundSide} on which the goal being defended is located
	 * @return player which is defending goal on given {@link PlaygroundSide}
	 */
	public Player getDefendingPlayer(PlaygroundSide playgroundSide) {
		return mFirstPlayer.getPlaygroundSide() == playgroundSide ?
				mFirstPlayer : mSecondPlayer;
	}

	/**
	 * Gets reference to player which is attacking goal on given {@link PlaygroundSide}.
	 * 
	 * @param playgroundSide
	 *            {@link PlaygroundSide} on which the goal being defended is located
	 * @return player which is attacking goal on given {@link PlaygroundSide}
	 */
	public Player getAttackingPlayer(PlaygroundSide playgroundSide) {
		return getOppositePlayer(getDefendingPlayer(playgroundSide));
	}

	/**
	 * Gets the opponent of given player.
	 * 
	 * @param player
	 *            player whose opponent is queried for
	 * @return opponent player
	 */
	public Player getOppositePlayer(Player player) {
		return getPlayer(Which.getTheOppositeOne(player.getWhich()));
	}

	/**
	 * Gets reference to player object associated with given index.
	 * 
	 * @param whichPlayer
	 * @return reference to player object associated with given index
	 */
	public Player getPlayer(Which whichPlayer) {
		switch (whichPlayer) {
			case FIRST:
				return mFirstPlayer;
			case SECOND:
				return mSecondPlayer;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	/**
	 * Sets current player by index.
	 * 
	 * @param activePlayer
	 *            index of player (0 or 1)
	 */
	public void setActivePlayer(Which activePlayer) {
		switch (activePlayer) {
			case FIRST:
			case SECOND:
				mWhichIsActive = activePlayer;
				break;

			default:
				throw new ArrayIndexOutOfBoundsException();
		}
	}

	public void savePawnsPositions() {
		mFirstPlayer.savePawnsPositions();
		mSecondPlayer.savePawnsPositions();
	}

	public void stopAllPawns() {
		mFirstPlayer.stopAllPawns();
		mSecondPlayer.stopAllPawns();
	}
}

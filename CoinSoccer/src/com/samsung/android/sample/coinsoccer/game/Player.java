package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.samsung.android.sample.coinsoccer.game.PlayerPawn.BodyBuilder;
import com.samsung.android.sample.coinsoccer.game.Playground.PlaygroundSide;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.android.sample.coinsoccer.settings.Which;
import com.samsung.android.sample.coinsoccer.statistics.PlayerStatistics;

public class Player implements CanBeRendered, IHasElements<PlayerPawn> {

	private final InputProcessor mPawnSelectionInputProcessor = new InputAdapter() {

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			if (!isGamePaused()) {
				final PlayerPawn touchedPawn = getPawnAtScreenCoords(screenX, screenY);
				if (touchedPawn != null) {
					onPawnTouchDown(touchedPawn, screenX, screenY);
					return true;
				}
			}
			return false;
		}
	};

	private final CoinSoccerGame mGame;
	private final PlayerPawn[] mPawns;
	private final PlaygroundSide mPlaygroundSide;
	private final PlayerSettings mPlayerSettings;
	private boolean mIsInPawnSelectionState;

	Player(CoinSoccerGame game, PlayerSettings playerSettings,
			PlaygroundSide playgroundSide, BodyBuilder pawnBodyBuilder) {
		mPlayerSettings = playerSettings;
		mGame = game;
		mPlaygroundSide = playgroundSide;
		PlaygroundHalf half = getPlaygroundHalf();
		mPawns = new PlayerPawn[half.getPlayerPawnPositionsCount()];
		Vector2 vector = new Vector2();
		Color color = PlayerSettings.createGdxColor(playerSettings.color);
		for (int i = 0; i < mPawns.length; i++) {
			half.readStartPlayerPawnPosition(i, vector);
			mPawns[i] = new PlayerPawn(game.getAssetsProvider(), this, i,
					pawnBodyBuilder, vector.x, vector.y, color);
			half.readDefaultPlayerPawnPosition(i, vector);
			mPawns[i].initSnapshotsChain(vector.x, vector.y, 0);
		}
	}

	public void initPawnSelection() {
		if (!mIsInPawnSelectionState) {
			mGame.addProcessor(0, mPawnSelectionInputProcessor);
			for (PlayerPawn p : mPawns) {
				p.setPawnSelectable(true);
			}
			mIsInPawnSelectionState = true;
		}
	}

	public void disablePawnSelection() {
		if (mIsInPawnSelectionState) {
			mGame.removeProcessor(mPawnSelectionInputProcessor);
			for (PlayerPawn p : mPawns) {
				p.setPawnSelectable(false);
			}
			mIsInPawnSelectionState = false;
		}
	}

	public void updatePawnSpritesToPhysics() {
		for (PlayerPawn p : mPawns) {
			p.updateSpriteToBody();
		}
	}

	public PlayerPawn getPawnAtScreenCoords(int screenX, int screenY) {
		Vector2 vector = new Vector2(screenX, screenY);
		mGame.getGameCameraManager().screenToWorld(vector);
		return getPawnAtStageCoords(vector.x, vector.y);
	}

	public PlayerPawn getPawnAtStageCoords(float stageX, float stageY) {
		for (PlayerPawn p : mPawns) {
			if (p.hitTest(stageX, stageY, false)) {
				return p;
			}
		}
		return null;
	}

	public PlaygroundSide getPlaygroundSide() {
		return mPlaygroundSide;
	}

	public PlaygroundHalf getPlaygroundHalf() {
		return mGame.getPlayground().getPlaygroundHalf(mPlaygroundSide);
	}

	public boolean isAtLeastOnePlayerMoving() {
		for (PlayerPawn p : mPawns) {
			if (p.isMoving()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void render(Camera camera, SpriteBatch spriteBatch, float deltaTime) {
		for (PlayerPawn p : mPawns) {
			p.render(null, spriteBatch, deltaTime);
		}
	}

	@Override
	public int getCount() {
		return mPawns.length;
	}

	@Override
	public PlayerPawn get(int index) {
		return mPawns[index];
	}

	public PlayerSettings getPlayerSettings() {
		return mPlayerSettings;
	}

	public Which getWhich() {
		return getPlayerSettings().which;
	}

	public void savePawnsPositions() {
		for (PlayerPawn p : mPawns) {
			p.saveSnapshot();
		}
	}

	public void stopAllPawns() {
		for (PlayerPawn p : mPawns) {
			p.stopMoving();
		}
	}

	public PlayerStatistics getStatistics() {
		return mGame.getStatistics().getPlayerStatistics(getWhich());
	}

	boolean isGamePaused() {
		return mGame.isGamePaused();
	}

	void onPawnTouchDown(PlayerPawn touchedPawn, int screenX, int screenY) {
		disablePawnSelection();
		mGame.onShootingPawnSelected(touchedPawn, screenX, screenY);
	}
}

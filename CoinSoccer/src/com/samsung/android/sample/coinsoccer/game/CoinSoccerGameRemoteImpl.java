package com.samsung.android.sample.coinsoccer.game;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.samsung.android.sample.coinsoccer.chord.IRemoteGame;
import com.samsung.android.sample.coinsoccer.game.Playground.PlaygroundSide;
import com.samsung.android.sample.coinsoccer.hud.HudInfoBoxes;
import com.samsung.android.sample.coinsoccer.hud.ThreadSafeHudUpdater;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.android.sample.coinsoccer.settings.Which;
import com.samsung.android.sample.coinsoccer.sounds.VolumeSettings;

public class CoinSoccerGameRemoteImpl extends CoinSoccerGame {

	private class RemoteBridgeImpl extends RemoteBridge {

		private static final byte BUFFERED_STEP = 1;
		private static final byte BUFFERED_GOAL = 2;
		private static final byte BUFFERED_FOUL = 3;
		private static final byte BUFFERED_COLLISION = 4;
		private static final byte BUFFERED_PHYSICS_END = 5;

		private final Queue<ByteBuffer> mCachedData = new ArrayDeque<ByteBuffer>();
		private final ByteBuffer mSendBuffer = ByteBuffer.allocate(1000);
		private boolean mShouldConsumeRemoteSimulationData;

		public RemoteBridgeImpl(IRemoteGame connection) {
			super(connection);
		}

		@Override
		protected void onStartingPlayerChosen(Which whichPlayerIsStarting) {
			getPlayers().setActivePlayer(whichPlayerIsStarting);
			startNextRound();
		}

		@Override
		protected void onShotReady() {
			mShouldConsumeRemoteSimulationData = true;
			mExposedDialogs.remotePlayerPreparingShot(false);
			mGameSounds.click();
			mHudUpdater.onTurnEnd(getStatistics().getCurrentTurnInGame());
			startPerformingPhysicsSimulation();
		}

		@Override
		protected void onShotExpired() {
			mExposedDialogs.remotePlayerPreparingShot(false);
			CoinSoccerGameRemoteImpl.super.onShotExpired();
		}

		@Override
		protected void onPauseFlagChanged(boolean pauseFlag) {
			mExposedDialogs.gamePauseInfo(isGamePaused(), mRemotePauseFlag = pauseFlag);
		}

		@Override
		protected void onRawData(byte[] data) {
			if (mShouldConsumeRemoteSimulationData) {
				mCachedData.add(ByteBuffer.wrap(data));
			}
		}

		public void applyRemoteStepIfAvailable() {
			ByteBuffer buffer = getCachedBuffer();
			if (buffer != null) {
				switch (buffer.get()) {
					case BUFFERED_GOAL:
						PlaygroundSide side = PlaygroundSide.values()[buffer.get()];
						applyRemoteStepIfAvailable();
						onGoalScored(getPlayground().getPlaygroundHalf(side).getGoalSensor());
						break;

					case BUFFERED_STEP:
						final int count = getCount();
						for (int i = 0; i < count; i++) {
							get(i).applyBodyState(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
						}
						break;

					case BUFFERED_FOUL:
						Which whichPlayer = Which.forOrdinal(buffer.get());
						int pawnIndex = buffer.get();
						applyRemoteStepIfAvailable();
						onFoulCommitted(getPlayers().getPlayer(whichPlayer).get(pawnIndex));
						break;

					case BUFFERED_COLLISION:
						mGameSounds.coinCollision();
						applyRemoteStepIfAvailable();
						break;

					case BUFFERED_PHYSICS_END: // end physics
						stopPerformingPhysicsSimulation();
						if (isGoalNotShotYetInThisRound() && mFoulNotCommitedInThisTurn) {
							onTurnEnd();
						}
						break;
				}
			}
		}

		public void clearCache() {
			mShouldConsumeRemoteSimulationData = false;
			mCachedData.clear();
		}

		public void sendGoalScored(GoalSensor goalSensor) {
			flushSendBufferIfNotEnough(2);
			mSendBuffer
					.put(BUFFERED_GOAL)
					.put((byte) goalSensor.getPlaygroundSide().ordinal());
		}

		public void sendFoulCommitted(PlayerPawn committer) {
			flushSendBufferIfNotEnough(3);
			mSendBuffer
					.put(BUFFERED_FOUL)
					.put((byte) committer.getPlayer().getWhich().ordinal())
					.put((byte) committer.getIndex());
		}

		public void sendWorldStep() {
			flushSendBufferIfNotEnough(1 + getCount() * 3 * 4);
			mSendBuffer.put(BUFFERED_STEP);
			Body pawnBody;
			for (int i = 0; i < getCount(); i++) {
				pawnBody = get(i).getBody();
				mSendBuffer
						.putFloat(pawnBody.getPosition().x)
						.putFloat(pawnBody.getPosition().y)
						.putFloat(pawnBody.getAngle());
			}
		}

		public void sendCollision() {
			flushSendBufferIfNotEnough(1);
			mSendBuffer.put(BUFFERED_COLLISION);
		}

		public void sendPhysicsEnd() {
			flushSendBufferIfNotEnough(1);
			mSendBuffer.put(BUFFERED_PHYSICS_END);
			flushSendBuffer();
		}

		private ByteBuffer getCachedBuffer() {
			ByteBuffer buffer = mCachedData.peek();
			if (buffer != null) {
				if (buffer.position() == buffer.capacity()) {
					mCachedData.remove();
					return getCachedBuffer();
				}
			}
			return buffer;
		}

		private void flushSendBufferIfNotEnough(int requestedSize) {
			if (mSendBuffer.capacity() - mSendBuffer.position() < requestedSize) {
				flushSendBuffer();
			}
		}

		private void flushSendBuffer() {
			byte[] data = new byte[mSendBuffer.position()];
			for (int i = 0; i < data.length; i++) {
				data[i] = mSendBuffer.get(i);
			}
			mSendBuffer.rewind();
			sendRawData(data);
		}
	};

	private final RemoteBridgeImpl mRemoteBridge;
	boolean mRemotePauseFlag;

	public CoinSoccerGameRemoteImpl(OnGameEndListener onGameEndListener, 
			HudInfoBoxes exposedDialogs, GameSettingsHelper gameSettingsHelper,
			PlayerSettings firstPlayerSettings, PlayerSettings secondPlayerSettings,
			ThreadSafeHudUpdater hudUpdater, VolumeSettings VolumeSettings,
			IRemoteGame connection) {
		super(onGameEndListener, exposedDialogs, gameSettingsHelper,
				firstPlayerSettings, secondPlayerSettings, hudUpdater, VolumeSettings);
		mRemoteBridge = new RemoteBridgeImpl(connection);
	}

	@Override
	public void create() {
		super.create();
		mRemoteBridge.connect();
	}

	@Override
	public void pause() {
		if (!isGamePaused()) {
			if (!mGameStatisticsCollector.isGameEnd()) {
				mRemoteBridge.sendPauseFlagChanged(true);
			}
		}
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
		if (!isGamePaused()) {
			if (!mGameStatisticsCollector.isGameEnd()) {
				mRemoteBridge.sendPauseFlagChanged(false);
			}
		}
	}

	@Override
	protected void doPauseGameOnGdxThread() {
		mRemoteBridge.sendPauseFlagChanged(isGamePaused());
		mExposedDialogs.gamePauseInfo(isGamePaused(), mRemotePauseFlag);
		mGameSounds.setFrozen(isGamePaused() || mRemotePauseFlag);
	}

	@Override
	protected boolean shouldDoOnlyBasicRendering() {
		return super.shouldDoOnlyBasicRendering() || mRemotePauseFlag;
	}

	@Override
	public void beginContact(Contact contact) {
		if (isLocalPlayerActive()) {
			super.beginContact(contact);
		}
	}

	@Override
	protected boolean shouldPlayCollisionSound(Contact contact) {
		if (super.shouldPlayCollisionSound(contact)) {
			mRemoteBridge.sendCollision();
			return true;
		}
		return false;
	}

	@Override
	protected void makeWorldStepAhead(float deltaTime) {
		if (isLocalPlayerActive()) {
			super.makeWorldStepAhead(deltaTime);
			mRemoteBridge.sendWorldStep();
		}
		else {
			mRemoteBridge.applyRemoteStepIfAvailable();
		}
	}

	@Override
	protected void checkTurnEnd() {
		if (isLocalPlayerActive()) {
			super.checkTurnEnd();
		}
	}

	@Override
	protected void onGoalScored(GoalSensor goalSensor) {
		if(isLocalPlayerActive()) {
			mRemoteBridge.sendGoalScored(goalSensor);
		}
		super.onGoalScored(goalSensor);
	}

	@Override
	protected void onFoulCommitted(PlayerPawn committer) {
		if (isLocalPlayerActive()) {
			mRemoteBridge.sendFoulCommitted(committer);
		}
		super.onFoulCommitted(committer);
	}

	@Override
	protected void stopPerformingPhysicsSimulation() {
		if(isLocalPlayerActive()) {
			super.stopPerformingPhysicsSimulation();
			mRemoteBridge.sendPhysicsEnd();
		}
		else {
			mRemoteBridge.clearCache();
			super.stopPerformingPhysicsSimulation();
		}
	}

	@Override
	protected void startGame() {
		if (mRemoteBridge.getConnection().isHost()) {
			Which startingPlayer = Which.getRandom();
			getPlayers().setActivePlayer(startingPlayer);
			mRemoteBridge.sendStartingPlayer(startingPlayer);
			startNextRound();
		}
	}

	@Override
	protected void onShotExpired() {
		if (isLocalPlayerActive()) {
			mRemoteBridge.sendShotExpired();
			super.onShotExpired();
		}
		else {
			throw new IllegalStateException(
					"Should be called in remote game only for local player!");
		}
	}

	@Override
	public void onShotVectorPrepared(PlayerPawn playerPawn, float shotVectorX, float shotVectorY) {
		if (isLocalPlayerActive()) {
			mRemoteBridge.sendShotReady();
			super.onShotVectorPrepared(playerPawn, shotVectorX, shotVectorY);
		}
		else {
			throw new IllegalStateException(
					"Should be called in remote game only for local player!");
		}
	}

	@Override
	protected void startPawnSelectionByActivePlayer() {
		if (isLocalPlayerActive()) {
			super.startPawnSelectionByActivePlayer();
		}
		else {
			mExposedDialogs.remotePlayerPreparingShot(true);
		}
	}

	protected void stopSelectionOnRemotePause() {
		if (isInShotSelection() && getShotInputWidget().isActive()) {
			getShotInputWidget().stop();
			onStartPawnSelectionByActivePlayer();
		}
	}

	private boolean isLocalPlayerActive() {
		return getPlayers().getActivePlayer().getWhich() == getWhichPlayerIsLocal();
	}

	private Which getWhichPlayerIsLocal() {
		return mRemoteBridge.getConnection().isHost() ? Which.FIRST : Which.SECOND;
	}
}

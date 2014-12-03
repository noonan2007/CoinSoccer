package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.samsung.android.sample.camera.CameraManager;
import com.samsung.android.sample.coinsoccer.game.taskqueue.ApplyFuturePositionsTask;
import com.samsung.android.sample.coinsoccer.game.taskqueue.DelayedSingleShotTask;
import com.samsung.android.sample.coinsoccer.game.taskqueue.DeployPawnsToInitialPositionsTask;
import com.samsung.android.sample.coinsoccer.game.taskqueue.RevertPawnsToLastSavedPositionsTask;
import com.samsung.android.sample.coinsoccer.game.taskqueue.TaskQueue;
import com.samsung.android.sample.coinsoccer.game.taskqueue.TaskSequence;
import com.samsung.android.sample.coinsoccer.hud.HudInfoBoxes;
import com.samsung.android.sample.coinsoccer.hud.ThreadSafeHudUpdater;
import com.samsung.android.sample.coinsoccer.settings.GameSettings;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.android.sample.coinsoccer.settings.Which;
import com.samsung.android.sample.coinsoccer.sounds.GameSounds;
import com.samsung.android.sample.coinsoccer.sounds.VolumeSettings;
import com.samsung.android.sample.coinsoccer.statistics.GameStatistics;
import com.samsung.android.sample.coinsoccer.statistics.GameStatisticsCollector;

public class CoinSoccerGame extends InputMultiplexer implements ApplicationListener,
		ContactListener, IHasElements<BaseCoinPawn> {

	public interface OnGameEndListener {

		void onGameEnd();
	}

	private static final float WORLD_TIME_STEP = 1 / 45.0f;
	private static final int WORLD_VELOCITY_ITERATIONS = 6;
	private static final int WORLD_POSITION_ITERATIONS = 2;

	private static final float CLEAR_COLOR_R = 0;
	private static final float CLEAR_COLOR_G = 0;
	private static final float CLEAR_COLOR_B = 0;
	private static final float CLEAR_COLOR_A = 1;

	private static final float PLAYGROUND_WIDTH = 40;
	private static final float PLAYGROUND_HEIGHT = 50;
	private static final float PLAYGROUND_PADDING_HORIZONTAL = 12;
	private static final float PLAYGROUND_PADDING_VERTICAL = 12;
	private static final float GOAL_WIDTH = 6;
	private static final float GOAL_HEIGHT = 2.5f;
	private static final float GOAL_AREA_WIDTH = 7.5f;
	private static final float GOAL_AREA_HEIGHT = 3.5f;

	private static final float PLAYER_PAWN_RADIUS = 0.9f;
	private static final float BALL_PAWN_RADIUS = 0.75f;

	private static final int DIALOG_SHOW_MILLIS = 1500;

	protected final HudInfoBoxes mExposedDialogs;
	protected final GameSounds mGameSounds;
	protected final ThreadSafeHudUpdater mHudUpdater;
	protected boolean mFoulNotCommitedInThisTurn;
	protected boolean mIsBeforeAttackingPlayersBallContact;
	protected GameStatisticsCollector mGameStatisticsCollector;
	private final OnGameEndListener mOnGameEndListener;
	private final GameSettingsHelper mGameSettingsHelper;
	private final PlayerSettings mFirstPlayerSettings;
	private final PlayerSettings mSecondPlayerSettings;
	private Playground mPlayground;
	private World mWorld;
	private TaskQueue mTaskQueue;
	private CameraManager mGameCameraManager;
	private Players mPlayers;
	private AssetsProvider mAssetsProvider;
	private SpriteBatch mSpriteBatch;
	private BallPawn mBallPawn;
	private TaskQueue.Task mAfterFoulTask;
	private TaskQueue.Task mDeployPawnsOnRoundStartTask;
	private TaskQueue.Task mGoalDialogTask;
	private TaskQueue.Task mGoalAreaCleaningTask;
	private TaskQueue.Task mShotExpiredDialogTask;
	private SearchIllegalPawnsFromGoalAreasHelper mSearchIllegalPawnsFromGoalAreasHelper;
	private GoalSensor mShotGoalSensor;
	private boolean mShouldPerformPhysicsSimulation;
	private boolean mIsInShotSelection;
	private volatile boolean mIsGamePaused;
	private ShotInputWidget mShotInputWidget;
	private final Runnable mPauseGameOnGdxThreadRunnable = new Runnable() {

		@Override
		public void run() {
			doPauseGameOnGdxThread();
		}
	};
	private final Runnable mForceGameEndOnGdxThreadRunnable = new Runnable() {

		@Override
		public void run() {
			onEndGame();
		}
	};

	public CoinSoccerGame(OnGameEndListener onGameEndListener,
			HudInfoBoxes exposedDialogs, GameSettingsHelper gameSettingsHelper,
			PlayerSettings firstPlayerSettings, PlayerSettings secondPlayerSettings,
			ThreadSafeHudUpdater hudUpdater, VolumeSettings VolumeSettings) {
		mOnGameEndListener = onGameEndListener;
		mExposedDialogs = exposedDialogs;
		mFirstPlayerSettings = firstPlayerSettings;
		mSecondPlayerSettings = secondPlayerSettings;
		mGameSettingsHelper = gameSettingsHelper;
		mHudUpdater = hudUpdater;
		mGameSounds = new GameSounds(VolumeSettings);
	}

	public void onShotVectorPrepared(PlayerPawn playerPawn, float shotVectorX, float shotVectorY) {
		mGameSounds.click();
		mIsInShotSelection = false;
		mHudUpdater.onTurnEnd(getStatistics().getCurrentTurnInGame());
		startPerformingPhysicsSimulation();
		playerPawn.getBody().setLinearVelocity(shotVectorX, shotVectorY);
	}

	public void onShotVectorPreparationCancelled(PlayerPawn playerPawn) {
		mGameSounds.shotCancelled();
		startPawnSelectionByActivePlayer();
	}

	public void onShootingPawnSelected(PlayerPawn touchedPawn, int screenX, int screenY) {
		mGameSounds.click();
		mShotInputWidget.start(touchedPawn, screenX, screenY);
	}

	protected void onShotExpired() {
		mIsInShotSelection = false;
		getPlayers().getActivePlayer().disablePawnSelection();
		mShotInputWidget.stop();
		mGameSounds.whistleShort();
		mHudUpdater.onTurnEnd(getStatistics().getCurrentTurnInGame());
		mGameStatisticsCollector.onShotExpired(getPlayers().getWhichIsActivePlayer());
		mTaskQueue.schedule(mShotExpiredDialogTask);
	}

	public CameraManager getGameCameraManager() {
		return mGameCameraManager;
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {}

	@Override
	public void endContact(Contact contact) {}

	@Override
	public void beginContact(Contact contact) {

		if (shouldPlayCollisionSound(contact)) {
			mGameSounds.coinCollision();
		}

		if (isGoalNotShotYetInThisRound() && mFoulNotCommitedInThisTurn) {

			BallPawn ballPawn = getUserObject(BallPawn.class, contact.getFixtureA());
			Fixture otherFixture = contact.getFixtureB();
			if (ballPawn == null) {
				ballPawn = getUserObject(BallPawn.class, contact.getFixtureB());
				otherFixture = contact.getFixtureA();
			}

			// no ball
			if (ballPawn == null) {

				// check for foul
				if (mIsBeforeAttackingPlayersBallContact) {
					PlayerPawn playerPawnA = getUserObject(PlayerPawn.class, contact.getFixtureA());
					if (playerPawnA != null) {
						PlayerPawn playerPawnB = getUserObject(PlayerPawn.class, contact.getFixtureB());
						if (playerPawnB != null) {
							if (playerPawnA.getPlayer() != playerPawnB.getPlayer()) {
								onFoulCommitted(playerPawnA.getPlayer() == getPlayers().getActivePlayer() ?
										playerPawnA : playerPawnB);
							}
						}
					}
				}
			}
			else { // we have a ball!!!

				// check goals
				GoalSensor goalSensor = getUserObject(GoalSensor.class, otherFixture);
				if (goalSensor != null) {
					onGoalScored(goalSensor);
					return;
				}

				// check first ball contact
				if (mIsBeforeAttackingPlayersBallContact) {
					PlayerPawn playerPawn = getUserObject(PlayerPawn.class, otherFixture);
					if (playerPawn != null) {
						if (playerPawn.getPlayer() == getPlayers().getActivePlayer()) {
							mIsBeforeAttackingPlayersBallContact = false;
						}
					}
				}
			}
		}
	}

	protected boolean shouldPlayCollisionSound(Contact contact) {
		return !contact.getFixtureA().isSensor() && !contact.getFixtureB().isSensor();
	}

	private <T> T getUserObject(Class<T> c, Fixture fixture) {
		Object userData = fixture.getBody().getUserData();
		if (c.isInstance(userData)) {
			return c.cast(userData);
		}
		return null;
	}

	@Override
	public void create() {

		Gdx.gl.glClearColor(CLEAR_COLOR_R, CLEAR_COLOR_G, CLEAR_COLOR_B, CLEAR_COLOR_A);
		Gdx.input.setInputProcessor(this);

		mGameSounds.onGdxCreate();

		mWorld = new World(new Vector2(), true);
		mWorld.setContactListener(this);

		mTaskQueue = new TaskQueue();
		mAssetsProvider = new AssetsProvider();
		mSpriteBatch = new SpriteBatch();
		mGameStatisticsCollector = new GameStatisticsCollector();

		mPlayground = new Playground(
				PLAYGROUND_WIDTH, PLAYGROUND_HEIGHT,
				PLAYGROUND_PADDING_HORIZONTAL, PLAYGROUND_PADDING_VERTICAL,
				GOAL_WIDTH, GOAL_HEIGHT, GOAL_AREA_WIDTH, GOAL_AREA_HEIGHT,
				BALL_PAWN_RADIUS, mAssetsProvider, mWorld);
		mBallPawn = new BallPawn(mAssetsProvider, mWorld,
				mPlayground.getCenterX(), mPlayground.getCenterY(),
				BALL_PAWN_RADIUS);
		mPlayers = new Players(this, PLAYER_PAWN_RADIUS);

		mGameCameraManager = new CameraManager(CameraManager.getNewCamera(
				mPlayground.getWidth(), mPlayground.getHeight()), this);
		mGameCameraManager.setCameraBounds(new Rectangle(
				mPlayground.getLeftX(), mPlayground.getBottomY(),
				mPlayground.getWidth(), mPlayground.getHeight()));

		mShotInputWidget = new ShotInputWidget(this);

		mSearchIllegalPawnsFromGoalAreasHelper =
				new SearchIllegalPawnsFromGoalAreasHelper();

		mAfterFoulTask = new TaskSequence(
				new DelayedSingleShotTask(DIALOG_SHOW_MILLIS) {

					@Override
					protected void onStart(long timestamp) {
						super.onStart(timestamp);
						mExposedDialogs.showFoulCommitedDialog();
					}

					@Override
					protected void cleanUp() {
						super.cleanUp();
						mExposedDialogs.hideFoulCommitedDialog();
					}

					@Override
					protected void onFinished() {
						super.onFinished();
						stopPerformingPhysicsSimulation();
					}
				},
				new RevertPawnsToLastSavedPositionsTask(this) {

					@Override
					protected void onFinished() {
						super.onFinished();
						onTurnEnd();
					}
				});
		mGoalAreaCleaningTask = new TaskSequence(
				new DelayedSingleShotTask(DIALOG_SHOW_MILLIS) {

					@Override
					protected void onStart(long timestamp) {
						super.onStart(timestamp);
						mExposedDialogs.showGoalAreaCleaningInfo();
					}

					@Override
					protected void cleanUp() {
						super.cleanUp();
						mExposedDialogs.hideGoalAreaCleaningInfo();
					}
				},
				new ApplyFuturePositionsTask() {

					@Override
					protected void onStart(long timestamp) {
						setTargetSnapshots(mSearchIllegalPawnsFromGoalAreasHelper);
						super.onStart(timestamp);
					}
				}) {

			@Override
			public boolean fire(long timestamp) {
				if (mSearchIllegalPawnsFromGoalAreasHelper.getCount() == 0) {
					onFinished();
					return true;
				}
				else {
					return super.fire(timestamp);
				}
			}

			@Override
			protected void onFinished() {
				super.onFinished();
				startNextTurn();
			}
		};
		mGoalDialogTask = new DelayedSingleShotTask(DIALOG_SHOW_MILLIS) {

			@Override
			protected void onStart(long timestamp) {
				super.onStart(timestamp);
				mExposedDialogs.showGoalShotDialog(
						getStatistics().getPlayerStatistics(Which.FIRST).getScore(),
						getStatistics().getPlayerStatistics(Which.SECOND).getScore());
			}

			@Override
			protected void onFinished() {
				super.onFinished();
				mExposedDialogs.hideGoalShotDialog();
				stopPerformingPhysicsSimulation();
				getPlayers().setActivePlayer(getPlayers().getDefendingPlayer(
						mShotGoalSensor.getPlaygroundSide()).getWhich());
				startNextRound();
			}
		};
		mShotExpiredDialogTask = new DelayedSingleShotTask(DIALOG_SHOW_MILLIS) {

			@Override
			protected void onStart(long timestamp) {
				super.onStart(timestamp);
				mExposedDialogs.showTurnExpiredDialog();
			}

			@Override
			protected void onFinished() {
				super.onFinished();
				mExposedDialogs.hideTurnExpiredDialog();
				getPlayers().swapActivePlayer();
				startNextTurn();
			}
		};
		mDeployPawnsOnRoundStartTask = new DeployPawnsToInitialPositionsTask(this) {

			@Override
			protected void onFinished() {
				super.onFinished();
				startNextTurn();
			}
		};

		resume();

		startGame();
	}

	@Override
	public void dispose() {
		mWorld.dispose();
		mSpriteBatch.dispose();
		mAssetsProvider.dispose();
		mGameSounds.dispose();
	}

	@Override
	public void render() {

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		mGameCameraManager.update();
		final OrthographicCamera gameCamera = mGameCameraManager.getCamera();
		mSpriteBatch.setProjectionMatrix(gameCamera.combined);

		final float deltaTime = Gdx.graphics.getDeltaTime();

		if (shouldDoOnlyBasicRendering()) {
			mSpriteBatch.begin();
			mPlayground.render(gameCamera, mSpriteBatch, deltaTime);
			mBallPawn.render(gameCamera, mSpriteBatch, deltaTime);
			mPlayers.render(gameCamera, mSpriteBatch, deltaTime);
			mSpriteBatch.end();
			return;
		}

		mTaskQueue.pulse();

		if (mShouldPerformPhysicsSimulation) {
			makeWorldStepAhead(deltaTime);
		}

		mSpriteBatch.begin();
		mPlayground.render(gameCamera, mSpriteBatch, deltaTime);
		mBallPawn.render(gameCamera, mSpriteBatch, deltaTime);
		mPlayers.render(gameCamera, mSpriteBatch, deltaTime);
		mShotInputWidget.render(gameCamera, mSpriteBatch, deltaTime);
		mSpriteBatch.end();

		if (mIsInShotSelection) {
			mGameSettingsHelper.pulse(Math.round(deltaTime * 1000));
			mHudUpdater.updateTurnCounter(mGameSettingsHelper.getTurnExpirationMillis());
			if (mGameSettingsHelper.isTurnExpired()) {
				onShotExpired();
			}
		}

		if (mShouldPerformPhysicsSimulation) {
			checkTurnEnd();
		}
	}

	protected boolean shouldDoOnlyBasicRendering() {
		return mIsGamePaused || getStatistics().isGameEnd();
	}

	protected void checkTurnEnd() {
		if (isGoalNotShotYetInThisRound() && mFoulNotCommitedInThisTurn) {
			if (!isAtLeastOnePlayerOrBallBodyActive()) {
				naturalTurnEnd();
			}
		}
	}

	protected void naturalTurnEnd() {
		stopPerformingPhysicsSimulation();
		onTurnEnd();
	}

	protected void makeWorldStepAhead(float deltaTime) {
		mWorld.step(WORLD_TIME_STEP, WORLD_VELOCITY_ITERATIONS, WORLD_POSITION_ITERATIONS);
		mPlayers.updatePawnSpritesToPhysics();
		mBallPawn.updateSpriteToBody();
	}

	@Override
	public void resize(int width, int height) {
		// TODO: resize camera
	}

	@Override
	public void pause() {
		mAssetsProvider.unloadAssets();
	}

	@Override
	public void resume() {
		mExposedDialogs.showAssetsLoadingDialog();
		mAssetsProvider.loadAssets();
		mExposedDialogs.hideAssetsLoadingDialog();
	}

	protected void doPauseGameOnGdxThread() {
		mExposedDialogs.gamePauseInfo(mIsGamePaused);
		mGameSounds.setFrozen(mIsGamePaused);
	}

	public void setGamePausedFromAndroidUi(boolean isGamePaused) {
		if (mIsGamePaused != isGamePaused) {
			mIsGamePaused = isGamePaused;
			Gdx.app.postRunnable(mPauseGameOnGdxThreadRunnable);
		}
	}

	public boolean isGamePaused() {
		return mIsGamePaused;
	}

	@Override
	public BaseCoinPawn get(int index) {
		int count = getCount();
		if (index >= 0 && index < count) {
			if (index == count - 1) {
				return mBallPawn;
			}
			// Notice that result of integer by integer division
			// is in java rounded to integer closer to 0
			return getPlayers().getPlayer(
					index % 2 == 0 ? Which.FIRST : Which.SECOND).get(index / 2);
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	@Override
	public int getCount() {
		return getPlayers().getPlayer(Which.FIRST).getCount() +
				getPlayers().getPlayer(Which.SECOND).getCount() + 1; // add 1 for ball pawn
	}

	public boolean isInShotSelection() {
		return mIsInShotSelection;
	}

	public ShotInputWidget getShotInputWidget() {
		return mShotInputWidget;
	}

	public AssetsProvider getAssetsProvider() {
		return mAssetsProvider;
	}

	public Playground getPlayground() {
		return mPlayground;
	}

	public Players getPlayers() {
		return mPlayers;
	}

	public World getWorld() {
		return mWorld;
	}

	public BallPawn getBallPawn() {
		return mBallPawn;
	}

	public PlayerSettings getFirstPlayerSettings() {
		return mFirstPlayerSettings;
	}

	public PlayerSettings getSecondPlayerSettings() {
		return mSecondPlayerSettings;
	}

	public GameSettings getSettings() {
		return mGameSettingsHelper.getSettings();
	}

	public GameStatistics getStatistics() {
		return mGameStatisticsCollector;
	}

	public void forceGameEndFromUiThread() {
		Gdx.app.postRunnable(mForceGameEndOnGdxThreadRunnable);
	}

	protected void onEndGame() {
		if (!mGameStatisticsCollector.isGameEnd()) {
			mGameSounds.whistleThrice();
			mGameStatisticsCollector.setGameEnd();
			mOnGameEndListener.onGameEnd();
		}
	}

	protected void startPerformingPhysicsSimulation() {
		mBallPawn.setActive(true);
		mShouldPerformPhysicsSimulation = true;
	}

	protected void stopPerformingPhysicsSimulation() {
		stopAllPawns();
		mBallPawn.setActive(false);
		mShouldPerformPhysicsSimulation = false;
	}

	protected void onTurnEnd() {
		mSearchIllegalPawnsFromGoalAreasHelper.recalculate(this);
		if (mSearchIllegalPawnsFromGoalAreasHelper.getCount() > 0) {
			mGameStatisticsCollector.onIllegalPawnPositionsFixed(
					getPlayers().getWhichIsActivePlayer());
		}
		mPlayers.swapActivePlayer();
		cleanGoalAreaAsync();
	}

	protected void cleanGoalAreaAsync() {
		if (mSearchIllegalPawnsFromGoalAreasHelper.getCount() > 0) {
			mGameSounds.whistleShort();
			mTaskQueue.schedule(mGoalAreaCleaningTask);
		}
		else {
			startNextTurn();
		}
	}

	protected void startNextTurn() {
		if (mGameSettingsHelper.endGameChecker.isGameEnd(this)) {
			onEndGame();
		}
		else {
			onStartNextTurn();
			startPawnSelectionByActivePlayer();
		}
	}

	protected void startPawnSelectionByActivePlayer() {
		mGameSettingsHelper.resetExpirationTimer();
		mHudUpdater.onTurnStart(mPlayers.getWhichIsActivePlayer(),
				mGameSettingsHelper.getTurnExpirationMillis());
		mIsInShotSelection = true;
		onStartPawnSelectionByActivePlayer();
	}

	protected void onStartPawnSelectionByActivePlayer() {
		mPlayers.getInactivePlayer().disablePawnSelection();
		mPlayers.getActivePlayer().initPawnSelection();
	}

	protected void onStartNextTurn() {
		savePawnsPositions();
		mGameStatisticsCollector.onTurnStart();
		mIsBeforeAttackingPlayersBallContact = true;
		mFoulNotCommitedInThisTurn = true;
	}

	protected void onGoalScored(GoalSensor goalSensor) {

		mGameSounds.goalCheers();
		mShotGoalSensor = goalSensor;

		Player defendingPlayer = mPlayers.getDefendingPlayer(goalSensor.getPlaygroundSide());
		Player attackingPlayer = mPlayers.getOppositePlayer(defendingPlayer);

		// update goals in game
		mGameStatisticsCollector.onGoalShot(
				attackingPlayer.getWhich(), defendingPlayer.getWhich(),
				mPlayers.getWhichIsActivePlayer());

		// update hud
		mHudUpdater.updateScore(
				getStatistics().getPlayerStatistics(Which.FIRST).getScore(),
				getStatistics().getPlayerStatistics(Which.SECOND).getScore());
		mTaskQueue.schedule(mGoalDialogTask);
	}

	protected void onFoulCommitted(PlayerPawn commiter) {
		mGameSounds.whistleLong();
		mFoulNotCommitedInThisTurn = false;
		mGameStatisticsCollector.onFoulCommitted(
				getPlayers().getWhichIsActivePlayer());
		mTaskQueue.schedule(mAfterFoulTask);
	}

	protected void startGame() {
		mPlayers.setActivePlayer(Which.getRandom());
		startNextRound();
	}

	protected void startNextRound() {
		mShotGoalSensor = null;
		mTaskQueue.schedule(mDeployPawnsOnRoundStartTask);
	}

	protected boolean isGoalNotShotYetInThisRound() {
		return mShotGoalSensor == null;
	}

	private void stopAllPawns() {
		mPlayers.stopAllPawns();
		mBallPawn.stopMoving();
	}

	private void savePawnsPositions() {
		mPlayers.savePawnsPositions();
		mBallPawn.saveSnapshot();
	}

	private boolean isAtLeastOnePlayerOrBallBodyActive() {
		return mBallPawn.isMoving() || mPlayers.isAtLeastOnePlayerPawnMoving();
	}
}

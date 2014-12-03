package com.samsung.android.sample.coinsoccer.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * AssetsProvider. How to use it:
 *
 * <pre>
 * <code>
 * 	// 1. Prepare a "renderable" object which implements also AssetsProvider#AssetsProviderClient.
 * 	class MySprite extends Sprite implements AssetsProvider.AssetsProviderClient {
 *
 * 		@Override
 * 		public void requestAssetsRefresh(AssetsProvider assetsProvider) {
 * 			setRegion(assetsProvider.getTrashTexture());
 * 		}
 * 	}
 *
 * 	//2. Register the sprite in the AssetsProvider.
 * 	assetsProvider.addClient(mySprite);
 * </code>
 * </pre>
 */
public class AssetsProvider implements Disposable {

	private static final String PACKED_GFX_ATLAS_PATH = "packed/packedgfx.atlas";

	private static final String ACCURACY_TEST_HAND_REGION = "hand";
	private static final String ACCURACY_TEST_AIM_REGION = "accuracy_test_view_finder";
	private static final String ACCURACY_TEST_BG_REGION = "accuracy_test_bg";
	private static final String ACCURACY_TEST_LIGHT_REGION = "accuracy_test_green_area";
	private static final String OUTER_COIN_REGION = "player_coin_top_layer";
	private static final String INNER_COIN_REGION = "player_coin_middle_layer";
	private static final String PENCIL_LINE_START_REGION = "pencil_start_red";
	private static final String PENCIL_LINE_STRETCHABLE_REGION = "pencil_stretchable_red";
	private static final String PENCIL_LINE_END_REGION = "pencil_end_red";
	private static final String GRADIENT_CIRCLE_REGION = "gradient_circle";
	private static final String CIRCLE_LINES_REGION = "lines_circle";

	private static final String PLAYGROUND_BG_TEXTURE_PATH = "wood_bg.png";
	private static final String LONG_LINE_TEXTURE_PATH = "lines.png";

	/**
	 * Class which uses assets managed by AssetsProvider instance should implement this interface and register with
	 * {@link AssetsProvider#addClient(AssetsProviderClient)} in order to be notified when it should re-query the
	 * provider for assets.
	 */
	public interface AssetsProviderClient {

		/**
		 * Called by {@link AssetsProvider} after its managed assets has been (re)loaded e.g. when its ready state has
		 * just switched to true. when {@link AssetsProvider} ready state switches to false then no rendering is done in
		 * main game loop. Particularly all AssetsProviderClient objects implementing also {@link CanBeRendered} are
		 * guaranteed that
		 * {@link CanBeRendered#render(com.badlogic.gdx.graphics.Camera, com.badlogic.gdx.graphics.g2d.SpriteBatch, float)}
		 * will not be called when {@link AssetsProvider#isReady()} is false.
		 *
		 * @see AssetsProvider#isReady()
		 *
		 * @param assetsProvider
		 *            {@link AssetsProvider} from which you can query for assets
		 */
		void requestAssetsRefresh(AssetsProvider assetsProvider);
	}

	private final List<AssetsProviderClient> mClients;
	private boolean mIsReady;
	private TextureAtlas mTextureAtlas;
	private final HashMap<String, TextureRegion> mRegions;
	private Texture mPlaygroundBgTexture;
	private Texture mLongLineTexture;

	public AssetsProvider() {
		mClients = new ArrayList<AssetsProviderClient>();
		mRegions = new HashMap<String, TextureRegion>();
		Texture.setEnforcePotImages(true);
	}

	/**
	 * Loads assets. It blocks until all assets are loaded!!! it also calls
	 * {@link AssetsProviderClient#requestAssetsRefresh(AssetsProvider)} when all assets are available.
	 */
	public void loadAssets() {
		if (!isReady()) {
			
			mTextureAtlas = new TextureAtlas(Gdx.files.internal(PACKED_GFX_ATLAS_PATH));
			
			mLongLineTexture = new Texture(Gdx.files.internal(LONG_LINE_TEXTURE_PATH));
			mLongLineTexture.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
			
			mPlaygroundBgTexture = new Texture(Gdx.files.internal(PLAYGROUND_BG_TEXTURE_PATH));
			mPlaygroundBgTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			
			setReadyState(true);
		}
	}

	/**
	 * Clears all assets should be called from within {@link ApplicationListener#pause()} clears all textures in.
	 */
	public void unloadAssets() {
		if (isReady()) {
			setReadyState(false);
			mRegions.clear();
			mTextureAtlas.dispose();
			mTextureAtlas = null;
			mLongLineTexture.dispose();
			mLongLineTexture = null;
			mPlaygroundBgTexture.dispose();
			mPlaygroundBgTexture = null;
		}
	}

	/**
	 * "Destructor-like" method. You must call it when the AssetProvider will no longer be used probably to be called
	 * from {@link ApplicationListener#dispose()} method.
	 */
	@Override
	public void dispose() {
		unloadAssets();
	}

	public Texture getPlaygroundBgTexture() {
		return mPlaygroundBgTexture;
	}

	public Texture getLongLineTexture() {
		return mLongLineTexture;
	}

	public TextureRegion getCircleLinesTexture() {
		return getRegionByKey(CIRCLE_LINES_REGION);
	}

	public TextureRegion getPencilStartTexture() {
		return getRegionByKey(PENCIL_LINE_START_REGION);
	}

	public TextureRegion getPencilStretchableTexture() {
		return getRegionByKey(PENCIL_LINE_STRETCHABLE_REGION);
	}

	public TextureRegion getPencilEndTexture() {
		return getRegionByKey(PENCIL_LINE_END_REGION);
	}

	public TextureRegion getCoinOuterTexture() {
		return getRegionByKey(OUTER_COIN_REGION);
	}

	public TextureRegion getCoinInnerTexture() {
		return getRegionByKey(INNER_COIN_REGION);
	}

	public TextureRegion getGradientCircleTexture() {
		return getRegionByKey(GRADIENT_CIRCLE_REGION);
	}

	public TextureRegion getIndicatorTexture() {
		return getRegionByKey(ACCURACY_TEST_HAND_REGION);
	}

	public TextureRegion getAccuracyTestGreenZoneTexture() {
		return getRegionByKey(ACCURACY_TEST_LIGHT_REGION);
	}

	public TextureRegion getAccuracyTestBgTexture() {
		return getRegionByKey(ACCURACY_TEST_BG_REGION);
	}

	public TextureRegion getAccuracyTestAimTexture() {
		return getRegionByKey(ACCURACY_TEST_AIM_REGION);
	}

	/**
	 * Registers an {@link AssetsProviderClient} object to be notified when it should refresh assets managed by this
	 * AssetsProvider.
	 *
	 * @param client
	 */
	public void addClient(AssetsProviderClient client) {
		mClients.add(client);
	}

	/**
	 * It removes client formerly added with {@link #addClient(AssetsProviderClient)}.
	 *
	 * @param client
	 * @return
	 */
	public boolean removeClient(AssetsProviderClient client) {
		return mClients.remove(client);
	}

	/**
	 * It returns if the AssetsProvider is ready for use - e.g. if it has all assets loaded.
	 *
	 * @return true if the AssetsProvider is ready for use - e.g. if it has all assets loaded, otherwise false
	 */
	public boolean isReady() {
		return mIsReady;
	}

	private TextureRegion getRegionByKey(String key) {
		TextureRegion region = mRegions.get(key);
		if (region == null) {
			region = mTextureAtlas.findRegion(key);
			mRegions.put(key, region);
		}
		return region;
	}

	private void setReadyState(boolean isReady) {
		mIsReady = isReady;
		if (mIsReady) {
			// iterate over copy Array - for case when something removes entry
			// from mClients at the same time
			AssetsProviderClient[] clientsCopy = mClients.toArray(new AssetsProviderClient[mClients.size()]);
			for (AssetsProviderClient client : clientsCopy) {
				client.requestAssetsRefresh(this);
			}
		}
	}
}

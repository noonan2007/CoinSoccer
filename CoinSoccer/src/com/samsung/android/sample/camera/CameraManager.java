/*
 ********************************************************************************
 * Copyright (c) 2013 Samsung Electronics, Inc.
 * All rights reserved.
 *
 * This software is a confidential and proprietary information of Samsung
 * Electronics, Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Samsung Electronics.
 ********************************************************************************
 */
package com.samsung.android.sample.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.samsung.android.sample.coinsoccer.game.CoinSoccerGame;

public class CameraManager {
	private static final Vector3 tmpVector3 = new Vector3();

	private final OrthographicCamera mCamera;

	private boolean isPanning = false;
	private boolean isZooming = false;
	private boolean isResseting = false;

	private float velX, velY, velZ;
	private float initialScale = 1;
	private float mZoomDamp = 4f;
	private float mPanDamp = 1f;
	private final float mResetDamp = 7f;

	private float mDefaultX;
	private float mDefaultY;

	private final float mMaxCameraZoom;
	private final float mMinCameraZoom;

	private Rectangle mCameraBounds;

	private final GestureListener mGestureListener = new GestureListener() {

		@Override
		public boolean touchDown(float x, float y, int pointer, int button) {
			initialScale = mCamera.zoom;
			isPanning = false;
			isZooming = false;
			isResseting = false;
			return true;
		}

		@Override
		public boolean tap(float x, float y, int count, int button) {
			if (count == 2) {
				resetToDefaultPosition();
			}
			return true;
		}

		@Override
		public boolean longPress(float x, float y) {
			return true;
		}

		@Override
		public boolean fling(float velocityX, float velocityY, int button) {
			return true;
		}

		@Override
		public boolean pan(float x, float y, float deltaX, float deltaY) {
			isPanning = true;
			velX = -deltaX * mCamera.zoom;
			velY = deltaY * mCamera.zoom;
			return true;
		}

		@Override
		public boolean zoom(float initialDistance, float distance) {
			float ratio = initialDistance / distance;
			velZ = initialScale * ratio;
			isZooming = true;
			return true;
		}

		@Override
		public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
			return true;
		}

	};

	/**
	 * Creates an instance of CameraManager and sets a new GestureDetector to handle camera controls.
	 *
	 * @param camera
	 */
	public CameraManager(OrthographicCamera camera, CoinSoccerGame game) {
		mCamera = camera;
		game.addProcessor(new GestureDetector(mGestureListener));
		mDefaultX = mCamera.position.x;
		mDefaultY = mCamera.position.y;
		mMaxCameraZoom = (game.getPlayground().getHeight() - 1f) / mCamera.viewportHeight;
		mMinCameraZoom = 0.3f;
	}

	/**
	 * Converts world position to screen point.
	 *
	 * @param position
	 * @param camera
	 */
	public static void screenToWorld(Vector2 position, Camera camera) {
		camera.unproject(tmpVector3.set(position.x, position.y, 0));
		position.x = tmpVector3.x;
		position.y = tmpVector3.y;
	}

	/**
	 * Converts world position to screen point.
	 *
	 * @param position
	 * @param camera
	 */
	public static void worldToScreen(Vector2 position, Camera camera) {
		camera.project(tmpVector3.set(position.x, position.y, 0));
		position.x = tmpVector3.x;
		position.y = tmpVector3.y;
	}

	/**
	 * Converts screen points to world position for CameraManager's camera.
	 *
	 * @param position
	 */
	public void screenToWorld(Vector2 position) {
		screenToWorld(position, mCamera);
	}

	/**
	 * Converts world position to screen point for CameraManager's camera.
	 *
	 * @param position
	 */
	public void worldToScreen(Vector2 position) {
		worldToScreen(position, mCamera);
	}

	/**
	 * Returns a new instance of camera.
	 *
	 * @return
	 */
	public static OrthographicCamera getNewCamera(float vWidth, float vHeight) {
		float viewportHeight = vHeight;
		float viewportWidth = vWidth;

		float pW = Gdx.graphics.getWidth();
		float pH = Gdx.graphics.getHeight();

		float aspect = viewportWidth/viewportHeight;

		if(pW/pH >= aspect) {
			viewportHeight = vHeight;
			viewportWidth = vHeight * pW/pH;
		} else {
			viewportWidth = vWidth;
			viewportHeight = viewportWidth * pH/pW;
		}

		OrthographicCamera camera = new OrthographicCamera(viewportWidth, viewportHeight);

		camera.position.set(vWidth/2 - camera.viewportWidth/2, vHeight/2 - camera.viewportHeight/2, 0);
		camera.update();

		return camera;
	}

	/**
	 * Returns an instance of camera connected to the manager.
	 * @return
	 */
	public OrthographicCamera getCamera() {
		return mCamera;
	}

	/**
	 * Sets camera default position.
	 *
	 * @param x
	 * @param y
	 */
	public void setDefaultPosition(float x, float y) {
		mDefaultX = x;
		mDefaultY = y;
	}

	/**
	 * Resets camera to default position.
	 */
	public void resetToDefaultPosition() {
		isResseting = true;
	}

	/**
	 * Sets camera zoom dampening value. The lower value means higher dampening and slower movement.
	 *
	 * @param value
	 */
	public void setZoomDamp(float value) {
		mZoomDamp = value;
	}

	/**
	 * Sets camera pan dampening value. The lower value means higher dampening and slower movement.
	 *
	 * @param value
	 */
	public void setPanDamp(float value) {
		mPanDamp = value;
	}

	/**
	 * Returns current zoom dampening value.
	 *
	 * @return
	 */
	public float getZoomDamp() {
		return mZoomDamp;
	}

	/**
	 * Returns current pan dampening value.
	 *
	 * @return
	 */
	public float getPanDamp() {
		return mPanDamp;
	}

	/**
	 * Sets the viewport height value.
	 *
	 * @param value
	 */
	public void setViewportHeight(float value) {
		mCamera.viewportHeight = value;
	}

	/**
	 * Sets the viewport width value.
	 *
	 * @param value
	 */
	public void setViewportWidth(float value) {
		mCamera.viewportWidth = value;
	}

	/**
	 * Updates CameraManager state. Should be invoked once per frame. CameraManager update invokes Camera.update() by
	 * itself so there no need to call it by hand.
	 */
	public void update() {

		if (isPanning) {
			velX *= 0.85f;
			velY *= 0.85f;

			mCamera.position.x = Interpolation.linear.apply(mCamera.position.x, mCamera.position.x + velX,
					Gdx.graphics.getDeltaTime() * mPanDamp);
			mCamera.position.y = Interpolation.linear.apply(mCamera.position.y, mCamera.position.y + velY,
					Gdx.graphics.getDeltaTime() * mPanDamp);

		} else {
			velX = 0;
			velY = 0;
		}

		if (isZooming) {
			mCamera.zoom = Interpolation.linear.apply(mCamera.zoom, velZ, Gdx.graphics.getDeltaTime() * mZoomDamp);

			if (Math.abs(mCamera.zoom - velZ) < 0.1f) {
				velZ = mCamera.zoom;
				isZooming = false;
			}
		} else {
			velZ = mCamera.zoom;
		}

		if (isResseting) {
			mCamera.position.x = Interpolation.linear.apply(mCamera.position.x, mDefaultX, Gdx.graphics.getDeltaTime()
					* mResetDamp);
			mCamera.position.y = Interpolation.linear.apply(mCamera.position.y, mDefaultY, Gdx.graphics.getDeltaTime()
					* mResetDamp);

			float dX = mCamera.position.x - mDefaultX;
			float dY = mCamera.position.y - mDefaultY;

			float distance = (float) Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));

			if (distance < 0.3f) {
				isResseting = false;
			}
		}

		fixCameraZoom(mCamera);
		fixCameraPosition(mCamera);

		mCamera.update();
	}

	/**
	 * Sets camera bounding box.
	 *
	 * @param bounds
	 *            - Rectangle indicating bounding box of camera.
	 */
	public void setCameraBounds(Rectangle bounds) {
		mCameraBounds = bounds;
	}

	/**
	 * Returns camera bounding box.
	 *
	 * @return - Rectangle indicating camer bounds.
	 */
	public Rectangle getCameraBounds() {
		return mCameraBounds;
	}

	/**
	 * Updates camera position according to bounding box set to the CameraManager with
	 * {@link #setCameraBounds(Rectangle)} method.
	 *
	 * @param camera
	 */
	public void fixCameraPosition(OrthographicCamera camera) {
		if (camera.position.x - camera.viewportWidth / 2 * camera.zoom < mCameraBounds.x) {
			camera.position.x = mCameraBounds.x + camera.viewportWidth / 2 * camera.zoom;
		} else if (camera.position.x + camera.viewportWidth / 2 * camera.zoom > mCameraBounds.x + mCameraBounds.width) {
			camera.position.x = mCameraBounds.x + mCameraBounds.width - camera.viewportWidth / 2 * camera.zoom;
		}

		if (camera.position.y - camera.viewportHeight / 2 * camera.zoom < mCameraBounds.y) {
			camera.position.y = mCameraBounds.y + camera.viewportHeight / 2 * camera.zoom;
		} else if (camera.position.y + camera.viewportHeight / 2 * camera.zoom > mCameraBounds.y + mCameraBounds.height) {
			camera.position.y = mCameraBounds.y + mCameraBounds.height - camera.viewportHeight / 2 * camera.zoom;
		}
	}

	/**
	 * Fixes camera zoom according to the given {@link #CAMERA_MIN_ZOOM} and {@link #CAMERA_MAX_ZOOM} values.
	 *
	 * @param camera
	 */
	public void fixCameraZoom(OrthographicCamera camera) {
		if (camera.zoom < mMinCameraZoom) {
			camera.zoom = mMinCameraZoom;
		} else if (camera.zoom > mMaxCameraZoom) {
			camera.zoom = mMaxCameraZoom;
		}
	}
}

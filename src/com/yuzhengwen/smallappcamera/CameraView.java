package com.yuzhengwen.smallappcamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	private Camera camera;
	private SurfaceHolder holder;
	public static String TAG = "MyApp";

	public CameraView(Context context, Camera c) {
		super(context);
		this.camera = c;
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		holder = getHolder();
		holder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		/*
		 * 
		 * // If your preview can change or rotate, take care of those events
		 * here. // Make sure to stop the preview before resizing or
		 * reformatting it. if (holder.getSurface() == null) // preview surface
		 * does not exist return;
		 * 
		 * // stop preview before making changes try { camera.stopPreview(); }
		 * catch (Exception e) { // ignore: tried to stop a non-existent preview
		 * }
		 * 
		 * // set preview size and make any resize, rotate or // reformatting
		 * changes here
		 * 
		 * // start preview with new settings try {
		 * camera.setPreviewDisplay(holder); camera.startPreview();
		 * 
		 * } catch (Exception e) { Log.d(TAG, "Error starting camera preview: "
		 * + e.getMessage()); }
		 * 
		 */
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			Log.d(TAG, "STARTING PREVIEW");
		} catch (Exception e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// stop camera preview
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
		}

	}
}

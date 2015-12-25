package com.yuzhengwen.smallappcamera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	private Camera camera;
	private SurfaceHolder holder;
	public static String TAG = "MyApp";
	private Context context;

	public CameraView(Context context, Camera c) {
		super(context);
		this.context = context;
		this.camera = c;
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		holder = getHolder();
		holder.addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			Camera.Parameters params = camera.getParameters();
			SmallAppMain s = (SmallAppMain) context;
			params.setPreviewSize(s.width, s.height);
			camera.setParameters(params);
			Log.d(TAG, "STARTING PREVIEW");
		} catch (Exception e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (holder.getSurface() == null)
			return;

		// stop preview before making changes
		try {
			camera.stopPreview();
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}

		int cameraId = SmallAppMain.cameraId;
		int displayOrientation = Functions.getCameraDisplayOrientation(getResources().getConfiguration(), cameraId);
		camera.setDisplayOrientation(displayOrientation);
		// Camera.Parameters params = camera.getParameters();
		// params.setPreviewSize(SmallAppMain.width, SmallAppMain.height);
		// camera.setParameters(params);

		// restart preview
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			
			Camera.Parameters params = camera.getParameters();
			params.setPreviewSize(width, height);
			camera.setParameters(params);
		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
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

	public void toggleCamera(int cameraId) {
		camera.stopPreview();
		camera.release();
		camera = Functions.getCameraInstance(cameraId);
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}

		int displayOrientation = Functions.getCameraDisplayOrientation(getResources().getConfiguration(), cameraId);
		camera.setDisplayOrientation(displayOrientation);

		camera.startPreview();
	}
}

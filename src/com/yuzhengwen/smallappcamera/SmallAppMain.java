package com.yuzhengwen.smallappcamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SmallAppMain extends SmallApplication
		implements OnClickListener, OnLongClickListener, OnCheckedChangeListener {

	Camera camera;
	private CameraView preview;
	FrameLayout f;
	ToggleButton toggle;
	public int width, height;
	MediaRecorder mRecorder;
	private boolean recording = false;
	public static final int MEDIA_TYPE_IMAGE = 0;
	public static final int MEDIA_TYPE_VIDEO = 1;
	public static final int CAMERA_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
	public static final int CAMERA_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
	public static int cameraId = CAMERA_BACK;
	public static final String PREF = "cameraPrefs";
	SharedPreferences sp;
	SharedPreferences.Editor e;
	public static final String CAMERA_TOGGLE_KEY = "camera";

	@Override
	protected void onCreate() {
		super.onCreate();
		setContentView(R.layout.main);

		SmallAppWindow.Attributes attr = getWindow().getAttributes();
		width = getResources().getDimensionPixelSize(R.dimen.width);
		height = getResources().getDimensionPixelSize(R.dimen.height);
		attr.width = width;
		attr.height = height;
		attr.flags |= SmallAppWindow.Attributes.FLAG_NO_TITLEBAR;
		attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
		attr.flags |= SmallAppWindow.Attributes.FLAG_HARDWARE_ACCELERATED;

		getWindow().setAttributes(attr);

		f = (FrameLayout) findViewById(R.id.camera_preview);
		toggle = (ToggleButton) findViewById(R.id.toggleCamera);
		toggle.setOnCheckedChangeListener(this);

		sp = getSharedPreferences(PREF, Context.MODE_PRIVATE);
		// cameraId = sp.getInt(CAMERA_TOGGLE_KEY, CAMERA_BACK) == 0 ?
		// CAMERA_BACK : CAMERA_FRONT;
		e = sp.edit();

		if (Functions.checkCameraHardware(this)) {
			camera = Functions.getCameraInstance(cameraId);

			if (camera != null) {
				int displayOrientation = Functions.getCameraDisplayOrientation(getResources().getConfiguration(),
						cameraId);
				camera.setDisplayOrientation(displayOrientation);
				Camera.Parameters params = camera.getParameters();
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				camera.setParameters(params);
				preview = new CameraView(this, camera);
				f.addView(preview);
				f.setOnLongClickListener(this);
				f.setOnClickListener(this);
			}
		} else
			Toast.makeText(this, "No camera detected!", Toast.LENGTH_SHORT).show();
	}

	// change camera
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		e.putInt(CAMERA_TOGGLE_KEY, isChecked ? 1 : 0);
		e.commit();
		if (isChecked)
			cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		else
			cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		preview.toggleCamera(cameraId);
	}

	/** Camera take picture method and save to internal sd */
	private void takePicture() {
		camera.takePicture(null, null, new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				File photo = Functions.getOutputMediaFile(MEDIA_TYPE_IMAGE);

				// GETTING IMAGE AND WRITING FILE------------------
				try {
					FileInputStream fis = new FileInputStream(photo);
					FileOutputStream fos = new FileOutputStream(photo);
					int b = 0;
					int current = 0;
					do {
						b = fis.read(data, current, (data.length - current));
						if (b >= 0)
							current += b;
						Log.d(CameraView.TAG, Integer.toString(b));
					} while (b > -1);

					// getting a bitmap from data[]
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					// using fileoutputstream to write bitmap to jpeg file
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);

					// releasing streams
					fos.flush();
					fos.close();
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Functions.galleryAddPic(photo, SmallAppMain.this);
			}
		});
	}

	private boolean prepareVideoRecorder() {
		File f = Functions.getOutputMediaFile(MEDIA_TYPE_VIDEO);
		mRecorder = new MediaRecorder();

		// Step 1: unlock camera and set media recorder camera
		camera.unlock();
		mRecorder.setCamera(camera);

		// Step 2: Set sources
		mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

		// Step 4: Set output file
		mRecorder.setOutputFile(f.toString());

		// Step 5: Set the preview output
		mRecorder.setPreviewDisplay(preview.getHolder().getSurface());

		// Step 6: Prepare configured MediaRecorder
		try {
			mRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(CameraView.TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(CameraView.TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		Functions.galleryAddPic(f, this);
		return true;
	}

	@Override
	public boolean onLongClick(View v) {
		if (!recording) {
			Toast.makeText(this, "Video Recording Started", Toast.LENGTH_SHORT).show();
			prepareVideoRecorder();
			mRecorder.start();
			recording = true;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		if (!recording) {
			takePicture();
			camera.startPreview();
		} else {
			Toast.makeText(this, "Video Recording Stopped", Toast.LENGTH_SHORT).show();
			mRecorder.stop();
			releaseMediaRecorder();
			recording = false;
		}
	}

	private void releaseMediaRecorder() {
		if (mRecorder != null) {
			mRecorder.reset(); // clear recorder configuration
			mRecorder.release(); // release the recorder object
			mRecorder = null;
			camera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (camera != null) {
			camera.release(); // release the camera for other applications
			camera = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseCamera();
	}
}

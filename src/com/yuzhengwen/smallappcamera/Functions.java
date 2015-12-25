package com.yuzhengwen.smallappcamera;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Functions {

	public static String TAG = CameraView.TAG;

	/** Determine and return the current camera display orientation */
	public static int getCameraDisplayOrientation(Configuration c, int cameraId) {
		// calculate display rotation
		int displayRotation = c.orientation == Configuration.ORIENTATION_PORTRAIT ? 0 : 90;

		CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + displayRotation) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - displayRotation + 360) % 360;
		}
		return result;
	}

	/** Check if this device has a camera */
	public static boolean checkCameraHardware(Context c) {
		if (c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		}
		return false;
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(int cameraId) {

		int cameraCount;
		Camera c = null;
		try {
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras();
			for (int camId = 0; camId < cameraCount; camId++) {
				Camera.getCameraInfo(camId, cameraInfo);
				if (cameraInfo.facing == cameraId)
					c = Camera.open(camId);
			}
			// attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Context context = new SmallAppMain();
			Toast.makeText(context, "CAmera unavailable (in use or doesn't exist", Toast.LENGTH_SHORT).show();
		}
		return c;
	}

	/**
	 * Get a valid and unique output file to store data (.jpeg or .mp4) in.
	 * MediaType 0 = pic, 1 = vid
	 */
	public static File getOutputMediaFile(int mediaType) {
		// CREATING FILE------------------
		int append = 0;
		File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Pictures/SmallAppCamera");
		// create directory if doesnt exist
		if (!dir.exists())
			dir.mkdir();
		// if photo or video
		File f;
		if (mediaType == 0)
			f = new File(dir + File.separator + "pic" + append + ".png");
		else
			f = new File(dir + File.separator + "vid" + append + ".mp4");

		// loop until get a unique filename
		while (f.exists()) {
			append++; // increment number to append behind filename
			if (mediaType == 0)
				f = new File(dir + File.separator + "pic" + append + ".png");
			else
				f = new File(dir + File.separator + "vid" + append + ".mp4");
		}
		// creating media file with the unique filename
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e1) {
				Log.d(CameraView.TAG, "IO Exception: " + e1);
			}
		}
		return f;
	}

	/** add photo to gallery */
	public static void galleryAddPic(File f, Context c) {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		c.sendBroadcast(mediaScanIntent);
	}
}

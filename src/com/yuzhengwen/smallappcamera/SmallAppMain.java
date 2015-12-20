package com.yuzhengwen.smallappcamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallApplication;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

public class SmallAppMain extends SmallApplication implements OnClickListener {

	private Camera camera;
	private CameraView preview;
	FrameLayout f;

	@Override
	protected void onCreate() {
		super.onCreate();
		f = new FrameLayout(this);
		setContentView(f);
		SmallAppWindow.Attributes attr = getWindow().getAttributes();
		attr.width = getResources().getDimensionPixelSize(R.dimen.width);
		attr.height = getResources().getDimensionPixelSize(R.dimen.height);
		attr.minWidth = getResources().getDimensionPixelSize(R.dimen.width);
		attr.minHeight = getResources().getDimensionPixelSize(R.dimen.height);
		attr.maxWidth = getResources().getDimensionPixelSize(R.dimen.width);
		attr.maxHeight = getResources().getDimensionPixelSize(R.dimen.height);
		attr.flags |= SmallAppWindow.Attributes.FLAG_NO_TITLEBAR;

		getWindow().setAttributes(attr);

		if (checkCameraHardware()) {
			camera = getCameraInstance();
			if (camera != null) {
				camera.setDisplayOrientation(90);
				preview = new CameraView(this, camera);
				f.addView(preview);
				f.setOnClickListener(this);
			}
		} else
			Toast.makeText(this, "No camera detected!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
		takePicture();
		camera.startPreview();
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware() {
		if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		}
		return false;
	}

	/** A safe way to get an instance of the Camera object. */
	public Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Toast.makeText(SmallAppMain.this, "CAmera unavailable (in use or doesn't exist", Toast.LENGTH_SHORT).show();
		}
		return c;
	}

	/** Camera take picture method and save to internal sd */
	private void takePicture() {
		camera.takePicture(null, null, new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

				// CREATING FILE------------------
				int append = 0;
				File dir = new File(
						Environment.getExternalStorageDirectory() + File.separator + "Pictures/SmallAppCamera");
				// create directory if doesnt exist
				if (!dir.exists())
					dir.mkdir();
				File photo = new File(dir + File.separator + "pic" + append + ".png");

				// loop until get a unique filename
				while (photo.exists()) {
					append++; // increment number to append behind filename
					photo = new File(dir + File.separator + "pic" + append + ".png");
				}
				// creating jpeg with the unique filename
				if (!photo.exists()) {
					try {
						photo.createNewFile();
					} catch (IOException e1) {
						Log.d(CameraView.TAG, "IO Exception: " + e1);
					}
				}

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
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (camera != null) {
			camera.release();
			camera = null;
			Log.d(CameraView.TAG, "CAMERA RELEASED");
		}
	}
}

package de.hsrm.objectify.camera;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import de.hsrm.objectify.R;
import de.hsrm.objectify.rendering.ObjectViewer;
import de.hsrm.objectify.ui.BaseActivity;
import de.hsrm.objectify.utils.ExternalDirectory;
import de.hsrm.objectify.utils.ImageHelper;

/**
 * This activity opens up the camera and processes the shot photos.
 * 
 * @author kwolf001
 * 
 */
public class CameraActivity extends BaseActivity {

	private static final String TAG = "CameraActivity";
	private CameraPreview cameraPreview;
	private Button triggerPicture;
	private LinearLayout left, right, up, down, shadow, progress;
	private int counter = 1;
	private Context context;
	private CompositePicture compositePicture;
	private static final int NUMBER_OF_PICTURES = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		context = this;
		disableActionBar();
		
		cameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
		left = (LinearLayout) findViewById(R.id.light_left);
		right = (LinearLayout) findViewById(R.id.light_right);
		up = (LinearLayout) findViewById(R.id.light_up);
		down = (LinearLayout) findViewById(R.id.light_down);
		shadow = (LinearLayout) findViewById(R.id.shadow);
		progress = (LinearLayout) findViewById(R.id.progress);
		triggerPicture = (Button) findViewById(R.id.trigger_picture_button);
		triggerPicture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				shadow.setVisibility(View.VISIBLE);
				takePictures();
			}
		});
		
		setScreenBrightness(1);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	/**
	 * Setting screen brightness. A value of less than 0, the default, means to
	 * use the preferred screen brightness.
	 * 
	 * @param intensity
	 *            value between 0 and 1
	 */
	private void setScreenBrightness(float intensity) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = intensity;
		getWindow().setAttributes(lp);
	}
	
	/**
	 * Takes several pictures in sequence. 
	 */
	private void takePictures() {
		cameraPreview.startPreview();
		setLightning();
		compositePicture = new CompositePicture();
		SystemClock.sleep(100);
		cameraPreview.takePicture(null, null, jpegCallback());
	}
	
	/**
	 * Sets up the white spaces on the screen to light up different parts of the
	 * object
	 */
	private void setLightning() {
		switch (counter) {
		case 1:
			left.setVisibility(View.VISIBLE);
			break;
		case 2:
			left.setVisibility(View.INVISIBLE);
			right.setVisibility(View.VISIBLE);
			break;
		case 3:
			right.setVisibility(View.INVISIBLE);
			up.setVisibility(View.VISIBLE);
			break;
		case 4:
			up.setVisibility(View.INVISIBLE);
			down.setVisibility(View.VISIBLE);
			break;
		default:
			darken();
		}
		
	}
	
	/**
	 * Makes the whole screen black.
	 */
	private void darken() {
		left.setVisibility(View.INVISIBLE);
		right.setVisibility(View.INVISIBLE);
		up.setVisibility(View.INVISIBLE);
		down.setVisibility(View.INVISIBLE);
	}
	
	private PictureCallback jpegCallback() {
		PictureCallback callback = new PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				switch (counter) {
				case 1:
					// left image
					compositePicture.setPicture1(data);
					break;
				case 2:
					// right image
					compositePicture.setPicture2(data);
					break;
				case 3:
					// upper image
					// TODO saving image for texture
					compositePicture.setPicture3(data);
					break;
				case 4:
					// lower image
					compositePicture.setPicture4(data);
					break;
				}
				if (counter < NUMBER_OF_PICTURES) {
					counter++;
					takePictures();
				} else {
					Log.d(TAG, "Photos taken. Calculating Object.");
					counter = 1;
					new CalculateModel().execute();
				}
			}
		};

		return callback;
	}
	
	/**
	 * Calculates normalmap and heightmap from given photos. The parameters
	 * for the AsyncTask are: <ul>
	 * <li>String: Path to an image used for texture</li>
	 * <li>Void: We use the static {@link CompositePicture} for memory reasons</li>
	 * <li>Boolean: Indicating whether we were successful or not</li></ul>
	 * 
	 * @author kwolf001
	 * 
	 */
	private class CalculateModel extends AsyncTask<String, Void, Boolean> {
		
		private static final String TAG = "CalculateModel";
		
		@Override
		protected void onPreExecute() {
			darken();
			progress.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			String path = params[0];
//			try {
//				path = ExternalDirectory.getExternalDirectory() + "/foo.jpg";
//				FileOutputStream fos = new FileOutputStream(path);
//				BufferedOutputStream bos = new BufferedOutputStream(fos);
//				image.compress(CompressFormat.JPEG, 100, bos);
//				bos.flush();
//				bos.close();
//			} catch (FileNotFoundException e) {
//				Log.e(TAG, e.getMessage());
//			} catch (IOException e) {
//				Log.e(TAG, e.getMessage());
//			}
//			SystemClock.sleep(1000);
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progress.setVisibility(View.GONE);
			Intent main = new Intent(context, ObjectViewer.class);
			startActivity(main);
			((Activity) context).finish();
		}
		

	}
	
}

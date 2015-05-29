package djps.br.com.video;

import java.io.IOException;
import java.util.jar.Manifest;

import junit.runner.Version;

import djps.br.com.cameramail.CameraPreview;
import djps.br.com.cameramail.R;
import djps.br.com.utils.Arquivo;

import android.Manifest.permission;
import android.app.Activity;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
//import android.media.CamcorderProfile;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraVideoActivity extends Activity {
	private boolean isRecording;
	private MediaRecorder mMediaRecorder;
	private Camera mCamera;
	private CameraPreview mPreview;
	private WakeLock wakeLock;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		
		// so faz as proximas acoes se tiver camera
		if (checkCameraHardware(getBaseContext())) {
			mCamera = getCameraInstance();

			// Criar o preview para a camera e seta-lo para nossa activity
			if (mCamera != null) {
				
				mPreview = new CameraPreview(this, mCamera);
				FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
				preview.addView(mPreview);
			}

			try {
				Arquivo.carregarDiretoriosAplicacao();
			} catch (IOException e) {
				Log.i("djps",
						"Problema ao carregar arquivo aplicacao "
								+ e.getMessage());
			}
		} else {
			Toast.makeText(this, "Sem camera instalada!!!", Toast.LENGTH_SHORT)
					.show();
		}
		
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		// Obtém a instância do PowerManager
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		// Liga o display do aparelho
		wakeLock = pm.newWakeLock(
				// segura a tela ligada com brilho maximo
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
				// Após liberar a tela para apagar,
				// mantém a tela ligada por um pouco
				// mais de tempo
				PowerManager.ON_AFTER_RELEASE,
				// Tag para debug
		"DJPS");
		
		// Liga a tela 
		wakeLock.acquire();
	}
	
	@Override
	protected void onStop() {
		
		super.onStop();
		if (wakeLock != null) {
			wakeLock.release();
		}
	}
	
	private boolean prepareVideoRecorder(){
		
		mMediaRecorder = null;
		System.gc();
	    mMediaRecorder = new MediaRecorder();	    

	    // Step 1: Unlock and set camera to MediaRecorder
    	mCamera.unlock();
	    mMediaRecorder.setCamera(mCamera);

	    // Step 2: Set sources
	    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

	    if(Build.VERSION.SDK_INT >= 8){
	    	// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
	    	mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
	    }else{
	    	//Step 3 : Para os casos de Api 2.1
		    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
	    	//Step 3 : Para os casos de Api 2.1
	    }
	    
	    // Step 4: Set output file
	    mMediaRecorder.setOutputFile(Arquivo.getOutputMediaFile(Arquivo.MEDIA_TYPE_VIDEO, Arquivo.recuperarPathPastaVid()).toString());

	    // Step 5: Set the preview output
	    mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

	    // Step 6: Prepare configured MediaRecorder
	    try {
	        mMediaRecorder.prepare();
	    } catch (IllegalStateException e) {
	        Log.d("DJPS", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	        Log.d("DJPS", "IOException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    }
	    return true;
	}
	
	
	@Override
	protected void onDestroy() {
		super.onStop();
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
		
		if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
        }
		
		System.gc();
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/** 
	 * Forma segura de conseguir uma instancia do objeto camera. 
	 * */
	private Camera getCameraInstance() {
		Camera c = null;
		System.gc();
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Toast.makeText(getBaseContext(), "Camera não foi Aberta: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
			Log.i("DJPS", "Camera não foi Aberta: " + e.getLocalizedMessage());
			finish();
		}
		return c; // retorna null se a camera esta insdisponivel
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			onKeyDown(KeyEvent.KEYCODE_CAMERA, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CAMERA));
		}
		
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			if (isRecording && mMediaRecorder!= null) {
                // stop recording and release camera
                mMediaRecorder.stop();  // stop the recording
                releaseMediaRecorder(); // release the MediaRecorder object
                mCamera.lock();       // take camera access back from MediaRecorder

                // inform the user that recording has stopped
                Toast.makeText(getBaseContext(), "Gravação Parada!!!", Toast.LENGTH_SHORT).show();
                
                isRecording = false;
            } else {
                // initialize video camera
                if (prepareVideoRecorder()) {
                    // Camera is available and unlocked, MediaRecorder is prepared,
                    // now you can start recording
                    mMediaRecorder.start();
                    // inform the user that recording has started
                    Toast.makeText(getBaseContext(), "Gravando!!!!!!", Toast.LENGTH_SHORT).show();
                    
                    isRecording = true;
                } else {
                    // prepare didn't work, release the camera
                    releaseMediaRecorder();
                    // inform user
                }
            }			
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            System.gc();
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0, 1, 0, "VIDEO");
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		if(item.getItemId() == 1){
			startActivity(new Intent(getBaseContext(), VideoPlayActivity.class));
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
}
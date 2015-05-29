package djps.br.com.cameramail;

import djps.br.com.video.CameraVideoActivity;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class PrincipalActivity extends Activity{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.principal);
		
		
		ImageButton btFoto = (ImageButton) findViewById(R.principal.btFoto);
		ImageButton btVideo = (ImageButton) findViewById(R.principal.btVideo);
		
		btFoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(getBaseContext(),CameraMailActivity.class);
				startActivity(it);
			}
		});
		btVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(getBaseContext(),CameraVideoActivity.class);
				startActivity(it);
			}
		});
	}
}

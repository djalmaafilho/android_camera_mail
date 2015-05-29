package djps.br.com.video;

import djps.br.com.cameramail.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayActivity extends Activity {

	private VideoView videoView;
	private MediaController mediaControler;

    //Iniciando esta implementacao. Objetivo receber o path do video  a
    //partir de uma intent que aponte para o video.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		videoView = (VideoView) findViewById(R.id.videoView1);
		videoView
				.setVideoPath("/sdcard/CameraMail/VID/VID_20120624_143712.mp4");
		mediaControler = new MediaController(this);
		videoView.setMediaController(mediaControler);
	}
	
	@Override
	protected void onResume() {
		videoView.start();
		super.onResume();
	}
}
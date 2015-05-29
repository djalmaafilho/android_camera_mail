package djps.br.com.cameramail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InitReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent it = new Intent(context, CameraMailActivity.class);
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(it);
	}
}

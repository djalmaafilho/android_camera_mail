package djps.br.com.cameramail;

import java.io.File;
import djps.br.com.utils.Arquivo;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ExibirFotosActivity extends Activity implements Runnable {

	private Handler handler;
	private File file, auxFile;
	private int indice;
	private ImageView imagem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exibir_fotos);
		imagem = (ImageView) findViewById(R.id.imageView1);
		file = new File(Arquivo.recuperarPathPastaDim());
		this.handler = new Handler();
	}

	/*
	 * inicia a acao de atualizacao das imagens (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		this.handler.post(this);
	}

	@Override
	protected void onStop() {
		imagem.setImageBitmap(null);
		Runtime.getRuntime().gc();
		super.onStop();
	}

	/*
	 * Para a acao de atualizacao das imagens (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		this.handler.removeCallbacks(this);
		super.onPause();
	}

	@Override
	public void run() {
		
		
		if (file.listFiles().length == 0) {
			imagem.setVisibility(View.INVISIBLE);
			Toast t = Toast.makeText(getBaseContext(), "Pasta de fotos vazia",
					Toast.LENGTH_LONG);
			t.show();
			finish();
			return;
		}

		if (indice < file.listFiles().length) {
			auxFile = file.listFiles()[indice];
			// usando imagem de back ground
			//imagem.setImageURI(Uri.fromFile(aux));
			imagem.setImageBitmap(null);
			Runtime.getRuntime().gc();
			//tentando resolver problema de latencia
			imagem.setImageBitmap(BitmapFactory.decodeFile(auxFile.getAbsolutePath()));
			indice++;
		} else {
			indice = 0;
		}
		this.handler.postDelayed(this, 3000);
	}
}
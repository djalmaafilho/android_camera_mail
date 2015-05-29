package djps.br.com.cameramail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import djps.br.com.utils.Arquivo;
import djps.br.com.utils.BancoDeDados;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraMailActivity extends Activity implements OnClickListener,
		PictureCallback {
	private Button btFotografar;
	private Button btFechar;
	private Button btPreview;
	private CheckBox chekBoxEnviarMail;
	private BancoDeDados db;
	private CameraCompressao compressao;
	private Camera mCamera;
	private CameraPreview mPreview;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		compressao = CameraCompressao.NENHUMA;
		btFotografar = (Button) findViewById(R.id.button_capture);
		btFechar = (Button) findViewById(R.id.bt_fechar);
		btPreview = (Button) findViewById(R.id.bt_preview);
		chekBoxEnviarMail = (CheckBox) findViewById(R.id.checkEnviarEmail);

		btFotografar.setOnClickListener(this);
		btFechar.setOnClickListener(this);
		btPreview.setOnClickListener(this);

		// so faz as proximas acoes se tiver camera
		if (checkCameraHardware(getBaseContext())) {
			mCamera = getCameraInstance();
			// Criar o preview para a camera e seta-lo para nossa activity
			if (mCamera != null) {
				mPreview = new CameraPreview(this, mCamera);
				FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
				preview.addView(mPreview);
			} else {
				mensagemToast("camera não está disponivel!!!");
				finish();
				return;
			}

			try {
				Arquivo.carregarDiretoriosAplicacao();
			} catch (IOException e) {
				mensagemToast("Problema ao carregar arquivo aplicacao");
				finish();
				return;
			}

		} else {
			mensagemToast("Sem camera instalada!!!");
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		btPreview.setVisibility(View.GONE);
		btFotografar.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onClick(View v) {

		if (v == btFechar) {
			this.finish();
		} else if (v == btFotografar) {
			btFotografar.setVisibility(View.GONE);
			btPreview.setVisibility(View.VISIBLE);
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					mCamera.takePicture(null, null, CameraMailActivity.this);
				}
			});
		} else if (v == btPreview) {
			btPreview.setVisibility(View.GONE);
			btFotografar.setVisibility(View.VISIBLE);
			mCamera.startPreview();
		}
	}

	@Override
	protected void onDestroy() {
		super.onStop();
		if (mCamera != null) {
			mCamera.release();
		}
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
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera não disponivel (em uso ou não existe)
			Log.i("DJPS", "Camera não foi Aberta: " + e.getLocalizedMessage());

		}
		return c; // retorna null se a camera esta insdisponivel
	}

	/*
	 * Metodo executado quando a imagem da foto retorna (non-Javadoc)
	 * 
	 * @see android.hardware.Camera.PictureCallback#onPictureTaken(byte[],
	 * android.hardware.Camera)
	 */
	@Override
	public void onPictureTaken(final byte[] data, Camera camera) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					File pictureFile = Arquivo.getOutputMediaFile(1,
							Arquivo.recuperarPathPastaDim());
					if (pictureFile == null) {
						mensagemToast("Arquivo nao foi criado ");
						Log.d("DJPS",
								"Arquivo nao foi criado, cheque as permissoes storage: ");
						return;
					}
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.flush();
					fos.close();
					fos = null;
					System.gc();

					// abre envio de foto se a opcao estiver marcada
					if (chekBoxEnviarMail.isChecked()) {
						try {
							enviarEmail(pictureFile);
						} catch (Exception e) {
							mensagemToast("Problema durante tentativa de envio de email ");
						}
					}

				} catch (FileNotFoundException e) {
					Log.d("DJPS", "Arquivo nao encontrado: " + e.getMessage());
				} catch (IOException e) {
					Log.d("DJPS",
							"Erro ao tentar acessar o arquivo: "
									+ e.getMessage());
				}

			}
		}).start();

		// Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
		// data.length);
		// bitmap.compress(Bitmap.CompressFormat.JPEG,
		// compressao.getQualidade(), fos);
		// fos.flush();
		// fos.close();
	}

	/*
	 * Envia a toto por email
	 */
	public synchronized void enviarEmail(File file)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		String nomeArquivo = file.getName();

		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);

		// destinatario
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				Arquivo.carregarContatos());

		// titulo do email
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, nomeArquivo);

		// corpo do email
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Foto "
				+ nomeArquivo + " by Camera Mail");

		// permissao para enviar o arquivo anexo no email
		emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		// anexo
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

		// formato do mime deve ser o do anexo
		emailIntent.setType("image/jpg");
		startActivity(Intent.createChooser(emailIntent, "Enviar Foto"));

	}

	/**
	 * Este metodo e chamado na primeira vez em que a aplicacao e criada. Nele
	 * carregaremos as opcoes do menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		SubMenu subMenu = menu.addSubMenu("Email");
		subMenu.setIcon(R.drawable.lapis);
		subMenu.add(0, 1, 0, "Cadastrar Email");
		subMenu.add(0, 2, 0, "Lista Emails");

		subMenu = menu.addSubMenu("Fotos");
		subMenu.setIcon(R.drawable.moldura);
		subMenu.add(0, 3, 0, "Slides");
		subMenu.add(0, 4, 0, "Galeria");

		subMenu = menu.addSubMenu("Compressao");
		subMenu.add(0, 5, 0, "ALTA");
		subMenu.add(0, 6, 0, "MEDIA");
		subMenu.add(0, 7, 0, "MODERADA");
		subMenu.add(0, 8, 0, "NENHUMA");

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent it = null;
		switch (item.getItemId()) {
		case 1:
			it = new Intent("CAMERA_MAIL_CAD_CONTATOS");
			startActivity(it);
			break;
		case 2:
			it = new Intent("CAMERA_MAIL_CONTATOS");
			startActivity(it);
			break;
		case 3:
			it = new Intent("CAMERA_MAIL_FOTOS");
			startActivity(it);
			break;
		case 4:
			it = new Intent("CAMERA_MAIL_GALERIA");
			startActivity(it);
			break;
		case 5:
			compressao = CameraCompressao.ALTA;
			parametrizarCamera();
			break;
		case 6:
			compressao = CameraCompressao.MEDIA;
			parametrizarCamera();
			break;
		case 7:
			compressao = CameraCompressao.MODERADA;
			parametrizarCamera();
			break;
		case 8:
			compressao = CameraCompressao.NENHUMA;
			parametrizarCamera();
			break;
		default:
			return false;
		}
		return true;
	}

	private void parametrizarCamera() {

		Parameters parametros = mCamera.getParameters();
		parametros.setSceneMode(Parameters.SCENE_MODE_ACTION);

		// parametros.setJpegQuality(qualidade);
		// parametros.setPictureFormat(256);
		// parametros.setPreviewSize(320, 480);
		// parametros.setPictureSize(parametros.getPictureSize().width/10,
		// parametros.getPictureSize().height/10);

		mCamera.setParameters(parametros);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_CAMERA && btFotografar.isShown()) {
			onClick(btFotografar);
		}
		return super.onKeyDown(keyCode, event);
	}

	private void mensagemToast(String mesage) {
		Toast t = Toast.makeText(getApplicationContext(), mesage,
				Toast.LENGTH_LONG);
		t.show();
	}
}
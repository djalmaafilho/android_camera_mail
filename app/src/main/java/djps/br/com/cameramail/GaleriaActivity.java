package djps.br.com.cameramail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import djps.br.com.entidade.Foto;
import djps.br.com.utils.Arquivo;
import djps.br.com.utils.CameraMailException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public class GaleriaActivity extends Activity {
	private List<Foto> listaFoto;
	private File arquivo;
	private Bitmap bitmapDefault;
	private int indice;
	private Toast toast;
	private Gallery galeria;
	private ProgressDialog dialog;
	private Handler hand, handAtualizaGaleria;
	private static int QTD_THREADS_PARALELAS = 1;
	private BaseAdapter baseAdapter;
	private List<Long> listIdThreadAutorizada;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Arquivo.carregarDiretoriosAplicacao();
			arquivo = new File(Arquivo.recuperarPathPastaDim());
		} catch (IOException e) {
			mensagemToast("Problema ao acessar pasta de fotos");
			finish();
			return;
		}

		setContentView(R.layout.galeria_fotos);
		galeria = (Gallery) findViewById(R.id.gallery1);
		dialog = ProgressDialog.show(this, "Galeria de Fotos",
				"carregando...", true);
		bitmapDefault = BitmapFactory.decodeResource(getResources(),
				R.drawable.foto_moldura);
		toast = new Toast(getApplicationContext());
		hand = new Handler();
		handAtualizaGaleria = new Handler();
		carregarGaleria();

	}

	/*
	 * inicia o processo de carregamento da galeria, caso existam fotos para
	 * serem carregadas da pasta de fotos da aplicacao
	 */
	private void carregarGaleria() {
		if (arquivo.list().length == 0) {
			dialog.dismiss();
			dialog = null;
			System.gc();
			mensagemToast("Pasta de fotos vazia");
			finish();
		} else {
			listaFoto = new ArrayList<Foto>();
			for (File arquivoAux : arquivo.listFiles()) {
				listaFoto.add(new Foto(arquivoAux, null));
			}

			// chamando threads paralelas mais desempenho
			listIdThreadAutorizada = new ArrayList<Long>();
			Thread t;
			for (int i = 0; i < QTD_THREADS_PARALELAS; i++) {
				t = new Thread(runnableCarregarBitmapsGaleria);
				listIdThreadAutorizada.add(t.getId());
				t.start();
			}
			hand.postDelayed(runnableIniciarGaleria, 1000);
		}
	}

	/*
	 * prepara a galeria para ser novamente carregada. apos esse processo
	 * deve-se carregar novamente a galeria
	 */
	private void resetarGaleria() {
		listIdThreadAutorizada = null;
		hand.removeCallbacks(runnableIniciarGaleria);
		listaFoto = null;
		zerarIndice();
		System.gc();
	}

	/*
	 * inicializa a galeria com listeners e adapters necessarios
	 */
	private Runnable runnableIniciarGaleria = new Runnable() {
		public void run() {
			setAdapterGaleria();
			setarListenerGaleria();
			if (dialog != null) {
				dialog.dismiss(); // parar barra de progresso
				dialog = null;
			}
			System.gc();
		}

		public void setarListenerGaleria() {
			galeria.setOnItemClickListener(new OnItemClickListener() {
				ImageView imgViewAux;

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int posicao, long arg3) {
					/*
					 * nao deve mudar imagem se toast nao acabou. limpa o toast
					 * anterior primeiro e precisa clicar de novo. evita
					 * problema com multiplos cliques.
					 */
					if (toast != null && toast.getView() != null
							&& toast.getView().isShown()) {
						limparToast();
						return;
					}

					// caso a lista ainda nao esteja completamente carregada
					if (listaFoto.get(posicao) != null) {
						// limpar dados caso existam
						imgViewAux = null;
						limparToast();

						imgViewAux = new ImageView(GaleriaActivity.this);
						imgViewAux.setImageBitmap(BitmapFactory
								.decodeFile(listaFoto.get(posicao).getArquivo()
										.getAbsolutePath()));
						toast.setView(imgViewAux);
						toast.setDuration(Toast.LENGTH_LONG);
						toast.show();
					}
				}
			});

			galeria.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int posicao, long arg3) {
					/*
					 * nao deve mudar imagem se toast nao acabou. limpa o toast
					 * anterior primeiro e precisa clicar de novo. evita
					 * problema com multiplos cliques.
					 */
					if (toast != null && toast.getView() != null
							&& toast.getView().isShown()) {
						limparToast();

					} else if (listaFoto.get(posicao) != null) {

						final int indice = posicao;
						AlertDialog.Builder alerta = new AlertDialog.Builder(
								GaleriaActivity.this);
						alerta.setTitle("DELETAR ARQUIVO");
						alerta.setMessage(listaFoto.get(indice).getArquivo()
								.getName());
						alerta.setPositiveButton("SIM", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								listaFoto.get(indice).getArquivo().delete();// deletar
																			// arquivo
								resetarGaleria();
								// apesar de mais custoso. carregar novamente a
								// galeria reflete o estado real da pasta de
								// fotos.
								carregarGaleria();
							}
						});

						alerta.setNegativeButton("NÃO", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});

						alerta.show();
						return true;
					}

					return false;
				}
			});
		}

		public void setAdapterGaleria() {
			baseAdapter = new BaseAdapter() {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {

					convertView = new ImageView(getApplicationContext());
					if (listaFoto.get(position).getBitmap() != null) {
						((ImageView) convertView).setImageBitmap(listaFoto.get(
								position).getBitmap());
					} else {
						((ImageView) convertView).setImageBitmap(bitmapDefault);
					}
					((ImageView) convertView)
							.setScaleType(ImageView.ScaleType.CENTER);
					((ImageView) convertView).setAdjustViewBounds(true);
					((ImageView) convertView).setPadding(20, 0, 20, 0);

					return convertView;
				}

				@Override
				public long getItemId(int position) {
					return position;
				}

				@Override
				public Object getItem(int position) {
					return position;
				}

				@Override
				public int getCount() {

					return listaFoto.size();
				}
			};

			galeria.setAdapter(baseAdapter);
		}
	};

	/*
	 * Executa em paralelo a busca pelos arquivos de fotos na pasta da aplicacao
	 * , e carrega lista de bitmaps para serem colocados nas posicoes da
	 * galeria. Apenas Threads autorizadas pela requisicao de atualizacao
	 * corrente podem realizar a acao.
	 */
	Runnable runnableCarregarBitmapsGaleria = new Runnable() {

		@Override
		public void run() {
			int idThread;

			try {
				idThread = recuperarIndiceInsercao(Thread.currentThread()
						.getId());
			} catch (CameraMailException e1) {
				return;
			}

			Bitmap btm = null;
			while (idThread < listaFoto.size()) {
				btm = BitmapFactory.decodeFile(listaFoto.get(idThread)
						.getArquivo().getAbsolutePath());
				btm = Bitmap.createScaledBitmap(btm, 60, 60, false);
				try {
					inserirBitmap(btm, idThread, Thread.currentThread().getId());
					handAtualizaGaleria.post(runnableAtualizaGaleria);
					btm = null;
					System.gc();
					idThread = recuperarIndiceInsercao(Thread.currentThread()
							.getId());
				} catch (CameraMailException e) {
					break;
				}
			}
		}
	};

	/*
	 * Forca o redesenho da tela e atualizacao do estado da galeria.
	 */
	Runnable runnableAtualizaGaleria = new Runnable() {
		public void run() {
			int posicaoInicio = galeria.getFirstVisiblePosition();
			int posicaoFim = galeria.getLastVisiblePosition();
			galeria.setAdapter(baseAdapter);
			galeria.setSelection((posicaoInicio + posicaoFim) / 2);
		}
	};

	/*
	 * reinicia o valor do indice de insercao de bitmaps na lista de bitmaps
	 */
	private synchronized void zerarIndice() {
		this.indice = 0;
	}

	/*
	 * Verifica se a thread esta autorizada pela requisicao de atualizacao
	 * corrente, a realizar acoes.
	 */
	private boolean isThreadAutorizada(long idThread) {
		if (listIdThreadAutorizada == null) {
			return false;
		}

		for (int j = 0; j < listIdThreadAutorizada.size(); j++) {
			if (listIdThreadAutorizada.get(j).longValue() == idThread) {
				return true;
			}
		}

		return false;
	}

	/*
	 * recupera a proxima posicao a ser inserida de um novo bitmap dentro da
	 * lista de bitmaps.
	 */
	private synchronized int recuperarIndiceInsercao(long idThread)
			throws CameraMailException {
		if (!isThreadAutorizada(idThread)) {
			throw new CameraMailException(
					"Thread não autorizada a recuperar informação");
		}
		int i = indice++;
		return i;
	}

	/*
	 * insere um novo bitmap, na posicao indicada, dentro lista de bitmaps
	 */
	private synchronized void inserirBitmap(Bitmap bitmap, int posicao,
			long idThread) throws CameraMailException {
		if (!isThreadAutorizada(idThread)) {
			throw new CameraMailException(
					"Thread não autorizada a alterar informação");
		}

		if (posicao < listaFoto.size()) {
			listaFoto.get(posicao).setBitmap(null);
			System.gc();
			listaFoto.get(posicao).setBitmap(bitmap);
		}
	}

	// cancela o toast caso ele esteja sendo exibido na hora
	// de fechar a aplicacao
	public void onBackPressed() {
		if (toast != null && toast.getView() != null
				&& toast.getView().isShown()) {
			limparToast();
		} else {
			super.onBackPressed();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		resetarGaleria();
	}

	/*
	 * Mostra nova mensagem temporarea na tela
	 */
	private void mensagemToast(String mesage) {
		Toast t = Toast.makeText(getApplicationContext(), mesage,
				Toast.LENGTH_LONG);
		t.show();
	}

	/*
	 * Realiza reinicializacao do toast principal da aplicacao e chama a limpeza
	 * de objetos logo apos.
	 */
	private void limparToast() {
		toast.cancel();
		toast.setView(null);
		System.gc();
	}
}
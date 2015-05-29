package djps.br.com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Arquivo {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final String NOME_PASTA = "CameraMail";
	public static final String SUB_PASTA_SIS = "SIS";
	public static final String SUB_PASTA_DIM = "DIM";
	public static final String SUB_PASTA_VID = "VID";
	public static final String ARQUIVO_CONTATOS = "contatos";
	private static File pastaSis;
	private static File pastaDim;
	private static File pastaVid;

	/** Create a file Uri for saving an image or video */
	public static Uri getOutputMediaFileUri(int type, String path) {
		return Uri.fromFile(getOutputMediaFile(type, path));
	}

	/** Create a File for saving an image or video */
	public static File getOutputMediaFile(int type, String path) {

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			Log.i("DJPS", "Path: " + path);
			mediaFile = new File(path + File.separator + "IMG_" + timeStamp
					+ ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(path + File.separator + "VID_" + timeStamp
					+ ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	public static void carregarDiretoriosAplicacao() throws IOException {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		// 2.1
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), NOME_PASTA);

		Log.i("DJPS", mediaStorageDir.getAbsolutePath());

		// 2.2
		// File mediaStorageDir = new
		// File(Environment.getExternalStoragePublicDirectory(
		// Environment.DIRECTORY_PICTURES), NOME_PASTA);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdir()) {
				Log.d(NOME_PASTA, "Falha ao criar o diretorio " + NOME_PASTA);
			}
		}

		pastaSis = new File(mediaStorageDir.getAbsolutePath(), SUB_PASTA_SIS);

		if (!pastaSis.exists()) {
			pastaSis.mkdir();
			if (!pastaSis.exists()) {
				Log.d(SUB_PASTA_SIS, "Falha ao criar o diretorio "
						+ SUB_PASTA_SIS);
			}
		}

		pastaDim = new File(mediaStorageDir.getAbsolutePath(), SUB_PASTA_DIM);

		if (!pastaDim.exists()) {
			pastaDim.mkdir();
			if (!pastaDim.exists()) {
				Log.d(SUB_PASTA_DIM, "Falha ao criar o diretorio "
						+ SUB_PASTA_DIM);
			}
		}

		pastaVid = new File(mediaStorageDir.getAbsolutePath(), SUB_PASTA_VID);

		if (!pastaVid.exists()) {
			pastaVid.mkdir();
			if (!pastaVid.exists()) {
				Log.d(SUB_PASTA_VID, "Falha ao criar o diretorio "
						+ SUB_PASTA_VID);
			}
		}
		
		criarArquivoContatos();

	}

	public static String recuperarPathPastaSis() {
		return pastaSis.getAbsolutePath();
	}

	public static String recuperarPathPastaDim() {
		return pastaDim.getAbsolutePath();
	}

	public static String recuperarPathPastaVid() {
		return pastaVid.getAbsolutePath();
	}	
	
	public static void criarArquivoContatos() throws IOException {
		File contatos = new File(recuperarPathPastaSis(), ARQUIVO_CONTATOS);
		if (!contatos.exists()) {
			contatos.createNewFile();
			FileOutputStream fos = new FileOutputStream(contatos);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(new String[] { "" });

			fos.close();
			oos.close();
		}
	}
	public static void salvarContatos(String[] listaContatos)
			throws IOException {

		File contatos = new File(recuperarPathPastaSis(), ARQUIVO_CONTATOS);
		FileOutputStream fos = new FileOutputStream(contatos);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(listaContatos);

		fos.close();
		oos.close();
	}

	public static String[] carregarContatos() throws StreamCorruptedException,
			IOException, ClassNotFoundException {

		File contatos = new File(Arquivo.recuperarPathPastaSis(),
				Arquivo.ARQUIVO_CONTATOS);
		FileInputStream fis = new FileInputStream(contatos);
		ObjectInputStream ois = new ObjectInputStream(fis);

		return (String[]) ois.readObject();
	}
}
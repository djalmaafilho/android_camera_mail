package djps.br.com.entidade;

import java.io.File;

import android.graphics.Bitmap;

public class Foto {

	private File arquivo;
	private Bitmap bitmap;

	public Foto(File arquivo, Bitmap bitmap) {
		super();
		this.arquivo = arquivo;
		this.bitmap = bitmap;
	}

	public File getArquivo() {
		return arquivo;
	}

	public void setArquivo(File arquivo) {
		this.arquivo = arquivo;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

}

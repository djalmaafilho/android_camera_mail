package djps.br.com.cameramail;

public enum CameraCompressao {

	ALTA(10), MEDIA(25), MODERADA(50), NENHUMA(100);

	private int qualidade;

	private CameraCompressao(int qualidade) {
		this.qualidade = qualidade;
	}

	public int getQualidade() {
		return qualidade;
	}
}

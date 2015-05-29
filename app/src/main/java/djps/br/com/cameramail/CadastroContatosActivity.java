package djps.br.com.cameramail;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import djps.br.com.utils.Arquivo;
import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter.LengthFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CadastroContatosActivity extends Activity implements
		OnClickListener {
	private Button btCadastrar;
	private EditText emailEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.cadastro_email);

		btCadastrar = (Button) findViewById(R.id.btCadastrarEmail);
		emailEditText = (EditText) findViewById(R.id.emailEditText);

		btCadastrar.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		if (v == btCadastrar
				&& !emailEditText.getText().toString().trim().equals("")) {

			String[] contatos;
			try {
				contatos = Arquivo.carregarContatos();
				ArrayList<String> listaAux = new ArrayList<String>();

				for (int i = 0; i < contatos.length; i++) {
					if (!contatos[i].equals("")) {
						listaAux.add(contatos[i]);
					}
				}

				listaAux.add(emailEditText.getText().toString());

				contatos = new String[listaAux.size()];
				for (int i = 0; i < contatos.length; i++) {
					contatos[i] = listaAux.get(i);
				}

				Arquivo.salvarContatos(contatos);
				emailEditText.setText("");
				Toast.makeText(getBaseContext(), "Contato Salvo!!!",
						Toast.LENGTH_SHORT).show();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
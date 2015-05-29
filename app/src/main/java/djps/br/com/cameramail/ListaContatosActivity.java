package djps.br.com.cameramail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import djps.br.com.utils.Arquivo;

public class ListaContatosActivity extends Activity {

	static String[] contatos ;
	ListView listaContatos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lista_contatos);
		
		try {
			contatos = Arquivo.carregarContatos();
			
			ArrayAdapter<String> adapter = new
			ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
					contatos);
			listaContatos = (ListView) findViewById(R.id.listView1);
			listaContatos.setAdapter(adapter);
		
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (ClassNotFoundException e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}	
}

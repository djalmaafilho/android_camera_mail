package djps.br.com.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;

public class BancoDeDados {
	SQLiteDatabase db;
	
	public BancoDeDados(Context ctx) {
		criarBanco(ctx);
		db.close();
		
	}
	
	private void criarBanco(Context ctx) {

		try {
			db = SQLiteDatabase.openDatabase("camera_mail", null, Context.MODE_PRIVATE);
			Log.i("DJPS", "Bd ja estava Criado e esta aberto "+db.isOpen());
			//db.execSQL("drop camera_mail;");
		} catch (SQLiteException e) {
			if(db == null){
				db = SQLiteDatabase.openOrCreateDatabase("camera_mail", null);
				Log.i("DJPS", "Bd Criado "+db.isOpen());
				Log.i("DJPS", "Path "+db.getPath());
				db.execSQL("create table tb_email (id integer primary key autoincrement, email text not null);");
			}
		}
	}
	
	
	public void salvarEmailBancoDados(String email){
		db.execSQL("insert into tb_email(email) values ('"+email+"');");
	}
	
	
	
	
	private void fecharBanco(){
		db.close();
	}
}

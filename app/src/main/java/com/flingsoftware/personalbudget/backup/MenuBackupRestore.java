/**
 * Questa classe gestisce l'Activity per la scelta del backup/restore del database.
 */

package com.flingsoftware.personalbudget.backup;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiNotifiche;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DROPBOX_TOKEN;

import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.utilita.UtilitaVarie;

import static com.flingsoftware.personalbudget.backup.BackupRestoreIntentService.CostantiBackupRestore.*;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.util.Log;
import android.support.v7.app.ActionBarActivity;


public class MenuBackupRestore extends ActionBarActivity {
	
	//costanti
	private static final int OPZIONE_MEMORIA_INTERNA = 0;
	private static final int OPZIONE_CARTELLA_DOWNLOAD = 1;
	private static final int OPZIONE_DROPBOX = 2;
	private final static String APP_KEY = "pfctpedx23u7xho";
	private final static String APP_SECRET = "ugrg4yemxetzxy6";
	private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_backuprestore);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		Spinner spinner = (Spinner) findViewById(R.id.menu_backuprestore_spRestore);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.menu_backuprestore_spinnerOptions, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(SpRestoreOnItemSelectedListener);
		
		lvBackups = (ListView) findViewById(R.id.menu_backuprestore_lvBackups);
		lstTimes = new ArrayList<Long>();
		backupAdapter = new BackupAdapter(this, lstTimes);
		lvBackups.setAdapter(backupAdapter);
		lvBackups.setOnItemClickListener(lvBackupsOnItemClickListener);
		new RetrieveBackupsTask().execute((Object) null);
		
		opzione = 0;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_backup_backuprestore, menu);
		
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_menubackuprestore_OK:
			conferma();

			return true;	
		case android.R.id.home:
			finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	public void onRadioButtonClicked(View view) {
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    switch(view.getId()) {
	        case R.id.menu_backuprestore_rbBackup:
	            if (checked) {
	                findViewById(R.id.menu_backuprestore_tvTitoloLista).setVisibility(View.GONE);
	                findViewById(R.id.menu_backuprestore_flSx).setVisibility(View.GONE);
	                findViewById(R.id.menu_backuprestore_flDx).setVisibility(View.GONE);
	                lvBackups.setVisibility(View.GONE);
	                ((Spinner) findViewById(R.id.menu_backuprestore_spRestore)).setSelection(0);
	                findViewById(R.id.menu_backuprestore_tvIndicazioni).setVisibility(View.GONE);
	                backup = true;
	            }
	            break;
	        case R.id.menu_backuprestore_rbRestore:
	            if (checked) {
	                findViewById(R.id.menu_backuprestore_tvTitoloLista).setVisibility(View.VISIBLE);
	                findViewById(R.id.menu_backuprestore_flSx).setVisibility(View.VISIBLE);
	                findViewById(R.id.menu_backuprestore_flDx).setVisibility(View.VISIBLE);
	            	((Spinner) findViewById(R.id.menu_backuprestore_spRestore)).setSelection(0);
	            	findViewById(R.id.menu_backuprestore_tvIndicazioni).setVisibility(View.GONE);
	            	//((TextView) findViewById(R.id.menu_backuprestore_tvIndicazioni)).setText(R.string.menu_backuprestore_istruzioniRestore);
	            	lvBackups.setVisibility(View.VISIBLE);
	            	backup = false;
	            }
	            break;
	    }
	}
	
	
	private AdapterView.OnItemSelectedListener SpRestoreOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    	opzione = pos;
	    	if(pos == OPZIONE_MEMORIA_INTERNA) {
                if(backup) {
    	    		findViewById(R.id.menu_backuprestore_tvTitoloLista).setVisibility(View.GONE);
                    findViewById(R.id.menu_backuprestore_flSx).setVisibility(View.GONE);
                    findViewById(R.id.menu_backuprestore_flDx).setVisibility(View.GONE);
                    lvBackups.setVisibility(View.GONE);
                    findViewById(R.id.menu_backuprestore_tvIndicazioni).setVisibility(View.GONE);
                }
                else {
    	    		findViewById(R.id.menu_backuprestore_tvTitoloLista).setVisibility(View.VISIBLE);
                    findViewById(R.id.menu_backuprestore_flSx).setVisibility(View.VISIBLE);
                    findViewById(R.id.menu_backuprestore_flDx).setVisibility(View.VISIBLE);
                    lvBackups.setVisibility(View.VISIBLE);
                    findViewById(R.id.menu_backuprestore_tvIndicazioni).setVisibility(View.GONE);
                }
	    	}
	    	else if(pos == OPZIONE_CARTELLA_DOWNLOAD) {
            	findViewById(R.id.menu_backuprestore_tvTitoloLista).setVisibility(View.GONE);
            	lvBackups.setVisibility(View.GONE);
                findViewById(R.id.menu_backuprestore_flSx).setVisibility(View.GONE);
                findViewById(R.id.menu_backuprestore_flDx).setVisibility(View.GONE);
            	findViewById(R.id.menu_backuprestore_tvIndicazioni).setVisibility(View.VISIBLE);
                if(backup) {
                	((TextView) findViewById(R.id.menu_backuprestore_tvIndicazioni)).setText(R.string.menu_backuprestore_istruzioniBackup);
                }
                else {
                	((TextView) findViewById(R.id.menu_backuprestore_tvIndicazioni)).setText(R.string.menu_backuprestore_istruzioniRestore);
                }
	    	}
	    	else if (pos == OPZIONE_DROPBOX) {
            	findViewById(R.id.menu_backuprestore_tvTitoloLista).setVisibility(View.GONE);
            	lvBackups.setVisibility(View.GONE);
                findViewById(R.id.menu_backuprestore_flSx).setVisibility(View.GONE);
                findViewById(R.id.menu_backuprestore_flDx).setVisibility(View.GONE);
            	findViewById(R.id.menu_backuprestore_tvIndicazioni).setVisibility(View.GONE);
	    	}
	    }

	    public void onNothingSelected(AdapterView<?> parent) {

	    }
	};
	
	
	//click on the items of the ListView
	OnItemClickListener lvBackupsOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			long time = lstTimes.get(arg2);
			final String fileName = "BudgetPersonale_backup_" + time;
			
			AlertDialog.Builder builder = new AlertDialog.Builder(MenuBackupRestore.this);
			builder.setTitle(R.string.menu_backuprestore_alertDialogTitolo);
			builder.setIcon(R.drawable.ic_action_warning);
			builder.setMessage(R.string.menu_backuprestore_alertDialogTesto);
			builder.setPositiveButton(R.string.generici_OK, new DialogInterface.OnClickListener() {		
				@Override
				public void onClick(DialogInterface dialog, int which) {
					NotificationCompat.Builder builder = new NotificationCompat.Builder(MenuBackupRestore.this);
					builder.setSmallIcon(R.drawable.ic_notifica);
					builder.setContentTitle(getString(R.string.notifica_backupDatabase_titolo));
					builder.setContentText(getString(R.string.notifica_backupDatabase_testo));
					builder.setProgress(0, 0, true);
					Notification notification = builder.build();
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.notify(CostantiNotifiche.NOTIFICA_BACKUPRESTORE_DATABASE, notification);
			    	
			    	Intent brIntent = new Intent(ACTION_RESTORE_AUTO);
                    brIntent.setClass(MenuBackupRestore.this, com.flingsoftware.personalbudget.backup.BackupRestoreIntentService.class);
			    	brIntent.putExtra(EXTRA_RESTORE_FILE_PATH, fileName);
			    	MenuBackupRestore.this.startService(brIntent);

			    	setResult(Activity.RESULT_OK);
			    	finish();
					
				}
			});
			builder.setCancelable(true);
			builder.setNegativeButton(R.string.generici_annulla, null);
			AlertDialog confirmDialog = builder.create();
			confirmDialog.show();
		}
	};
	
	
	private void conferma() {   	
		if(backup && opzione == OPZIONE_CARTELLA_DOWNLOAD) {
			operazione = AZIONE_BACKUP;
		}
		else if(!backup && opzione == OPZIONE_CARTELLA_DOWNLOAD) {
			operazione = AZIONE_RESTORE;
		}
		else if(backup && opzione == OPZIONE_MEMORIA_INTERNA) {
			operazione = ACTION_BACKUP_AUTO;
		}
		else if (!backup && opzione == OPZIONE_MEMORIA_INTERNA) {
			new MioToast(this, getString(R.string.toast_menubackuprestore_seleziona1Backup)).visualizza(Toast.LENGTH_SHORT);
			return;
		}
		else if(backup && opzione == OPZIONE_DROPBOX) {
			operazione = ACTION_BACKUP_DROPBOX;
			attivaSessioneDropbox();
			return;
		}
		else if(!backup && opzione == OPZIONE_DROPBOX) {
			operazione = ACTION_RESTORE_DROPBOX;
			attivaSessioneDropbox();
			return;
		}
		
		controllaPermessiEConferma();
	}

	/*
	Controlla permesso di lettura/scrittura su memoria esterna per Marshmallow.
	 */
	private void controllaPermessiEConferma() {
		if (opzione == OPZIONE_CARTELLA_DOWNLOAD && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
			}
			else {
				confermaOperazione();
			}
		}
		else { // versione precedente a Marshmallow (permesso gi? concesso all'installazione)
			confermaOperazione();
		}
	}


	// L'utente ha concesso il permesso di scrittura o no? (Marshmallow)
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_WRITE_EXTERNAL_STORAGE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					confermaOperazione();
				} else {
					UtilitaVarie.visualizzaDialogOKAnnulla(this, getResources().getString(R.string.permessi_negati_titolo), getResources().getString(R.string.permessi_negati_descrizione), getResources().getString(R.string.ok), false, null, R.drawable.ic_action_warning, null);
				}
				return;
			}
		}
	}

	
	private void confermaOperazione() {
		if(!backup) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MenuBackupRestore.this);
			builder.setTitle(R.string.menu_backuprestore_alertDialogTitolo);
			builder.setIcon(R.drawable.ic_action_warning);
			builder.setMessage(R.string.menu_backuprestore_alertDialogTesto);
			builder.setPositiveButton(R.string.generici_OK, new DialogInterface.OnClickListener() {		
				@Override
				public void onClick(DialogInterface dialog, int which) {
					backupRestore();		
				}
			});
			builder.setCancelable(true);
			builder.setNegativeButton(R.string.generici_annulla, null);
			AlertDialog confirmDialog = builder.create();
			confirmDialog.show();
		}
		else {
			backupRestore();
		}
	}
	
	
	private void backupRestore() {
		//creo notifica per l'operazione in background di esportazione del database
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_notifica);
		builder.setContentTitle(getString(R.string.notifica_backupDatabase_titolo));
		builder.setContentText(getString(R.string.notifica_backupDatabase_testo));
		builder.setProgress(0, 0, true);
		Notification notification = builder.build();
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(CostantiNotifiche.NOTIFICA_BACKUPRESTORE_DATABASE, notification);
    	
    	Intent brIntent = new Intent(operazione);
        brIntent.setClass(this, com.flingsoftware.personalbudget.backup.BackupRestoreIntentService.class);
    	brIntent.putExtra(EXTRA_CHIUSURA_APP, false);
    	MenuBackupRestore.this.startService(brIntent);

    	setResult(Activity.RESULT_OK);
    	finish();
	}
	
	
	private static class ViewHolder {
		TextView tvData;
		TextView tvOra;
	}
	
	
	private class BackupAdapter extends ArrayAdapter<Long> {
		private List<Long> items;
		private LayoutInflater inflater;
		
		public BackupAdapter(Context context, List<Long> items) {
			super(context, -1, items);
			this.items = items;
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.menu_backuprestore_listview_item, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.tvData = (TextView) convertView.findViewById(R.id.menu_backuprestore_listview_item_tvData);
				viewHolder.tvOra = (TextView) convertView.findViewById(R.id.menu_backuprestore_listview_item_tvOra);
				convertView.setTag(viewHolder);
			}
			else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			long time = items.get(position);
			SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yy", miaLocale);
			SimpleDateFormat sdfOra = new SimpleDateFormat("HH:mm:ss", miaLocale);
			viewHolder.tvData.setText(sdfData.format(new Date(time)));
			viewHolder.tvOra.setText(sdfOra.format(new Date(time)));
			
			return convertView;
		}
	}
	
	
	//AsyncTask to retrieve the last 6 auto backups
	private class RetrieveBackupsTask extends AsyncTask<Object, Object, Boolean> {	
		protected Boolean doInBackground(Object... params) {
			String fileList[] = MenuBackupRestore.this.fileList();
			for(String tmp : fileList) {
				if(tmp.contains("BudgetPersonale_backup_")) {
					String timeStr = tmp.substring(23);
					long timeLong = Long.parseLong(timeStr);
					lstTimes.add(timeLong);
				}
			}
			if(lstTimes.size() > 0) {
				Collections.sort(lstTimes);
				Collections.reverse(lstTimes);
				return true;
			}
			else {
				return false;
			}
		}
		
		protected void onPostExecute(Boolean result) {
			if(result) {
				backupAdapter.notifyDataSetChanged();
			}
		}
	}
	
	
	//stabilisco una sessione con Dropbox
	private void attivaSessioneDropbox() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		accessToken = sharedPreferences.getString(DROPBOX_TOKEN, null);
		
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		
		if(accessToken == null) {
			mDBApi.getSession().startOAuth2Authentication(MenuBackupRestore.this);
		}
		else {
			confermaOperazione();
		}
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mDBApi != null) {
			if(mDBApi.getSession().authenticationSuccessful()) {
				try {
					mDBApi.getSession().finishAuthentication();
					accessToken = mDBApi.getSession().getOAuth2AccessToken();
					
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(DROPBOX_TOKEN, accessToken);
					editor.apply();
					
					confermaOperazione();
				} catch (IllegalStateException e) {
					Log.i("DbAuthLog", "Error authenticating", e);
				}
			}
		}
	}


	//variabili di istanza
	private boolean backup = true;
	private int opzione;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	private ArrayList<Long> lstTimes;
	private BackupAdapter backupAdapter;
	private ListView lvBackups;
	private DropboxAPI<AndroidAuthSession> mDBApi;
	String accessToken;
	String operazione;
}

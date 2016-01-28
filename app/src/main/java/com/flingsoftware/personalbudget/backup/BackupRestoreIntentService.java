/**
 * Questa classe avvia le operazioni di backup/restore del database in un thread separato
 * utilizzando un IntentService e comunicando i risultati alla main Activity con un
 * BroadcastReceiver (registrato nella main Activity).
 */

package com.flingsoftware.personalbudget.backup;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.LOCAL_BROADCAST_BACKUPRESTORE_DATABASE;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiNotifiche;
import com.flingsoftware.personalbudget.backup.BackupRestore;
import static com.flingsoftware.personalbudget.backup.BackupRestoreIntentService.CostantiBackupRestore.EXTRA_CHIUSURA_APP;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;


public class BackupRestoreIntentService extends IntentService {
	//costanti 
	private static final String NOME = "BackupRestoreIntentService";
	
	//interfaccia con costanti pubbliche che servono per utilizzare questa classe
	public interface CostantiBackupRestore {
		String AZIONE_BACKUP = "com.flingsoftware.personalbudget.BACKUP";
		String ACTION_BACKUP_AUTO = "com.flingsoftware.personalbudget.BACKUP_AUTO";
		String ACTION_BACKUP_DROPBOX = "com.flingsoftware.personalbudget.BACKUP_DROPBOX";
		String AZIONE_RESTORE = "com.flingsoftware.personalbudget.RESTORE";
		String ACTION_RESTORE_AUTO = "com.flingsoftware.personalbudget.RESTORE_AUTO";
		String ACTION_RESTORE_DROPBOX = "com.flingsoftware.personalbudget.RESTORE_DROPBOX";
		String EXTRA_RISULT = "risultIntent";
		String EXTRA_OPERAZIONE = "operazione";
		String EXTRA_RESTORE_FILE_PATH = "restore_file_path";
		String EXTRA_CHIUSURA_APP = "chiusuraApp";
	}
	
	
	public BackupRestoreIntentService() {
		super(NOME);
		setIntentRedelivery(false);
	}
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		boolean risult = false; //operazione avvenuta con successo?
		String action = intent.getAction();
		boolean chiusuraApp = intent.getBooleanExtra(EXTRA_CHIUSURA_APP, false);
		
		if(CostantiBackupRestore.AZIONE_BACKUP.equals(action)) {
			risult = backupDatabase();
		}
		else if(CostantiBackupRestore.AZIONE_RESTORE.equals(action)) {
			risult = restoreDatabase();
		}
		else if(CostantiBackupRestore.ACTION_BACKUP_AUTO.equals(action)) {
			createNotification();
			risult = backupDatabaseAuto();
		}
		else if(CostantiBackupRestore.ACTION_RESTORE_AUTO.equals(action)) {
			risult = restoreDatabaseAuto(intent.getExtras().getString(CostantiBackupRestore.EXTRA_RESTORE_FILE_PATH));
		}
		else if(CostantiBackupRestore.ACTION_BACKUP_DROPBOX.equals(action)) {
			risult = backupDatabaseDropbox();
		}
		else if(CostantiBackupRestore.ACTION_RESTORE_DROPBOX.equals(action)) {
			risult = restoreDatabaseDropbox();
		}
		
		if(CostantiBackupRestore.ACTION_BACKUP_AUTO.equals(action) && chiusuraApp) { //auto backup
			if(!risult) {
				updateNotificationWithError();
			}
			else {
				deleteNotification();
			}
		}
		else { //manual backup/restore and auto restore
			Intent broadcastIntent = new Intent(LOCAL_BROADCAST_BACKUPRESTORE_DATABASE);
			broadcastIntent.putExtra(CostantiBackupRestore.EXTRA_RISULT, risult);
			broadcastIntent.putExtra(CostantiBackupRestore.EXTRA_OPERAZIONE, action);
			LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
			localBroadcastManager.sendBroadcast(broadcastIntent);		
		}
	}
	
	
	private boolean backupDatabase() {
		BackupRestore backupRestore = new BackupRestore(this);
		return backupRestore.backupDatabase(false);
	}
	
	
	private boolean restoreDatabase() {
		BackupRestore backupRestore = new BackupRestore(this);
		return backupRestore.restoreDatabase(false, "");
	}
	
	
	private boolean backupDatabaseAuto() {
		BackupRestore backupRestore = new BackupRestore(this);
		return backupRestore.backupDatabase(true);
	}
	
	
	private boolean restoreDatabaseAuto(String restoreFile) {
		BackupRestore backupRestore = new BackupRestore(this);
		return backupRestore.restoreDatabase(true, restoreFile);
	}
	
	
	private boolean backupDatabaseDropbox() {
		BackupRestore backupRestore = new BackupRestore(this);
		return backupRestore.backupDropbox();
	}
	
	
	private boolean restoreDatabaseDropbox() {
		BackupRestore backupRestore = new BackupRestore(this);
		return backupRestore.restoreDropbox();
	}
	
	
	private void createNotification() {
		//notification for auto backup operation
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_notifica);
		builder.setContentTitle(getString(R.string.notifica_backupDatabase_titolo));
		builder.setContentText(getString(R.string.notifica_backupDatabase_testo));
		builder.setProgress(0, 0, true);
		Notification notification = builder.build();
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(CostantiNotifiche.NOTIFICA_BACKUPRESTORE_DATABASE, notification);
	}
	
	
	private void updateNotificationWithError() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_notifica);
		builder.setContentTitle(getString(R.string.notifica_backupDatabase_titolo));
		builder.setContentText(getString(R.string.notifica_backupDatabaseCompletato_testoErrore));
		builder.setProgress(0, 0, false);
					
		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(CostantiNotifiche.NOTIFICA_BACKUPRESTORE_DATABASE, notification);
	}
	
	
	private void deleteNotification() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(CostantiNotifiche.NOTIFICA_BACKUPRESTORE_DATABASE);
	}
}

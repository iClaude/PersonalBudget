/**
 * Questa classe gestisce il backup e restore del file delle preferenze sui/dai
 * server di Google.
 */

package com.flingsoftware.personalbudget.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;


public class MyBackupAgent extends BackupAgentHelper {
	//costanti
	public final String FILE_PREFERENZE = "com.flingsoftware.personalbudget_preferences";
	public static final String CHIAVE_BACKUP_PREF = "prefBackup";
	
	
	@Override
	public void onCreate() {
		super.onCreate();
			
		SharedPreferencesBackupHelper sharedPreferencesBackupHelper = new SharedPreferencesBackupHelper(this, FILE_PREFERENZE);
		addHelper(CHIAVE_BACKUP_PREF, sharedPreferencesBackupHelper);
	}
}

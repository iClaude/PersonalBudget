package com.flingsoftware.personalbudget.database;

import java.io.File;
import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupDataInput;
import android.os.ParcelFileDescriptor;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelper.NOME_DATABASE;
import static com.flingsoftware.personalbudget.database.DatabaseOpenHelper.sDataLock;


public class MyBackupAgent extends BackupAgentHelper {
	// A key to uniquely identify the set of backup data 
	public static final String FILES_BACKUP_KEY = "myDatabase";
	
	@Override
	public void onCreate() {
		FileBackupHelper helper = new FileBackupHelper(this, NOME_DATABASE);
		addHelper(FILES_BACKUP_KEY, helper);
	}
	   
	@Override
	public File getFilesDir() {
		File path = getDatabasePath(NOME_DATABASE);
		return path.getParentFile();
	}
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		synchronized (sDataLock) {
			super.onBackup(oldState, data, newState);
		}
	}
	
	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		synchronized (sDataLock) {
			super.onRestore(data, appVersionCode, newState);
		}
	}
	
}
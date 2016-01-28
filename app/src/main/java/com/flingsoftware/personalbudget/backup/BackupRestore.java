/**
 * Questa classe contiene il codice che gestisce concretamente il backup e restore del
 * database.
 */

package com.flingsoftware.personalbudget.backup;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DROPBOX_TOKEN;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.ULTIMO_BACKUP;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;


public class BackupRestore {
	
	//costanti
	private final static String APP_KEY = "pfctpedx23u7xho";
	private final static String APP_SECRET = "ugrg4yemxetzxy6";
	
	
	public BackupRestore(Context mioContext) {
		this.mioContext = mioContext;
	}
	

	/**
	 * Backs up the database in the download folder (backupAuto = false) or in the app's internal storage
	 * (backupAuto = true).
	 * @param backupAuto false (backs up the database in the download folder, when the user selects a backup 
	 * operation) or true (backs up the database in the app's internal storage, automatically when the app closes)
	 * @return true on success, false on failure
	 */
	public boolean backupDatabase(boolean backupAuto) {
		if(!backupAuto) {
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state)) { //controllo se la directory esterna è disponibile per la scrittura
				return false;
			}
			
	        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);      
	        if (!exportDir.exists()) 
	        {
	            exportDir.mkdirs();
	        }
		}
         
        final String inFileName = mioContext.getFilesDir().getParentFile().getPath()+ "/databases/" + "BudgetPersonale";
        File dbFile = new File(inFileName);
        FileInputStream fis = null;
        OutputStream output = null;
        long oraBackup = 0;
        
        try {
        	fis = new FileInputStream(dbFile);

        	if(!backupAuto) {
	        	String outFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/PersonalBudget_backup";
		        output = new FileOutputStream(outFileName);
        	}
        	else {
        		oraBackup = System.currentTimeMillis();
            	String outFileName = "BudgetPersonale_backup_" + oraBackup;
    	        output = mioContext.openFileOutput(outFileName, Context.MODE_PRIVATE);
        	}
	
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = fis.read(buffer))>0){
	            output.write(buffer, 0, length);
	        }
        }
        catch(IOException exc) {
        	return false;
        }
        finally {
	        try {
	        	output.flush();
		        output.close();
		        fis.close();
	        }
	        catch(IOException exc) {
	        	return false;
	        }
        }
        
        if(backupAuto) {
            //keep only the last 10 backups (delete the old ones)
            String fileList[] = mioContext.fileList();
            Arrays.sort(fileList);
            int backups = 0;
            for(int i=fileList.length - 1; i>=0; i--) {
            	if(fileList[i].contains("BudgetPersonale_backup")) {
            		backups++;
            		if(backups > 10) {
            			String filePath = mioContext.getFilesDir().getPath() + "/" + fileList[i];
            			new File(filePath).delete();
            		}
            	}
            }
            
            //registro nelle preferenze il momento dell'ultimo backup
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mioContext);
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putLong(ULTIMO_BACKUP, oraBackup);
            prefEditor.apply();
        }
        
        return true;
	}
	
	
	/**
	 * Restores the database.
	 * @param restoreAuto false (restores the database from the download folder) or true (restore the database from
	 * the app's internal storage - automatic backups)
	 * @param fileInput if restoreAuto is true, fileInput is the path of the file in the app's internal storage to
	 * restore
	 * @return true on success, false on failure
	 */
	public boolean restoreDatabase(boolean restoreAuto, String fileInput) {
		if(!restoreAuto) {
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state)) { //controllo se la directory esterna è disponibile per la scrittura
				return false;
			}
		}
        
        FileInputStream fis = null;
        OutputStream output = null;        
        try {
        	if(!restoreAuto) {
    			String inFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/PersonalBudget_backup";
    			File dbFile = new File(inFileName);
    	        if(!dbFile.exists()) {
    	        	return false;
    	        }
        		fis = new FileInputStream(dbFile);
        	}
        	else {
        		fis = mioContext.openFileInput(fileInput);
        	}

        	String outFileName = mioContext.getFilesDir().getParentFile().getPath()+ "/databases/" + "BudgetPersonale";
	        output = new FileOutputStream(outFileName);
	
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = fis.read(buffer))>0){
	            output.write(buffer, 0, length);
	        }
        }
        catch(IOException exc) {
        	return false;
        }
        finally {
	        try {
	        	output.flush();
		        output.close();
		        fis.close();
	        }
	        catch(IOException exc) {
	        	return false;
	        }
	        catch(Exception exc) {
	        	return false;
	        }
        }
        
        return true;
	}
	
	
	/**
	 * Fa il backup del database su Dropbox nella cartella specifica della app.
	 * @return
	 */
	public boolean backupDropbox() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mioContext);
		accessToken = sharedPreferences.getString(DROPBOX_TOKEN, null);
		
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		mDBApi.getSession().setOAuth2AccessToken(accessToken);
		
		//cancello eventuali backup già effettuati
		try {
			mDBApi.delete("/BudgetPersonale");
		}
		catch(Exception e) {
			//se il file non esiste tanto meglio!!
		}
		
		final String inFileName = mioContext.getFilesDir().getParentFile().getPath()+ "/databases/" + "BudgetPersonale";
        File dbFile = new File(inFileName);
        
        FileInputStream inputStream = null;
        try {
        	inputStream = new FileInputStream(dbFile);
        	mDBApi.putFile("/BudgetPersonale", inputStream, dbFile.length(), null, null);
        } 
        catch(Exception exc) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(DROPBOX_TOKEN, null);
			editor.apply();
			
        	return false;
        }
        finally {
        	try {
        		inputStream.close();
        	}
        	catch(IOException exc) {
        		return false;
        	}
        }
        
        return true;
	}
	
	
	public boolean restoreDropbox() {
		boolean risultato = false;
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mioContext);
		accessToken = sharedPreferences.getString(DROPBOX_TOKEN, null);
		
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		mDBApi.getSession().setOAuth2AccessToken(accessToken);
		
		//faccio una copia provvisoria nella cache dir
		String outFileName = mioContext.getCacheDir().getAbsolutePath() + "/BudgetPersonale";
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(outFileName);
			mDBApi.getFile("/BudgetPersonale", null, outputStream, null);
			
			//copio il database nella cartella corretta e cancello la copia provvisoria
	        final String fileProvvPath = mioContext.getCacheDir().getAbsolutePath() + "/BudgetPersonale";
	        File fileProvv = new File(fileProvvPath);
	        FileInputStream fis = null;
	        final String fileDefPath = mioContext.getFilesDir().getParentFile().getPath()+ "/databases/" + "BudgetPersonale";
	        OutputStream output = null;        
	        try {
	        	fis = new FileInputStream(fileProvv);
		        output = new FileOutputStream(fileDefPath);
		
		        byte[] buffer = new byte[1024];
		        int length;
		        while ((length = fis.read(buffer))>0){
		            output.write(buffer, 0, length);
		        }
		        
		        risultato = true;
	        }
	        catch(IOException exc) {
	        	risultato = false;
	        }
	        finally {
		        try {
		        	output.flush();
			        output.close();
			        fis.close();
			        fileProvv.delete();
		        }
		        catch(IOException exc) {
		        	risultato = false;
		        }
	        }
		}
		catch(Exception exc) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(DROPBOX_TOKEN, null);
			editor.apply();
			
			risultato = false;
		}
		finally {
			try {
				outputStream.close();
			}
			catch(IOException exc) {
				risultato = false;
			}
		}
		
		return risultato;
	}

	
	//variabili di istanza
	private Context mioContext;
	private DropboxAPI<AndroidAuthSession> mDBApi;
	String accessToken;
}

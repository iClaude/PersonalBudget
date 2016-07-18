/*
 * COSE DA SISTEMARE:
 * - nuove animazioni
 * - password con fingerprint per Android 6
 * - sincronizzazione con google app engine
 */

package com.flingsoftware.personalbudget.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.FragmentEntrate.EarningsDeletedListener;
import com.flingsoftware.personalbudget.app.FragmentSpese.ExpensesDeletedListener;
import com.flingsoftware.personalbudget.backup.BackupRestoreIntentService.CostantiBackupRestore;
import com.flingsoftware.personalbudget.backup.MenuBackupRestore;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.AggiornamentoDatabaseIntentService;
import com.flingsoftware.personalbudget.database.Conto;
import com.flingsoftware.personalbudget.database.DBCConti;
import com.flingsoftware.personalbudget.database.DBCEntrateRipetute;
import com.flingsoftware.personalbudget.database.DBCEntrateVoci;
import com.flingsoftware.personalbudget.database.DBCSpeseRipetute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.preferenze.BootUpReceiver;
import com.flingsoftware.personalbudget.preferenze.BudgetStatusService;
import com.flingsoftware.personalbudget.preferenze.PreferenzeActivity;
import com.flingsoftware.personalbudget.preferenze.ReminderService;
import com.flingsoftware.personalbudget.ricvoc.RiconoscimentoVocale;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_PICCOLO_AGGIORNA;
import static com.flingsoftware.personalbudget.app.MenuPeriodo.TIPO_DATA_AUTOMATICA;
import static com.flingsoftware.personalbudget.app.Password.CostantiPassword;
import static com.flingsoftware.personalbudget.backup.BackupRestoreIntentService.CostantiBackupRestore.ACTION_BACKUP_AUTO;
import static com.flingsoftware.personalbudget.backup.BackupRestoreIntentService.CostantiBackupRestore.EXTRA_CHIUSURA_APP;
import static com.flingsoftware.personalbudget.database.AggiornamentoDatabaseIntentService.CostantiPubbliche.ACTION_UPDATE_DATABASE;
import static com.flingsoftware.personalbudget.database.AggiornamentoDatabaseIntentService.CostantiPubbliche.EXTRA_RESULT;
import static com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService.CostantiIntentService.EXTRA_FORMATO_OUTPUT;
import static com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService.CostantiIntentService.EXTRA_INDIRIZZO_EMAIL;
import static com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService.CostantiIntentService.EXTRA_RISULT_INTENT;


public class MainPersonalBudget extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, ExpensesDeletedListener, EarningsDeletedListener,OnQueryTextListener {
	
	// Object for intrinsic lock
	public static final Object sDataLock = new Object();
	
	
	//costanti varie
	public interface CostantiVarie {
		String TAG = "PERSONAL BUDGET APP";
		String ID_DATEPICKER = "id_datepicker";
		String LOCAL_BROADCAST_ACTION = "localBroadcast";
		String LOCAL_BROADCAST_UPDATE_BUDGET ="localBroadcastUpdateBudget";
		String LOCAL_BROADCAST_UPDATE_VOCI_RIPETUTE = "localBroadcastUpdateSpeseRipetute";
		String LOCAL_BROADCAST_UPDATE_DATABASE = "localBroadcastUpdateDatabase";
		String LOCAL_BROADCAST_DETTAGLIO_VOCE = "localBroadcastDettaglioVoce";
		String LOCAL_BROADCAST_BACKUPRESTORE_DATABASE = "localBroadcastBackupDatabase";
		String WIDGET_AGGIORNA = "com.flingsoftware.personalbudget.UPDATE_WIDGET";
		String WIDGET_PICCOLO_AGGIORNA = "com.flingsoftware.personalbudget.UPDATE_WIDGET_PICCOLO";
	}
	
	
	//costanti per le Activity che visualizzano le spese, entrate e budget, permettendo di eliminarle o modificarle
	public interface CostantiDettaglioVoce {
		String TIPO_OPERAZIONE = "tipo_operazione";
		int OPERAZIONE_ELIMINAZIONE = 0;
		int OPERAZIONE_MODIFICA = 1;
	}

	
	//costanti identificative delle preferenze
	public interface CostantiPreferenze {
		String DATA_INIZIO = "data_inizio";
		String DATA_FINE = "data_fine";
		//public static final String DATA_30_GG = "data_30_gg"; //valore boolean (periodo su ultimi 30 gg?)
		String DATA_AUTOMATICA = "DATA_AUTOMATICA"; // ultimi 30gg, prossimi 30 gg, mese corrente
		int DATA_AUTOMATICA_30GG_ULTIMI = 0;
		int DATA_AUTOMATICA_30GG_PROSSIMI = 1;
		int DATA_AUTOMATICA_MESE_CORRENTE = 2;
		String DATA_OFFSET = "data_offset"; //da che giorno inizia il mese finanziario?
		String VALUTA_PRINCIPALE = "pref_generale_valuta_principale";
		String VALUTA_CORRENTE = "pref_generale_valuta_corrente";
		String SUONI_ABILITATI = "pref_altro_suoni";
		String REMINDER_ABILITATO = "pref_generale_reminder";
		String PRIMO_AVVIO_APP = "pref_generale_primo_avvio_app";
		String LOWNDES_TIPS_ABILITATI = "pref_altro_lowndes_tips_abilitati";
		String LOWNDES_TIPS_PRIMA_SPESA = "pref_altro_lowndes_tips_prima_spesa";
		String LOWNDES_TIPS_PRIMO_BUDGET = "pref_altro_lowndes_tips_primo_budget";
		int VISUALIZZA_PER_DATA = 0;
		int VISUALIZZA_PER_VOCE = 1;
		String ULTIMO_BACKUP = "ultimo_backup";
		String DROPBOX_TOKEN = "dropbox_token";
		String CONTO_UTILIZZATO = "conto";
		String DRAWER_APERTO = "drawer_aperto";
		String CONTO_DEFAULT = "conto_default";
		String TAG_SPESE_ULTIMO = "tag_spese_ultimo";
		String TAG_ENTRATE_ULTIMO = "tag_entrate_ultimo";
	}
	
	
	//costanti identificative delle varie Activity
	public interface CostantiActivity {
		int ACTIVITY_MENUPERIODO = 0;
		int ACTIVITY_MENUESPORTA = 1;
		int ACTIVITY_SPESE_DETTAGLIOVOCE = 2;
		int ACTIVITY_SPESE_AGGIUNGI = 3;
		int ACTIVITY_ENTRATE_DETTAGLIOVOCE = 4;
		int ACTIVITY_ENTRATE_AGGIUNGI = 5;
		int ACTIVITY_BUDGET_DETTAGLIOVOCE = 6;
		int ACTIVITY_BUDGET_SPESEINCLUSE = 7;
		int ACTIVITY_BUDGET_BUDGETANALOGHI = 8;
		int ACTIVITY_BUDGET_AGGIUNGI = 9;
		int ACTIVITY_BUDGET_MODIFICA = 10;
		int ACTIVITY_MAIN = 11;
		int ACTIVITY_PREFERENZE = 12;
		int ACTIVITY_RIC_VOCALE = 13;
		int ACTIVITY_CONTIELENCO = 14;
		int ACTIVITY_PREFERITI = 15;
	}
	
	
	//costanti x suoni
	public interface CostantiSuoni {
		int SUONO_AGGIUNGI_SPESA_ENTRATA = 0;
		int SUONO_AGGIUNGI_BUDGET = 1;
		int SUONO_CANCELLAZIONE = 2;
		int SUONO_OPERAZIONE_COMPLETATA = 3;
		int SUONO_BLOCCO_APP = 4;
	}
	
	
	//costanti per le notifiche
	public interface CostantiNotifiche {
		int NOTIFICA_AGGIORNAMENTO_DATABASE = 1001;
		int NOTIFICA_ESPORTA_DATABASE = 1002;
		int NOTIFICA_AVVIA_APP = 1003;
		int NOTIFICA_BACKUPRESTORE_DATABASE = 1004;
	}
	
	
	//costanti identificative dei tab
	private static final int TAB_SPESE = 0;
	private static final int TAB_ENTRATE = 1;
	private static final int TAB_SALDO = 2;
	private static final int TAB_BUDGET = 3;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		//requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_personal_budget);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		// Floating action buttons.
		fab = (FloatingActionButton) findViewById(R.id.fab);
		fabMic = (FloatingActionButton) findViewById(R.id.fabMic);
		fabFav = (FloatingActionButton) findViewById(R.id.fabFav);
		fabAgg = (FloatingActionButton) findViewById(R.id.fabAgg);

        //registrazione listener per cambio preferenze (qua serve per la password)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        ricavaPreferenze();

		// Navigation Drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		alConti = new ArrayList<String>();
		contiAdapter = new ContiAdapter(this, alConti);
		mDrawerList.setAdapter(contiAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		new RecuperaContiTask().execute((Object[]) null);
		// Drawer toggle per collegamento con comportamento action bar
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
		{
			/** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                impostaTitolo(conto);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // Apro il Navigation Drawer le prime due volte    
        if(drawerAperto < 2) {
        	SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        	prefEditor.putInt(CostantiPreferenze.DRAWER_APERTO, ++drawerAperto);
        	prefEditor.apply();
        	mDrawerLayout.openDrawer(GravityCompat.START);
        }

		if(savedInstanceState == null) {
			speseFragment = new FragmentSpese();
			entrateFragment = new FragmentEntrate();
			saldoFragment = new FragmentSaldo();
			budgetFragment = new FragmentBudget();
		}
		else {
			speseFragment = getSupportFragmentManager().getFragment(savedInstanceState, "speseFragment");
			entrateFragment = getSupportFragmentManager().getFragment(savedInstanceState, "entrateFragment");
			saldoFragment = getSupportFragmentManager().getFragment(savedInstanceState, "saldoFragment");
			budgetFragment = getSupportFragmentManager().getFragment(savedInstanceState, "budgetFragment");
		}
		
		((FragmentSpese) speseFragment).setExpensesDeletedListener(this);
		((FragmentEntrate) entrateFragment).setEarningsDeletedListener(this);
		
		//disabilitare il menu mentre aggiorna il database (vedi metodo onPrepareOptionsMenu)
		menuVisualizzato = false;
		
		//LocalBroadcastReceiver per sapere quando termina l'aggiornamento del database
		attivaLocalBroadcastReceiverUpdateDatabase();
			
		/*
		 * impostazioni iniziali del database
		 * aggiornamento iniziale database (spese, entrate ripetute e nuovi budget) tramite IntentService
		 */
		if(!AggiornamentoDatabaseIntentService.serviceAttivo) { //controllo che l'aggiornamento non sia gi� in corso (es. quando lancio l'app, la chiudo subito e la riapro)
			//impostazioni iniziali del database
			if(!databaseImpostato()) {
				impostaDatabase();
			}
			
			
			//creo notifica per l'operazione in background di aggiornamento del database
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
			builder.setSmallIcon(R.drawable.ic_notifica);
			builder.setContentTitle(getString(R.string.notifica_aggiornamentoDatabase_titolo));
			builder.setContentText(getString(R.string.notifica_aggiornamentoDatabase_testo));
			builder.setProgress(0, 0, true);
			Notification notification = builder.build();
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(CostantiNotifiche.NOTIFICA_AGGIORNAMENTO_DATABASE, notification);
			
	    	Intent intUpdateDatabase = new Intent(ACTION_UPDATE_DATABASE);
            intUpdateDatabase.setClass(this, com.flingsoftware.personalbudget.database.AggiornamentoDatabaseIntentService.class);
	    	startService(intUpdateDatabase);
		}
		
		/*
		 * Carico i suoni dell'app in un thread separato per non bloccare l'avvio dell'app. La variabile
		 * booleana suoniCaricati � impostata su true, e quindi i suoni possono essere utilizzati, solo
		 * dopo che il caricamente � stato completato.
		 */
		new CaricaSuoniTask().execute();
		
		SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
		preferencesEditor.putInt(CostantiPreferenze.PRIMO_AVVIO_APP, 1);
		preferencesEditor.apply();
	}
	
	
	// Navigation Drawer
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
	
	
	// Navigation Drawer
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		getSupportFragmentManager().putFragment(outState, "speseFragment", arrFragment[0]);
		getSupportFragmentManager().putFragment(outState, "entrateFragment", arrFragment[1]);
		getSupportFragmentManager().putFragment(outState, "saldoFragment", arrFragment[2]);
		getSupportFragmentManager().putFragment(outState, "budgetFragment", arrFragment[3]);
	}
	
	
	/*
	 * Questo metodo fa partire l'app quando termina l'aggiornamento automatico del database. E' richiamato
	 * dal metodo attivaLocalBroadcastReceiverUpdateDatabase, che a sua volta raccoglie in broadcast lanciato
	 * dall'IntentService quando termine l'aggiornamento del database.
	 */
	private void launchApp() {	
		arrFragment = new Fragment[4];
		arrFragment[0] = speseFragment;
		arrFragment[1] = entrateFragment;
		arrFragment[2] = saldoFragment;
		arrFragment[3] = budgetFragment;

		myCustomPagerAdapter = new MyAdapter(getSupportFragmentManager(), this);
		myViewPager = (ViewPager) findViewById(R.id.pager);
		myViewPager.setOffscreenPageLimit(4); //forzo il caricamento di 4 fragment. In caso contrario i fragment verrebbero caricati volta per volta in caso di bisogno (valore di default � 1)
		myViewPager.setAdapter(myCustomPagerAdapter);
		// Collego il TabLayout al ViewPager.
		TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		tabLayout.setupWithViewPager(myViewPager);
		
		myViewPager.setVisibility(View.VISIBLE);
		findViewById(R.id.main_rlUpdatingDatabase).setVisibility(View.GONE);
		
		fab.setVisibility(View.VISIBLE);

		myViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				fragSel = position;
			}

            // Cambio il layout solo dopo che lo scorrimento è terminato per renderlo più fluido
            @Override
            public void onPageScrollStateChanged (int state)
            {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
					fab.setVisibility(View.VISIBLE);
					fab.setAlpha(1.0f);
					fabMic.setVisibility(View.GONE);
					fabFav.setVisibility(View.GONE);
					fabAgg.setVisibility(View.GONE);
                    if(fragSel == TAB_SPESE || fragSel == TAB_ENTRATE || fragSel == TAB_BUDGET) {
						fab.setImageResource(R.drawable.ic_content_new);
                    }
                    else if(fragSel == TAB_SALDO) {
                        fab.setImageResource(R.drawable.ic_action_copy);
                    }
                }
            }
		});

		//abilitare il menu
		menuVisualizzato = true;
		invalidateOptionsMenu();
	}

	
	// Navigation Drawer: imposto il titolo della app su Personal Budget - conto
	private void impostaTitolo(String conto) {
		String nomeConto = conto.equals("%") ? getString(R.string.conti_tutti) : conto;
		getSupportActionBar().setTitle(getString(R.string.main_conto) + nomeConto);
	}


	public class MyAdapter extends FragmentPagerAdapter {
		private final int PAGE_COUNT = 4;
        Context mContext;
		
		public MyAdapter(FragmentManager fm, Context context) {
			super(fm);
            mContext = context;
		}
		
		@Override
		public Fragment getItem(int i) {
            return arrFragment[i];
		}
		
		@Override
		public int getCount() {
			return PAGE_COUNT;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			switch(position) {
			case TAB_SPESE:
				return getResources().getString(R.string.tab_spese).toUpperCase();
			case TAB_ENTRATE:
				return getResources().getString(R.string.tab_entrate).toUpperCase();
			case TAB_SALDO:
				return getResources().getString(R.string.tab_saldo).toUpperCase();
			case TAB_BUDGET:
				return getResources().getString(R.string.tab_budget).toUpperCase();
			default:
				return null;
			}
		}
	}

	
	private void ricavaPreferenze() {
		//preferenze periodo
		dataInizio = new GregorianCalendar();
		dataFine = new GregorianCalendar();
		
		int tipoDataAutomatica = sharedPreferences.getInt(CostantiPreferenze.DATA_AUTOMATICA, -1);
		int offset = sharedPreferences.getInt(CostantiPreferenze.DATA_OFFSET, 1);
		
		if(tipoDataAutomatica == -1) {
			GregorianCalendar dataComodo = new GregorianCalendar();
			int mese = dataComodo.get(GregorianCalendar.MONTH);
			int anno = dataComodo.get(GregorianCalendar.YEAR);
			dataComodo = new GregorianCalendar(anno, mese, 1);
		
			long dataInizioLong = sharedPreferences.getLong(CostantiPreferenze.DATA_INIZIO, dataComodo.getTimeInMillis());
			dataInizio.setTimeInMillis(dataInizioLong);
			
			dataComodo.add(GregorianCalendar.MONTH, 1);
			dataComodo.add(GregorianCalendar.DATE, -1);
			long dataFineLong = sharedPreferences.getLong(CostantiPreferenze.DATA_FINE, dataComodo.getTimeInMillis());
			dataFine.setTimeInMillis(dataFineLong);
		}
		else {
			FunzioniComuni.impostaPeriodoAutomatico(tipoDataAutomatica, dataInizio, dataFine, offset);
		}
		
		//preferenze Lowndes tips
		lowndesTipsAbilitati = sharedPreferences.getBoolean(CostantiPreferenze.LOWNDES_TIPS_ABILITATI , true);
		primaSpesa = sharedPreferences.getBoolean(CostantiPreferenze.LOWNDES_TIPS_PRIMA_SPESA , true);
		primoBudget = sharedPreferences.getBoolean(CostantiPreferenze.LOWNDES_TIPS_PRIMO_BUDGET , true);
		
		//conto
		conto = sharedPreferences.getString(CostantiPreferenze.CONTO_UTILIZZATO, "%");
		impostaTitolo(conto);
		drawerAperto = sharedPreferences.getInt(CostantiPreferenze.DRAWER_APERTO, 0);
	}


	//AsyncTask per caricare la HashMap con i suoni dell'app
	private class CaricaSuoniTask extends AsyncTask<Object, Object, Boolean> {
			
		protected Boolean doInBackground(Object... params) {		
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainPersonalBudget.this);
			boolean abilitazioneSuoni = pref.getBoolean(CostantiPreferenze.SUONI_ABILITATI, false);
			if(abilitazioneSuoni) {
				soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
				mappaSuoni = new SparseIntArray(2);
				mappaSuoni.put(CostantiSuoni.SUONO_OPERAZIONE_COMPLETATA, soundPool.load(MainPersonalBudget.this, R.raw.operazione_completata, 1));
				mappaSuoni.put(CostantiSuoni.SUONO_BLOCCO_APP, soundPool.load(MainPersonalBudget.this, R.raw.blocco_app, 1));
				mappaSuoni.put(CostantiSuoni.SUONO_CANCELLAZIONE, soundPool.load(MainPersonalBudget.this, R.raw.cancellazione, 1));
			}
			
			return abilitazioneSuoni;
		}
			
		protected void onPostExecute(Boolean result) {
			//una volta caricati i suoni nella Map l'app � pronta ad utilizzarli, non prima
			suoniAbilitati = result;
		}
	}
	
	
	// Navigation Drawer: recupero elenco dei conti
	private class RecuperaContiTask extends AsyncTask<Object, Object, Cursor> {
		DBCConti dbcConti = new DBCConti(MainPersonalBudget.this);
		ArrayList<String> alContiComodo = new ArrayList<String>();
		
		protected Cursor doInBackground(Object... params) {
			dbcConti.openLettura();
			Cursor curConti = dbcConti.getTuttiIContiNonOrdinato();
			
			return curConti;
		}
		
		protected void onPostExecute(Cursor curResult) {
			alContiComodo.add(getString(R.string.conti_tutti));
			while(curResult.moveToNext()) {
				alContiComodo.add(curResult.getString(curResult.getColumnIndex("conto")));
			}
			alContiComodo.add(getString(R.string.drawer_gestioneConti));
			
			dbcConti.close();
			curResult.close();
			
			alConti.clear();
			alConti.addAll(alContiComodo);
			mDrawerList.setAdapter(null);
			mDrawerList.setAdapter(contiAdapter);
			//contiAdapter.notifyDataSetChanged();
		}
	}
	
	
	// Navigation Drawer: adapter custom per la ListView
	private static class ViewHolder {
		ImageView ivIcona;
		TextView tvVoce;
	}
	
	
	// Navigation Drawer: adapter custom per la ListView
	private class ContiAdapter extends ArrayAdapter<String> {
		private LayoutInflater inflater;
		private List<String> lstVoci;
		
		public ContiAdapter(Context context, List<String> lstVoci) {
		    super(context, -1, lstVoci);
		    this.lstVoci = lstVoci;
		    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.ivIcona = (ImageView) convertView.findViewById(R.id.dli_ivIcona);
				viewHolder.tvVoce = (TextView) convertView.findViewById(R.id.dli_tvConto);
				convertView.setTag(viewHolder);
			}
			else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			String voce = lstVoci.get(position);
            viewHolder.tvVoce.setTextColor(getResources().getColor(R.color.text_primary));

			String contoComodo = conto.equals("%") ? getResources().getString(R.string.conti_tutti) : conto;
			if(voce.equals(contoComodo)) {
				convertView.setBackgroundColor(getResources().getColor(R.color.nav_drawer_itemSelezionato));
				viewHolder.ivIcona.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_news_blue));
				viewHolder.tvVoce.setTextColor(getResources().getColor(R.color.primary_dark));
			}
			else if(voce.equals(getResources().getString(R.string.drawer_gestioneConti))) {
				viewHolder.ivIcona.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_settings));
			}
			else {
				viewHolder.ivIcona.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_news));
			}

			viewHolder.tvVoce.setText(voce);
			
			return convertView;
		}
	}
	
	
	// Navigation Drawer: gestione click sugli elementi per cambiare conto
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(position == alConti.size() - 1) {
				Intent intent = new Intent(MainPersonalBudget.this, ContiElenco.class);
				startActivityForResult(intent, CostantiActivity.ACTIVITY_CONTIELENCO);
			}
			else {
				if(position == 0) {
					conto = "%";
				}
				else {
					conto = ((TextView) view.findViewById(R.id.dli_tvConto)).getText().toString();
				}
				aggiornaCursor(new int[] {1,1,1,1});
				impostaTitolo(conto);
				
				// Seleziono l'elemento selezionato e deseleziono quello precedente
				new RecuperaContiTask().execute((Object[]) null);
				
				// Salvo conto utilizzato nelle preferenze
				SharedPreferences.Editor prefEditor = sharedPreferences.edit();
				prefEditor.putString(CostantiPreferenze.CONTO_UTILIZZATO, conto);
				prefEditor.apply();
			}
			
			mDrawerLayout.closeDrawers();
		}
	}
	
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,  resultCode, data);
		
		switch(requestCode) {
		case CostantiActivity.ACTIVITY_MENUPERIODO:
			if(resultCode == RESULT_OK) {
				long dataInizioMillis = data.getExtras().getLong(CostantiPreferenze.DATA_INIZIO);
				long dataFineMillis = data.getExtras().getLong(CostantiPreferenze.DATA_FINE);
				boolean dataAutomatica = data.getExtras().getBoolean(CostantiPreferenze.DATA_AUTOMATICA);
				int tipoDataAutomatica = data.getExtras().getInt(TIPO_DATA_AUTOMATICA);
				
				dataInizio.setTimeInMillis(dataInizioMillis);
				dataFine.setTimeInMillis(dataFineMillis);
				
				//aggiorno il file delle preferenze
				SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
				preferencesEditor.putLong(CostantiPreferenze.DATA_INIZIO, dataInizioMillis);
				preferencesEditor.putLong(CostantiPreferenze.DATA_FINE, dataFineMillis);
				preferencesEditor.putInt(CostantiPreferenze.DATA_AUTOMATICA, tipoDataAutomatica);
				preferencesEditor.apply();
				
				//aggiorno i Cursor dei vari Fragment
				aggiornaCursor(new int[] {1,1,1,0});
				
				final Intent intAggiornaWidget = new Intent (WIDGET_PICCOLO_AGGIORNA);
				sendBroadcast(intAggiornaWidget);
			}
			break;
		case CostantiActivity.ACTIVITY_PREFERITI:
			if(resultCode == Activity.RESULT_OK) {
				aggiornaCursor(new int[] {1,1,1,1});
			}

			break;
		case CostantiActivity.ACTIVITY_SPESE_DETTAGLIOVOCE:
			if(resultCode == Activity.RESULT_OK) {
				aggiornaCursor(new int[] {1,0,1,1});
			}
			break;
		case CostantiActivity.ACTIVITY_ENTRATE_DETTAGLIOVOCE:
			if(resultCode == Activity.RESULT_OK) {
				aggiornaCursor(new int[] {0,1,1,0});
			}
			break;
		case CostantiActivity.ACTIVITY_BUDGET_DETTAGLIOVOCE:
				if(resultCode == Activity.RESULT_OK) {
					aggiornaCursor(new int[] {1,0,1,1});
				}
			break;
		case CostantiActivity.ACTIVITY_SPESE_AGGIUNGI:
			if(resultCode == Activity.RESULT_OK) {
				aggiornaCursor(new int[] {1,0,1,1});
				
				if(lowndesTipsAbilitati) {
					visualizzaLowndesTips("spese");
				}
			}
			break;
		case CostantiActivity.ACTIVITY_ENTRATE_AGGIUNGI:
			if(resultCode == Activity.RESULT_OK) {
				aggiornaCursor(new int[] {0,1,1,0});
				
				if(lowndesTipsAbilitati) {
					visualizzaLowndesTips("entrate");
				}
			}
			break;
		case CostantiActivity.ACTIVITY_BUDGET_AGGIUNGI:
			if(resultCode == Activity.RESULT_OK) {
				aggiornaCursor(new int[] {0,0,0,1});
				
				if(lowndesTipsAbilitati) {
					visualizzaLowndesTips("budget");
				}
			}
			break;
			
		case CostantiActivity.ACTIVITY_PREFERENZE:
			if(resultCode == Activity.RESULT_OK) {
				aggiornaCursor(new int[] {1,1,1,1});
			}
			
			break;
		case CostantiActivity.ACTIVITY_RIC_VOCALE:
			if(resultCode == Activity.RESULT_OK) {
				aggiornaCursor(new int[] {1,1,1,1});
			}
			
			break;
		case CostantiActivity.ACTIVITY_CONTIELENCO:
			new RecuperaContiTask().execute((Object[]) null);
			aggiornaCursor(new int[] {1,1,1,0});
			
			break;
		}
	}
	
	
	/*
	 * Restituisce true se il database � impostato con i valori iniziali (l'app � gi� stata avviata
	 * una volta), altrimenti false.
	 */
	private boolean databaseImpostato() {
		boolean risultato = false;
		
		DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(this);
		dbcSpeseVoci.openLettura();
		Cursor cVoce = dbcSpeseVoci.getVoceSpesa(1);
		if(cVoce.getCount() > 0) {
			risultato = true;
		}
		cVoce.close();
		dbcSpeseVoci.close();
		
		return risultato;
	}
	
	
	private void impostaDatabase() {
		//tabella spese_voci
		String arrVoci[] = getResources().getStringArray(R.array.database_arrVociSpese);
		DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(this);
		dbcSpeseVoci.inserisciVoceSpesa(getString(R.string.database_varie), 0);
		dbcSpeseVoci.inserisciVoceSpesa(arrVoci[0], 14);
		dbcSpeseVoci.inserisciVoceSpesa(arrVoci[1], 11);
		dbcSpeseVoci.inserisciVoceSpesa(arrVoci[2], 6);
		dbcSpeseVoci.inserisciVoceSpesa(arrVoci[3], 39);
		dbcSpeseVoci.inserisciVoceSpesa(arrVoci[4], 34);
		dbcSpeseVoci.inserisciVoceSpesa(arrVoci[5], 49);
		dbcSpeseVoci.inserisciVoceSpesa(arrVoci[6], 54);
		dbcSpeseVoci.inserisciVoceSpesa(arrVoci[7], 58);
		
		//tabella conti
		Conto mioConto = new Conto(1, "default", 0, 0);
		DBCConti dbcConti = new DBCConti(this);
		dbcConti.inserisciConto(mioConto);
		
		//tabella spese_ripet
		DBCSpeseRipetute dbcSpeseRipetute = new DBCSpeseRipetute(this);
		dbcSpeseRipetute.inserisciSpesaRipetuta(getString(R.string.database_varie), "nessuna", 0.0, "EUR", 0.0, "riga di comodo", 0L, 1, 0L, 0L, "default");		
		
		//tabella entrate_voci
		String arrVociEntrate[] = getResources().getStringArray(R.array.database_arrVociEntrate);
		DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(this);
		dbcEntrateVoci.inserisciVoceEntrata(getString(R.string.database_varie), 1);
		dbcEntrateVoci.inserisciVoceEntrata(arrVociEntrate[0], 41);
		dbcEntrateVoci.inserisciVoceEntrata(arrVociEntrate[1], 57);
		dbcEntrateVoci.inserisciVoceEntrata(arrVociEntrate[2], 58);
		
		//tabella entrate_ripet
		DBCEntrateRipetute dbcEntrateRipetute = new DBCEntrateRipetute(this);
		dbcEntrateRipetute.inserisciEntrataRipetuta(getString(R.string.database_varie), "nessuna", 0.0, "EUR", 0.0, "riga di comodo", 0L, 1, 0L, 0L, "default");
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_mainpersonalbudget, menu);
		
		//SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_ricerca));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getString(R.string.menu_ricerca));
        searchView.setOnQueryTextListener(this);
		
		return true;
	}
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupEnabled(R.id.menu_gruppoPrincipale, menuVisualizzato);
		
		return true;
	}


    /*
    Implementazione interfaccia android.support.v7.widget.SearchView.OnQueryTextListener per intercettare
    eventi riguardanti la SearchView.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(searchView != null && !searchView.isIconified()) {
            ((FragmentSpese) speseFragment).impostaRicerca(newText);
            ((FragmentEntrate) entrateFragment).impostaRicerca(newText);
            ((FragmentBudget) budgetFragment).impostaRicerca(newText);
        }

        return false;
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Navigation Drawer
		if (mDrawerToggle.onOptionsItemSelected(item)) {
	          return true;
	    }
		
		switch(item.getItemId()) {
		case R.id.menu_intervalloTempo:
			Intent activityImpostaPeriodo = new Intent(MainPersonalBudget.this, MenuPeriodo.class);
			activityImpostaPeriodo.putExtra(CostantiPreferenze.DATA_INIZIO, dataInizio.getTimeInMillis());
			activityImpostaPeriodo.putExtra(CostantiPreferenze.DATA_FINE, dataFine.getTimeInMillis());
			startActivityForResult(activityImpostaPeriodo, CostantiActivity.ACTIVITY_MENUPERIODO);
			
			return true;
		case R.id.menu_preferenze:
			Intent activityPreferenze = new Intent(MainPersonalBudget.this, PreferenzeActivity.class);
			startActivityForResult(activityPreferenze, CostantiActivity.ACTIVITY_PREFERENZE);
			
			return true;
		case R.id.menu_esporta:	
			attivaLocalBroadcastReceiverExportDatabase();
					
			Intent menuEsporta = new Intent(MainPersonalBudget.this, MenuEsporta.class);
			menuEsporta.putExtra(CostantiPreferenze.DATA_INIZIO, dataInizio.getTimeInMillis());
			menuEsporta.putExtra(CostantiPreferenze.DATA_FINE, dataFine.getTimeInMillis());
			startActivity(menuEsporta);
			
			return true;	
		case R.id.menu_backuprestore:	
			attivaLocalBroadcastReceiverBackupDatabase();
					
			Intent menuBackupRestore = new Intent(MainPersonalBudget.this, MenuBackupRestore.class);
			startActivity(menuBackupRestore);
			
			return true;
		case R.id.menu_visualizzaPerVoce:
			if(tipoVisualizzazione == CostantiPreferenze.VISUALIZZA_PER_DATA) {
				tipoVisualizzazione = CostantiPreferenze.VISUALIZZA_PER_VOCE;
			}
			else {
				tipoVisualizzazione = CostantiPreferenze.VISUALIZZA_PER_DATA;
			}
			
			((FragmentSpese) speseFragment).aggiornaTipoVisualizzazione(tipoVisualizzazione);
			((FragmentEntrate) entrateFragment).aggiornaTipoVisualizzazione(tipoVisualizzazione);
			
			return true;	

		case R.id.menu_info:
			Intent intInfo = new Intent(MainPersonalBudget.this, MenuInfo.class);
			startActivity(intInfo);
			
			return true;
		case R.id.menu_stats:
			Intent intStat = new Intent(this, Statistiche.class);
			startActivity(intStat);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	/*
	L'azione dipende dal Fragment selezionato. Fragment spese ed entrate: espande o comprime il
	bottone mostrando altre opzioni. Fragment saldo: lancia l'Activity per visualizzare i conti.
	Fragment budget: lancia l'Activity per aggiungere un nuovo budget.
	 */
	public void animateFab(View v) {
		switch (fragSel) {
			case 2:
				Intent intent = new Intent(MainPersonalBudget.this, ContiElenco.class);
				startActivityForResult(intent, CostantiActivity.ACTIVITY_CONTIELENCO);
				break;
			case 3:
				((FragmentBudget) budgetFragment).aggiungi();
				break;
			default:
				if (fabMic.getVisibility() == View.VISIBLE) {
					compressFab();
				} else {
					expandFab();
				}
		}
	}

	private void expandFab() {
		fabMic.setVisibility(View.VISIBLE);
		fabFav.setVisibility(View.VISIBLE);
		fabAgg.setVisibility(View.VISIBLE);

		float scale = getResources().getDisplayMetrics().density;

		ObjectAnimator animFabAlpha = ObjectAnimator.ofFloat(fab, "alpha", 1.0f, 0.5f);
		ObjectAnimator animFabRot = ObjectAnimator.ofFloat(fab, "rotation", 0.0f, 360.0f);
		AnimatorSet animSetFab = new AnimatorSet();
		animSetFab.setDuration(500);
		animSetFab.playTogether(animFabAlpha, animFabRot);


		ObjectAnimator animMicAlpha = ObjectAnimator.ofFloat(fabMic, "alpha", 0.0f, 1.0f);
		ObjectAnimator animMicY = ObjectAnimator.ofFloat(fabMic, "translationY", 0.0f * scale, -70.0f * scale);
		animMicY.setInterpolator(new BounceInterpolator());
		AnimatorSet animSetMic = new AnimatorSet();
		animSetMic.setDuration(500);
		animSetMic.playTogether(animMicAlpha, animMicY);


		ObjectAnimator animFavAlpha = ObjectAnimator.ofFloat(fabFav, "alpha", 0.0f, 1.0f);
		ObjectAnimator animFavY = ObjectAnimator.ofFloat(fabFav, "translationY", 0.0f * scale, -140.0f * scale);
		animFavY.setInterpolator(new BounceInterpolator());
		AnimatorSet animSetFav = new AnimatorSet();
		animSetFav.setDuration(500);
		animSetFav.playTogether(animFavAlpha, animFavY);

		ObjectAnimator animAggAlpha = ObjectAnimator.ofFloat(fabAgg, "alpha", 0.0f, 1.0f);
		ObjectAnimator animAggY = ObjectAnimator.ofFloat(fabAgg, "translationY", 0.0f * scale, -210.0f * scale);
		animAggY.setInterpolator(new BounceInterpolator());
		AnimatorSet animSetAgg = new AnimatorSet();
		animSetAgg.setDuration(500);
		animSetAgg.playTogether(animAggAlpha, animAggY);

		animSetFab.start();
		animSetMic.start();
		animSetFav.start();
		animSetAgg.start();
	}

	private void compressFab() {
		float scale = getResources().getDisplayMetrics().density;

		ObjectAnimator animFabAlpha = ObjectAnimator.ofFloat(fab, "alpha", 0.5f, 1.0f);
		ObjectAnimator animFabRot = ObjectAnimator.ofFloat(fab, "rotation", 360.0f, 0.0f);
		AnimatorSet animSetFab = new AnimatorSet();
		animSetFab.setDuration(500);
		animSetFab.playTogether(animFabAlpha, animFabRot);

		ObjectAnimator animMicAlpha = ObjectAnimator.ofFloat(fabMic, "alpha", 1.0f, 0.0f);
		ObjectAnimator animMicY = ObjectAnimator.ofFloat(fabMic, "translationY", -70.0f * scale, 0.0f * scale);
		animMicY.setInterpolator(new LinearInterpolator());
		AnimatorSet animSetMic = new AnimatorSet();
		animSetMic.setDuration(500);
		animSetMic.playTogether(animMicAlpha, animMicY);

		ObjectAnimator animFavAlpha = ObjectAnimator.ofFloat(fabFav, "alpha", 1.0f, 0.0f);
		ObjectAnimator animFavY = ObjectAnimator.ofFloat(fabFav, "translationY", -140.0f * scale, 0.0f * scale);
		animFavY.setInterpolator(new LinearInterpolator());
		AnimatorSet animSetFav = new AnimatorSet();
		animSetFav.setDuration(500);
		animSetFav.playTogether(animFavAlpha, animFavY);

		ObjectAnimator animAggAlpha = ObjectAnimator.ofFloat(fabAgg, "alpha", 1.0f, 0.0f);
		ObjectAnimator animAggY = ObjectAnimator.ofFloat(fabAgg, "translationY", -210.0f * scale, 0.0f * scale);
		animAggY.setInterpolator(new LinearInterpolator());
		AnimatorSet animSetAgg = new AnimatorSet();
		animSetAgg.setDuration(500);
		animSetAgg.playTogether(animAggAlpha, animAggY);

		animSetFab.start();
		animSetMic.start();
		animSetFav.start();
		animSetAgg.start();
		animSetFav.addListener(animatorListener);
	}

	/*
	Animator listener used to hide the 2 floating buttons for favorites and voice recognition after
	animation ends.
	 */
	Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationEnd(Animator animation) {
			fabMic.setVisibility(View.GONE);
			fabFav.setVisibility(View.GONE);
			fabAgg.setVisibility(View.GONE);
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationStart(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	};

	// Launch Activity for vocal recognition.
	public void launchVocRec(View v) {
		Intent intent = new Intent(this, RiconoscimentoVocale.class);
		startActivityForResult(intent, CostantiActivity.ACTIVITY_RIC_VOCALE);
		fab.setAlpha(1.0f);
		fabMic.setVisibility(View.GONE);
		fabFav.setVisibility(View.GONE);
		fabAgg.setVisibility(View.GONE);
	}

	// Launch Activity to add from favorites.
	public void launchFav(View v) {
		Intent intent = new Intent(MainPersonalBudget.this, Preferiti.class);
		startActivityForResult(intent, CostantiActivity.ACTIVITY_PREFERITI);
		fab.setAlpha(1.0f);
		fabMic.setVisibility(View.GONE);
		fabFav.setVisibility(View.GONE);
		fabAgg.setVisibility(View.GONE);
	}

	// Aggiungi nuova spesa, entrata o budget.
	public void aggiungi(View v) {
		switch(fragSel) {
			case 0:
				attivaLocalBroadcastReceiverUpdateDatabaseInserimentoVoci();
				((FragmentSpese) speseFragment).aggiungi();
				break;
			case 1:
				attivaLocalBroadcastReceiverUpdateDatabaseInserimentoVoci();
				((FragmentEntrate) entrateFragment).aggiungi();
				break;
			case 2:
				Intent intent = new Intent(MainPersonalBudget.this, ContiElenco.class);
				startActivityForResult(intent, CostantiActivity.ACTIVITY_CONTIELENCO);
				break;
			case 3:
				((FragmentBudget) budgetFragment).aggiungi();
				break;
			default:
				//
		}

		fab.setAlpha(1.0f);
		fabMic.setVisibility(View.GONE);
		fabFav.setVisibility(View.GONE);
		fabAgg.setVisibility(View.GONE);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();	
    	
		//deregistro il LocalBroadcastReceiver per l'aggiornamento dei budget, spese ed entrate ripetute, backup del database
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		localBroadcastManager.unregisterReceiver(mLocalReceiverSpeseRipetute);
		localBroadcastManager.unregisterReceiver(mLocalReceiverEntrateRipetute);
		localBroadcastManager.unregisterReceiver(mLocalReceiverAggiornamentoBudget);
		localBroadcastManager.unregisterReceiver(mLocalReceiverBackupRestoreDatabase);
		
		//deregistrazione listener per cambio preferenze
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		pref.unregisterOnSharedPreferenceChangeListener(this);
		
		//libero risorse relative ai suoni
		if(suoniAbilitati) {
			soundPool.release();
			soundPool = null;
		}
		
		//database auto backup (una sola volta al giorno)
		if(backupNonEffettuato()) {
	    	Intent brIntent = new Intent(ACTION_BACKUP_AUTO);
            brIntent.setClass(this, com.flingsoftware.personalbudget.backup.BackupRestoreIntentService.class);
	    	brIntent.putExtra(EXTRA_CHIUSURA_APP, true);
	    	startService(brIntent);
		}
	}
	
	
	//verifico che oggi non ho ancora fatto un backup interno (in caso contrario non serve farlo)
	private boolean backupNonEffettuato() {
		long ultimoBackup = sharedPreferences.getLong(CostantiPreferenze.ULTIMO_BACKUP, 0);
		Calendar cUltimoBackup = Calendar.getInstance();
		cUltimoBackup.setTimeInMillis(ultimoBackup);
		
		Calendar cOggi = Calendar.getInstance();
		
		return (cUltimoBackup.get(Calendar.YEAR) != cOggi.get(Calendar.YEAR) || cUltimoBackup.get(Calendar.MONTH) != cOggi.get(Calendar.MONTH) || cUltimoBackup.get(Calendar.DATE) != cOggi.get(Calendar.DATE));	
	}
	
	
	private void aggiornaCursor(int arrFlag[]) {
		if(arrFlag.length < 4) {
			return;
		}
		
		if(arrFlag[0] > 0) {
			((FragmentSpese) speseFragment).aggiornaCursor();	
		}
		if(arrFlag[1] > 0) {
			((FragmentEntrate) entrateFragment).aggiornaCursor();
		}
		if(arrFlag[2] > 0) {
			((FragmentSaldo) saldoFragment).aggiornaCursor();
		}
		if(arrFlag[3] > 0) {
			((FragmentBudget) budgetFragment).aggiornaCursor();
		}
	}
	
	
	private void attivaLocalBroadcastReceiverExportDatabase() {
		//attivo il BroadcastReceiver per sapere quando l'esportazione � completata
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter(CostantiVarie.LOCAL_BROADCAST_ACTION);
		mLocalReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				//ricavo i risultati dell'operazione di esportazione
				boolean risult = intent.getBooleanExtra(EXTRA_RISULT_INTENT, false);
				String indirizzoEmail = intent.getStringExtra(EXTRA_INDIRIZZO_EMAIL);
				String formatoOutput = intent.getStringExtra(EXTRA_FORMATO_OUTPUT);
				
				File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				String nomeFile = "PersonalBudget." + formatoOutput;
				File file = new File(exportDir, nomeFile);
				Uri uri = Uri.fromFile(file);
				
				if(suoniAbilitati) {
					soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_OPERAZIONE_COMPLETATA), 1, 1, 1, 0, 1f);
				}
				
				//modifico la notifica per l'operazione in background di esportazione del database
				NotificationCompat.Builder builder = new NotificationCompat.Builder(MainPersonalBudget.this);
				builder.setSmallIcon(R.drawable.ic_notifica);
				builder.setContentTitle(getString(R.string.notifica_esportaDatabaseCompletato_titolo));
				builder.setContentText(getString(R.string.notifica_esportaDatabaseCompletato_testo));
				builder.setTicker(getString(R.string.notifica_esportaDatabaseCompletato_testo));
				builder.setProgress(0, 0, false);
				
				String mimeType = new String();
				if(formatoOutput.equals("pdf")) {
					mimeType = "application/pdf";
				}
				else if(formatoOutput.equals("csv")) {
					mimeType = "text/plain";
				}
				else if(formatoOutput.equals("xls")) {
					mimeType = "application/vnd.ms-excel";
				}
				
				Intent intNotifica = new Intent();
				intNotifica.setAction(android.content.Intent.ACTION_VIEW);
				intNotifica.setDataAndType(uri, mimeType);
				PendingIntent pendNotifica = PendingIntent.getActivity(MainPersonalBudget.this, 0, intNotifica, PendingIntent.FLAG_ONE_SHOT);
				
				builder.setContentIntent(pendNotifica);
				
				Notification notification = builder.build();
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(CostantiNotifiche.NOTIFICA_ESPORTA_DATABASE, notification);
								
				if(!risult) {
					new MioToast(MainPersonalBudget.this, getString(R.string.toast_menuesporta_errore)).visualizza(Toast.LENGTH_SHORT);
				}
				else {
					if(indirizzoEmail.length() == 0) {
						new MioToast(MainPersonalBudget.this, getString(R.string.toast_menuesporta_successo)).visualizza(Toast.LENGTH_SHORT);
					}
					else {
						//invio file via email				
						if(file.exists() && file.canRead()) {
							//invio la mail solo se il file esiste e si pu� leggere
							Intent invioEmail = new Intent(Intent.ACTION_SEND);
							invioEmail.setType("message/rfc822");
							invioEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{indirizzoEmail});
							invioEmail.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.menu_esporta_oggettomail));
							invioEmail.putExtra(Intent.EXTRA_TEXT, 
									Html.fromHtml(new StringBuilder()
										.append("<HTML>")
										.append("<HEAD>")
										.append("</HEAD>")
										.append("<BODY LANG=\"it-IT\" DIR=\"LTR\">")
										.append("<P>" + getString(R.string.menu_esporta_testoemail) + "</P>")
										.append("<P><BR><BR><BR><BR></P>")
										.append("<P ALIGN=LEFT STYLE=\"margin-bottom: 0cm\"><FONT SIZE=6><B>PERSONAL BUDGET APP - FLING SOFTWARE&copy;</B></FONT></P>")
										.append("<P ALIGN=LEFT>&ldquo;<FONT SIZE=2 STYLE=\"font-size: 9pt\"><I><SPAN STYLE=\"font-weight: normal\">Take care of the pence, and the pounds will take care of themselves</SPAN></I></FONT><FONT SIZE=2 STYLE=\"font-size: 9pt\"><SPAN STYLE=\"font-weight: normal\">&rdquo;(William Lowndes, 1750)</SPAN></FONT></P>")
										.append("<P STYLE=\"margin-bottom: 0.3cm\"><A HREF=\"https://www.facebook.com/personalbudgetapp\">Website</A></P>")
										.append("<P><A HREF=\"https://play.google.com/store/apps/developer?id=Fling%20Software&copy;&amp;hl=it\"><FONT SIZE=3><SPAN STYLE=\"font-weight: normal\">Google Play</SPAN></FONT></A></P>")
										.append("</BODY>")
										.append("</HTML>")
										.toString())
						    );
							invioEmail.putExtra(Intent.EXTRA_STREAM, uri);
							
							try {
							    startActivity(Intent.createChooser(invioEmail, getString(R.string.menu_esporta_sceltaclientemail)));
							} catch (android.content.ActivityNotFoundException ex) {
								new MioToast(MainPersonalBudget.this, getString(R.string.toast_menuesporta_clientmailmancante)).visualizza(Toast.LENGTH_SHORT);
							}
						}
					}
				}
				
				localBroadcastManager.unregisterReceiver(mLocalReceiver);
			}		
		};
		localBroadcastManager.registerReceiver(mLocalReceiver, intentFilter);
	}
	
	
	private void attivaLocalBroadcastReceiverBackupDatabase() {
		//attivo il BroadcastReceiver per sapere quando l'operazione � completata
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter(CostantiVarie.LOCAL_BROADCAST_BACKUPRESTORE_DATABASE);
		mLocalReceiverBackupRestoreDatabase = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				//ricavo i risultati dell'operazione di esportazione
				boolean risult = intent.getBooleanExtra(CostantiBackupRestore.EXTRA_RISULT, false);
				String operazione = intent.getStringExtra(CostantiBackupRestore.EXTRA_OPERAZIONE);
				
				if(suoniAbilitati) {
					soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_OPERAZIONE_COMPLETATA), 1, 1, 1, 0, 1f);
				}
				
				//modifico la notifica per l'operazione in background di esportazione del database
				NotificationCompat.Builder builder = new NotificationCompat.Builder(MainPersonalBudget.this);
				builder.setSmallIcon(R.drawable.ic_notifica);
				builder.setContentTitle(getString(R.string.notifica_backupDatabase_titolo));
				if(risult) {
					builder.setContentText(getString(R.string.notifica_backupDatabaseCompletato_testo));
					builder.setTicker(getString(R.string.notifica_backupDatabaseCompletato_testo));
				}
				else {
					builder.setContentText(getString(R.string.notifica_backupDatabaseCompletato_testoErrore));
					builder.setTicker(getString(R.string.notifica_backupDatabaseCompletato_testoErrore));
				}
				builder.setProgress(0, 0, false);
							
				Notification notification = builder.build();
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(CostantiNotifiche.NOTIFICA_BACKUPRESTORE_DATABASE, notification);
								
				if(!risult) {
					new MioToast(MainPersonalBudget.this, getString(R.string.notifica_backupDatabaseCompletato_testoErrore)).visualizza(Toast.LENGTH_SHORT);
				}
			
				localBroadcastManager.unregisterReceiver(mLocalReceiverBackupRestoreDatabase);
				
				if(operazione.equals(CostantiBackupRestore.AZIONE_RESTORE) || operazione.equals(CostantiBackupRestore.ACTION_RESTORE_AUTO) || operazione.equals(CostantiBackupRestore.ACTION_RESTORE_DROPBOX)) {
					// Reimposto il conto utilizzato e di default su tutti
					SharedPreferences.Editor prefEditor = sharedPreferences.edit();
					prefEditor.putString(CostantiPreferenze.CONTO_UTILIZZATO, "%");
					conto = "%";
					prefEditor.apply();
					
					aggiornaCursor(new int[] {1,1,1,1});
					new RecuperaContiTask().execute((Object[]) null);
				}
			}		
		};
		localBroadcastManager.registerReceiver(mLocalReceiverBackupRestoreDatabase, intentFilter);
	}
	
	
	/*
	 * BroadcastReceiver registrato quando si lancia l'app per intercettare quando termina l'aggiornamento
	 * del database (spese ripetute, entrate ripetute e nuovi budget).
	 * Quindi si fa partire l'app chiamando il metodo launchApp().
	 */
	private void attivaLocalBroadcastReceiverUpdateDatabase() {
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter(CostantiVarie.LOCAL_BROADCAST_UPDATE_DATABASE);
		mLocalReceiverUpdateDatabase = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				boolean result = intent.getBooleanExtra(EXTRA_RESULT, false);

				if(!result) {
					new MioToast(MainPersonalBudget.this, getString(R.string.toast_aggiornamentoDatabase_errore)).visualizza(Toast.LENGTH_SHORT);
				}
				
				localBroadcastManager.unregisterReceiver(mLocalReceiverUpdateDatabase);
				
				//cancello la notifica per l'operazione in background di aggiornamento del database
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(CostantiNotifiche.NOTIFICA_AGGIORNAMENTO_DATABASE);
				
				launchApp();
			}		
		};
		localBroadcastManager.registerReceiver(mLocalReceiverUpdateDatabase, intentFilter);
	}
	

	/*
	 * BroadcastReceiver registrato quando si inseriscono entrate e spese per sapere quando si possono aggiornare i
	 * Cursor. Questo perch� potrebbero essere inserite spese/entrate ripetute per le quali l'aggiornamento del
	 * database potrebbe richiedere tempo.
	 * Il Broadcast viene inviato dalla classe che gestisce il widget piccolo.
	 */
	private void attivaLocalBroadcastReceiverUpdateDatabaseInserimentoVoci() {
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter(CostantiVarie.LOCAL_BROADCAST_UPDATE_VOCI_RIPETUTE);
		mLocalReceiverUpdateDatabaseInserimentoVoci = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				
				localBroadcastManager.unregisterReceiver(mLocalReceiverUpdateDatabaseInserimentoVoci);
				aggiornaCursor(new int[] {1,1,1,1});
			}		
		};
		localBroadcastManager.registerReceiver(mLocalReceiverUpdateDatabaseInserimentoVoci, intentFilter);
	}
	
	
	/*
	 * Listener attivato quando si cambiano le preferenze generali dell'applicazione. Questo
	 * metodo serve solamente per i cambiamenti generali riguardanti l'intera app.
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		/*
		 * Se ho disabilitato la password azzero il campo password nel file delle preferenze.
		 * In questo modo, quando riattivo la password, password sar� impostata su null (se
		 * non si imposta una password specifica) e questo impedisce di bloccare l'avvio della
		 * app.
		 */
		if(key.equals(CostantiPassword.PREFERENZE_PASSWORD_ATTIVATA)) {
			boolean protetta = sharedPreferences.getBoolean(CostantiPassword.PREFERENZE_PASSWORD_ATTIVATA, true);
			if(!protetta) {		
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(CostantiPassword.PREFERENZE_PASSWORD, "");
				editor.apply();
			}
			if(suoniAbilitati) {
				soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_BLOCCO_APP), 1, 1, 1, 0, 1f);
			}
		}
		else if(key.equals(CostantiPreferenze.SUONI_ABILITATI)) {
			suoniAbilitati = sharedPreferences.getBoolean(CostantiPreferenze.SUONI_ABILITATI, false);
			if(suoniAbilitati) {
				soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
				mappaSuoni = new SparseIntArray(2);
				mappaSuoni.put(CostantiSuoni.SUONO_OPERAZIONE_COMPLETATA, soundPool.load(this, R.raw.operazione_completata, 1));
				mappaSuoni.put(CostantiSuoni.SUONO_BLOCCO_APP, soundPool.load(this, R.raw.blocco_app, 1));
			}
			else {
				soundPool.release();
				soundPool = null;
				mappaSuoni.clear();
			}
		}
		else if(key.equals(CostantiPreferenze.LOWNDES_TIPS_ABILITATI)) {
			lowndesTipsAbilitati = sharedPreferences.getBoolean(CostantiPreferenze.LOWNDES_TIPS_ABILITATI, false);
		}
		else if(key.equals(CostantiPreferenze.VALUTA_PRINCIPALE)) {
			final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
		}
		else if(key.equals(CostantiPreferenze.REMINDER_ABILITATO)) {
			boolean reminderAbilitato = sharedPreferences.getBoolean(CostantiPreferenze.REMINDER_ABILITATO, false);
			
			if(reminderAbilitato) {
				//abilito la classe BootupReceiver (attivata ogni volta che si accende il telefono)
				PackageManager packageManager = getPackageManager();
				packageManager.setComponentEnabledSetting(new ComponentName(this, BootUpReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				
				//attivo l'AlarmManager per il reminder  (la notifica � creata nel Service ReminderService)
				Intent myIntent = new Intent(this , ReminderService.class);     
				piReminder = PendingIntent.getService(this, 0, myIntent, 0);
				alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, 20);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000 , piReminder);  //set repeating every 24 hours		
				
				//attivo l'AlarmManager per il controllo dei budget (la notifica � creata nel Service BudgetStatusService)
				Intent myIntent2 = new Intent(this , BudgetStatusService.class);    
				PendingIntent pendingIntent2 = PendingIntent.getService(this, 0, myIntent2, 0);
				AlarmManager alarmManager2 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				calendar.set(Calendar.HOUR_OF_DAY, 12);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				alarmManager2.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000 , pendingIntent2);  //set repeating every 24 hours		
			}
			else if(key.equals(CostantiPreferenze.CONTO_UTILIZZATO)) {
				aggiornaCursor(new int[] {1,1,1,0});
			}
			else {
				//disabilito la classe BootupReceiver (disattivata ogni volta che si accende il telefono)
				PackageManager packageManager = getPackageManager();
				packageManager.setComponentEnabledSetting(new ComponentName(this, BootUpReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				
				//disattivo l'AlarmManager
				if(alarmManager != null) {
					alarmManager.cancel(piReminder);
				}
			}
		}
		
		BackupManager mBackupManager = new BackupManager(this);
		mBackupManager.dataChanged();
	}
	
	
	private void visualizzaLowndesTips(String ambito) {	
		String titolo = new String();
		String messaggio = new String();
		
		if(ambito.equals("spese") && primaSpesa) {
			titolo = getString(R.string.lowndes_tips_titolo);
			messaggio = getString(R.string.lowndes_tips_spesa_1);
			SharedPreferences.Editor prefEditor = sharedPreferences.edit();
			prefEditor.putBoolean(CostantiPreferenze.LOWNDES_TIPS_PRIMA_SPESA, false);
			prefEditor.apply();
			primaSpesa = false;
		}
		else if(ambito.equals("budget") && primoBudget) {
			titolo = getString(R.string.lowndes_tips_titolo);
			messaggio = getString(R.string.lowndes_tips_budget_1);
			SharedPreferences.Editor prefEditor = sharedPreferences.edit();
			prefEditor.putBoolean(CostantiPreferenze.LOWNDES_TIPS_PRIMO_BUDGET, false);
			prefEditor.apply();
			primoBudget = false;
		}
		else {
			Random rnd = new Random();
			int numCas = rnd.nextInt(3);
			if(numCas == 2) {
				titolo = getString(R.string.lowndes_tips_titolo);
				String arrTips[] = getResources().getStringArray(R.array.lowndes_tips_suggerimenti);
				messaggio = arrTips[rnd.nextInt(20)];
			}
			else {
				return;
			}
		}
		
		LowndesTips lowndesTips = new LowndesTips();
		Bundle args = new Bundle();
		args.putString(LowndesTips.TITOLO, titolo);
		args.putString(LowndesTips.MESSAGGIO, messaggio);
		lowndesTips.setArguments(args);
		lowndesTips.show(getSupportFragmentManager(), "LowndesTips");
	}
	
	
	//implementazione interfacce per comunicazioni Fragmen->>Activity
	@Override
	public void onDeletedExpense() {
		aggiornaCursor(new int[] {1,0,1,1});
		if(suoniAbilitati) {
			soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_CANCELLAZIONE), 1, 1, 1, 0, 1f);
		}
	}
	
	
	@Override
	public void onDeletedEarning() {
		aggiornaCursor(new int[] {0,1,1,0});
		if (suoniAbilitati) {
			soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_CANCELLAZIONE), 1, 1, 1, 0, 1f);
		}
	}
	
	
	//variabili di istanza
	private int tipoVisualizzazione = CostantiPreferenze.VISUALIZZA_PER_DATA;
	private int fragSel = 0;
	private Fragment arrFragment[];
	private Fragment speseFragment;
	private Fragment entrateFragment;
	private Fragment saldoFragment;
	private Fragment budgetFragment;
	private MyAdapter myCustomPagerAdapter;
	private ViewPager myViewPager;
	private SharedPreferences sharedPreferences;
	private GregorianCalendar dataInizio;
	private GregorianCalendar dataFine;
	private BroadcastReceiver mLocalReceiver;
	private BroadcastReceiver mLocalReceiverAggiornamentoBudget;
	private BroadcastReceiver mLocalReceiverSpeseRipetute;
	private BroadcastReceiver mLocalReceiverEntrateRipetute;
	private BroadcastReceiver mLocalReceiverUpdateDatabase;
	private BroadcastReceiver mLocalReceiverBackupRestoreDatabase;
	private BroadcastReceiver mLocalReceiverUpdateDatabaseInserimentoVoci;
	private boolean menuVisualizzato;
    private SearchView searchView;

	//gestione suoni
	private SoundPool soundPool;
	private SparseIntArray mappaSuoni;
	private boolean suoniAbilitati;
	
	//reminder registrazione spese
	PendingIntent piReminder;
	AlarmManager alarmManager;
	
	//Lowndes tips
	private boolean lowndesTipsAbilitati;
	private boolean primaSpesa;
	private boolean primoBudget;
	
	// Navigation Drawer: gestione conti
	public static String conto;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;
	private ContiAdapter contiAdapter;
	private ArrayList<String> alConti;
	private int drawerAperto;

	// Floating action buttons.
	FloatingActionButton fab;
	FloatingActionButton fabMic;
	FloatingActionButton fabFav;
	FloatingActionButton fabAgg;
}

package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.*;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiActivity.*;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.*;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.*;
import static com.flingsoftware.personalbudget.database.StringheSQL.ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA;

import com.flingsoftware.personalbudget.R;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.support.v7.widget.SearchView;
import android.widget.Toast;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.AbsListView.MultiChoiceModeListener;
//import android.support.v7.view.ActionMode;
import android.view.ActionMode;
import android.view.MenuInflater;


public class FragmentSpese extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	//comunicazione all'activity per eliminazione spese: l'Activity poi richiede l'aggiornamento degli altri Fragment
	public interface ExpensesDeletedListener {
		void onDeletedExpense();
	}
	
	public void setExpensesDeletedListener(ExpensesDeletedListener listener) {
		expensesDeletedListener = listener;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		//gestione efficiente icone
		iconeVeloci = new ListViewIconeVeloce(getActivity());
		new PlaceHolderWorkerTask().execute(R.drawable.tag_0);
		new CaricaHashMapIconeTask().execute((Object[]) null);
		
		dbcSpeseSostenute = new DBCSpeseSostenute(getActivity());
		tipoVisualizzazione = VISUALIZZA_PER_DATA;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spese, container, false);
		
		ivWallet = (ImageView) rootView.findViewById(R.id.fragment_spese_ivWallet);
		tvToccaPiu = (TextView) rootView.findViewById(R.id.fragment_spese_tvToccaPiu);
		
		//aggiorno tvPeriodo con il periodo iniziale e registro l'ascoltatore per il cambio preferenze
		tvPeriodo = (TextView) rootView.findViewById(R.id.fs_tvPeriodo);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		pref.registerOnSharedPreferenceChangeListener(this);
		ricavaPeriodo(pref);
		ricavaValuta(pref);
		aggiornaTextViewPeriodo();
		
		//popolo la listview con i dati contenuti nel database in un thread separato
		new RefreshGroupsCursorTask().execute((Object[]) null);
		
		elv = (ExpandableListView) rootView.findViewById(R.id.elvSpese);

		// Per avere l'effetto della Toolbar a scomparsa (funziona solo con Lollipop).
		// TODO: 19/07/2016 cercare soluzione per versioni precedenti
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			elv.setNestedScrollingEnabled(true);
		}
		
		//adapter per personalizzare il formato dei dati visualizzati sulla elv. NB: adapter per la visualizzazione per data
		mAdapter = new MyExpandableListAdapter(null, getActivity(), R.layout.fragment_spese_entrate_group_item, R.layout.fragment_spese_entrate_child_item, new String[] {"data", "totale_spesa"}, new int[] {R.id.tvVoceGruppo, R.id.fragment_spese_tvImportoGruppo}, new String[] {"voce", "ripetizione_id", "importo_valprin", "data"}, new int[] {R.id.tvVoceChild, R.id.fragment_spese_entrate_child_item_tvRipetizione, R.id.tvImportoChild, R.id.tvDataComodo});
		mAdapter.setViewBinder(new SimpleCursorTreeAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue (View view, final Cursor cursor, int columnIndex) {
				int viewId = view.getId();
				
				NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
				DecimalFormat nfRidotto = new DecimalFormat("#,##0.00");
				
				switch(viewId) {
				case R.id.tvVoceGruppo:
					long dataDatabaseMillis = cursor.getLong(columnIndex);
					GregorianCalendar oggi = new GregorianCalendar();
					int giorno = oggi.get(GregorianCalendar.DATE);
					int mese = oggi.get(GregorianCalendar.MONTH);
					int anno = oggi.get(GregorianCalendar.YEAR);
					oggi = new GregorianCalendar(anno, mese, giorno);
					
					//imposto la label con la data
					SimpleDateFormat mioSdf = new SimpleDateFormat("dd", miaLocale);
                    TextView tvGiorno = (TextView) ((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_tvGiorno);
					tvGiorno.setText(mioSdf.format(new Date(dataDatabaseMillis)));

                    // Date future con label grigia.
                    if(dataDatabaseMillis > oggi.getTimeInMillis()) {
                        tvGiorno.setBackgroundResource(R.drawable.background_tondo_grigio);
                    }
                    else {
                        tvGiorno.setBackgroundResource(R.drawable.background_tondo_primary);
                    }

					if(oggi.getTimeInMillis() == dataDatabaseMillis) {
						((TextView) view).setText(getResources().getString(R.string.generale_oggi));
						return true;
					}
					
					oggi.add(GregorianCalendar.DATE, -1);
					if(oggi.getTimeInMillis() == dataDatabaseMillis) {
						((TextView) view).setText(getResources().getString(R.string.generale_ieri));
						return true;
					}
					
					mioSdf = new SimpleDateFormat("dd/MMM/yyyy", miaLocale);
					((TextView) view).setText(mioSdf.format(new Date(dataDatabaseMillis)));
					//testo scorrevole
					((TextView) view).setEllipsize(TextUtils.TruncateAt.MARQUEE);
					((TextView) view).setSingleLine(true);
					((TextView) view).setMarqueeRepeatLimit(5);
					view.setSelected(true);
					
					return true;
				case R.id.fragment_spese_tvImportoGruppo:
					double importoGruppo = cursor.getDouble(columnIndex);
					if(!valutaAlternativa) {
						String importoGruppoFormattato = nf.format(importoGruppo);
						((TextView) view).setText(importoGruppoFormattato);
						((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta).setVisibility(View.GONE);
					}
					else {
						String importoGruppoFormattato = nfRidotto.format(importoGruppo);
						((TextView) view).setText(importoGruppoFormattato);
                        ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta).setVisibility(View.VISIBLE);
						((TextView) ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta)).setText("(" + currValuta.getSymbol() + ")");
					}
					
					return true;
				case R.id.fragment_spese_entrate_child_item_tvRipetizione:
					long ripetizioneId = cursor.getLong(cursor.getColumnIndex("ripetizione_id"));
					if(ripetizioneId != 1) {
						((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_child_item_ivRipetizione).setVisibility(View.VISIBLE);
					}
					else {
						((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_child_item_ivRipetizione).setVisibility(View.GONE);
					}
					
					return true;
				case R.id.tvImportoChild:
					double importoChild = cursor.getDouble(columnIndex);			
					if(!valutaAlternativa) {
						String importoChildFormattato = nf.format(importoChild);
						((TextView) view).setText(importoChildFormattato);
					}
					else {
						String importoChildFormattato = nfRidotto.format(importoChild);
						((TextView) view).setText(importoChildFormattato);
					}
					
					return true;		
				case R.id.tvVoceChild:
					String voceChild = cursor.getString(columnIndex);
					
					//testo scorrevole su voci child							
					((TextView) view).setText(voceChild);
					((TextView) view).setEllipsize(TextUtils.TruncateAt.MARQUEE);
					((TextView) view).setSingleLine(true);
					((TextView) view).setMarqueeRepeatLimit(5);
					view.setSelected(true);
					
					//icona
					ImageView ivIcona = (ImageView) (((View) (view.getParent())).findViewById(R.id.menu_esporta_ivFormato));
					Integer icona = hmIcone.get(voceChild);
					if(icona == null) {
						ivIcona.setImageBitmap(mPlaceHolderBitmapSpese);
					}
					else {
						iconeVeloci.loadBitmap(icona, ivIcona, mPlaceHolderBitmapSpese, 40, 40);
					}
					
					return true;
				case R.id.tvDataComodo:
					long data = cursor.getLong(columnIndex);

					return true;
				}
				return false;
			}
		});
		elv.setAdapter(mAdapter);
		
		//adapter per personalizzare il formato dei dati visualizzati sulla elv. NB: adapter per la visualizzazione per voci. Lo creo e poi lo associo alla elv alla bisogna
		mAdapterVoci = new MyExpandableListAdapter(null, getActivity(), R.layout.fragment_spese_entrate_group_item, R.layout.fragment_spese_entrate_child_item, new String[] {"voce", "totale_spesa"}, new int[] {R.id.tvVoceGruppo, R.id.fragment_spese_tvImportoGruppo}, new String[] {"data", "ripetizione_id", "importo_valprin", "voce"}, new int[] {R.id.tvVoceChild, R.id.fragment_spese_entrate_child_item_tvRipetizione, R.id.tvImportoChild, R.id.fragment_spese_entrate_child_item_tvVoce});
		mAdapterVoci.setViewBinder(new SimpleCursorTreeAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue (View view, final Cursor cursor, int columnIndex) {
				int viewId = view.getId();
				
				NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
				DecimalFormat nfRidotto = new DecimalFormat("#,##0.00");
				
				switch(viewId) {
				case R.id.tvVoceChild:
					long dataDatabaseMillis = cursor.getLong(columnIndex);
					GregorianCalendar oggi = new GregorianCalendar();
					int giorno = oggi.get(GregorianCalendar.DATE);
					int mese = oggi.get(GregorianCalendar.MONTH);
					int anno = oggi.get(GregorianCalendar.YEAR);
					oggi = new GregorianCalendar(anno, mese, giorno);

					if(oggi.getTimeInMillis() == dataDatabaseMillis) {
						((TextView) view).setText(getResources().getString(R.string.generale_oggi));
						return true;
					}
					
					oggi.add(GregorianCalendar.DATE, -1);
					if(oggi.getTimeInMillis() == dataDatabaseMillis) {
						((TextView) view).setText(getResources().getString(R.string.generale_ieri));
						return true;
					}
					
					SimpleDateFormat mioSdf = new SimpleDateFormat("dd/MMM/yyyy", miaLocale);
					((TextView) view).setText(mioSdf.format(new Date(dataDatabaseMillis)));
					//testo scorrevole
					((TextView) view).setEllipsize(TextUtils.TruncateAt.MARQUEE);
					((TextView) view).setSingleLine(true);
					((TextView) view).setMarqueeRepeatLimit(5);
					view.setSelected(true);
					
					return true;
				case R.id.fragment_spese_tvImportoGruppo:
					double importoGruppo = cursor.getDouble(columnIndex);
					if(!valutaAlternativa) {
						String importoGruppoFormattato = nf.format(importoGruppo);
						((TextView) view).setText(importoGruppoFormattato);
						((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta).setVisibility(View.GONE);
					}
					else {
						String importoGruppoFormattato = nfRidotto.format(importoGruppo);
						((TextView) view).setText(importoGruppoFormattato);
                        ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta).setVisibility(View.VISIBLE);
						((TextView) ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta)).setText("(" + currValuta.getSymbol() + ")");
					}
					
					return true;
				case R.id.fragment_spese_entrate_child_item_tvRipetizione:
					long ripetizioneId = cursor.getLong(cursor.getColumnIndex("ripetizione_id"));
					if(ripetizioneId != 1) {
						((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_child_item_ivRipetizione).setVisibility(View.VISIBLE);
					}
					else {
						((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_child_item_ivRipetizione).setVisibility(View.GONE);
					}
					
					return true;
				case R.id.tvImportoChild:
					double importoChild = cursor.getDouble(columnIndex);
					if(!valutaAlternativa) {
						String importoChildFormattato = nf.format(importoChild);
						((TextView) view).setText(importoChildFormattato);
					}
					else {
						String importoChildFormattato = nfRidotto.format(importoChild);
						((TextView) view).setText(importoChildFormattato);
					}
					
					return true;			
				case R.id.tvVoceGruppo:
					//imposto la label
					String voceItem = cursor.getString(columnIndex);
					
					//testo scorrevole
					((TextView) view).setText(voceItem);
					((TextView) view).setEllipsize(TextUtils.TruncateAt.MARQUEE);
					((TextView) view).setSingleLine(true);
					((TextView) view).setMarqueeRepeatLimit(5);
					view.setSelected(true);
						
					((TextView) ((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_tvGiorno)).setText(voceItem.substring(0,1).toUpperCase());
					
					return true;
					
				case R.id.fragment_spese_entrate_child_item_tvVoce:
					//icona
					String voce = cursor.getString(columnIndex);
					ImageView ivIcona = (ImageView) (((View) (view.getParent())).findViewById(R.id.menu_esporta_ivFormato));
					Integer icona = hmIcone.get(voce);
					if(icona == null) {
						ivIcona.setImageBitmap(mPlaceHolderBitmapSpese);
					}
					else {
						iconeVeloci.loadBitmap(icona, ivIcona, mPlaceHolderBitmapSpese, 40, 40);
					}
					
					return true;
				}
				return false;
			}
		});

		elv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				if(mActionMode != null) {
					mActionMode.finish();
					return true;
				}
				
				return false;
			}
		});
		
		elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {		
				if(mActionMode == null) {
					dbcSpeseSostenute.openLettura();
					Cursor cDettaglioVoce = dbcSpeseSostenute.getSpesaSostenutaX(id);
					cDettaglioVoce.moveToFirst();
					
					String valuta = cDettaglioVoce.getString(cDettaglioVoce.getColumnIndex("valuta"));
					double importoValprin = cDettaglioVoce.getDouble(cDettaglioVoce.getColumnIndex("importo_valprin"));
					double importo = cDettaglioVoce.getDouble(cDettaglioVoce.getColumnIndex("importo"));
					String voce = cDettaglioVoce.getString(cDettaglioVoce.getColumnIndex("voce"));
					long data = cDettaglioVoce.getLong(cDettaglioVoce.getColumnIndex("data"));
					String descrizione = cDettaglioVoce.getString(cDettaglioVoce.getColumnIndex("descrizione"));
					long ripetizione_id = cDettaglioVoce.getLong(cDettaglioVoce.getColumnIndex("ripetizione_id"));
					String conto = cDettaglioVoce.getString(cDettaglioVoce.getColumnIndex("conto"));
					int preferito = cDettaglioVoce.getInt(cDettaglioVoce.getColumnIndex("favorite"));
					
					cDettaglioVoce.close();
					dbcSpeseSostenute.close();
					Intent visualizzaVoceIntent = new Intent(getActivity(), SpeseDettaglioVoce.class);
					visualizzaVoceIntent.putExtra(VOCE_ID, id);
					visualizzaVoceIntent.putExtra(VOCE_IMPORTO, importo);
					visualizzaVoceIntent.putExtra(VOCE_TAG, voce);
					visualizzaVoceIntent.putExtra(VOCE_DATA, data);
					visualizzaVoceIntent.putExtra(VOCE_DESCRIZIONE, descrizione);
					visualizzaVoceIntent.putExtra(VOCE_RIPETIZIONE_ID, ripetizione_id);
					visualizzaVoceIntent.putExtra(VOCE_VALUTA, valuta);
					visualizzaVoceIntent.putExtra(VOCE_IMPORTO_VALPRIN, importoValprin);
					visualizzaVoceIntent.putExtra(VOCE_CONTO, conto);
					visualizzaVoceIntent.putExtra(VOCE_FAVORITE, preferito);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						getActivity().startActivityForResult(visualizzaVoceIntent, ACTIVITY_SPESE_DETTAGLIOVOCE, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
					}
                    else {
                        getActivity().startActivityForResult(visualizzaVoceIntent, ACTIVITY_SPESE_DETTAGLIOVOCE);
                    }
					
					return true;
				}
				else if(mActionMode != null && expandableListSelectionType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					int flatPosition = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
				    boolean selezionato = parent.isItemChecked(flatPosition);
					parent.setItemChecked(flatPosition, !selezionato);
					
					if(alSelezionati.contains(id)) {
						if(mActionMode != null) {
							alSelezionati.remove(id);
						}
					}
					else {
						if(mActionMode != null) {
							alSelezionati.add(id);
						}
					}
					
					return true;
				}
				
				return false;
			}
		});
		
		//contextual action bar per multiselezione ed eliminazione elementi (solo child)
		elv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		elv.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			
		    @Override
		    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
	            int count = elv.getCheckedItemCount();

	            if (count == 1) {
	              expandableListSelectionType = ExpandableListView.getPackedPositionType(elv.getExpandableListPosition(position));
	              if(expandableListSelectionType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
	            	  mode.finish();
	            	  return;
	              }
	              
	              if(primaSelezione) {
	            	  primaSelezione = false;
	            	  long packedPos = elv.getExpandableListPosition(position);
	            	  int childPos = ExpandableListView.getPackedPositionChild(packedPos);
	            	  int groupPos = ExpandableListView.getPackedPositionGroup(packedPos);
	            	  long childId = elv.getExpandableListAdapter().getChildId(groupPos, childPos);
	            	  alSelezionati.add(childId);
	              }
	            }
	            
	            mode.setTitle(String.valueOf(count) + " " + getString(R.string.fragment_selezionati));
		    }

		    @Override
		    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		        // Respond to clicks on the actions in the CAB
		        switch (item.getItemId()) {
		            case R.id.menu_cancella:
		    			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    			builder.setTitle(R.string.dettagli_voce_conferma_elimina_titolo);
		    			builder.setMessage(R.string.fragment_eliminazione_voci_selezionate_conferma_msg);
		    			builder.setNegativeButton(R.string.cancella, null);
		    			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {	
		    				@Override
		    				public void onClick(DialogInterface dialog, int which) {
				            	Long arrSelezionati[] = new Long[alSelezionati.size()];
				            	arrSelezionati = alSelezionati.toArray(arrSelezionati);
				                new EliminaSpeseSelezionateTask().execute(arrSelezionati);
				                
				                mActionMode.finish(); // Action picked, so close the CAB
		    				}
		    			});
		    			builder.setCancelable(true);
		    			AlertDialog confirmDialog = builder.create();
		    			confirmDialog.show();
		                
		                return true;
		            default:
		                return false;
		        }
		    }

		    @Override
		    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		    	expandableListSelectionType = ExpandableListView.PACKED_POSITION_TYPE_GROUP;
		        MenuInflater inflater = mode.getMenuInflater();
		        inflater.inflate(R.menu.menu_contestuale, menu);

				// Cambio colore della status bar (grigio scuro) quando seleziono elementi.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					Window window = getActivity().getWindow();
					window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
					window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
					window.setStatusBarColor(getResources().getColor(R.color.status_bar_elementi_selezionati));
				}

		        mode.setTitle(String.valueOf(elv.getCheckedItemCount()) + " " + getString(R.string.fragment_selezionati));
	            mActionMode = mode;
	            primaSelezione = true;
		        return true;
		    }

		    @Override
		    public void onDestroyActionMode(ActionMode mode) {
				// Ripristino il normale colore della status bar quando chiudo l'action mode.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					Window window = getActivity().getWindow();
					window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
					window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
					window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
				}

		        mActionMode = null;
		        alSelezionati.clear();
		    }

		    @Override
		    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		        // Here you can perform updates to the CAB due to
		        // an invalidate() request
		        return false;
		    }
		});

		return rootView;
	}

    //Nascondo la voce di menu stats.
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_ricerca));
		searchView.setIconified(true);
	}
	

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		//deregistro l'ascoltatore per il cambio preferenze
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		pref.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}

	/*
	 * Ricavo il periodo di riferimento dal file delle preferenze.
	 */
	private void ricavaPeriodo(SharedPreferences prefTempo) {
		int tipoDataAutomatica = prefTempo.getInt(CostantiPreferenze.DATA_AUTOMATICA, -1);
		int offset = prefTempo.getInt(CostantiPreferenze.DATA_OFFSET, 1);
		
		if(tipoDataAutomatica == -1) {
			GregorianCalendar dataComodo = new GregorianCalendar();
			int mese = dataComodo.get(GregorianCalendar.MONTH);
			int anno = dataComodo.get(GregorianCalendar.YEAR);
			dataComodo = new GregorianCalendar(anno, mese, 1);
		
			long dataInizioLong = prefTempo.getLong(CostantiPreferenze.DATA_INIZIO, dataComodo.getTimeInMillis());
			dataInizio.setTimeInMillis(dataInizioLong);
			
			dataComodo.add(GregorianCalendar.MONTH, 1);
			dataComodo.add(GregorianCalendar.DATE, -1);
			long dataFineLong = prefTempo.getLong(CostantiPreferenze.DATA_FINE, dataComodo.getTimeInMillis());
			dataFine.setTimeInMillis(dataFineLong);
		}
		else {
			FunzioniComuni.impostaPeriodoAutomatico(tipoDataAutomatica, dataInizio, dataFine, offset);
		}
	}

	/*
	 * Ricavo la valuta principale salvata nelle preferenze.
	 */
	private void ricavaValuta(SharedPreferences prefValuta) {
		String valuta = prefValuta.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValuta = Currency.getInstance(valuta);
		valutaAlternativa = !currValuta.getCurrencyCode().equals(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
	}
	
	/*
	 * Aggiorno la TextView del periodo.
	 */
	private void aggiornaTextViewPeriodo() {
		if(tvPeriodo != null) {
			DateFormat df = new SimpleDateFormat("dd/MMM/yyyy", miaLocale);
			tvPeriodo.setText(df.format(dataInizio.getTime()) + " - " + df.format(dataFine.getTime()));
		}
	}
	
	/*
	 * Listener per il cambio delle preferenze.
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(CostantiPreferenze.DATA_INIZIO) || key.equals(CostantiPreferenze.DATA_FINE)) {
			GregorianCalendar dataComodo = new GregorianCalendar();
			int mese = dataComodo.get(GregorianCalendar.MONTH);
			int anno = dataComodo.get(GregorianCalendar.YEAR);
			dataComodo = new GregorianCalendar(anno, mese, 1);
			
			if(key.equals(CostantiPreferenze.DATA_INIZIO)) {
				long dataInizioLong = sharedPreferences.getLong(CostantiPreferenze.DATA_INIZIO, dataComodo.getTimeInMillis());
				dataInizio.setTimeInMillis(dataInizioLong);
			}
			else if(key.equals(CostantiPreferenze.DATA_FINE)) {
				dataComodo.add(GregorianCalendar.MONTH, 1);
				dataComodo.add(GregorianCalendar.DATE, -1);
				long dataFineLong = sharedPreferences.getLong(CostantiPreferenze.DATA_FINE, dataComodo.getTimeInMillis());
				dataFine.setTimeInMillis(dataFineLong);
			}
			
			aggiornaTextViewPeriodo();
		}
		else if(key.equals(CostantiPreferenze.VALUTA_PRINCIPALE)) {
			//cancello il file preferenze con gli ultimi cambi usati (lo faccio solo da qui perch� basta farlo una sola volta)
			//salvo il tasso di cambio nelle preferenze
			SharedPreferences cambiSalvati = getActivity().getSharedPreferences("cambi", Context.MODE_PRIVATE);
			SharedPreferences.Editor cambiEditor = cambiSalvati.edit();
			cambiEditor.clear().apply();
			
			String valuta = sharedPreferences.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
			currValuta = Currency.getInstance(valuta);
			valutaAlternativa = !currValuta.getCurrencyCode().equals(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
			elv.invalidateViews();
		}
	}
	
	public void aggiornaCursor() {
		new CaricaHashMapIconeTask().execute((Object[]) null);
		new RefreshGroupsCursorTask().execute((Object[]) null);
	}
	
	/*
	 * Metodo richiamato dall'Activity madre quando si cambia il tipo di visualizzazione (per data o
	 * per voce).
	 */
	public void aggiornaTipoVisualizzazione(int tipoVisualizzazione) {
		this.tipoVisualizzazione = tipoVisualizzazione;
		if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
			elv.setAdapter(mAdapter);
		}
		else {
			elv.setAdapter(mAdapterVoci);
		}
		
		aggiornaCursor();
	}
	
	/* Metodo chiamato da MainPersonalBudget quando imposto una stringa di ricerca per filtrare l'elenco
	 * delle spese.
	 */
	public void impostaRicerca(String str) {
		ricerca.delete(0, ricerca.length());
		ricerca.append(str);
		
		aggiornaCursor();
	}
	
	//adapter del database per l'expandablelistview
	public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {
		
		public MyExpandableListAdapter(Cursor cursor, Context context, int groupLayout, int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom, int[] childrenTo) {
			super (context, cursor, groupLayout, groupFrom, groupTo, childLayout, childrenFrom, childrenTo);
		}
		
		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
				long data = groupCursor.getLong(groupCursor.getColumnIndex("data"));
				new RefreshChildrenCursorTask(groupCursor.getPosition(), null).execute(data);
			}
			else {
				String mVoce = groupCursor.getString(groupCursor.getColumnIndex("voce"));
				new RefreshChildrenCursorTask(groupCursor.getPosition(), mVoce).execute(0L);
			}
			
			return null;
		}
	}
	
	
	//aggiungere una spesa
	public void aggiungi() {
		Intent aggiungiSpesa = new Intent(getActivity(), SpeseAggiungi.class);
		getActivity().startActivityForResult(aggiungiSpesa, ACTIVITY_SPESE_AGGIUNGI);
	}
	
		
	//AsyncTask per caricare i totali per data (group items) in un thread separato
	private class RefreshGroupsCursorTask extends AsyncTask<Object, Void, Cursor> {
		
		protected Cursor doInBackground(Object... params) {	    	
	    	//refresh dei gruppi
			dbcSpeseSostenute.openLettura();
			if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
				mGroupsCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaliPerDataFiltrato(MainPersonalBudget.conto, dataInizio.getTimeInMillis(), dataFine.getTimeInMillis(), ricerca.toString());
			}
			else {
				mGroupsCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaliPerVoceFiltrato(MainPersonalBudget.conto, dataInizio.getTimeInMillis(), dataFine.getTimeInMillis(), ricerca.toString());
			}
			mGroupsCursor.moveToFirst();
			
			return mGroupsCursor;
		}
		
		protected void onPostExecute(Cursor groupsCursor) {
			try {			
				if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
					mAdapter.changeCursor(groupsCursor);
				}
				else {
					mAdapterVoci.changeCursor(groupsCursor);
				}
				//dbcSpeseSostenute.close();
				elv.expandGroup(0);
				
				if(groupsCursor.getCount() > 0) {
					ivWallet.setVisibility(View.INVISIBLE);
					tvToccaPiu.setVisibility(View.INVISIBLE);
				}
				else {
					ivWallet.setVisibility(View.VISIBLE);
					tvToccaPiu.setVisibility(View.VISIBLE);
				}
			}
			catch(NullPointerException exc) {
				return;
			}
		}
	}
	
	//AsyncTask per caricare il dettaglio delle entrate (child items) in un thread separato
	private class RefreshChildrenCursorTask extends AsyncTask<Long, Void, Cursor> {
		private int mGroupPosition;
		private String mVoce;
		
		public RefreshChildrenCursorTask(int groupPosition, String mVoce) {
			this.mGroupPosition = groupPosition;
			this.mVoce = mVoce;
		}
		
		@Override
		protected Cursor doInBackground(Long... params) {
			long data = params[0];
			dbcSpeseSostenute.openLettura();
			if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
				childCursor = dbcSpeseSostenute.getSpeseSostenuteDataXElencoFiltrato(MainPersonalBudget.conto, data, ricerca.toString());
			}
			else {
				childCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXFiltrato(MainPersonalBudget.conto, dataInizio.getTimeInMillis(), dataFine.getTimeInMillis(), mVoce, ricerca.toString());
			}
			childCursor.moveToFirst();
			
			return childCursor;
		}
		
		@Override
		protected void onPostExecute(Cursor childrenCursor) {
			try {
				if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
					mAdapter.setChildrenCursor(mGroupPosition, childrenCursor);
				}
				else {
					mAdapterVoci.setChildrenCursor(mGroupPosition, childrenCursor);
				}
				//dbcSpeseSostenute.close();
			}
			catch(NullPointerException exc) {
				return;
			}
		}
	}
	
	
	//AsyncTask per eliminare le spese selezionate della ListView
	private class EliminaSpeseSelezionateTask extends AsyncTask<Long, Object, Integer> {
		@Override
		protected Integer doInBackground(Long... params) {	
			int speseEliminate = 0;
			FunzioniAggiornamento aggBudget = new FunzioniAggiornamento(getActivity());
			
			for(long idSpesa : params) {
				dbcSpeseSostenute.openModifica();
				Cursor curSpesa = dbcSpeseSostenute.getSpesaSostenuta(idSpesa);
				curSpesa.moveToFirst();
				String voce = curSpesa.getString(curSpesa.getColumnIndex("voce"));
				long data = curSpesa.getLong(curSpesa.getColumnIndex("data"));
				
				speseEliminate += dbcSpeseSostenute.eliminaSpesaSostenuta(idSpesa);		
				dbcSpeseSostenute.close();
				curSpesa.close();
				//aggiorno i budget per ogni spesa eliminata
				aggBudget.aggiornaTabBudgetSpeseSost(ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA, "%" + voce + "%", Long.valueOf(data).toString(),  Long.valueOf(data).toString());
				
			}

			return speseEliminate;
		}
		
		@Override
		protected void onPostExecute(Integer result) {		
			String msg = getResources().getQuantityString(R.plurals.dettagli_voce_x_voci_eliminate, result, result);
			new MioToast(getActivity(), msg).visualizza(Toast.LENGTH_SHORT);
			aggiornaCursor();
			
			if(result>0) {
				expensesDeletedListener.onDeletedExpense();
			}
			
			final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
			getActivity().sendBroadcast(intAggiornaWidget);
		}
	}


	//recupero icona placeholder della listview
	private class PlaceHolderWorkerTask extends AsyncTask<Integer, Void, Object> {   
	    // Decode image in background.
	    @Override
	    protected Object doInBackground(Integer... params) {
	    	mPlaceHolderBitmapSpese = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[0], 40, 40);
	    	return null;
	    }
	}

	
	//recupero hashmap icone
	private class CaricaHashMapIconeTask extends AsyncTask<Object, Void, Object> {   
	    @Override
	    protected Object doInBackground(Object... params) {
			//recupero info su icone voci
	    	DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(getActivity());
	    	dbcSpeseVoci.openLettura();
	    	Cursor curVoci = dbcSpeseVoci.getTutteLeVoci();
	    	while(curVoci.moveToNext()) {
	    		String voce = curVoci.getString(curVoci.getColumnIndex("voce"));
	    		int icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
	    		hmIcone.put(voce, icona);
	    	}
	    	curVoci.close();
	    	dbcSpeseVoci.close();
	    	return null;
	    }
	}
	

	//variabili di istanza
	private int tipoVisualizzazione;
	private GregorianCalendar dataInizio = new GregorianCalendar();
	private GregorianCalendar dataFine = new GregorianCalendar();
	private DBCSpeseSostenute dbcSpeseSostenute;
	private Cursor childCursor;
	private Cursor mGroupsCursor;
	private MyExpandableListAdapter mAdapter;
	private MyExpandableListAdapter mAdapterVoci;
	private Currency currValuta;
	private StringBuilder ricerca = new StringBuilder(); //filtro per le ricerche
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	private boolean valutaAlternativa;

	//gestione contextual action bar
	private int expandableListSelectionType;
	private ActionMode mActionMode;
	private ArrayList<Long> alSelezionati = new ArrayList<Long>();
	private boolean primaSelezione;
	private ExpensesDeletedListener expensesDeletedListener;

	// Widget.
	private TextView tvPeriodo;
	private ExpandableListView elv;
	private ImageView ivWallet;
	private TextView tvToccaPiu;
	
	//gestione efficiente icone
	private Bitmap mPlaceHolderBitmapSpese;
	private ListViewIconeVeloce iconeVeloci;
	private HashMap<String,Integer> hmIcone = new HashMap<String,Integer>();
}

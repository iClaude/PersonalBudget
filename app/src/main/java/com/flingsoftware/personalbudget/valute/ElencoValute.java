/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.valute;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.DETTAGLIO_VALUTA_CODICE;
import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.DETTAGLIO_VALUTA_SIMBOLO;
import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.DETTAGLIO_VALUTA_TASSO;


public class ElencoValute extends ActionBarActivity {

	//costanti
	public interface CostantiPubbliche {
		String ELENCO_VALUTE_VALUTA_CODICE = "codiceValuta";
		String ELENCO_VALUTE_VALUTADEFAULT_CODICE = "codiceValutaDefault";
		String ELENCO_VALUTE_VALUTA_NOME = "nomeValuta";
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.valute_elenco);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		etRicerca = (EditText) findViewById(R.id.valute_elenco_etRicerca);
		//aggiunta funzionalit� di ricerca alla EditText
		etRicerca.addTextChangedListener(new TextWatcher() {
		     
		    @Override
		    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
		        // When user changed the Text
		        ElencoValute.this.listViewValuteAdapter.getFilter().filter(cs.toString());   
		    }
		     
		    @Override
		    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		         
		    }
		     
		    @Override
		    public void afterTextChanged(Editable arg0) {
		    }
		});
		
		ListView listView = (ListView) findViewById(R.id.ve_lvValute);
		impostaHashMapCodiciNomi();		
		nomiEstesiValute = new ArrayList<String>(hmNomiCodici.keySet());
        Collections.sort(nomiEstesiValute);
        listViewValuteAdapter = new ListViewValuteAdapter(this, nomiEstesiValute);
        listView.setAdapter(listViewValuteAdapter);       
        listView.setTextFilterEnabled(true);
        
        listView.setOnItemClickListener(lvValuteListener);
	}
	
	
	/*
	 * Creo una HashMap con codice ISO4217 e nome esteso di tutte le valute
	 */
	private void impostaHashMapCodiciNomi() {
		hmNomiCodici = new HashMap<String, String>();
		hmNomiCodici.put("United Arab Emirates dirham", "AED");
		hmNomiCodici.put("Afghan afghanim", "AFN");
		hmNomiCodici.put("Albanian lek", "ALL");
		hmNomiCodici.put("Armenian dram", "AMD");
		hmNomiCodici.put("Netherlands Antillean guilder", "ANG");
		hmNomiCodici.put("Angolan kwanza", "AOA");
		hmNomiCodici.put("Argentine peso", "ARS");
		hmNomiCodici.put("Australian dollar", "AUD");
		hmNomiCodici.put("Aruban florin", "AWG");
		hmNomiCodici.put("Azerbaijani manat", "AZN");
		
		hmNomiCodici.put("Bosnia and Herzegovina mark", "BAM");
		hmNomiCodici.put("Barbados dollar", "BBD");
		hmNomiCodici.put("Bangladeshi taka", "BDT");
		hmNomiCodici.put("Bulgarian lev", "BGN");
		hmNomiCodici.put("Bahraini dinar", "BHD");
		hmNomiCodici.put("Burundian franc", "BIF");
		hmNomiCodici.put("Bermudian dollar", "BMD");
		hmNomiCodici.put("Brunei dollar", "BND");
		hmNomiCodici.put("Boliviano", "BOB");
		hmNomiCodici.put("Bolivian Mvdol", "BOV");
		hmNomiCodici.put("Brazilian real", "BRL");
		hmNomiCodici.put("Bahamian dollar", "BSD");
		hmNomiCodici.put("Bhutanese ngultrum", "BTN");
		hmNomiCodici.put("Botswana pula", "BWP");
		hmNomiCodici.put("Belarusian ruble", "BYR");
		hmNomiCodici.put("Belize dollar", "BZD");
		
		hmNomiCodici.put("Canadian dollar", "CAD");
		hmNomiCodici.put("Congolese franc", "CDF");
		hmNomiCodici.put("WIR Euro", "CHE");
		hmNomiCodici.put("Swiss franc", "CHF");
		hmNomiCodici.put("WIR Franc", "CHW");
		hmNomiCodici.put("Unidad de Fomento", "CLF");
		hmNomiCodici.put("Chilean peso", "CLP");
		hmNomiCodici.put("Chinese yuan", "CNY");
		hmNomiCodici.put("Colombian peso", "COP");
		hmNomiCodici.put("Unidad de Valor Real", "COU");
		hmNomiCodici.put("Costa Rican colon", "CRC");
		hmNomiCodici.put("Cuban convertible peso", "CUC");
		hmNomiCodici.put("Cuban peso", "CUP");
		hmNomiCodici.put("Cape Verde escudo", "CVE");
		hmNomiCodici.put("Czech koruna", "CZK");
		
		hmNomiCodici.put("Djiboutian franc", "DJF");
		hmNomiCodici.put("Danish krone", "DKK");
		hmNomiCodici.put("Dominican peso", "DOP");
		hmNomiCodici.put("Algerian dinar", "DZD");
		
		hmNomiCodici.put("Egyptian pound", "EGP");
		hmNomiCodici.put("Eritrean nakfa", "ERN");
		hmNomiCodici.put("Ethiopian birr", "ETB");
		hmNomiCodici.put("Euro", "EUR");
		
		hmNomiCodici.put("Fiji dollar", "FJD");
		hmNomiCodici.put("Falkland Islands pound", "FKP");
		
		hmNomiCodici.put("Pound sterling", "GBP");
		hmNomiCodici.put("Georgian lari", "GEL");
		hmNomiCodici.put("Ghanaian cedi", "GHS");
		hmNomiCodici.put("Gibraltar pound", "GIP");
		hmNomiCodici.put("Gambian dalasi", "GMD");
		hmNomiCodici.put("Guinean franc", "GNF");
		hmNomiCodici.put("Guatemalan quetzal", "GTQ");
		hmNomiCodici.put("Guyanese dollar", "GYD");
		
		hmNomiCodici.put("Hong Kong dollar", "HKD");
		hmNomiCodici.put("Honduran lempira", "HNL");
		hmNomiCodici.put("Croatian kuna", "HRK");
		hmNomiCodici.put("Haitian gourde", "HTG");
		hmNomiCodici.put("Hungarian forint", "HUF");
		
		hmNomiCodici.put("Indonesian rupiah", "IDR");
		hmNomiCodici.put("Israeli new shekel", "ILS");
		hmNomiCodici.put("Indian rupee", "INR");
		hmNomiCodici.put("Iraqi dinar", "IQD");
		hmNomiCodici.put("Iranian rial", "IRR");
		hmNomiCodici.put("Icelandic kr�na", "ISK");
		
		hmNomiCodici.put("Jamaican dollar", "JMD");
		hmNomiCodici.put("Jordanian dinar", "JOD");
		hmNomiCodici.put("Japanese yen", "JPY");
		
		hmNomiCodici.put("Kenyan shilling", "KES");
		hmNomiCodici.put("Kyrgyzstani som", "KGS");
		hmNomiCodici.put("Cambodian riel", "KHR");
		hmNomiCodici.put("Comoro franc", "KMF");
		hmNomiCodici.put("North Korean won", "KPW");
		hmNomiCodici.put("South Korean won", "KRW");
		hmNomiCodici.put("Kuwaiti dinar", "KWD");
		hmNomiCodici.put("Cayman Islands dollar", "KYD");
		hmNomiCodici.put("Kazakhstani tenge", "KZT");
		
		hmNomiCodici.put("Lao kip", "LAK");
		hmNomiCodici.put("Lebanese pound", "LBP");
		hmNomiCodici.put("Sri Lankan rupee", "LKR");
		hmNomiCodici.put("Liberian dollar", "LRD");
		hmNomiCodici.put("Lesotho loti", "LSL");
		hmNomiCodici.put("Lithuanian litas", "LTL");
		hmNomiCodici.put("Libyan dinar", "LYD");
		
		hmNomiCodici.put("Moroccan dirham", "MAD");
		hmNomiCodici.put("Moldovan leu", "MDL");
		hmNomiCodici.put("Malagasy ariary", "MGA");
		hmNomiCodici.put("Macedonian denar", "MKD");
		hmNomiCodici.put("Myanma kyat", "MMK");
		hmNomiCodici.put("Mongolian tugrik", "MNT");
		hmNomiCodici.put("Macanese pataca", "MOP");
		hmNomiCodici.put("Mauritanian ouguiya", "MRO");
		hmNomiCodici.put("Mauritian rupee", "MUR");
		hmNomiCodici.put("Maldivian rufiyaa", "MVR");
		hmNomiCodici.put("Malawian kwacha", "MWK");
		hmNomiCodici.put("Mexican peso", "MXN");
		hmNomiCodici.put("Mexican Unidad de Inversion", "MXV");
		hmNomiCodici.put("Malaysian ringgit", "MYR");
		hmNomiCodici.put("Mozambican metical", "MZN");
		
		hmNomiCodici.put("Namibian dollar", "NAD");
		hmNomiCodici.put("Nigerian naira", "NGN");
		hmNomiCodici.put("Nicaraguan c�rdoba", "NIO");
		hmNomiCodici.put("Norwegian krone", "NOK");
		hmNomiCodici.put("Nepalese rupee", "NPR");
		hmNomiCodici.put("New Zealand dollar", "NZD");
		
		hmNomiCodici.put("Omani rial", "OMR");
		
		hmNomiCodici.put("Panamanian balboa", "PAB");
		hmNomiCodici.put("Peruvian nuevo sol", "PEN");
		hmNomiCodici.put("Papua New Guinean kina", "PGK");
		hmNomiCodici.put("Philippine peso", "PHP");
		hmNomiCodici.put("Pakistani rupee", "PKR");
		hmNomiCodici.put("Polish zloty", "PLN");
		hmNomiCodici.put("Paraguayan guaran�", "PYG");
		
		hmNomiCodici.put("Qatari riyal", "QAR");
		
		hmNomiCodici.put("Romanian new leu", "RON");
		hmNomiCodici.put("Serbian dinar", "RSD");
		hmNomiCodici.put("Russian ruble", "RUB");
		hmNomiCodici.put("Rwandan franc", "RWF");
		
		hmNomiCodici.put("Saudi riyal", "SAR");
		hmNomiCodici.put("Solomon Islands dollar", "SBD");
		hmNomiCodici.put("Seychelles rupee", "SCR");
		hmNomiCodici.put("Sudanese pound", "SDG");
		hmNomiCodici.put("Swedish krona", "SEK");
		hmNomiCodici.put("Singapore dollar", "SGD");
		hmNomiCodici.put("Saint Helena pound", "SHP");
		hmNomiCodici.put("Sierra Leonean leone", "SLL");
		hmNomiCodici.put("Somali shilling", "SOS");
		hmNomiCodici.put("Surinamese dollar", "SRD");
		hmNomiCodici.put("South Sudanese pound", "SSP");
		hmNomiCodici.put("Sao Tom� and Pr�ncipe dobra", "STD");
		hmNomiCodici.put("Syrian pound", "SYP");
		hmNomiCodici.put("Swazi lilangeni", "SZL");
		
		hmNomiCodici.put("Thai baht", "THB");
		hmNomiCodici.put("Tajikistani somoni", "TJS");
		hmNomiCodici.put("Turkmenistani manat", "TMT");
		hmNomiCodici.put("Tunisian dinar", "TND");
		hmNomiCodici.put("Tongan pa'anga", "TOP");
		hmNomiCodici.put("Turkish lira", "TRY");
		hmNomiCodici.put("Trinidad and Tobago dollar", "TTD");
		hmNomiCodici.put("New Taiwan dollar", "TWD");
		hmNomiCodici.put("Tanzanian shilling", "TZS");
		
		hmNomiCodici.put("Ukrainian hryvnia", "UAH");
		hmNomiCodici.put("Ugandan shilling", "UGX");
		hmNomiCodici.put("United States dollar", "USD");
		//hmNomiCodici.put("United States dollar", "USN");
		//hmNomiCodici.put("United States dollar", "USS");
		hmNomiCodici.put("Uruguay Peso en Unidades Indexadas", "UYI");
		hmNomiCodici.put("Uruguayan peso", "UYU");
		hmNomiCodici.put("Uzbekistan som", "UZS");
		
		hmNomiCodici.put("Venezuelan bolivar", "VEF");
		hmNomiCodici.put("Vietnamese dong", "VND");
		hmNomiCodici.put("Vanuatu vatu", "VUV");
		
		hmNomiCodici.put("Samoan tala", "WST");
		
		hmNomiCodici.put("CFA franc BEAC", "XAF");
		hmNomiCodici.put("Silver", "XAG");
		hmNomiCodici.put("Gold", "XAU");
		hmNomiCodici.put("European Monetary Unit", "XBA");
		hmNomiCodici.put("European Unit of Account 9", "XBC");
		hmNomiCodici.put("European Unit of Account 17", "XBD");
		hmNomiCodici.put("East Caribbean dollar", "XCD");
		hmNomiCodici.put("Special drawing rights", "XDR");
		hmNomiCodici.put("UIC franc", "XFU");
		hmNomiCodici.put("CFA franc BCEAO", "XOF");
		hmNomiCodici.put("Palladium", "XPD");
		hmNomiCodici.put("CFP franc", "XPF");
		hmNomiCodici.put("Platinum", "XPT");
		
		hmNomiCodici.put("Yemeni rial", "YER");
		hmNomiCodici.put("South African rand", "ZAR");
		hmNomiCodici.put("Zimbabwe dollar", "ZWL");
	}
	
	
	/*
	 * ViewHolder per la ListView delle valute
	 */
	private static class ViewHolder {
		TextView tvNomeValuta;
		TextView tvCodiceValuta;
		TextView tvSimboloValuta;
	}
	
	
	/*
	 * Classe che estende ArrayAdapter per creare una ListView personalizzata.
	 */
	private class ListViewValuteAdapter extends ArrayAdapter<String> {
		private List<String> nomiValuteOriginale;
		private List<String> nomiValute;
		private LayoutInflater inflater;
		private FiltroListView filtroListView;
		
		public ListViewValuteAdapter(Context context, List<String> nomiValute) {
			super(context, -1, nomiValute);
			
			this.nomiValute = new ArrayList<String>();
			this.nomiValute.addAll(nomiValute);
			nomiValuteOriginale = new ArrayList<String>();
			nomiValuteOriginale.addAll(nomiValute);
			
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.valute_elenco_elementolistview, null);
				
				viewHolder = new ViewHolder();
				viewHolder.tvNomeValuta = (TextView) convertView.findViewById(R.id.valute_elenco_elementolistview_tvNomeValuta);
				viewHolder.tvCodiceValuta = (TextView) convertView.findViewById(R.id.valute_elenco_elementolistview_tvCodiceValuta);
				viewHolder.tvSimboloValuta = (TextView) convertView.findViewById(R.id.valute_elenco_elementoListView_tvSimboloValuta);
				convertView.setTag(viewHolder);
			}
			else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			String nomeValuta = nomiValute.get(position);
			String codiceValuta = hmNomiCodici.get(nomeValuta);
			Currency mCurrency = Currency.getInstance(codiceValuta);
			String simboloValuta = mCurrency.getSymbol();
			
			viewHolder.tvNomeValuta.setText(nomeValuta);
			viewHolder.tvCodiceValuta.setText(codiceValuta);
			viewHolder.tvSimboloValuta.setText(simboloValuta);
			
			return convertView;
		}
		
		
		@Override
		public Filter getFilter() {
		   	if (filtroListView == null){
		   		filtroListView  = new FiltroListView();
		   	}
		   	
		   	return filtroListView;
		}
		
		
		private class FiltroListView extends Filter {
		 
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				constraint = constraint.toString().toLowerCase(Locale.getDefault());
				FilterResults result = new FilterResults();
				if(constraint != null && constraint.toString().length() > 0) {
					ArrayList<String> filteredItems = new ArrayList<String>();
		 
					for(int i = 0, l = nomiValuteOriginale.size(); i < l; i++) {
						String nomeValuta = nomiValuteOriginale.get(i);
						if(nomeValuta.toString().toLowerCase(Locale.getDefault()).contains(constraint)) {
							filteredItems.add(nomeValuta);
						} 
					}
		    	
					result.count = filteredItems.size();
					result.values = filteredItems;
				}
				else {
					synchronized(this) {
						result.values = nomiValuteOriginale;
						result.count = nomiValuteOriginale.size();
					}
				}
		    
				return result;
			}
		 
			
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				nomiValute = (ArrayList<String>) results.values;
				notifyDataSetChanged();
				clear();
				for(int i = 0, l = nomiValute.size(); i < l; i++) {
					add(nomiValute.get(i));
				}
				notifyDataSetInvalidated();
			}
		}
	}
	
	
	//gestione click su elementi della ListView
	OnItemClickListener lvValuteListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent dettaglioValuta = new Intent(ElencoValute.this, DettaglioValuta.class);
			dettaglioValuta.putExtra(CostantiPubbliche.ELENCO_VALUTE_VALUTA_CODICE, hmNomiCodici.get(nomiEstesiValute.get(arg2)));
			dettaglioValuta.putExtra(CostantiPubbliche.ELENCO_VALUTE_VALUTA_NOME, nomiEstesiValute.get(arg2));
			
			startActivityForResult(dettaglioValuta, 0);
		}
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			//comunico i dettagli della valuta scelta all'Activity chiamante
			Intent intRitorno = new Intent();
			intRitorno.putExtra(DETTAGLIO_VALUTA_CODICE, data.getStringExtra(DETTAGLIO_VALUTA_CODICE));
			intRitorno.putExtra(DETTAGLIO_VALUTA_SIMBOLO, data.getStringExtra(DETTAGLIO_VALUTA_SIMBOLO));
			intRitorno.putExtra(DETTAGLIO_VALUTA_TASSO, data.getFloatExtra(DETTAGLIO_VALUTA_TASSO, 1f));
			setResult(Activity.RESULT_OK, intRitorno);
			
			finish();
		}
	}
	
	
	//variabili di istanza
	private EditText etRicerca;
	private ArrayList<String> nomiEstesiValute;
	private HashMap<String, String> hmNomiCodici;
	private ListViewValuteAdapter listViewValuteAdapter;
}

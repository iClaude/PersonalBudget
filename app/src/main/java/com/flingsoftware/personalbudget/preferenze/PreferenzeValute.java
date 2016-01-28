package com.flingsoftware.personalbudget.preferenze;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;


public class PreferenzeValute extends ListPreference {

    public PreferenzeValute(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mioContext = context;
        
        impostaHashMapNomiCodici();
    	impostaArrayNomiECodici();
        setEntries(entries());
        setEntryValues(entryValues());
        setValueIndex(initializeIndex());     
    }

    
    public PreferenzeValute(Context context) {
        this(context, null);
    	
        mioContext = context;
    }

    
    private CharSequence[] entries() {
        //action to provide entry data in char sequence array for list		
    	return arrNomi;
    }

    
    private CharSequence[] entryValues() {
        //action to provide value data for list 	
    	return arrCodici;
    }
    
    
    private int initializeIndex() {
    	SharedPreferences preferenze = PreferenceManager.getDefaultSharedPreferences(mioContext);
    	String valuta = preferenze.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
    	 	
    	int lung = arrCodici.length;
    	int indiceValuta = 0;
    	for(int i=0; i<lung; i++) {
    		if(valuta.equals(arrCodici[i])) {
    			indiceValuta = i;
    			break;
    		}
    	}
    	
    	return indiceValuta;
    }
    
    
    /*
	 * Creo una HashMap con codice ISO4217 e nome esteso di tutte le valute
	 */
	private void impostaHashMapNomiCodici() {
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
		hmNomiCodici.put("Icelandic króna", "ISK");
		
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
		hmNomiCodici.put("Nicaraguan córdoba", "NIO");
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
		hmNomiCodici.put("Paraguayan guaraní", "PYG");
		
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
		hmNomiCodici.put("Sao Tomé and Príncipe dobra", "STD");
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
	 * Ricavo dalla HashMap un array con i nomi estesi delle valute. Utilizzo questi per visualizzare la ListView.
	 */
	private void impostaArrayNomiECodici() {
		Set<String> setNomi = hmNomiCodici.keySet();
		arrNomi = new String[setNomi.size()];
		setNomi.toArray(arrNomi);
		Arrays.sort(arrNomi);
		
		arrCodici = new String[arrNomi.length];
		int lung = arrNomi.length;
		for(int i=0; i<lung; i++) {
			arrCodici[i] = hmNomiCodici.get(arrNomi[i]);
		}
	}
	
	
	//variabili di istanza
	private Context mioContext;
	private HashMap<String, String> hmNomiCodici;
	private String[] arrNomi;
	private String[] arrCodici;
}

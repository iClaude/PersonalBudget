package com.flingsoftware.personalbudget.database;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.flingsoftware.personalbudget.oggetti.SpesaEntrata;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_PICCOLO_AGGIORNA;

public class InserimentoMultiploIntentService extends IntentService {

    // Costanti private
    private static final String NAME = "EntrateMultipleIntentService";
    private static final String TIPO_VOCE = "tipo_voce";
    private static final String RIPETIZIONE = "ripetizione";
    private static final String DATA_FINE = "data_fine";
    private static final String VOCE = "voce";

    // Costanti pubbliche
    public static final String EXTRA_RESULT = "result";
    public static final String TIPO_VOCE_SPESA = "spesa";
    public static final String TIPO_VOCE_ENTRATA = "entrata";


    public static Intent creaIntent(Context context, String tipoVoce, String ripetizione, long dataFine, SpesaEntrata voce) {
        Intent intent = new Intent(context, InserimentoMultiploIntentService.class);
        intent.putExtra(TIPO_VOCE, tipoVoce);
        intent.putExtra(RIPETIZIONE, ripetizione);
        intent.putExtra(DATA_FINE, dataFine);
        intent.putExtra(VOCE, voce);

        return intent;
    }

    public InserimentoMultiploIntentService() {
        super(NAME);
        setIntentRedelivery(false);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String tipoVoce = intent.getStringExtra(TIPO_VOCE);
        String ripetizione = intent.getStringExtra(RIPETIZIONE);
        long dataFine = intent.getLongExtra(DATA_FINE, 0);
        SpesaEntrata voce = intent.getParcelableExtra(VOCE);
        int vociInserite = 0;

        vociInserite = inserimentoMultiplo(tipoVoce, voce, ripetizione, dataFine);

        // Invio broadcast per aggiornare i widget e l'app
        if(vociInserite > 0) {
            final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
            sendBroadcast(intAggiornaWidget);
        }
    }

    private int inserimentoMultiplo(String tipoVoce, SpesaEntrata voce, String ripetizione, long dataFine) {
        FunzioniAggiornamento funzioniAggiornamento = new FunzioniAggiornamento(this);
        return funzioniAggiornamento.inserimentoMultiplo(tipoVoce, voce, ripetizione, dataFine);
    }

}

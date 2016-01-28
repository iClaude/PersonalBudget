package com.flingsoftware.personalbudget.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCEntrateVoci;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.oggetti.SpesaEntrata;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * Questa Activity contiene una lista delle transazioni preferite impostate dall'utente.
 * Premendo su uno dei preferiti viene lanciata l'Activity per l'inserimento della spesa o
 * entrata con i parametri impostati, ma la data è quella attuale.
 */
public class Preferiti extends AppCompatActivity {

    // Variabili.
    private final ArrayList<SpesaEntrata> lstPreferitiSpese = new ArrayList<>();
    private final ArrayList<SpesaEntrata> lstPreferitiEntrate = new ArrayList<>();
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final HashMap<String, Integer> hmIconeSpese = new HashMap<>();
    private final HashMap<String, Integer> hmIconeEntrate = new HashMap<>();
    private final ArrayList<SpesaEntrata> alSpeseSelezionate = new ArrayList<>();
    private final ArrayList<SpesaEntrata> alEntrateSelezionate = new ArrayList<>();
    SpesaEntrata prefDaEliminare;

    // Funzionamento RecyclerView spese.
    private RecyclerView rvPreferitiSpese;
    private RecyclerView.Adapter prefAdapterSpese;
    private RecyclerView.LayoutManager lmPreferitiSpese;
    // Funzionamento RecyclerView entrate.
    private RecyclerView rvPreferitiEntrate;
    private RecyclerView.Adapter prefAdapterEntrate;
    private RecyclerView.LayoutManager lmPreferitiEntrate;

    // Caricamento Bitmap efficiente.
    private ListViewIconeVeloce iconeVeloci;
    private Bitmap mPlaceHolderBitmapSpese;
    private Bitmap mPlaceHolderBitmapEntrate;

    // Contextual action bar per eliminazione preferiti.
    private ActionMode mActionMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferiti);

        // Toolbar per menu opzioni.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Gestione caricamento icone.
        iconeVeloci = new ListViewIconeVeloce(this);
        new CaricaHashMapIconeSpese().execute((Object[]) null);
        new CaricaHashMapIconeEntrate().execute((Object[]) null);
        new PlaceHolderWorkerTask().execute(R.drawable.tag_0, R.drawable.tag_1);

        // Gestione RecyclerView spese.
        rvPreferitiSpese = (RecyclerView) findViewById(R.id.rvPreferitiSpese);
        rvPreferitiSpese.setHasFixedSize(true);
        lmPreferitiSpese = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvPreferitiSpese.setLayoutManager(lmPreferitiSpese);
        prefAdapterSpese = new PreferitiAdapter(lstPreferitiSpese);
        rvPreferitiSpese.setAdapter(prefAdapterSpese);

        // Gestione RecyclerView entrate.
        rvPreferitiEntrate = (RecyclerView) findViewById(R.id.rvPreferitiEntrate);
        rvPreferitiEntrate.setHasFixedSize(true);
        lmPreferitiEntrate = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvPreferitiEntrate.setLayoutManager(lmPreferitiEntrate);
        prefAdapterEntrate = new PreferitiAdapter(lstPreferitiEntrate);
        rvPreferitiEntrate.setAdapter(prefAdapterEntrate);

        /*
        Click sugli elementi della RecyclerView. Questo codice va bene solo se non si hanno listener
        specifici per le child view contenuti in ogni elemento della RecyclerView, ovvero quando
        interessa in modo generico intercettare i click sugli elementi della RecyclerView.
         */
        /*rvPreferiti.addOnItemTouchListener(
            new PreferitiRecyclerItemClickListener(this, new PreferitiRecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    new MioToast(Preferiti.this, "" + view.getContentDescription()).visualizza(Toast.LENGTH_SHORT);
                    //aggiungiPreferito(position);
                }
            })
        );*/
        new RecuperaPreferiti().execute((Object[]) null);

        // Pulsante con freccia per chiudere l'Activity.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    // Pulsante con freccia per chiudere l'Activity.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*
        Gestione RecyclerView. Adapter utilizzato dalla RecyclerView con relativo
        ViewHolder.
     */
    public class PreferitiAdapter extends RecyclerView.Adapter<PreferitiAdapter.ViewHolder> {

        // Variabili dell'adapter.
        private final List<SpesaEntrata> lstPreferiti;


        public PreferitiAdapter(List<SpesaEntrata> lstPreferiti) {
            this.lstPreferiti = lstPreferiti;
        }

        @Override
        public int getItemCount() {
            return lstPreferiti.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View elementoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.preferiti_recyclerview_elemento, parent, false);
            return new ViewHolder(elementoView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SpesaEntrata spesaEntrata = lstPreferiti.get(position);
            holder.rlParent.setTag(spesaEntrata);

            holder.tvImporto.setText(nf.format(spesaEntrata.getImporto()));
            holder.tvVoce.setText(spesaEntrata.getVoce());
            holder.tvDescrizione.setText(spesaEntrata.getDescrizione());
            holder.tvConto.setText(spesaEntrata.getConto().toUpperCase());

            if (spesaEntrata.getTipoVoce() == SpesaEntrata.VOCE_SPESA) {
                iconeVeloci.loadBitmap(hmIconeSpese.get(spesaEntrata.getVoce()), holder.ivIcona, mPlaceHolderBitmapSpese, 80, 80);
            } else {
                iconeVeloci.loadBitmap(hmIconeEntrate.get(spesaEntrata.getVoce()), holder.ivIcona, mPlaceHolderBitmapEntrate, 80, 80);
            }

            anima(holder);
        }

        public void anima(RecyclerView.ViewHolder viewHolder) {
            float startTranslationX = viewHolder.itemView.getWidth() / 2;
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(viewHolder.itemView, "translationX", -startTranslationX, 0.0f);
            objectAnimator.setDuration(1000);
            objectAnimator.setInterpolator(new AnticipateOvershootInterpolator());
            objectAnimator.start();
        }

        // ViewHolder.
        public class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {
            protected RelativeLayout rlParent;
            protected ImageView ivIcona;
            protected TextView tvVoce;
            protected TextView tvImporto;
            protected TextView tvDescrizione;
            protected TextView tvConto;
            protected ImageButton ibOverflow;

            public ViewHolder(View v) {
                super(v);
                rlParent = (RelativeLayout) v.findViewById(R.id.rlParent);
                ivIcona = (ImageView) v.findViewById(R.id.ivIcona);
                tvImporto = (TextView) v.findViewById(R.id.tvImporto);
                tvVoce = (TextView) v.findViewById(R.id.tvVoce);
                tvDescrizione = (TextView) v.findViewById(R.id.tvDescrizione);
                tvConto = (TextView) v.findViewById(R.id.tvConto);
                ibOverflow = (ImageButton) v.findViewById(R.id.ibOverflow);

                // Listener per avere il ripple effect sulle view child del relative layout.
                // RippleForegroundListener rippleForegroundListener = new RippleForegroundListener(rlParent);

                rlParent.setOnClickListener(tuttoIlRestoListener);
                ivIcona.setOnClickListener(tuttoIlRestoListener);
                //ivIcona.setOnTouchListener(rippleForegroundListener);
                tvImporto.setOnClickListener(tuttoIlRestoListener);
                //tvImporto.setOnTouchListener(rippleForegroundListener);
                tvVoce.setOnClickListener(tuttoIlRestoListener);
                //tvVoce.setOnTouchListener(rippleForegroundListener);
                tvDescrizione.setOnClickListener(tuttoIlRestoListener);
                //tvDescrizione.setOnTouchListener(rippleForegroundListener);
                tvConto.setOnClickListener(tuttoIlRestoListener);
                //tvConto.setOnTouchListener(rippleForegroundListener);
                ibOverflow.setOnClickListener(overflowButtonListener);
            }

            /*
             Clicco su una qualsiasi altra view della card view. Lancio l'Activity per inserire
            il preferito
            */
            public OnClickListener tuttoIlRestoListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parent = v instanceof RelativeLayout ? v : (View) v.getParent();
                    SpesaEntrata spesaEntrata = (SpesaEntrata) parent.getTag();
                    aggiungiPreferito(spesaEntrata);
                }
            };

            // Clicco sui 3 puntini e visualizzo un popup per eliminare il preferito.
            public OnClickListener overflowButtonListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    prefDaEliminare = (SpesaEntrata) ((RelativeLayout) v.getParent()).getTag();
                    showPopup(v);
                }
            };

            // Menu popup per il singolo preferito nelle Recycler View.
            public void showPopup(View v) {
                PopupMenu popup = new PopupMenu(Preferiti.this, v);
                popup.setOnMenuItemClickListener(this);
                popup.inflate(R.menu.menu_preferiti_singolo_preferito);
                popup.show();
            }

            // Operazioni associate al menu popup di ogni preferito (eliminazione, ecc.).
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.elimina:
                        new EliminaPreferitoAsyncTask().execute(prefDaEliminare);

                        return true;
                    default:
                        return false;
                }
            }

            /*
            Questo listener serve per avere il ripple effect sulle view child del RelativeLayout.
            Senza questo listener, che viene attivato quando si preme su una view child, i listener
            precedenti (OnClickListener) che servono per aggiungere il preferito impediscono al
            ripple effect di funzionare.
            Con questo listener quando si preme una child view viene impostato lo stato pressed
            del RelativeLayout padre per avere l'effetto ripple.
             */
            /*
            public class RippleForegroundListener implements View.OnTouchListener {
                private RelativeLayout rlParent;

                public RippleForegroundListener(RelativeLayout rlParent) {
                    this.rlParent = rlParent;
                }

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Convert to card view coordinates. Assumes the host view is
                    // a direct child and the card view is not scrollable.
                    float x = event.getX() + v.getLeft();
                    float y = event.getY() + v.getTop();

                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        // Simulate motion on the card view.
                        rlParent.drawableHotspotChanged(x, y);
                    }

                    // Simulate pressed state on the card view.
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            rlParent.setPressed(true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            rlParent.setPressed(false);
                            break;
                    }

                    // Pass all events through to the host view.
                    return false;
                }
            }
            */
        }
    }


    // Gestione RecyclerView. Recupero delle entrate e spese preferiti in un thread separato.
    private class RecuperaPreferiti extends AsyncTask<Object, Object, Object> {
        final DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(Preferiti.this);
        final DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(Preferiti.this);
        Cursor curSpesePreferite;
        Cursor curEntratePreferite;

        protected Object doInBackground(Object... params) {
            // Definisco gli indici delle colonne del Cursor per migliorare le prestazioni.
            final int colId = 0;
            final int colData = 1;
            final int colVoce = 2;
            final int colImporto = 3;
            final int colValuta = 4;
            final int colValprin = 5;
            final int colDescrizione = 6;
            final int colRipetizioneId = 7;
            final int colConto = 8;

            // Spese preferite.
            dbcSpeseSostenute.openLettura();
            curSpesePreferite = dbcSpeseSostenute.getSpeseSostenutePreferite();
            lstPreferitiSpese.clear();
            for (int i = 1; curSpesePreferite.moveToNext(); i++) {
                lstPreferitiSpese.add(new SpesaEntrata(SpesaEntrata.VOCE_SPESA, curSpesePreferite.getLong(colId), curSpesePreferite.getLong(colData), curSpesePreferite.getString(colVoce), curSpesePreferite.getDouble(colImporto), curSpesePreferite.getString(colValuta), curSpesePreferite.getDouble(colValprin), curSpesePreferite.getString(colDescrizione), curSpesePreferite.getLong(colRipetizioneId), curSpesePreferite.getString(colConto)));
            }

            // Entrate preferite.
            dbcEntrateIncassate.openLettura();
            curEntratePreferite = dbcEntrateIncassate.getEntrateIncassatePreferite();
            lstPreferitiEntrate.clear();

            for (int i = 1; curEntratePreferite.moveToNext(); i++) {
                lstPreferitiEntrate.add(new SpesaEntrata(SpesaEntrata.VOCE_ENTRATA, curEntratePreferite.getLong(colId), curEntratePreferite.getLong(colData), curEntratePreferite.getString(colVoce), curEntratePreferite.getDouble(colImporto), curEntratePreferite.getString(colValuta), curEntratePreferite.getDouble(colValprin), curEntratePreferite.getString(colDescrizione), curEntratePreferite.getLong(colRipetizioneId), curEntratePreferite.getString(colConto)));
            }

            return null;
        }

        protected void onPostExecute(Object result) {
            curSpesePreferite.close();
            curEntratePreferite.close();
            dbcSpeseSostenute.close();
            dbcEntrateIncassate.close();

            if (lstPreferitiSpese.size() > 0 || lstPreferitiEntrate.size() > 0) {
                findViewById(R.id.llIstruzioni).setVisibility(View.GONE);

                if (lstPreferitiSpese.size() > 0 && lstPreferitiEntrate.size() <= 0) {
                    findViewById(R.id.llPreferitiEntrate).setVisibility(View.GONE);
                    animaEntrata(findViewById(R.id.llPreferitiSpese));
                }
                else if (lstPreferitiSpese.size() <= 0 && lstPreferitiEntrate.size() > 0) {
                    findViewById(R.id.llPreferitiSpese).setVisibility(View.GONE);
                    animaEntrata(findViewById(R.id.llPreferitiEntrate));
                }
                else {
                    animaEntrata(findViewById(R.id.llPreferitiSpese), findViewById(R.id.llPreferitiEntrate));
                }

                prefAdapterSpese.notifyDataSetChanged();
                prefAdapterEntrate.notifyDataSetChanged();
            } else {
                findViewById(R.id.llIstruzioni).setVisibility(View.VISIBLE);
                findViewById(R.id.llPreferitiSpese).setVisibility(View.GONE);
                findViewById(R.id.llPreferitiEntrate).setVisibility(View.GONE);
            }
        }
    }


    private void animaEntrata(View... viewDaVisualizzare) {
        ObjectAnimator arrAnimator[] = new ObjectAnimator[viewDaVisualizzare.length];

        arrAnimator[0] = ObjectAnimator.ofFloat(viewDaVisualizzare[0], "alpha", 0.0f, 1.0f);
        viewDaVisualizzare[0].setVisibility(View.VISIBLE);
        if(viewDaVisualizzare.length > 1) {
            arrAnimator[1] = ObjectAnimator.ofFloat(viewDaVisualizzare[1], "alpha", 0.0f, 1.0f);
            viewDaVisualizzare[1].setVisibility(View.VISIBLE);
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(arrAnimator);
        animSet.setDuration(1000);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.start();
    }


    /*
    Dato un oggetto SpesaEntrata che rappresenta un preferito, si lancia l'apposita Activity per
    aggiungere una spesa o una entrata.
     */
    private void aggiungiPreferito(SpesaEntrata spesaEntrata) {
        Intent addFavoriteIntent;

        if (spesaEntrata.getTipoVoce() == SpesaEntrata.VOCE_SPESA) {
            addFavoriteIntent = SpeseAggiungi.makeIntent(Preferiti.this, spesaEntrata.getId(), spesaEntrata.getVoce(), spesaEntrata.getImporto(), spesaEntrata.getValuta(), spesaEntrata.getImportoValprin(), spesaEntrata.getData(), spesaEntrata.getDescrizione(), spesaEntrata.getRipetizioneId(), spesaEntrata.getConto(), -1);
        } else {
            addFavoriteIntent = EntrateAggiungi.makeIntent(Preferiti.this, spesaEntrata.getId(), spesaEntrata.getVoce(), spesaEntrata.getImporto(), spesaEntrata.getValuta(), spesaEntrata.getImportoValprin(), spesaEntrata.getData(), spesaEntrata.getDescrizione(), spesaEntrata.getRipetizioneId(), spesaEntrata.getConto(), -1);
        }

        startActivityForResult(addFavoriteIntent, 0);
    }


    // Ritorno dall'Activity di aggiunta spese/entrate.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }


    /*
        Elimino il preferito in un thread separato. Il preferito ? rimosso unicamente dalla lista
        dei preferiti (impostando il flag preferito su false). Si tratta in realt? di un update.
     */
    private class EliminaPreferitoAsyncTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            SpesaEntrata spesaEntrata = (SpesaEntrata) params[0];

            if (spesaEntrata.getTipoVoce() == SpesaEntrata.VOCE_SPESA) {
                DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(Preferiti.this);
                dbcSpeseSostenute.openModifica();
                dbcSpeseSostenute.aggiornaSpesaSostenuta(spesaEntrata.getId(), spesaEntrata.getData(), spesaEntrata.getVoce(), spesaEntrata.getImporto(), spesaEntrata.getValuta(), spesaEntrata.getImportoValprin(), spesaEntrata.getDescrizione(), spesaEntrata.getRipetizioneId(), spesaEntrata.getConto(), 0);
                dbcSpeseSostenute.close();
            } else {
                DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(Preferiti.this);
                dbcEntrateIncassate.openModifica();
                dbcEntrateIncassate.aggiornaEntrataIncassata(spesaEntrata.getId(), spesaEntrata.getData(), spesaEntrata.getVoce(), spesaEntrata.getImporto(), spesaEntrata.getValuta(), spesaEntrata.getImportoValprin(), spesaEntrata.getDescrizione(), spesaEntrata.getRipetizioneId(), spesaEntrata.getConto(), 0);
                dbcEntrateIncassate.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            new RecuperaPreferiti().execute((Object[]) null);
        }
    }


    /*
        Caricamento Bitmap efficiente. Creo una HashMap con voce e icona (spese) in un thread
        separato come da tabella spese_voci.
     */
    private class CaricaHashMapIconeSpese extends AsyncTask<Object, Void, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            //recupero info su icone voci
            DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(Preferiti.this);
            dbcSpeseVoci.openLettura();
            Cursor curVoci = dbcSpeseVoci.getTutteLeVoci();
            while (curVoci.moveToNext()) {
                String voce = curVoci.getString(curVoci.getColumnIndex("voce"));
                int icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
                hmIconeSpese.put(voce, icona);
            }
            curVoci.close();
            dbcSpeseVoci.close();
            return null;
        }
    }

    /*
        Caricamento Bitmap efficiente. Creo una HashMap con voce e icona (entrate) in un thread
        separato come da tabella entrate_voci.
     */
    private class CaricaHashMapIconeEntrate extends AsyncTask<Object, Void, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            //recupero info su icone voci
            DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(Preferiti.this);
            dbcEntrateVoci.openLettura();
            Cursor curVoci = dbcEntrateVoci.getTutteLeVoci();
            while (curVoci.moveToNext()) {
                String voce = curVoci.getString(curVoci.getColumnIndex("voce"));
                int icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
                hmIconeEntrate.put(voce, icona);
            }
            curVoci.close();
            dbcEntrateVoci.close();
            return null;
        }
    }

    /*
        Caricamento Bitmap efficiente. Ricavo le Bitmap di default (segnaposto) per spese e entrate
        in un thread separato.
     */
    private class PlaceHolderWorkerTask extends AsyncTask<Integer, Void, Object> {
        @Override
        protected Object doInBackground(Integer... params) {
            mPlaceHolderBitmapSpese = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[0], 80, 80);
            mPlaceHolderBitmapEntrate = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[1], 80, 80);
            return null;
        }
    }
}

package com.flingsoftware.personalbudget.esporta;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;


/**
 * Exports the internal database into a csv file.
 * Plays the role of ConcreteStrategy in the Strategy Pattern.
 */
public class ExportCSVStrategy implements ExportStrategy {

    // Instance variables.
    private Context mioContext;
    Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
    private String trasf;
    private SharedPreferences sharedPreferences;
    private Currency valutaPrincipale;


    public ExportCSVStrategy(Context context, String transf, Currency mainCurrency) {
        this.mioContext = context;
        this.trasf = transf;
        this.valutaPrincipale = mainCurrency;
    }


    /**
     * This method exports the database into a csv file (plain text).
     *
     * @param exportDetails ExportDetails object containing details about the export operation (
     *                      exporting expenses, earnings, start and ending dates).
     * @return true for success, false for failure
     */
    @Override
    public boolean exportDatabase(ExportDetails exportDetails) {
        // Retrieve details about export operation.
        boolean esportaSpese = exportDetails.isExportExpenses();
        boolean esportaEntrate = exportDetails.isExportEarnings();
        long dataInizio = exportDetails.getDateStart();
        long dataFine = exportDetails.getDateEnd();

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.getDefault());

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) { //controllo se la directory esterna ? disponibile per la scrittura
            return false;
        }
        else {
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            File file;
            PrintWriter printWriter = null;
            try
            {
                String nomeFile = "PersonalBudget.csv";
                file = new File(exportDir, nomeFile);
                file.createNewFile();
                printWriter = new PrintWriter(new FileWriter(file));

                if(esportaSpese) {
                    DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(mioContext);
                    dbcSpeseSostenute.openLettura();

                    //tabella spese_sost
                    Cursor curCSV = dbcSpeseSostenute.getSpeseSostenuteIntervalloNoTrasf(dataInizio, dataFine, trasf);
                    printWriter.println(mioContext.getString(R.string.menu_esporta_tabellaspese));
                    printWriter.println(mioContext.getString(R.string.menu_esporta_databasetitolicolonne));
                    while(curCSV.moveToNext())
                    {
                        Long data = curCSV.getLong(curCSV.getColumnIndex("data"));
                        String voce = curCSV.getString(curCSV.getColumnIndex("voce"));
                        String conto = curCSV.getString(curCSV.getColumnIndex("conto"));
                        Double importo = curCSV.getDouble(curCSV.getColumnIndex("importo"));
                        String valuta = curCSV.getString(curCSV.getColumnIndex("valuta"));
                        Double importoValprin = curCSV.getDouble(curCSV.getColumnIndex("importo_valprin"));
                        String descrizione = curCSV.getString(curCSV.getColumnIndex("descrizione"));

                        String record = df.format(new Date(data)) + "," + voce + "," + conto + "," + nf.format(importo) + "," + valuta + "," + nf.format(importoValprin) + "," + descrizione + ",";
                        printWriter.println(record);
                    }

                    curCSV.close();
                    dbcSpeseSostenute.close();
                }

                printWriter.println();
                printWriter.println();

                if(esportaEntrate) {
                    DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(mioContext);
                    dbcEntrateIncassate.openLettura();

                    //tabella entrate_inc
                    Cursor curCSV = dbcEntrateIncassate.getEntrateIncassateIntervalloNoTrasf(dataInizio, dataFine, trasf); //considero tutti i conti
                    printWriter.println(mioContext.getString(R.string.menu_esporta_tabellaentrate));
                    printWriter.println(mioContext.getString(R.string.menu_esporta_databasetitolicolonne));
                    while(curCSV.moveToNext())
                    {
                        Long data = curCSV.getLong(curCSV.getColumnIndex("data"));
                        String voce = curCSV.getString(curCSV.getColumnIndex("voce"));
                        String conto = curCSV.getString(curCSV.getColumnIndex("conto"));
                        Double importo = curCSV.getDouble(curCSV.getColumnIndex("importo"));
                        String valuta = curCSV.getString(curCSV.getColumnIndex("valuta"));
                        Double importoValprin = curCSV.getDouble(curCSV.getColumnIndex("importo_valprin"));
                        String descrizione = curCSV.getString(curCSV.getColumnIndex("descrizione"));

                        String record = df.format(new Date(data)) + "," + voce + "," + conto + "," + importo.toString() + "," + valuta + "," + importoValprin.toString() + "," + descrizione + ",";
                        printWriter.println(record);
                    }

                    curCSV.close();
                    dbcEntrateIncassate.close();
                }
            }
            catch(Exception exc)
            {
                exc.printStackTrace();
                return false;
            }
            finally {
                if(printWriter != null) printWriter.close();
            }
        }

        return true;
    }


    @Override
    public String getOutputFormat() {
        return "csv";
    }
}

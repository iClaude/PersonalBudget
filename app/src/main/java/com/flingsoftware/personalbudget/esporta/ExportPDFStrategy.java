package com.flingsoftware.personalbudget.esporta;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Environment;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.pdfjet.Align;
import com.pdfjet.Border;
import com.pdfjet.Cell;
import com.pdfjet.Color;
import com.pdfjet.CoreFont;
import com.pdfjet.Font;
import com.pdfjet.Image;
import com.pdfjet.ImageType;
import com.pdfjet.Letter;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import com.pdfjet.Table;
import com.pdfjet.TextLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Exports the internal database into a pdf file.
 * Plays the role of ConcreteStrategy in the Strategy Pattern.
 */
public class ExportPDFStrategy implements ExportStrategy {

    // Instance variables.
    private Context mioContext;
    Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
    private String trasf;
    private SharedPreferences sharedPreferences;
    private Currency valutaPrincipale;


    public ExportPDFStrategy(Context context, String transf, Currency mainCurrency) {
        this.mioContext = context;
        this.trasf = transf;
        this.valutaPrincipale = mainCurrency;
    }


    /**
     * This method exports the database into a pdf file with proper formatting using the external
     * library PDFJET.
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
        NumberFormat nfOrig = NumberFormat.getCurrencyInstance(Locale.getDefault());
        NumberFormat nfConv = NumberFormat.getCurrencyInstance(Locale.getDefault());
        nfConv.setCurrency(valutaPrincipale);

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
            file = new File(exportDir, "PersonalBudget.pdf");

            try {
                //impostazione generali del pdf
                PDF pdf = new PDF(new FileOutputStream(file));

                Font f0 = new Font(pdf, CoreFont.HELVETICA_BOLD);
                f0.setSize(14f);
                Font f1 = new Font(pdf, CoreFont.HELVETICA_BOLD);
                f1.setSize(9.8f);
                Font f2 = new Font(pdf, CoreFont.HELVETICA);
                f2.setSize(9.8f);

                //immagine con logo app
                AssetManager assets = mioContext.getAssets();
                InputStream is = assets.open("images/wallet.png");
                Image iLogo = new Image(pdf, is, ImageType.PNG);
                iLogo.scaleBy(0.112);

                if(esportaSpese) {
                    DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(mioContext);
                    dbcSpeseSostenute.openLettura();
                    Cursor curCSV = dbcSpeseSostenute.getSpeseSostenuteIntervalloNoTrasf(dataInizio, dataFine, trasf);
                    Cursor curTotale = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf("%", dataInizio, dataFine, trasf); //considero tutti i conti

                    //nuova pagina
                    Page page = new Page(pdf, Letter.LANDSCAPE);

                    //informazioni iniziali su nome app e siti
                    TextLine nomeAppAzienda = new TextLine(f0, "PERSONAL BUDGET APP - FLING SOFTWARE?");

                    TextLine sito = new TextLine(f2, "Visit Website");
                    sito.setColor(Color.blue);
                    sito.setUnderline(true);
                    sito.setURIAction("https://www.facebook.com/personalbudgetapp");

                    TextLine googlePlay = new TextLine(f2, "Google Play Page");
                    googlePlay.setColor(Color.blue);
                    googlePlay.setUnderline(true);
                    googlePlay.setURIAction("https://play.google.com/store/apps/developer?id=Fling%20Software%C2%A9&hl=it");

                    //titolo tabella
                    TextLine titoloTabella = new TextLine(f1, mioContext.getString(R.string.menu_esporta_tabellapdf_tabellaSpese));
                    titoloTabella.setFont(f0);
                    titoloTabella.setUnderline(true);

                    //periodo
                    String strPeriodo = mioContext.getString(R.string.menu_esporta_tabellaPdf_periodo) + " " + df.format(new Date(dataInizio)) + " - " + df.format(new Date(dataFine));
                    TextLine tlPeriodo = new TextLine(f1, strPeriodo);

                    //creazione tabella
                    Table table = new Table();
                    List<List<Cell>> tableData = new ArrayList<List<Cell>>();

                    List<Cell> titoliColonne = new ArrayList<Cell>();
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaData)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaVoce)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaConto)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaImporto)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaValuta)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaImportoConv)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaDescrizione)));
                    for(int i=0; i<7; i++) {
                        titoliColonne.get(i).setBgColor(Color.lightyellow);
                        titoliColonne.get(i).setTextAlignment(Align.CENTER);
                    }
                    tableData.add(titoliColonne);

                    int i = 1;
                    while(curCSV.moveToNext())
                    {
                        Long data = curCSV.getLong(curCSV.getColumnIndex("data"));
                        String voce = curCSV.getString(curCSV.getColumnIndex("voce"));
                        String conto = curCSV.getString(curCSV.getColumnIndex("conto"));
                        Double importo = curCSV.getDouble(curCSV.getColumnIndex("importo"));
                        String valuta = curCSV.getString(curCSV.getColumnIndex("valuta"));
                        nfOrig.setCurrency(Currency.getInstance(valuta));
                        Double importoValprin = curCSV.getDouble(curCSV.getColumnIndex("importo_valprin"));
                        String descrizione = curCSV.getString(curCSV.getColumnIndex("descrizione"));

                        List<Cell> record = new ArrayList<Cell>();
                        Cell dataCell = new Cell(f2, df.format(new Date(data)));
                        dataCell.setTextAlignment(Align.CENTER);
                        record.add(dataCell);
                        Cell voceCell = new Cell(f2, voce);
                        record.add(voceCell);
                        Cell contoCell = new Cell(f2, conto);
                        record.add(contoCell);
                        Cell importoCell = new Cell(f2, nfOrig.format(importo));
                        importoCell.setTextAlignment(Align.RIGHT);
                        record.add(importoCell);
                        Cell valutaCell = new Cell(f2, valuta);
                        valutaCell.setTextAlignment(Align.CENTER);
                        record.add(valutaCell);
                        Cell importoValprinCell = new Cell(f2, nfConv.format(importoValprin));
                        importoValprinCell.setTextAlignment(Align.RIGHT);
                        record.add(importoValprinCell);
                        Cell descrizioneCell = new Cell(f2, descrizione);
                        record.add(descrizioneCell);
                        tableData.add(record);

                        if(i%2 == 0) {
                            dataCell.setBgColor(Color.lightgray);
                            voceCell.setBgColor(Color.lightgray);
                            contoCell.setBgColor(Color.lightgray);
                            importoCell.setBgColor(Color.lightgray);
                            valutaCell.setBgColor(Color.lightgray);
                            importoValprinCell.setBgColor(Color.lightgray);
                            descrizioneCell.setBgColor(Color.lightgray);
                        }
                        i++;
                    }

                    //totale spese
                    curTotale.moveToFirst();
                    double totaleSpese = curTotale.getDouble(curTotale.getColumnIndex("totale_spesa"));
                    List<Cell> record = new ArrayList<Cell>();
                    Cell cellComodoSx = new Cell(f1, "");
                    cellComodoSx.setBgColor(Color.lightyellow);
                    cellComodoSx.setBorder(Border.RIGHT, false);
                    Cell cellComodoDx = new Cell(f1, "");
                    cellComodoDx.setBorder(Border.LEFT, false);
                    cellComodoDx.setBgColor(Color.lightyellow);
                    Cell cellComodoCen1 = new Cell(f1, "");
                    cellComodoCen1.setBgColor(Color.lightyellow);
                    cellComodoCen1.setBorder(Border.LEFT, false);
                    cellComodoCen1.setBorder(Border.RIGHT, false);
                    Cell cellComodoCen2 = new Cell(f1, "");
                    cellComodoCen2.setBgColor(Color.lightyellow);
                    cellComodoCen2.setBorder(Border.LEFT, false);
                    cellComodoCen2.setBorder(Border.RIGHT, false);
                    Cell cellComodoCen3 = new Cell(f1, "");
                    cellComodoCen3.setBgColor(Color.lightyellow);
                    cellComodoCen3.setBorder(Border.LEFT, false);
                    cellComodoCen3.setBorder(Border.RIGHT, false);

                    record.add(cellComodoSx);
                    Cell cellTotaleLabel = new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_totale));
                    cellTotaleLabel.setBgColor(Color.lightyellow);
                    cellTotaleLabel.setUnderline(true);
                    cellTotaleLabel.setBorder(Border.LEFT, false);
                    cellTotaleLabel.setBorder(Border.RIGHT, false);
                    record.add(cellTotaleLabel);
                    record.add(cellComodoCen1);
                    record.add(cellComodoCen2);
                    record.add(cellComodoCen3);
                    Cell cellTotaleSpese = new Cell(f1, nfConv.format(totaleSpese));
                    cellTotaleSpese.setBgColor(Color.lightyellow);
                    cellTotaleSpese.setUnderline(true);
                    cellTotaleSpese.setBorder(Border.LEFT, false);
                    cellTotaleSpese.setBorder(Border.RIGHT, false);
                    cellTotaleSpese.setTextAlignment(Align.RIGHT);
                    record.add(cellTotaleSpese);
                    record.add(cellComodoDx);
                    tableData.add(record);

                    table.setData(tableData, Table.DATA_HAS_1_HEADER_ROWS);
                    table.autoAdjustColumnWidths();
                    table.setColumnWidth(0, 70.0f);
                    table.setColumnWidth(1, 102.0f);
                    table.setColumnWidth(2, 102.0f);
                    table.setColumnWidth(3, 106.0f);
                    table.setColumnWidth(5, 106.0f);
                    table.setColumnWidth(6, 160.0f);
                    table.wrapAroundCellText();

                    //calcolo dimensioni tabella e coordinate per centrarla
                    float largTab = table.getWidth();
                    float largPag = page.getWidth();
                    float margSin = largPag/2 - largTab/2;
                    float largTitolo = titoloTabella.getWidth();
                    iLogo.setPosition(margSin, 35f);
                    nomeAppAzienda.setPosition(margSin + iLogo.getWidth() + 10f, 52.0f);
                    sito.setPosition(margSin + iLogo.getWidth() + 10f, 67.0f);
                    googlePlay.setPosition(margSin + iLogo.getWidth() + 10f, 82.0f);
                    tlPeriodo.setPosition(margSin + largTab - tlPeriodo.getWidth(), 52f);
                    titoloTabella.setPosition(largPag/2 - largTitolo/2, 125.0f);
                    table.setPosition(margSin, 145.0f);

                    //stampo l'intestazione e il titolo centrati
                    iLogo.drawOn(page);
                    nomeAppAzienda.drawOn(page);
                    sito.drawOn(page);
                    googlePlay.drawOn(page);
                    tlPeriodo.drawOn(page);
                    titoloTabella.drawOn(page);

                    int numOfPages = table.getNumberOfPages(page);
                    int pagCorr = 0;
                    while (true) {
                        table.drawOn(page);
                        //scrivo il numero di pagine
                        TextLine numPag = new TextLine(f2, mioContext.getString(R.string.menu_esporta_tabellaPdf_numPag, ++pagCorr, numOfPages));
                        numPag.setPosition(largPag - 30.0f - numPag.getWidth(), page.getHeight() - 10.0f);
                        numPag.drawOn(page);

                        //scrivo il nome tabella a fondo pagina
                        TextLine tabella = new TextLine(f2, mioContext.getString(R.string.menu_esporta_tabellaPdf_fondoPaginaSpese));
                        tabella.setPosition(30.0f, page.getHeight() - 10.0f);
                        tabella.drawOn(page);

                        if (!table.hasMoreData()) {
                            // Allow the table to be drawn again later:
                            table.resetRenderedPagesCount();
                            break;
                        }
                        //imposto una nuova pagina
                        page = new Page(pdf, Letter.LANDSCAPE);
                        iLogo.drawOn(page);
                        nomeAppAzienda.drawOn(page);
                        sito.drawOn(page);
                        googlePlay.drawOn(page);
                        tlPeriodo.drawOn(page);
                        table.setPosition(margSin, 125.0f);
                    }

                    curCSV.close();
                    curTotale.close();
                    dbcSpeseSostenute.close();
                }

                if(esportaEntrate) {
                    DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(mioContext);
                    dbcEntrateIncassate.openLettura();
                    Cursor curCSV = dbcEntrateIncassate.getEntrateIncassateIntervalloNoTrasf(dataInizio, dataFine, trasf); //considero tutti i conti
                    Cursor curTotale = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf("%", dataInizio, dataFine, trasf); //considero tutti i conti

                    //nuova pagina
                    Page page = new Page(pdf, Letter.LANDSCAPE);

                    //informazioni iniziali su nome app e siti
                    TextLine nomeAppAzienda = new TextLine(f0, "PERSONAL BUDGET APP - FLING SOFTWARE?");

                    TextLine sito = new TextLine(f2, "Visit Website");
                    sito.setColor(Color.blue);
                    sito.setUnderline(true);
                    sito.setURIAction("https://www.facebook.com/personalbudgetapp");

                    TextLine googlePlay = new TextLine(f2, "Google Play Page");
                    googlePlay.setColor(Color.blue);
                    googlePlay.setUnderline(true);
                    googlePlay.setURIAction("https://play.google.com/store/apps/developer?id=Fling%20Software%C2%A9&hl=it");

                    //titolo tabella
                    TextLine titoloTabella = new TextLine(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_tabellaEntrate));
                    titoloTabella.setFont(f0);
                    titoloTabella.setUnderline(true);

                    //periodo
                    String strPeriodo = mioContext.getString(R.string.menu_esporta_tabellaPdf_periodo) + " " + df.format(new Date(dataInizio)) + " - " + df.format(new Date(dataFine));
                    TextLine tlPeriodo = new TextLine(f1, strPeriodo);

                    //creazione tabella
                    Table table = new Table();
                    List<List<Cell>> tableData = new ArrayList<List<Cell>>();

                    List<Cell> titoliColonne = new ArrayList<Cell>();
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaData)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaVoce)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaConto)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaImporto)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaValuta)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaImportoConv)));
                    titoliColonne.add(new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaDescrizione)));
                    for(int i=0; i<7; i++) {
                        titoliColonne.get(i).setBgColor(Color.lightyellow);
                        titoliColonne.get(i).setTextAlignment(Align.CENTER);
                    }
                    tableData.add(titoliColonne);

                    int i = 1;
                    while(curCSV.moveToNext())
                    {
                        Long data = curCSV.getLong(curCSV.getColumnIndex("data"));
                        String voce = curCSV.getString(curCSV.getColumnIndex("voce"));
                        String conto = curCSV.getString(curCSV.getColumnIndex("conto"));
                        Double importo = curCSV.getDouble(curCSV.getColumnIndex("importo"));
                        String valuta = curCSV.getString(curCSV.getColumnIndex("valuta"));
                        nfOrig.setCurrency(Currency.getInstance(valuta));
                        Double importoValprin = curCSV.getDouble(curCSV.getColumnIndex("importo_valprin"));
                        String descrizione = curCSV.getString(curCSV.getColumnIndex("descrizione"));

                        List<Cell> record = new ArrayList<Cell>();
                        Cell dataCell = new Cell(f2, df.format(new Date(data)));
                        dataCell.setTextAlignment(Align.CENTER);
                        record.add(dataCell);
                        Cell voceCell = new Cell(f2, voce);
                        record.add(voceCell);
                        Cell contoCell = new Cell(f2, conto);
                        record.add(contoCell);
                        Cell importoCell = new Cell(f2, nfOrig.format(importo));
                        importoCell.setTextAlignment(Align.RIGHT);
                        record.add(importoCell);
                        Cell valutaCell = new Cell(f2, valuta);
                        valutaCell.setTextAlignment(Align.CENTER);
                        record.add(valutaCell);
                        Cell importoValprinCell = new Cell(f2, nfConv.format(importoValprin));
                        importoValprinCell.setTextAlignment(Align.RIGHT);
                        record.add(importoValprinCell);
                        Cell descrizioneCell = new Cell(f2, descrizione);
                        record.add(descrizioneCell);
                        tableData.add(record);

                        if(i%2 == 0) {
                            dataCell.setBgColor(Color.lightgray);
                            voceCell.setBgColor(Color.lightgray);
                            contoCell.setBgColor(Color.lightgray);
                            importoCell.setBgColor(Color.lightgray);
                            valutaCell.setBgColor(Color.lightgray);
                            importoValprinCell.setBgColor(Color.lightgray);
                            descrizioneCell.setBgColor(Color.lightgray);
                        }
                        i++;
                    }

                    //totale entrate
                    curTotale.moveToFirst();
                    double totaleEntrate = curTotale.getDouble(curTotale.getColumnIndex("totale_entrata"));
                    List<Cell> record = new ArrayList<Cell>();
                    Cell cellComodoSx = new Cell(f1, "");
                    cellComodoSx.setBgColor(Color.lightyellow);
                    cellComodoSx.setBorder(Border.RIGHT, false);
                    Cell cellComodoDx = new Cell(f1, "");
                    cellComodoDx.setBorder(Border.LEFT, false);
                    cellComodoDx.setBgColor(Color.lightyellow);
                    Cell cellComodoCen1 = new Cell(f1, "");
                    cellComodoCen1.setBgColor(Color.lightyellow);
                    cellComodoCen1.setBorder(Border.LEFT, false);
                    cellComodoCen1.setBorder(Border.RIGHT, false);
                    Cell cellComodoCen2 = new Cell(f1, "");
                    cellComodoCen2.setBgColor(Color.lightyellow);
                    cellComodoCen2.setBorder(Border.LEFT, false);
                    cellComodoCen2.setBorder(Border.RIGHT, false);
                    Cell cellComodoCen3 = new Cell(f1, "");
                    cellComodoCen3.setBgColor(Color.lightyellow);
                    cellComodoCen3.setBorder(Border.LEFT, false);
                    cellComodoCen3.setBorder(Border.RIGHT, false);

                    record.add(cellComodoSx);
                    Cell cellTotaleLabel = new Cell(f1, mioContext.getString(R.string.menu_esporta_tabellaPdf_totale));
                    cellTotaleLabel.setBgColor(Color.lightyellow);
                    cellTotaleLabel.setUnderline(true);
                    cellTotaleLabel.setBorder(Border.LEFT, false);
                    cellTotaleLabel.setBorder(Border.RIGHT, false);
                    record.add(cellTotaleLabel);
                    record.add(cellComodoCen1);
                    record.add(cellComodoCen2);
                    record.add(cellComodoCen3);
                    Cell cellTotaleEntrate = new Cell(f1, nfConv.format(totaleEntrate));
                    cellTotaleEntrate.setBgColor(Color.lightyellow);
                    cellTotaleEntrate.setUnderline(true);
                    cellTotaleEntrate.setBorder(Border.LEFT, false);
                    cellTotaleEntrate.setBorder(Border.RIGHT, false);
                    cellTotaleEntrate.setTextAlignment(Align.RIGHT);
                    record.add(cellTotaleEntrate);
                    record.add(cellComodoDx);
                    tableData.add(record);

                    table.setData(tableData, Table.DATA_HAS_1_HEADER_ROWS);
                    table.autoAdjustColumnWidths();
                    table.setColumnWidth(0, 70.0f);
                    table.setColumnWidth(1, 102.0f);
                    table.setColumnWidth(2, 102.0f);
                    table.setColumnWidth(3, 106.0f);
                    table.setColumnWidth(5, 106.0f);
                    table.setColumnWidth(6, 160.0f);
                    table.wrapAroundCellText();

                    //calcolo dimensioni tabella e coordinate per centrarla
                    float largTab = table.getWidth();
                    float largPag = page.getWidth();
                    float margSin = largPag/2 - largTab/2;
                    float largTitolo = titoloTabella.getWidth();
                    iLogo.setPosition(margSin, 35f);
                    iLogo.setPosition(margSin, 35f);
                    nomeAppAzienda.setPosition(margSin + iLogo.getWidth() + 10f, 52.0f);
                    sito.setPosition(margSin + iLogo.getWidth() + 10f, 67.0f);
                    googlePlay.setPosition(margSin + iLogo.getWidth() + 10f, 82.0f);
                    tlPeriodo.setPosition(margSin + largTab - tlPeriodo.getWidth(), 52f);
                    titoloTabella.setPosition(largPag/2 - largTitolo/2, 125.0f);
                    table.setPosition(margSin, 145.0f);

                    //stampo l'intestazione e il titolo centrati
                    iLogo.drawOn(page);
                    nomeAppAzienda.drawOn(page);
                    sito.drawOn(page);
                    googlePlay.drawOn(page);
                    tlPeriodo.drawOn(page);
                    titoloTabella.drawOn(page);

                    int numOfPages = table.getNumberOfPages(page);
                    int pagCorr = 0;
                    while (true) {
                        table.drawOn(page);
                        //scrivo il numero di pagina
                        TextLine numPag = new TextLine(f2, mioContext.getString(R.string.menu_esporta_tabellaPdf_numPag, ++pagCorr, numOfPages));
                        numPag.setPosition(largPag - 30.0f - numPag.getWidth(), page.getHeight() - 10.0f);
                        numPag.drawOn(page);

                        //scrivo il nome tabella a fondo pagina
                        TextLine tabella = new TextLine(f2, mioContext.getString(R.string.menu_esporta_tabellaPdf_fondoPaginaEntrate));
                        tabella.setPosition(30.0f, page.getHeight() - 10.0f);
                        tabella.drawOn(page);

                        if (!table.hasMoreData()) {
                            // Allow the table to be drawn again later:
                            table.resetRenderedPagesCount();
                            break;
                        }
                        //imposto una nuova pagina
                        page = new Page(pdf, Letter.LANDSCAPE);
                        iLogo.drawOn(page);
                        nomeAppAzienda.drawOn(page);
                        sito.drawOn(page);
                        googlePlay.drawOn(page);
                        tlPeriodo.drawOn(page);
                        table.setPosition(margSin, 125.0f);
                    }

                    curCSV.close();
                    curTotale.close();
                    dbcEntrateIncassate.close();
                }

                pdf.flush();
            }

            catch(Exception exc) {
                exc.printStackTrace();
                return false;
            }
        }

        return true;
    }


    @Override
    public String getOutputFormat() {
        return "pdf";
    }
}

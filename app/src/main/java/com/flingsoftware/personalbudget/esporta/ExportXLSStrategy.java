package com.flingsoftware.personalbudget.esporta;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Exports the internal database into a xls file.
 * Plays the role of ConcreteStrategy in the Strategy Pattern.
 */
public class ExportXLSStrategy implements ExportStrategy {

    // Instance variables.
    private Context mioContext;
    Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
    private String trasf;


    public ExportXLSStrategy(Context context, String transf) {
        this.mioContext = context;
        this.trasf = transf;
    }


    /**
     * This method exports the database into a xls file (excel).
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

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) { //controllo se la directory esterna è disponibile per la scrittura
            return false;
        }
        else {
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            File file;
            FileOutputStream fileOut = null;
            try
            {
                String nomeFile = "PersonalBudget.xls";
                file = new File(exportDir, nomeFile);
                file.createNewFile();

                //creazione Workbook
                Workbook wb = new HSSFWorkbook();
                CreationHelper createHelper = wb.getCreationHelper();

                if(esportaSpese) {
                    DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(mioContext);
                    dbcSpeseSostenute.openLettura();
                    Cursor curCSV = dbcSpeseSostenute.getSpeseSostenuteIntervalloNoTrasf(dataInizio, dataFine, trasf);

                    //creazione foglio e riga titoli
                    Sheet sheet = wb.createSheet(mioContext.getString(R.string.menu_esporta_XLS_nomeFoglioSpese));
                    Row rowTitolo = sheet.createRow((short)0);
                    rowTitolo.setHeightInPoints(20);
                    Font fontTitolo = wb.createFont();
                    fontTitolo.setBoldweight(Font.BOLDWEIGHT_BOLD);

                    CellStyle cellStyleTitoli = wb.createCellStyle();
                    cellStyleTitoli.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleTitoli.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleTitoli.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleTitoli.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleTitoli.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleTitoli.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleTitoli.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleTitoli.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    cellStyleTitoli.setAlignment(CellStyle.ALIGN_CENTER);
                    cellStyleTitoli.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
                    cellStyleTitoli.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                    cellStyleTitoli.setFillPattern((short)1);
                    cellStyleTitoli.setFont(fontTitolo);

                    Cell cellDataTitolo = rowTitolo.createCell(0);
                    cellDataTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaData)));
                    cellDataTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellVoceTitolo = rowTitolo.createCell(1);
                    cellVoceTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaVoce)));
                    cellVoceTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellContoTitolo = rowTitolo.createCell(2);
                    cellContoTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaConto)));
                    cellContoTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellImportoTitolo = rowTitolo.createCell(3);
                    cellImportoTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaImporto)));
                    cellImportoTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellValutaTitolo = rowTitolo.createCell(4);
                    cellValutaTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaValuta)));
                    cellValutaTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellImportoConvTitolo = rowTitolo.createCell(5);
                    cellImportoConvTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaImportoConv)));
                    cellImportoConvTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellDescrizioneTitolo = rowTitolo.createCell(6);
                    cellDescrizioneTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaDescrizione)));
                    cellDescrizioneTitolo.setCellStyle(cellStyleTitoli);

                    //impostazione formati delle celle dei dati
                    CellStyle cellStyleGenerico = wb.createCellStyle();
                    cellStyleGenerico.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleGenerico.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleGenerico.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleGenerico.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleGenerico.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleGenerico.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleGenerico.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleGenerico.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    DataFormat format = wb.createDataFormat();
                    CellStyle cellStyleData = wb.createCellStyle();
                    cellStyleData.setAlignment(CellStyle.ALIGN_CENTER);
                    cellStyleData.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleData.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleData.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleData.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleData.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleData.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleData.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleData.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    CellStyle cellStyleImporto = wb.createCellStyle();
                    cellStyleImporto.setDataFormat(format.getFormat("#,##0.00"));
                    cellStyleImporto.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleImporto.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleImporto.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleImporto.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleImporto.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleImporto.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleImporto.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleImporto.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    CellStyle cellStyleValuta = wb.createCellStyle();
                    cellStyleValuta.setAlignment(CellStyle.ALIGN_CENTER);
                    cellStyleValuta.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleValuta.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleValuta.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleValuta.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleValuta.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleValuta.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleValuta.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleValuta.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    CellStyle cellStyleDescrizione = wb.createCellStyle();
                    cellStyleDescrizione.setWrapText(true);
                    cellStyleDescrizione.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleDescrizione.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleDescrizione.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleDescrizione.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleDescrizione.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleDescrizione.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleDescrizione.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleDescrizione.setTopBorderColor(IndexedColors.BLACK.getIndex());


                    int i = 0;
                    while(curCSV.moveToNext())
                    {
                        Long data = curCSV.getLong(curCSV.getColumnIndex("data"));
                        String voce = curCSV.getString(curCSV.getColumnIndex("voce"));
                        String conto = curCSV.getString(curCSV.getColumnIndex("conto"));
                        Double importo = curCSV.getDouble(curCSV.getColumnIndex("importo"));
                        String valuta = curCSV.getString(curCSV.getColumnIndex("valuta"));
                        Double importoValprin = curCSV.getDouble(curCSV.getColumnIndex("importo_valprin"));
                        String descrizione = curCSV.getString(curCSV.getColumnIndex("descrizione"));

                        //creazione record
                        i++;
                        Row rowRecord = sheet.createRow((short)i);

                        Cell cellData = rowRecord.createCell(0);
                        cellData.setCellValue(createHelper.createRichTextString(df.format(new Date(data))));
                        cellData.setCellStyle(cellStyleData);

                        Cell cellVoce = rowRecord.createCell(1);
                        cellVoce.setCellValue(createHelper.createRichTextString(voce));
                        cellVoce.setCellStyle(cellStyleGenerico);

                        Cell cellConto = rowRecord.createCell(2);
                        cellConto.setCellValue(createHelper.createRichTextString(conto));
                        cellConto.setCellStyle(cellStyleGenerico);

                        Cell cellImporto = rowRecord.createCell(3);
                        cellImporto.setCellValue(importo);
                        cellImporto.setCellStyle(cellStyleImporto);

                        Cell cellValuta = rowRecord.createCell(4);
                        cellValuta.setCellValue(createHelper.createRichTextString(valuta));
                        cellValuta.setCellStyle(cellStyleValuta);

                        Cell cellImportoConv = rowRecord.createCell(5);
                        cellImportoConv.setCellValue(importoValprin);
                        cellImportoConv.setCellStyle(cellStyleImporto);

                        Cell cellDescrizione = rowRecord.createCell(6);
                        cellDescrizione.setCellValue(createHelper.createRichTextString(descrizione));
                        cellDescrizione.setCellStyle(cellStyleDescrizione);
                    }

                    //formattazione tabella
                    sheet.setColumnWidth(0, 3072);
                    sheet.setColumnWidth(1, 6144);
                    sheet.setColumnWidth(2, 5760);
                    sheet.setColumnWidth(3, 3072);
                    sheet.setColumnWidth(4, 3072);
                    sheet.setColumnWidth(5, 4352);
                    sheet.setColumnWidth(6, 8576);

                    curCSV.close();
                    dbcSpeseSostenute.close();
                }

                if(esportaEntrate) {
                    DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(mioContext);
                    dbcEntrateIncassate.openLettura();
                    Cursor curCSV = dbcEntrateIncassate.getEntrateIncassateIntervalloNoTrasf(dataInizio, dataFine, trasf); //considero tutti i conti

                    //creazione foglio e riga titoli
                    Sheet sheet = wb.createSheet(mioContext.getString(R.string.menu_esporta_XLS_nomeFoglioEntrate));
                    Row rowTitolo = sheet.createRow((short)0);
                    rowTitolo.setHeightInPoints(20);
                    Font fontTitolo = wb.createFont();
                    fontTitolo.setBoldweight(Font.BOLDWEIGHT_BOLD);

                    CellStyle cellStyleTitoli = wb.createCellStyle();
                    cellStyleTitoli.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleTitoli.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleTitoli.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleTitoli.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleTitoli.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleTitoli.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleTitoli.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleTitoli.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    cellStyleTitoli.setAlignment(CellStyle.ALIGN_CENTER);
                    cellStyleTitoli.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
                    cellStyleTitoli.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                    cellStyleTitoli.setFillPattern((short) 1);
                    cellStyleTitoli.setFont(fontTitolo);

                    Cell cellDataTitolo = rowTitolo.createCell(0);
                    cellDataTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaData)));
                    cellDataTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellVoceTitolo = rowTitolo.createCell(1);
                    cellVoceTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaVoce)));
                    cellVoceTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellContoTitolo = rowTitolo.createCell(2);
                    cellContoTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaConto)));
                    cellContoTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellImportoTitolo = rowTitolo.createCell(3);
                    cellImportoTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaImporto)));
                    cellImportoTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellValutaTitolo = rowTitolo.createCell(4);
                    cellValutaTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaValuta)));
                    cellValutaTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellImportoConvTitolo = rowTitolo.createCell(5);
                    cellImportoConvTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaImportoConv)));
                    cellImportoConvTitolo.setCellStyle(cellStyleTitoli);
                    Cell cellDescrizioneTitolo = rowTitolo.createCell(6);
                    cellDescrizioneTitolo.setCellValue(createHelper.createRichTextString(mioContext.getString(R.string.menu_esporta_tabellaPdf_colonnaDescrizione)));
                    cellDescrizioneTitolo.setCellStyle(cellStyleTitoli);

                    //impostazione formati delle celle dei dati
                    CellStyle cellStyleGenerico = wb.createCellStyle();
                    cellStyleGenerico.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleGenerico.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleGenerico.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleGenerico.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleGenerico.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleGenerico.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleGenerico.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleGenerico.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    DataFormat format = wb.createDataFormat();
                    CellStyle cellStyleData = wb.createCellStyle();
                    cellStyleData.setAlignment(CellStyle.ALIGN_CENTER);
                    cellStyleData.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleData.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleData.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleData.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleData.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleData.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleData.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleData.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    CellStyle cellStyleImporto = wb.createCellStyle();
                    cellStyleImporto.setDataFormat(format.getFormat("#,##0.00"));
                    cellStyleImporto.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleImporto.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleImporto.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleImporto.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleImporto.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleImporto.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleImporto.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleImporto.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    CellStyle cellStyleValuta = wb.createCellStyle();
                    cellStyleValuta.setAlignment(CellStyle.ALIGN_CENTER);
                    cellStyleValuta.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleValuta.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleValuta.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleValuta.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleValuta.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleValuta.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleValuta.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleValuta.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    CellStyle cellStyleDescrizione = wb.createCellStyle();
                    cellStyleDescrizione.setWrapText(true);
                    cellStyleDescrizione.setBorderBottom(CellStyle.BORDER_THIN);
                    cellStyleDescrizione.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleDescrizione.setBorderLeft(CellStyle.BORDER_THIN);
                    cellStyleDescrizione.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleDescrizione.setBorderRight(CellStyle.BORDER_THIN);
                    cellStyleDescrizione.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    cellStyleDescrizione.setBorderTop(CellStyle.BORDER_THIN);
                    cellStyleDescrizione.setTopBorderColor(IndexedColors.BLACK.getIndex());

                    int i = 0;
                    while(curCSV.moveToNext())
                    {
                        Long data = curCSV.getLong(curCSV.getColumnIndex("data"));
                        String voce = curCSV.getString(curCSV.getColumnIndex("voce"));
                        String conto = curCSV.getString(curCSV.getColumnIndex("conto"));
                        Double importo = curCSV.getDouble(curCSV.getColumnIndex("importo"));
                        String valuta = curCSV.getString(curCSV.getColumnIndex("valuta"));
                        Double importoValprin = curCSV.getDouble(curCSV.getColumnIndex("importo_valprin"));
                        String descrizione = curCSV.getString(curCSV.getColumnIndex("descrizione"));

                        //creazione record
                        i++;
                        Row rowRecord = sheet.createRow((short)i);

                        Cell cellData = rowRecord.createCell(0);
                        cellData.setCellValue(createHelper.createRichTextString(df.format(new Date(data))));
                        cellData.setCellStyle(cellStyleData);

                        Cell cellVoce = rowRecord.createCell(1);
                        cellVoce.setCellValue(createHelper.createRichTextString(voce));
                        cellVoce.setCellStyle(cellStyleGenerico);

                        Cell cellConto = rowRecord.createCell(2);
                        cellConto.setCellValue(createHelper.createRichTextString(conto));
                        cellConto.setCellStyle(cellStyleGenerico);

                        Cell cellImporto = rowRecord.createCell(3);
                        cellImporto.setCellValue(importo);
                        cellImporto.setCellStyle(cellStyleImporto);

                        Cell cellValuta = rowRecord.createCell(4);
                        cellValuta.setCellValue(createHelper.createRichTextString(valuta));
                        cellValuta.setCellStyle(cellStyleValuta);

                        Cell cellImportoConv = rowRecord.createCell(5);
                        cellImportoConv.setCellValue(importoValprin);
                        cellImportoConv.setCellStyle(cellStyleImporto);

                        Cell cellDescrizione = rowRecord.createCell(6);
                        cellDescrizione.setCellValue(createHelper.createRichTextString(descrizione));
                        cellDescrizione.setCellStyle(cellStyleDescrizione);
                    }

                    //formattazione tabella
                    sheet.setColumnWidth(0, 3072);
                    sheet.setColumnWidth(1, 6144);
                    sheet.setColumnWidth(2, 5760);
                    sheet.setColumnWidth(3, 3072);
                    sheet.setColumnWidth(4, 3072);
                    sheet.setColumnWidth(5, 4352);
                    sheet.setColumnWidth(6, 8576);

                    curCSV.close();
                    dbcEntrateIncassate.close();
                }

                //foglio finale con info
                Sheet sheetFinale = wb.createSheet("Info");

                //cell style for hyperlinks
                CellStyle hlink_style = wb.createCellStyle();
                Font hlink_font = wb.createFont();
                hlink_font.setUnderline(Font.U_SINGLE);
                hlink_font.setColor(IndexedColors.BLUE.getIndex());
                hlink_style.setFont(hlink_font);

                //cell style per nome app e citazione
                Font fontNome = wb.createFont();
                fontNome.setBoldweight(Font.BOLDWEIGHT_BOLD);
                CellStyle cellStyleNome = wb.createCellStyle();
                cellStyleNome.setAlignment(CellStyle.ALIGN_CENTER);
                cellStyleNome.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
                cellStyleNome.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                cellStyleNome.setFillPattern((short) 1);
                cellStyleNome.setFont(fontNome);

                Font fontCitazione = wb.createFont();
                fontCitazione.setItalic(true);
                fontCitazione.setFontHeightInPoints((short) 10);
                CellStyle cellStyleCitazione = wb.createCellStyle();
                cellStyleCitazione.setAlignment(CellStyle.ALIGN_CENTER);
                cellStyleCitazione.setFont(fontCitazione);

                //info
                Row rowNome = sheetFinale.createRow(0);
                rowNome.setHeightInPoints(20);
                Cell cellNome = rowNome.createCell((short)0);
                cellNome.setCellValue("PERSONAL BUDGET APP - FLING SOFWARE©");
                cellNome.setCellStyle(cellStyleNome);

                Cell citazioneCell = sheetFinale.createRow(1).createCell((short)0);
                citazioneCell.setCellValue(mioContext.getString(R.string.generale_citazione) + " " + mioContext.getString(R.string.generale_citazioneAutore));
                citazioneCell.setCellStyle(cellStyleCitazione);

                Cell websiteCell = sheetFinale.createRow(2).createCell((short)0);
                websiteCell.setCellValue("Visit Website");
                Hyperlink mioLink = createHelper.createHyperlink(Hyperlink.LINK_URL);
                mioLink.setAddress("https://www.facebook.com/personalbudgetapp");
                websiteCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) mioLink);
                websiteCell.setCellStyle(hlink_style);

                Cell googlePlayCell = sheetFinale.createRow(3).createCell((short)0);
                googlePlayCell.setCellValue("Google Play");
                Hyperlink mioLink2 = createHelper.createHyperlink(Hyperlink.LINK_URL);
                mioLink2.setAddress("https://play.google.com/store/apps/developer?id=Fling%20Software%C2%A9&hl=it");
                googlePlayCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) mioLink2);
                googlePlayCell.setCellStyle(hlink_style);

                sheetFinale.setColumnWidth(0, 22500);

                fileOut = new FileOutputStream(file);
                wb.write(fileOut);
            }
            catch(Exception exc)
            {
                exc.printStackTrace();
                return false;
            }
            finally {
                try {
                    if(fileOut != null) {
                        fileOut.close();
                    }
                }
                catch(IOException exc) {
                    exc.printStackTrace();
                    return false;
                }
            }
        }

        return true;
    }


    @Override
    public String getOutputFormat() {
        return "xls";
    }
}

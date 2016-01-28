package com.flingsoftware.personalbudget.esporta;

/**
 * This class contains info used by ConcreteStrategy classes to export the database.
 */
public class ExportDetails {
    private boolean exportExpenses;
    private boolean exportEarnings;
    private long dateStart;
    private long dateEnd;
    private String eMail;


    public ExportDetails() {}


    public ExportDetails(boolean exportExpenses, boolean exportEarnings, long dateStart, long dateEnd) {
        this.exportExpenses = exportExpenses;
        this.exportEarnings = exportEarnings;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    public boolean isExportExpenses() {
        return exportExpenses;
    }

    public boolean isExportEarnings() {
        return exportEarnings;
    }

    public long getDateStart() {
        return dateStart;
    }

    public long getDateEnd() {
        return dateEnd;
    }

    public String geteMail() {
        return eMail;
    }

    public void setExportExpenses(boolean exportExpenses) {
        this.exportExpenses = exportExpenses;
    }

    public void setExportEarnings(boolean exportEarnings) {
        this.exportEarnings = exportEarnings;
    }

    public void setDateStart(long dateStart) {
        this.dateStart = dateStart;
    }

    public void setDateEnd(long dateEnd) {
        this.dateEnd = dateEnd;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }
}

/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.oggetti;


import android.os.Parcel;
import android.os.Parcelable;

public class ExpenseEarning implements Parcelable {

    // Costanti
    public static final int VOCE_SPESA = 0;
    public static final int VOCE_ENTRATA = 1;

    // Variabili
    private int tipoVoce;
    private long id;
    private long data;
    private String voce;
    private double importo;
    private String valuta;
    private double importoValprin;
    private String descrizione;
    private long ripetizioneId;
    private String conto;

    public ExpenseEarning() {

    }

    public ExpenseEarning(int tipoVoce, long id, long data, String voce, double importo, String valuta, double importoValprin, String descrizione, long ripetizioneId, String conto) {
        this.tipoVoce = tipoVoce;
        this.id = id;
        this.data = data;
        this.voce = voce;
        this.importo = importo;
        this.valuta = valuta;
        this.importoValprin = importoValprin;
        this.descrizione = descrizione;
        this.ripetizioneId = ripetizioneId;
        this.conto = conto;

    }

    public int getTipoVoce() {
        return tipoVoce;
    }

    public void setTipoVoce(int tipoVoce) {
        this.tipoVoce = tipoVoce;
    }

    public long getId() { return id; }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public String getValuta() {
        return valuta;
    }

    public void setValuta(String valuta) {
        this.valuta = valuta;
    }

    public double getImportoValprin() {
        return importoValprin;
    }

    public void setImportoValprin(double importoValprin) {
        this.importoValprin = importoValprin;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setId(long id) { this.id = id; }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public long getRipetizioneId() {
        return ripetizioneId;
    }

    public void setRipetizioneId(long ripetizioneId) {
        this.ripetizioneId = ripetizioneId;
    }

    public String getConto() {
        return conto;
    }

    public void setConto(String conto) {
        this.conto = conto;
    }

    public String getVoce() {
        return voce;
    }

    public void setVoce(String voce) {
        this.voce = voce;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    // Implementazione interfaccia Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tipoVoce);
        dest.writeLong(id);
        dest.writeLong(data);
        dest.writeString(voce);
        dest.writeDouble(importo);
        dest.writeString(valuta);
        dest.writeDouble(importoValprin);
        dest.writeString(descrizione);
        dest.writeLong(ripetizioneId);
        dest.writeString(conto);
    }

    private ExpenseEarning(Parcel in) {
        tipoVoce = in.readInt();
        id = in.readLong();
        data = in.readLong();
        voce = in.readString();
        importo = in.readDouble();
        valuta = in.readString();
        importoValprin = in.readDouble();
        descrizione = in.readString();
        ripetizioneId = in.readLong();
        conto = in.readString();
    }

    public static final Parcelable.Creator<ExpenseEarning> CREATOR =
            new Parcelable.Creator<ExpenseEarning>() {
                public ExpenseEarning createFromParcel(Parcel in) {
                    return new ExpenseEarning(in);
                }

                public ExpenseEarning[] newArray(int size) {
                    return new ExpenseEarning[size];
                }
            };
}


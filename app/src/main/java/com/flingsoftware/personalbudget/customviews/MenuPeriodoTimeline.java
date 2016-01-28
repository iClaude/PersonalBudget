package com.flingsoftware.personalbudget.customviews;

import java.text.DateFormat;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.Date;


public class MenuPeriodoTimeline extends View {
	
	// costanti
	private static final float POS_ALT = 0.67f;
	
	
	// costruttori
	public MenuPeriodoTimeline(Context context) {
		super(context);
	}
	
	
	public MenuPeriodoTimeline(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	
	public MenuPeriodoTimeline(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
				
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		scala = metrics.density;
		
		df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
		
		impostaPaints();
		impostaOggi();
	}
	
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
	
	
	// impostazione variabili Paint per il disegno
	private void impostaPaints() {
		paintTestoTitolo = new Paint();
		paintTestoTitolo.setColor(Color.BLACK);
		paintTestoTitolo.setAntiAlias(true);
		paintTestoTitolo.setTextAlign(Paint.Align.CENTER);
		
		paintSottolineato = new Paint();
		paintSottolineato.setColor(Color.BLACK);
		paintSottolineato.setAntiAlias(true);
		paintSottolineato.setStyle(Paint.Style.STROKE);
		paintSottolineato.setStrokeCap(Paint.Cap.ROUND);
		paintSottolineato.setStrokeWidth(1 * scala);

		paintIntervallo = new Paint();
		paintIntervallo.setColor(Color.GREEN);
		paintIntervallo.setAntiAlias(true);
		paintIntervallo.setStyle(Paint.Style.STROKE);
		paintIntervallo.setStrokeCap(Paint.Cap.ROUND);
		paintIntervallo.setStrokeWidth(4 * scala);
		
		paintIntervalloPrimaDopo = new Paint();
		paintIntervalloPrimaDopo.setColor(Color.RED);
		paintIntervalloPrimaDopo.setAntiAlias(true);
		paintIntervalloPrimaDopo.setStyle(Paint.Style.STROKE);
		paintIntervalloPrimaDopo.setStrokeCap(Paint.Cap.ROUND);
		paintIntervalloPrimaDopo.setStrokeWidth(4 * scala);
		
		paintTratteggioPrimaDopo = new Paint();
		paintTratteggioPrimaDopo.setColor(Color.RED);
		paintTratteggioPrimaDopo.setAntiAlias(true);
		paintTratteggioPrimaDopo.setStyle(Paint.Style.STROKE);
		paintTratteggioPrimaDopo.setStrokeWidth(4 * scala);
		paintTratteggioPrimaDopo.setPathEffect(new DashPathEffect(new float[] {5, 10}, 0));
		
		paintCerchiDate = new Paint();
		paintCerchiDate.setColor(Color.GREEN);
		paintCerchiDate.setAntiAlias(true);
		paintCerchiDate.setStyle(Paint.Style.FILL_AND_STROKE);
		paintCerchiDate.setStrokeCap(Paint.Cap.ROUND);
		paintCerchiDate.setStrokeWidth(3 * scala);
		
		paintFreccia = new Paint();
		paintFreccia.setColor(Color.GREEN);
		paintFreccia.setAntiAlias(true);
		paintFreccia.setStyle(Paint.Style.STROKE);
		paintFreccia.setStrokeCap(Paint.Cap.ROUND);
		paintFreccia.setStrokeWidth(1 * scala);
		
		paintTesto = new Paint();
		paintTesto.setColor(Color.BLACK);
		paintTesto.setAntiAlias(true);
		paintTesto.setTextAlign(Paint.Align.CENTER);
	
		paintOvale = new Paint();
		paintOvale.setColor(Color.GREEN);
		paintOvale.setAntiAlias(true);
		paintOvale.setStyle(Paint.Style.FILL_AND_STROKE);
	}
	
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		w = getWidth();
		h = getHeight();
		
		// dimensioni testo
		paintTesto.setTextSize(14 * scala);
		paintTestoTitolo.setTextSize(18 * scala);
		
		// calcolo pti coordinate delle varie linee e figure
		rectBordo.set(1 * scala, 1 * scala, w - 1 * scala, h - 1 * scala);
	
		intervalloX1 = (float) (w/2 - (w*0.5)/2);
		intervalloY1 = h*POS_ALT;
		intervalloX2 = (float) (w/2 + (w*0.5)/2);
		intervalloY2 = h*POS_ALT;
		
		intervalloPrimaX1 = (float) (intervalloX1 - w*0.1);
		intervalloPrimaY1 = h*POS_ALT;
		intervalloPrimaX2 = intervalloX1;
		intervalloPrimaY2 = h*POS_ALT;
		
		intervalloDopoX1 = intervalloX2;
		intervalloDopoY1 = h*POS_ALT;
		intervalloDopoX2 = (float) (intervalloX2 + w*0.1);
		intervalloDopoY2 = h*POS_ALT;
		
		tratteggioPrimaX1 = (float) (intervalloPrimaX1 - w*0.1);
		tratteggioPrimaY1 = h*POS_ALT;
		tratteggioPrimaX2 = intervalloPrimaX1;
		tratteggioPrimaY2 = h*POS_ALT;
		
		tratteggioDopoX1 = intervalloDopoX2;
		tratteggioDopoY1 = h*POS_ALT;
		tratteggioDopoX2 = (float) (intervalloDopoX2 + w*0.1);
		tratteggioDopoY2 = h*POS_ALT;
		
		cerchioDataInizioX = intervalloX1;
		cerchioDataInizioY = intervalloY1;
		cerchioDataInizioRaggio = 5 * scala;
		
		cerchioDataFineX = intervalloX2;
		cerchioDataFineY = intervalloY2;
		cerchioDataFineRaggio = 5 * scala;
	
		testoDataInizioX = cerchioDataInizioX;
		testoDataInizioY = cerchioDataInizioY + 24 * scala;
		
		testoDataFineX = cerchioDataFineX;
		testoDataFineY = cerchioDataFineY + 24 * scala;
				
		impostaOggi();
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawLine(intervalloX1, intervalloY1, intervalloX2, intervalloY2, paintIntervallo);
		canvas.drawLine(intervalloPrimaX1, intervalloPrimaY1, intervalloPrimaX2, intervalloPrimaY2, paintIntervalloPrimaDopo);
		canvas.drawLine(intervalloDopoX1, intervalloDopoY1, intervalloDopoX2, intervalloDopoY2, paintIntervalloPrimaDopo);
		
		for(float i=tratteggioPrimaX1; i<tratteggioPrimaX2; i=i+10*scala) {
			canvas.drawLine(i, tratteggioPrimaY1, i+5*scala, tratteggioPrimaY2, paintTratteggioPrimaDopo);
		}
		
		for(float i=tratteggioDopoX1; i<tratteggioDopoX2; i=i+10*scala) {
			canvas.drawLine(i, tratteggioDopoY1, i+5*scala, tratteggioDopoY2, paintTratteggioPrimaDopo);
		}
		
		canvas.drawCircle(cerchioDataInizioX, cerchioDataInizioY, cerchioDataInizioRaggio, paintCerchiDate);
		canvas.drawCircle(cerchioDataFineX, cerchioDataFineY, cerchioDataFineRaggio, paintCerchiDate);
		canvas.drawLine(frecciaX1, frecciaY1, frecciaX2, frecciaY2, paintFreccia);
		canvas.drawLine(frecciaSinX, frecciaSinY, frecciaX2, frecciaY2, paintFreccia);
		canvas.drawLine(frecciaDesX, frecciaDesY, frecciaX2, frecciaY2, paintFreccia);
		
		dataComodo.setTime(dataInizio);
		canvas.drawText(df.format(dataComodo), testoDataInizioX, testoDataInizioY, paintTesto);
		dataComodo.setTime(dataFine);
		canvas.drawText(df.format(dataFine), testoDataFineX, testoDataFineY, paintTesto);
				
		canvas.drawOval(rectOggi, paintOvale);
		canvas.drawText("TODAY", testoOggiX, testoOggiY, paintTesto);
	}
	
	
	// impostazione variabili incapsulate
	
	public void setDataInizio(long dataInizio) {
		this.dataInizio = dataInizio;
	}
	
	
	public void setDataFine(long dataFine) {
		this.dataFine = dataFine;
	}
	
	
	public void setDataOggi(long dataOggi) {
		this.dataOggi = dataOggi;
	}
	
	
	// aggiorna la label oggi e ridisegna la view
	
	public void ridisegna() {
		impostaOggi();
		invalidate();
	}
	
	
	// imposto parametri (posizione e colori) della label oggi
	private void impostaOggi() {
		if(dataOggi>=dataInizio && dataOggi<=dataFine) {
			paintFreccia.setColor(Color.GREEN);
			paintOvale.setColor(Color.GREEN);
			
			float percOggi = (dataFine-dataInizio)>0 ? ((float)dataOggi-dataInizio) / (dataFine-dataInizio) : ((float)dataOggi-dataInizio) / 1;
			frecciaX1 = intervalloX1 + (intervalloX2-intervalloX1) * percOggi;		
			frecciaY1 = h*POS_ALT;
			frecciaX2 = frecciaX1;
			frecciaY2 = h*POS_ALT - 25 * scala;
			frecciaSinX = frecciaX1 - 10 * scala;
			frecciaSinY = frecciaY2 + 10 * scala;
			frecciaDesX = frecciaX1 + 10 * scala;
			frecciaDesY = frecciaY2 + 10 * scala;
			
			testoOggiX = frecciaX1;
			testoOggiY = frecciaY2 - 10 * scala;
			
			rectOggi.set(testoOggiX - 35 * scala, testoOggiY - 20 * scala, testoOggiX + 35 * scala, testoOggiY + 8 * scala);
		}
		else if(dataOggi<dataInizio) {
			paintFreccia.setColor(Color.RED);
			paintOvale.setColor(Color.RED);
			
			frecciaX1 = intervalloPrimaX1;
			frecciaY1 = h*POS_ALT;
			frecciaX2 = frecciaX1;
			frecciaY2 = h*POS_ALT - 25 * scala;
			frecciaSinX = intervalloPrimaX1 - 10 * scala;
			frecciaSinY = frecciaY2 + 10 * scala;
			frecciaDesX = intervalloPrimaX1 + 10 * scala;
			frecciaDesY = frecciaY2 + 10 * scala;
			
			testoOggiX = intervalloPrimaX1;
			testoOggiY = frecciaY2 - 10 * scala;
			
			rectOggi.set(testoOggiX - 35 * scala, testoOggiY - 20 * scala, testoOggiX + 35 * scala, testoOggiY + 8 * scala);
		}
		else if(dataOggi>dataFine) {
			paintFreccia.setColor(Color.RED);
			paintOvale.setColor(Color.RED);
			
			frecciaX1 = intervalloDopoX2;
			frecciaY1 = h*POS_ALT;
			frecciaX2 = frecciaX1;
			frecciaY2 = h*POS_ALT - 25 * scala;
			frecciaSinX = intervalloDopoX2 - 10 * scala;
			frecciaSinY = frecciaY2 + 10 * scala;
			frecciaDesX = intervalloDopoX2 + 10 * scala;
			frecciaDesY = frecciaY2 + 10 * scala;
			
			testoOggiX = intervalloDopoX2;
			testoOggiY = frecciaY2 - 10 * scala;
			
			rectOggi.set(testoOggiX - 35 * scala, testoOggiY - 20 * scala, testoOggiX + 35 * scala, testoOggiY + 8 * scala);
		}
	}
	
	
	// variabili di istanza incapsulate
	private long dataInizio = 100;
	private long dataFine = 300;
	private long dataOggi = 200;
	
	// variabili generali
	private float scala;
	DateFormat df;
	private int w, h; // larghezza e altezza view
	private Date dataComodo = new Date();
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	
	// variabili Paint per le varie componenti del disegno
	private Paint paintTestoTitolo, paintSottolineato, paintIntervallo, paintIntervalloPrimaDopo, paintTratteggioPrimaDopo, paintCerchiDate, paintFreccia, paintTesto, paintOvale;
	
	// figure complesse
	private RectF rectBordo = new RectF();
	private RectF rectOggi = new RectF();
	
	// coordinate pti iniziali e finali delle varie linee e figure
	private float intervalloX1, intervalloY1, intervalloX2, intervalloY2;
	private float intervalloPrimaX1, intervalloPrimaY1, intervalloPrimaX2, intervalloPrimaY2;
	private float intervalloDopoX1, intervalloDopoY1, intervalloDopoX2, intervalloDopoY2;
	private float tratteggioPrimaX1, tratteggioPrimaY1, tratteggioPrimaX2, tratteggioPrimaY2;
	private float tratteggioDopoX1, tratteggioDopoY1, tratteggioDopoX2, tratteggioDopoY2;
	private float cerchioDataInizioX, cerchioDataInizioY, cerchioDataInizioRaggio;
	private float cerchioDataFineX, cerchioDataFineY, cerchioDataFineRaggio;
	private float frecciaX1, frecciaY1, frecciaX2, frecciaY2;
	private float frecciaSinX, frecciaSinY, frecciaDesX, frecciaDesY;
	private float testoDataInizioX, testoDataInizioY, testoDataFineX, testoDataFineY;
	private float testoOggiX, testoOggiY;
}

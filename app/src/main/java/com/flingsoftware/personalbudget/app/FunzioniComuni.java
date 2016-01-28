/*
 * Questa classe contiene alcune funzioni di utilità, in genere static, utilizzate da vari
 * componenti di questa app.
 */

package com.flingsoftware.personalbudget.app;

import java.util.GregorianCalendar;

public class FunzioniComuni {
	public static long getDataAttuale() {
		// calcolo la data attuale in millisecondi (ore, minuti e secondi impostati a zero)
		GregorianCalendar dataAttuale = new GregorianCalendar();
		int anno = dataAttuale.get(GregorianCalendar.YEAR);
		int mese = dataAttuale.get(GregorianCalendar.MONTH);
		int giorno = dataAttuale.get(GregorianCalendar.DATE);
		dataAttuale = new GregorianCalendar(anno, mese, giorno);
		
		return dataAttuale.getTimeInMillis();
	}

	/**
	 * Funzione di utilità che permette di impostare la data iniziale e finale quando il periodo
	 * è impostato su automatico (ultimi 30 gg, prossimi 30 gg o mese corrente).
	 * @param tipoDataAutomatica può assumere i seguenti valori: CostantiPreferenze.DATA_AUTOMATICA_30GG_ULTIMI,
	 *                           CostantiPreferenze.DATA_AUTOMATICA_30GG_PROSSIMI,
	 *                           CostantiPreferenze.DATA_AUTOMATICA_MESE_CORRENTE
	 * @param dataInizio reference alla variabile GregorianCalendar della data iniziale da impostare
	 * @param dataFine reference alla variabile GregorianCalendar della data finale da impostare
	 * @param offset numero di giorni offset (utilizzato per l'opzione mese corrente)
	 */
	public static void impostaPeriodoAutomatico(int tipoDataAutomatica, GregorianCalendar dataInizio, GregorianCalendar dataFine, int offset) {
		dataInizio.setTimeInMillis(getDataAttuale());
		dataFine.setTimeInMillis(getDataAttuale());

		switch (tipoDataAutomatica) {
			case MainPersonalBudget.CostantiPreferenze.DATA_AUTOMATICA_30GG_ULTIMI:
				dataInizio.add(GregorianCalendar.DATE, -30);
				break;
			case MainPersonalBudget.CostantiPreferenze.DATA_AUTOMATICA_30GG_PROSSIMI:
				dataFine.add(GregorianCalendar.DATE, 30);
				break;
			case MainPersonalBudget.CostantiPreferenze.DATA_AUTOMATICA_MESE_CORRENTE:
				int giornoOggi = dataInizio.get(GregorianCalendar.DATE);

				if(giornoOggi >= offset) {
					dataInizio.set(GregorianCalendar.DATE, offset);
					dataFine.set(GregorianCalendar.DATE, offset);
					dataFine.add(GregorianCalendar.MONTH, 1);
					dataFine.add(GregorianCalendar.DATE, -1);
				}
				else {
					dataInizio.set(GregorianCalendar.DATE, offset);
					dataFine.set(GregorianCalendar.DATE, offset);
					dataInizio.add(GregorianCalendar.MONTH, -1);
					dataFine.add(GregorianCalendar.DATE, -1);
				}

				break;
		}
	}
}

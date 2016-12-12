/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.database;

public interface StringheSQL {
	/**
	 * Stringhe SQL per creare le varie tabelle.
	 */
	String CREA_TABELLA_SPESE_VOCI = "CREATE TABLE spese_voci(_id integer primary key autoincrement, voce TEXT UNIQUE NOT NULL, icona INTEGER);";
	String CREA_TABELLA_SPESE_SOST = "CREATE TABLE spese_sost(_id integer primary key autoincrement, data INTEGER, voce TEXT, importo REAL, valuta TEXT NOT NULL, importo_valprin REAL, descrizione TEXT, ripetizione_id INTEGER, conto TEXT, favorite INTEGER, FOREIGN KEY(voce) REFERENCES spese_voci(voce) ON UPDATE CASCADE ON DELETE CASCADE);";
	String CREA_TABELLA_SPESE_RIPET = "CREATE TABLE spese_ripet(_id integer primary key autoincrement, voce TEXT, ripetizione TEXT CHECK (ripetizione='giornaliero' OR ripetizione='settimanale' OR ripetizione='bisettimanale' OR ripetizione='mensile' OR ripetizione='annuale' OR ripetizione='giorni_lavorativi' OR ripetizione='weekend' OR ripetizione='nessuna'), importo REAL, valuta TEXT NOT NULL, importo_valprin REAL, descrizione TEXT, data_inizio INTEGER, flag_fine INTEGER CHECK (flag_fine=0 OR flag_fine=1), data_fine INTEGER, aggiornato_a INTEGER, conto TEXT, FOREIGN KEY(voce) REFERENCES spese_voci(voce) ON UPDATE CASCADE ON DELETE CASCADE);";
	String CREA_TABELLA_SPESE_BUDGET = "CREATE TABLE spese_budget(_id integer primary key autoincrement, voce TEXT, ripetizione TEXT CHECK (ripetizione='giornaliero' OR ripetizione='settimanale' OR ripetizione='bisettimanale' OR ripetizione='mensile' OR ripetizione='annuale' OR ripetizione='una_tantum'), importo REAL, valuta TEXT NOT NULL, importo_valprin REAL, data_inizio INTEGER, data_fine INTEGER, aggiungere_rimanenza INTEGER, spesa_sost REAL, risparmio REAL, budget_iniziale INTEGER, ultimo_aggiunto INTEGER);";
	String CREA_TABELLA_ENTRATE_VOCI = "CREATE TABLE entrate_voci(_id integer primary key autoincrement, voce TEXT UNIQUE NOT NULL, icona INTEGER);";
	String CREA_TABELLA_ENTRATE_INC = "CREATE TABLE entrate_inc(_id integer primary key autoincrement, data INTEGER, voce TEXT, importo REAL, valuta TEXT NOT NULL, importo_valprin REAL, descrizione TEXT, ripetizione_id INTEGER, conto TEXT, favorite INTEGER, FOREIGN KEY(voce) REFERENCES entrate_voci(voce) ON UPDATE CASCADE ON DELETE CASCADE);";
	String CREA_TABELLA_ENTRATE_RIPET = "CREATE TABLE entrate_ripet(_id integer primary key autoincrement, voce TEXT, ripetizione TEXT CHECK (ripetizione='giornaliero' OR ripetizione='settimanale' OR ripetizione='bisettimanale' OR ripetizione='mensile' OR ripetizione='annuale' OR ripetizione='giorni_lavorativi' OR ripetizione='weekend' OR ripetizione='nessuna'), importo REAL, valuta TEXT NOT NULL, importo_valprin REAL, descrizione TEXT, data_inizio INTEGER, flag_fine INTEGER CHECK (flag_fine=0 OR flag_fine=1), data_fine INTEGER, aggiornato_a INTEGER, conto TEXT, FOREIGN KEY(voce) REFERENCES entrate_voci(voce) ON UPDATE CASCADE ON DELETE CASCADE);";
	String CREA_TABELLA_CONTI = "CREATE TABLE conti(_id integer primary key autoincrement, conto TEXT UNIQUE NOT NULL, saldo REAL, data_saldo INTEGER);";
	
	/**
	 * Eliminazione di tutte le tabelle dal database.
	 */
	String ELIMINA_TABELLE = "DROP TABLE IF EXISTS spese_voci, spese_sost, spese_ripet, spese_budget, entrate_voci, entrate_inc, entrate_ripet, conti";

	/*
	 * Ricerca nelle tabelle spese_voci e entrate_voci tutte le voci contenenti la stringa specificata.
	 */
	String SPESE_VOCI_VOCI_CONTENENTI_STRINGA = "SELECT _id, voce, icona FROM spese_voci WHERE voce LIKE ?";
	String ENTRATE_VOCI_VOCI_CONTENENTI_STRINGA = "SELECT _id, voce, icona FROM entrate_voci WHERE voce LIKE ?";
	
	
	/** Query di ricerca complesse in SQL per la tabella delle spese (spese_sost). Vedi la classe 
	 * DBCSpeseSostenute per maggiori dettagli.
	 */
	String SPESE_SOST_INTERVALLO = "SELECT * FROM spese_sost WHERE data >= ? AND data <= ? ORDER BY data DESC";
	String SPESE_SOST_INTERVALLO_NO_TRASF = "SELECT * FROM spese_sost WHERE data >= ? AND data <= ? AND voce <> ? ORDER BY data DESC";
	String SPESE_SOST_INTERVALLO_TOTALI_PER_DATA_FILTRATO = "SELECT _id, data, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE conto LIKE ? AND data >= ? AND data <= ? AND (voce LIKE ? OR descrizione LIKE ?) GROUP BY data ORDER BY data DESC";
	String SPESE_SOST_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO = "SELECT _id, voce, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE data >= ? AND data <= ? GROUP BY voce ORDER BY totale_spesa DESC";
	String SPESE_SOST_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO_NO_TRASF = "SELECT _id, voce, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE data >= ? AND data <= ? AND voce <> ? GROUP BY voce ORDER BY totale_spesa DESC";
	String SPESE_SOST_INTERVALLO_TOTALI_PER_VOCE_FILTRATO = "SELECT _id, voce, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE conto LIKE ? AND data >= ? AND data <= ? AND (voce LIKE ? OR descrizione LIKE ?) GROUP BY voce ORDER BY voce";
	String SPESE_SOST_INTERVALLO_SPESAX_FILTRATO = "SELECT _id, data, voce, importo_valprin, ripetizione_id FROM spese_sost WHERE conto LIKE ? AND data >= ? AND data <= ? AND voce=? AND (voce LIKE ? OR descrizione LIKE ?) ORDER BY data DESC";
	String SPESE_SOST_INTERVALLO_TOTALE = "SELECT _id, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE conto LIKE ? AND data >= ? AND data <= ?";
	String SPESE_SOST_INTERVALLO_TOTALE_NO_TRASF = "SELECT _id, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE conto LIKE ? AND data >= ? AND data <= ? AND voce <> ?";
	String SPESE_SOST_INTERVALLO_SPESAX_TOTALE = "SELECT _id, SUM(spese_sost.importo_valprin) AS totale_spesa FROM spese_sost WHERE data >= ? AND data <= ? AND voce = ?";
	String SPESE_SOST_INTERVALLO_SPESAX_MIN = "SELECT _id, MIN(importo_valprin) AS min_spesa FROM spese_sost WHERE data >= ? AND data <= ? AND voce = ?";
	String SPESE_SOST_INTERVALLO_SPESAX_MAX = "SELECT _id, MAX(importo_valprin) AS max_spesa FROM spese_sost WHERE data >= ? AND data <= ? AND voce = ?";
	String SPESE_SOST_INTERVALLO_SPESAX_AVG = "SELECT _id, AVG(importo_valprin) AS avg_spesa FROM spese_sost WHERE data >= ? AND data <= ? AND voce = ?";
	String SPESE_SOST_INTERVALLO_MIN_MAX_PER_VOCE = "SELECT _id, voce, MIN(importo_valprin) AS min, MAX(importo_valprin) AS max FROM spese_sost WHERE data >= ? AND data <= ? AND voce <> ? GROUP BY voce ORDER BY voce ASC";
	String SPESE_SOST_DATAX_ELENCO_FILTRATO = "SELECT _id, voce, ripetizione_id, importo_valprin, data FROM spese_sost WHERE conto LIKE ? AND data=? AND (voce LIKE ? OR descrizione LIKE ?)";
	String SPESE_SOST_SPESAX_DETTAGLIO = "SELECT spese_sost._id, spese_sost.voce, spese_sost.importo, spese_sost.valuta, spese_sost.importo_valprin, spese_sost.data, spese_sost.descrizione, spese_sost.ripetizione_id, spese_sost.conto, spese_sost.favorite, spese_ripet.ripetizione FROM spese_sost JOIN spese_ripet ON (spese_sost.ripetizione_id=spese_ripet._id) WHERE spese_sost._id=?";
	String SPESE_SOST_PREFERITE = "SELECT * FROM spese_sost WHERE favorite = 1 ORDER BY voce ASC";
	String SPESE_SOST_INTERVALLO_SPESAX_X_BUDGET_SPESE_INCLUSE = "SELECT * FROM spese_sost WHERE data >= ? AND data <= ? AND voce=? ORDER BY data DESC";

	
	/** Query di ricerca complesse in SQL per la tabella delle entrate (entrate_inc). Vedi la classe 
	 * DBCEntrateIncassate per maggiori dettagli.
	 */
	String ENTRATE_INC_INTERVALLO = "SELECT * FROM entrate_inc WHERE data >= ? AND data <= ? ORDER BY data DESC";
	String ENTRATE_INC_INTERVALLO_NO_TRASF = "SELECT * FROM entrate_inc WHERE data >= ? AND data <= ? AND voce <> ? ORDER BY data DESC";
	String ENTRATE_INC_INTERVALLO_TOTALI_PER_DATA_FILTRATO = "SELECT _id, data, SUM(importo_valprin) AS totale_entrata FROM entrate_inc WHERE conto LIKE ? AND data >= ? AND data <= ? AND (voce LIKE ? OR descrizione LIKE ?) GROUP BY data ORDER BY data DESC";
	String ENTRATE_INC_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO = "SELECT _id, voce, SUM(importo_valprin) AS totale_entrata FROM entrate_inc WHERE conto LIKE ? AND data >= ? AND data <= ? GROUP BY voce ORDER BY totale_entrata DESC";
	String ENTRATE_INC_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO_NO_TRASF = "SELECT _id, voce, SUM(importo_valprin) AS totale_entrata FROM entrate_inc WHERE conto LIKE ? AND data >= ? AND data <= ? AND voce <> ? GROUP BY voce ORDER BY totale_entrata DESC";
	String ENTRATE_INC_INTERVALLO_TOTALI_PER_VOCE_FILTRATO = "SELECT _id, voce, SUM(importo_valprin) AS totale_entrata FROM entrate_inc WHERE conto LIKE ? AND data >= ? AND data <= ? AND (voce LIKE ? OR descrizione LIKE ?) GROUP BY voce ORDER BY voce";
	String ENTRATE_INC_INTERVALLO_ENTRATAX_FILTRATO = "SELECT _id, data, voce, importo_valprin, ripetizione_id FROM entrate_inc WHERE conto LIKE ? AND data >= ? AND data <= ? AND voce = ? AND (voce LIKE ? OR descrizione LIKE ?) ORDER BY data DESC";
	String ENTRATE_INC_INTERVALLO_TOTALE = "SELECT _id, SUM(importo_valprin) AS totale_entrata FROM entrate_inc WHERE conto LIKE ? AND data >= ? AND data <= ?";
	String ENTRATE_INC_INTERVALLO_TOTALE_NO_TRASF = "SELECT _id, SUM(importo_valprin) AS totale_entrata FROM entrate_inc WHERE conto LIKE ? AND data >= ? AND data <= ? AND voce <> ?";
	String ENTRATE_INC_INTERVALLO_ENTRATAX_TOTALE = "SELECT _id, SUM(entrate_inc.importo_valprin) AS totale_entrata FROM entrate_inc WHERE conto LIKE ? AND data >= ? AND data <= ? AND voce = ?";
	String ENTRATE_INC_INTERVALLO_ENTRATAX_MIN = "SELECT _id, MIN(importo_valprin) AS min_entrata FROM entrate_inc WHERE data >= ? AND data <= ? AND voce = ?";
	String ENTRATE_INC_INTERVALLO_ENTRATAX_MAX = "SELECT _id, MAX(importo_valprin) AS max_entrata FROM entrate_inc WHERE data >= ? AND data <= ? AND voce = ?";
	String ENTRATE_INC_INTERVALLO_ENTRATAX_AVG = "SELECT _id, AVG(importo_valprin) AS avg_entrata FROM entrate_inc WHERE data >= ? AND data <= ? AND voce = ?";
	String ENTRATE_INC_INTERVALLO_MIN_MAX_PER_VOCE = "SELECT _id, voce, MIN(importo_valprin) AS min, MAX(importo_valprin) AS max FROM entrate_inc WHERE data >= ? AND data <= ? AND voce <> ? GROUP BY voce ORDER BY voce ASC";
	String ENTRATE_INC_DATAX_ELENCO_FILTRATO = "SELECT _id, voce, ripetizione_id, importo_valprin, data FROM entrate_inc WHERE conto LIKE ? AND data=? AND (voce LIKE ? OR descrizione LIKE ?)";
	String ENTRATE_INC_ENTRATAX_DETTAGLIO = "SELECT entrate_inc._id, entrate_inc.voce, entrate_inc.importo, entrate_inc.valuta, entrate_inc.importo_valprin, entrate_inc.data, entrate_inc.descrizione, entrate_inc.ripetizione_id, entrate_inc.conto, entrate_inc.favorite, entrate_ripet.ripetizione FROM entrate_inc JOIN entrate_ripet ON (entrate_inc.ripetizione_id=entrate_ripet._id) WHERE entrate_inc._id=?";
	String ENTRATE_INC_PREFERITE = "SELECT * FROM entrate_inc WHERE favorite = 1 ORDER BY voce ASC";

		
	/**
	 * Query di ricerca complesse per tabella dei budget (spese_budget). Vedi la classe DBCSpeseBudget 
	 * per maggiori dettagli.
	 */
	String SPESE_BUDGET_ELENCO_COMPLETO_GREZZO = "SELECT * FROM spese_budget";
	String SPESE_BUDGET_ELENCO_COMPLETO = "SELECT _id, ripetizione||' '||voce AS voce_budget, importo_valprin, data_inizio, data_fine, aggiungere_rimanenza, spesa_sost, risparmio, budget_iniziale, ultimo_aggiunto FROM spese_budget";
	String SPESE_BUDGET_ELENCO_NON_SCADUTI = "SELECT _id, ripetizione||' '||voce AS voce_budget, importo_valprin, data_inizio, data_fine, aggiungere_rimanenza, spesa_sost, risparmio, budget_iniziale, ultimo_aggiunto FROM spese_budget WHERE ?<=data_fine ORDER BY voce_budget ASC";
	String SPESE_BUDGET_ELENCO_NON_SCADUTI_FILTRATO = "SELECT _id, ripetizione||' '||voce AS voce_budget, importo_valprin, data_inizio, data_fine, aggiungere_rimanenza, spesa_sost, risparmio, budget_iniziale, ultimo_aggiunto FROM spese_budget WHERE ?<=data_fine AND voce LIKE ? ORDER BY voce_budget ASC";
	String SPESE_BUDGET_ELENCO_ANNUALE = "SELECT _id, ripetizione||' '||voce AS voce_budget, importo_valprin, data_inizio, data_fine, aggiungere_rimanenza, spesa_sost, risparmio FROM spese_budget WHERE ripetizione = 'annuale'";
	String SPESE_BUDGET_ELENCO_MENSILE = "SELECT _id, ripetizione||' '||voce AS voce_budget, importo_valprin, data_inizio, data_fine, aggiungere_rimanenza, spesa_sost, risparmio FROM spese_budget WHERE ripetizione = 'mensile'";
	String SPESE_BUDGET_ELENCO_BISETTIMANALE = "SELECT _id, ripetizione||' '||voce AS voce_budget, importo_valprin, data_inizio, data_fine, aggiungere_rimanenza, spesa_sost, risparmio FROM spese_budget WHERE ripetizione = 'bisettimanale'";
	String SPESE_BUDGET_ELENCO_SETTIMANALE = "SELECT _id, ripetizione||' '||voce AS voce_budget, importo_valprin, data_inizio, data_fine, aggiungere_rimanenza, spesa_sost, risparmio FROM spese_budget WHERE ripetizione = 'settimanale'";
	String SPESE_BUDGET_ELENCO_GIORNALIERO = "SELECT _id, ripetizione||' '||voce AS voce_budget, importo_valprin, data_inizio, data_fine, aggiungere_rimanenza, spesa_sost, risparmio FROM spese_budget WHERE ripetizione = 'giornaliero'";
	String SPESE_BUDGET_ELENCO_UNA_TANTUM = "SELECT _id, ripetizione||' '||voce AS voce_budget, importo_valprin, data_inizio, data_fine, aggiungere_rimanenza, spesa_sost, risparmio FROM spese_budget WHERE ripetizione = 'una_tantum'";
	String SPESE_BUDGET_ELENCO_BUDGET_ANALOGHI = "SELECT * FROM spese_budget WHERE budget_iniziale=? ORDER BY data_inizio DESC";
	String SPESE_BUDGET_ELENCO_BUDGET_SCADUTI_O_QUASI = "SELECT _id, ripetizione, voce FROM spese_budget WHERE ((risparmio<=0 OR risparmio/importo_valprin <= .1) AND ?<=data_fine) ORDER BY ripetizione ASC";
	String SPESE_BUDGET_ELENCO_BUDGET_CONTENENTI_VOCE = "SELECT * FROM spese_budget WHERE voce LIKE ?";
	

	/**
	 * Query di ricerca complesse per le funzioni di aggiornamento automatico del database. Vedi la
	 * classe FunzioniAggiornamento per maggiori dettagli.
	 */
	String ESTRAI_SPESERIPETUTE_GIORNALIERE = "SELECT * FROM spese_ripet WHERE ripetizione='giornaliero' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_SPESERIPETUTE_SETTIMANALI = "SELECT * FROM spese_ripet WHERE ripetizione='settimanale' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_SPESERIPETUTE_BISETTIMANALI = "SELECT * FROM spese_ripet WHERE ripetizione='bisettimanale' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_SPESERIPETUTE_MENSILI = "SELECT * FROM spese_ripet WHERE ripetizione='mensile' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_SPESERIPETUTE_ANNUALI = "SELECT * FROM spese_ripet WHERE ripetizione='annuale' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_SPESERIPETUTE_GIORNI_LAVORATIVI = "SELECT * FROM spese_ripet WHERE ripetizione='giorni_lavorativi' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_SPESERIPETUTE_WEEKEND = "SELECT * FROM spese_ripet WHERE ripetizione='weekend' AND flag_fine=0 AND ?>aggiornato_a";
	
	String ESTRAI_ENTRATERIPETUTE_GIORNALIERE = "SELECT * FROM entrate_ripet WHERE ripetizione='giornaliero' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_ENTRATERIPETUTE_SETTIMANALI = "SELECT * FROM entrate_ripet WHERE ripetizione='settimanale' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_ENTRATERIPETUTE_BISETTIMANALI = "SELECT * FROM entrate_ripet WHERE ripetizione='bisettimanale' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_ENTRATERIPETUTE_MENSILI = "SELECT * FROM entrate_ripet WHERE ripetizione='mensile' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_ENTRATERIPETUTE_ANNUALI = "SELECT * FROM entrate_ripet WHERE ripetizione='annuale' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_ENTRATERIPETUTE_GIORNI_LAVORATIVI = "SELECT * FROM entrate_ripet WHERE ripetizione='giorni_lavorativi' AND flag_fine=0 AND ?>aggiornato_a";
	String ESTRAI_ENTRATERIPETUTE_WEEKEND = "SELECT * FROM entrate_ripet WHERE ripetizione='weekend' AND flag_fine=0 AND ?>aggiornato_a";

	String ESTRAI_BUDGET_PERIODICI_ULTIMI_SCADUTI = "SELECT * FROM spese_budget WHERE (ultimo_aggiunto=1 AND ?>data_fine)";
	String ESTRAI_BUDGET_SPECIFICO = "SELECT * FROM spese_budget WHERE voce=? AND ripetizione=? AND data_inizio=? AND data_fine=? AND aggiungere_rimanenza=?";
	String ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA = "SELECT * FROM spese_budget WHERE voce LIKE ? AND (? >= data_inizio AND ? <= data_fine)";
	String ESTRAI_BUDGET_PER_MODIFICA_SPESA = "SELECT * FROM spese_budget WHERE (voce LIKE ? AND (? >= data_inizio AND ?<= data_fine)) OR (voce LIKE ? AND (? >= data_inizio AND ?<= data_fine))";
	String ESTRAI_BUDGET_PER_ELIMINAZIONE_SPESE_RIPETUTE = "SELECT * FROM spese_budget WHERE voce LIKE ? AND ((data_inizio <= ? AND data_fine >= ?) OR (data_inizio >= ? AND data_fine <= ?) OR (data_inizio <= ? AND data_fine >= ?) OR (data_inizio <= ? AND data_fine >= ?))";
}

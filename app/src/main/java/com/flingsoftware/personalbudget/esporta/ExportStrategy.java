package com.flingsoftware.personalbudget.esporta;

import java.io.Serializable;

/**
 * Generic interface for exporting the database.
 * Plays the role of Strategy in the Strategy Pattern.
 */
public interface ExportStrategy {
    boolean exportDatabase(ExportDetails exportDetails);
    String getOutputFormat();
}

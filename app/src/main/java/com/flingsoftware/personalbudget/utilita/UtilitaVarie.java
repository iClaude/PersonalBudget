package com.flingsoftware.personalbudget.utilita;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;

public class UtilitaVarie {

    /**
     * Converte una bitmap passata come parametro in scala di grigi.
     * @param originalImage la bitmap da convertire in grigio
     */
    public static Bitmap filtroGrigio(Bitmap originalImage) {
        boolean hasTransparent = originalImage.hasAlpha();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Bitmap grayScaleImage =
                originalImage.copy(originalImage.getConfig(),
                        true);

        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (int i = 0; i < height; ++i) {
            // Break out if we've been interrupted.
            if (Thread.interrupted())
                return null;

            for (int j = 0; j < width; ++j) {
                // Check if the pixel is transparent in the original
                // by checking if the alpha is 0.
                if (hasTransparent
                        && ((grayScaleImage.getPixel(j, i)
                        & 0xff000000) >> 24) == 0)
                    continue;

                // Convert the pixel to grayscale.
                int pixel = grayScaleImage.getPixel(j, i);
                int grayScale =
                        (int) (Color.red(pixel) * .299
                                + Color.green(pixel) * .587
                                + Color.blue(pixel) * .114);
                grayScaleImage.setPixel(j, i,
                        Color.rgb(grayScale,
                                grayScale,
                                grayScale));
            }
        }

        return grayScaleImage;
    }


    /*
    Visualizza un AlertDialog con pulsanti OK, con relativo listener che definisce le operazioni
    da eseguire, e Annulla (se la variabile boolean ? impostata su true), che in questo caso non
    fa nulla.
 */
    public static void visualizzaDialogOKAnnulla(Context context, String titolo, String msg, String OK, boolean bottAnnulla, String annulla, int idIcona, DialogInterface.OnClickListener OKListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titolo);
        builder.setMessage(msg);
        builder.setPositiveButton(OK, OKListener);
        if (bottAnnulla) {
            builder.setNegativeButton(annulla, null);
        }
        builder.setCancelable(true);
        if(idIcona != 0) {
            builder.setIcon(idIcona);
        }
        AlertDialog confirmDialog = builder.create();
        confirmDialog.show();
    }
}
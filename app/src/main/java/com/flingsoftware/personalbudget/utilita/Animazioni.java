package com.flingsoftware.personalbudget.utilita;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Classe di utilit? per la gestione di animazioni richiamate da diverse parti dell'app.
 * Implementa il pattern Singleton per fare in modo che di questa classe venga creata un'unica
 * istanza.
 * Created by Claudio on 14/01/2016.
 */
public class Animazioni {

    private Animazioni() {}

    private static class Contenitore {
        private final static Animazioni ISTANZA = new Animazioni();
    }

    public static Animazioni getInstance() {
        return Contenitore.ISTANZA;
    }

    public void ruotaFreccia (View v, boolean espanso) {
        float gradiIniz = espanso? 180.0f : 0.0f;
        float gradiFine = espanso? 0.0f : 180.0f;
        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation", gradiIniz, gradiFine);
        anim.setDuration(500);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }
}

/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.utility;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.SparseIntArray;

import com.flingsoftware.personalbudget.R;

/**
 * This class manages all sound effects used in the app. It is implemented as a Singleton
 * (thread safe implementation).
 * Clients must:
 * 1) get an instance of this class via getInstance
 * 2) play the sound via playSound
 */
public class SoundEffectsManager {
    // Constants for each sound effect.
    public static final int SOUND_ADDED = 0;
    public static final int SOUND_DELETED = 1;
    public static final int SOUND_COMPLETED = 2;
    public static final int SOUND_APP_LOCKED = 3;
    public static final int SOUND_CONFIRM = 4;
    public static final int SOUND_ERROR = 5;

    // Variables.
    private SoundPool soundPool;
    private SparseIntArray soundMap;
    private volatile boolean soundsLoaded;


    private SoundEffectsManager() {

    }

    private static class Container {
        private final static SoundEffectsManager INSTANCE = new SoundEffectsManager();
    }

    public static SoundEffectsManager getInstance() {
        return Container.INSTANCE;
    }

    /*
        Load sound effects into the SoundPool if this is null. Sounds are loaded in a separate
        thread. When the sounds are loaded soundsLoaded is true.
     */
    public synchronized void loadSounds(final Context context) {
        if (soundsLoaded) return; // sounds already loaded

        soundPool = new SoundPoolFactory().makeSoundPool();
        soundMap = new SparseIntArray(6);

        final Context applicationContext = context.getApplicationContext();
        new Thread(new Runnable() {
            public void run() {
                soundMap.put(SOUND_ADDED, soundPool.load(applicationContext, R.raw.sound_added, 1));
                soundMap.put(SOUND_DELETED, soundPool.load(applicationContext, R.raw.sound_deleted, 1));
                soundMap.put(SOUND_COMPLETED, soundPool.load(applicationContext, R.raw.sound_completed, 1));
                soundMap.put(SOUND_APP_LOCKED, soundPool.load(applicationContext, R.raw.sound_app_locked, 1));
                soundMap.put(SOUND_CONFIRM, soundPool.load(applicationContext, R.raw.sound_confirm, 1));
                soundMap.put(SOUND_ERROR, soundPool.load(applicationContext, R.raw.sound_error, 1));

                soundsLoaded = true;
            }
        }).start();
    }

    // Play the sound with the specified id.
    public void playSound(int soundId) {
        if (soundsLoaded) {
            soundPool.play(soundMap.get(soundId), 1, 1, 1, 0, 1f);
        }
    }

    // Release all resources associated with this SoundEffectsManager.
    public void release() {
        if (soundsLoaded) {
            soundMap.clear();
            soundPool.release();
            soundPool = null;
            soundsLoaded = false;
        }
    }

    public boolean isSoundsLoaded() {
        return soundsLoaded;
    }

    /*
            Factory class that returns a SoundPool object. The creation steps depends on the
            Android version.
         */
    @SuppressWarnings("deprecation")
    private class SoundPoolFactory {
        public SoundPool makeSoundPool() {
            SoundPool soundPool;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            } else {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                soundPool = new SoundPool.Builder()
                        .setMaxStreams(1)
                        .setAudioAttributes(audioAttributes)
                        .build();
            }

            return soundPool;
        }
    }
}

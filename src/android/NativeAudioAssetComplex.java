//
//
//  NativeAudioAssetComplex.java
//
//  Created by Sidney Bofah on 2014-06-26.
//

package com.rjfun.cordova.plugin.nativeaudio;

import java.io.IOException;
import java.io.FileDescriptor;
import java.util.concurrent.Callable;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;

public class NativeAudioAssetComplex implements OnPreparedListener, OnCompletionListener {

    private static final int INVALID = 0;//not loaded
    private static final int PREPARED = 1;//loaded, never played
    private static final int PENDING_PLAY = 2;//preparing, need to play once
    private static final int PLAYING = 3;//playing once
    private static final int PENDING_LOOP = 4;//preparing, need to loop
    private static final int LOOPING = 5;//looping
    private static final int STOPPED = 6;//loaded & played, now stopped
    private static final int PAUSED = 7;//paused during playback
    private static final String LOGTAG = "NativeAudio";

    private MediaPlayer mp;
    private int state;
    Callable<Void> completeCallback;

    public NativeAudioAssetComplex(FileDescriptor afd, int length, float volume) throws IOException {
        state = INVALID;
        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);
        mp.setOnPreparedListener(this);
        //mp.setDataSource( afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mp.setDataSource(afd, 0, length);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setVolume(volume, volume);
        mp.prepare();
    }

    public void play(Callable<Void> completeCb) throws IOException {
        completeCallback = completeCb;
        invokePlay(false);
    }

    private void invokePlay(Boolean loop) {
        Boolean playing = mp.isPlaying();
        if (playing) {
            mp.pause();
//            mp.setLooping(loop);
//            mp.seekTo(0);
//            mp.start();
        }
        if (!playing && state == PREPARED) {
            state = (loop ? PENDING_LOOP : PENDING_PLAY);
            onPrepared(mp);
        } else if (!playing) {
            state = (loop ? PENDING_LOOP : PENDING_PLAY);
            mp.setLooping(loop);
            mp.start();
        }
    }

    public void pause() throws IOException {
        try {
            if (mp.isPlaying()) {
                mp.pause();
            }
        } catch (IllegalStateException e) {
            System.out.println("DEBUG NativeAudioAssetComplex IllegalStateException");
            // I don't know why this gets thrown; catch here to save app
        }
    }

    public int getPosition() throws IOException {
        if (state == INVALID || state == STOPPED || state == PREPARED)
            return -1;
        else
            return mp.getCurrentPosition();
    }

    public void setPosition(int position) throws IOException {
        System.out.println(position);
        mp.seekTo(position);
    }

    public int getDuration() throws IOException {
        if (state == INVALID)
            return 0;
        else
            return mp.getDuration();
    }

    public void resume() {
        mp.start();
    }

    public void stop() {
        try {
            if (mp.isPlaying()) {
                state = INVALID;
                mp.pause();
                mp.seekTo(0);
            }
        } catch (IllegalStateException e) {
            // I don't know why this gets thrown; catch here to save app
        }
    }

    public void setVolume(float volume) {
        try {
            mp.setVolume(volume, volume);
        } catch (IllegalStateException e) {
            // I don't know why this gets thrown; catch here to save app
        }
    }

    public void loop() throws IOException {
        invokePlay(true);
    }

    public void unload() throws IOException {
        this.stop();
        mp.release();
    }

    public void onPrepared(MediaPlayer mPlayer) {
        if (state == PENDING_PLAY) {
            mp.setLooping(false);
            mp.seekTo(0);
            mp.start();
            state = PLAYING;
        } else if (state == PENDING_LOOP) {
            mp.setLooping(true);
            mp.seekTo(0);
            mp.start();
            state = LOOPING;
        } else {
            state = PREPARED;
            mp.seekTo(0);
        }
    }

    public void onCompletion(MediaPlayer mPlayer) {
        Log.d(LOGTAG, "NA onCompletion state=" + state);
        if (state != LOOPING) {
            this.state = INVALID;
            try {
                this.stop();
                if (completeCallback != null)
                    completeCallback.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

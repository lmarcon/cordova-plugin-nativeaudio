//
//
//  NativeAudioAsset.java
//
//  Created by Sidney Bofah on 2014-06-26.
//

package com.rjfun.cordova.plugin.nativeaudio;

import java.io.IOException;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.content.res.AssetFileDescriptor;

public class NativeAudioAsset {

    private ArrayList<NativeAudioAssetComplex> voices;
    private int playIndex = 0;

    public NativeAudioAsset(FileDescriptor afd, int length, int numVoices, float volume) throws IOException {
        voices = new ArrayList<NativeAudioAssetComplex>();

        if (numVoices < 0)
            numVoices = 1;

        for (int x = 0; x < numVoices; x++) {
            NativeAudioAssetComplex voice = new NativeAudioAssetComplex(afd, length, volume);
            voices.add(voice);
        }
    }

    public void play(Callable<Void> completeCb) throws IOException {
        NativeAudioAssetComplex voice = voices.get(playIndex);
        voice.play(completeCb);
        playIndex++;
        playIndex = playIndex % voices.size();
    }

//    public boolean pause() throws IOException {
//        System.out.println("DEBUG NativeAudioAsset pause() enter");
//        boolean wasPlaying = false;
//        for (int x = 0; x < voices.size(); x++) {
//            System.out.println("DEBUG NativeAudioAsset pause() voice: " + x);
//            NativeAudioAssetComplex voice = voices.get(x);
//            wasPlaying |= voice.pause();
//        }
//        return wasPlaying;
//    }

    public void pause() throws IOException {
        for (int x = 0; x < voices.size(); x++) {
            NativeAudioAssetComplex voice = voices.get(x);
            voice.pause();
        }
    }

    public void resume() {
        // only resumes first instance, assume being used on a stream and not multiple sfx
        if (voices.size() > 0) {
            NativeAudioAssetComplex voice = voices.get(0);
            voice.resume();
        }
    }

    public void stop() {
        for (int x = 0; x < voices.size(); x++) {
            NativeAudioAssetComplex voice = voices.get(x);
            voice.stop();
        }
    }

    public void loop() throws IOException {
        NativeAudioAssetComplex voice = voices.get(playIndex);
        voice.loop();
        playIndex++;
        playIndex = playIndex % voices.size();
    }

    public void unload() throws IOException {
        this.stop();
        for (int x = 0; x < voices.size(); x++) {
            NativeAudioAssetComplex voice = voices.get(x);
            voice.unload();
        }
        voices.removeAll(voices);
    }

    public void setVolume(float volume) {
        for (int x = 0; x < voices.size(); x++) {
            NativeAudioAssetComplex voice = voices.get(x);
            voice.setVolume(volume);
        }
    }

    public int getDuration() throws IOException {
        NativeAudioAssetComplex voice = voices.get(0);
        return voice.getDuration();
    }

    public int getPosition() throws IOException {

        for (int x = 0; x < voices.size(); x++) {
            NativeAudioAssetComplex voice = voices.get(x);
            return voice.getPosition();
        }
        return 0;
    }

    public int setPosition(int position) throws IOException {
        for (int x = 0; x < voices.size(); x++) {
            NativeAudioAssetComplex voice = voices.get(x);
            voice.setPosition(position);
        }
        return 0;
    }
}

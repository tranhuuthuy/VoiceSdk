package com.viettel.webrtc_sdk.utils;

public class AudioConfig {
    private int sampleRate;
    private boolean useStereo;

    public AudioConfig(int sampleRate, boolean useStereo) {
        this.sampleRate = sampleRate;
        this.useStereo = useStereo;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public boolean isUseStereo() {
        return useStereo;
    }

    public void setUseStereo(boolean useStereo) {
        this.useStereo = useStereo;
    }
}

package com.viettel.webrtc_sdk.webrtc;

public class MediaConfig {
    private boolean video;
    private AudioConfig audio;

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public AudioConfig getAudio() {
        return audio;
    }

    public void setAudio(AudioConfig audio) {
        this.audio = audio;
    }
}

package com.viettel.webrtc_sdk.utils;

public class VideoConfig {
    private int width;
    private int height;
    private int fps;

    public VideoConfig(int width, int height, int fps) {
        this.width = width;
        this.height = height;
        this.fps = fps;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }
}

package com.viettel.webrtc_sdk.webrtc;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SimpleSdpObserver implements SdpObserver {
    private static final String TAG = "MediaSimpleSdpObserver";
    private String label;

    public SimpleSdpObserver(String label) {
        this.label = label;
    }

    public SimpleSdpObserver() {
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "[SimpleSdpObserver::onCreateSuccess] " + label + sessionDescription);
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "[SimpleSdpObserver::onSetSuccess] " + label);
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "[SimpleSdpObserver::onCreateFailure] " + label);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, "[SimpleSdpObserver::onSetFailure] " + label + " " + s);
    }
}

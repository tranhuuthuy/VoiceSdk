package com.viettel.webrtc_sdk.webrtc;

import android.app.Activity;
import android.util.Log;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.VideoCapturer;

import java.util.ArrayList;
import java.util.Arrays;

public class MediaUtils {
    private static String TAG = "MediaUtils";
    private Activity activity;

    public MediaUtils(Activity activity) {
        this.activity = activity;
    }

    public VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(activity));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    /*
     * Read more about Camera2 here
     * https://developer.android.com/reference/android/hardware/camera2/package-summary.html
     * */
    public boolean useCamera2() {
        return Camera2Enumerator.isSupported(activity);
    }

    public VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        Log.d(TAG, "[MediaUtils::createCameraCapturer] Show list camera : " + new ArrayList<>(Arrays.asList(deviceNames)));
        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    Log.d(TAG, "[MediaUtils::createCameraCapturer] 1.Using front facing camera : " + deviceName);
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    Log.d(TAG, "[MediaUtils::createCameraCapturer] 2.Using camera : " + deviceName);
                    return videoCapturer;
                }
            }
        }

        return null;
    }
}

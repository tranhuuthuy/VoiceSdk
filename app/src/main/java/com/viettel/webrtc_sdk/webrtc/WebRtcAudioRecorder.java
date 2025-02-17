package com.viettel.webrtc_sdk.webrtc;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class WebRtcAudioRecorder implements Runnable {
    private static final String TAG = "WebRtcAudioRecorder";
    private static final long TIME_SLEEP = 20;
    private static final int MAX_QUEUE_SIZE = 200000;
    private BlockingQueue<short[]> queue;
    private int numSamplesToTake;

    public WebRtcAudioRecorder(int numSamplesToTake) {
        this.queue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        this.numSamplesToTake = numSamplesToTake;
    }

    public void put(short[] shorts) throws InterruptedException {
        queue.put(shorts);
    }

    public short[] take(int numElements) throws InterruptedException {
        short[] result = new short[numElements];
        int shortsRead = 0;

        while (shortsRead < numElements && !queue.isEmpty()) {
            short[] currentBytes = queue.take();

            int shortsToRead = Math.min(numElements - shortsRead, currentBytes.length);
            System.arraycopy(currentBytes, 0, result, shortsRead, shortsToRead);
            shortsRead += shortsToRead;

            if (currentBytes.length > shortsToRead) {
                // Đẩy phần còn lại của mảng byte trở lại hàng đợi
                short[] remainingBytes = new short[currentBytes.length - shortsToRead];
                System.arraycopy(currentBytes, shortsToRead, remainingBytes, 0, remainingBytes.length);
                queue.put(remainingBytes);
            }
        }

        if (shortsRead == 0) {
            return null; // Không có dữ liệu
        } else if (shortsRead < numElements) {
            // Trả về mảng byte với số lượng byte thực tế đọc được
            short[] actualResult = new short[shortsRead];
            System.arraycopy(result, 0, actualResult, 0, shortsRead);
            return actualResult;
        } else {
            return result;
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (!queue.isEmpty()) {
                    short[] samples = take(numSamplesToTake);
                    if (samples != null) {
                        if (samples.length < numSamplesToTake) {
                            put(samples);
                            Thread.sleep(TIME_SLEEP);
                        } else {
                            processSamples(samples);
                        }
                    }
                } else {
                    Thread.sleep(TIME_SLEEP);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "WebRtcAudioRecorder error " + e);
        }
    }

    public abstract void processSamples(short[] samples);
}
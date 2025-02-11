package com.viettel.webrtc_sdk.utils;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicrophoneRunnable implements Runnable {
    private static long TS = 400;
    private Activity activity;
    private ArrayList<RoundedImageView> eclipse;
    private RoundedImageView circle;

    private TextView textView;

    private AtomicBoolean isVisible;
    private Thread thread;

    private boolean isAudioRecorder;

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;
    private int bufferSize;

    public void setAudioRecorder(boolean audioRecorder) {
        isAudioRecorder = audioRecorder;
    }

    @SuppressLint("MissingPermission")
    public MicrophoneRunnable(Activity activity, ArrayList<RoundedImageView> eclipse, RoundedImageView circle, TextView textView) {
        this.activity = activity;
        this.eclipse = eclipse;
        this.circle = circle;
        this.textView = textView;
        this.thread = new Thread(this);
        this.isVisible = new AtomicBoolean(true);
    }



    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        try {
            new Thread(() -> {
                try {
                    while (true) {
                        if(isVisible.get()) {
                            for (RoundedImageView ec : eclipse) {
                                activity.runOnUiThread(() -> {
                                    ec.setVisibility(VISIBLE);
                                });
                                Thread.sleep(TS);
                            }
                            for (RoundedImageView ec : eclipse) {
                                activity.runOnUiThread(() -> {
                                    ec.setVisibility(INVISIBLE);
                                });
                            }
                        }
                        Thread.sleep(TS);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }).start();

            if(isAudioRecorder) {
                this.bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
                this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
                short[] audioBuffer = new short[bufferSize];
                float[] waveform = new float[bufferSize];
                int width = 400;
                float maxScale = 3f, minScale = 1f;

                audioRecord.startRecording();
                while (true) {
                    int read = audioRecord.read(audioBuffer, 0, bufferSize);
                    float sumWaveForm = 0f;
                    for (int i = 0; i < read; i++) {
                        waveform[i] = audioBuffer[i];
                        sumWaveForm += Math.abs(waveform[i]);
                    }
                    float avgWaveForm = (sumWaveForm / read) / width;
                    float sc = avgWaveForm >= maxScale ? maxScale : (avgWaveForm <= minScale ? minScale : avgWaveForm);
                    if (isVisible.get()) {
                        activity.runOnUiThread(() -> {
                            circle.setScaleX(sc);
                            circle.setScaleY(sc);
                        });
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void start(){
        thread.start();
    }

    public void setVisible(boolean visible){
        if(visible){
            isVisible.set(true);
            activity.runOnUiThread(() -> {
               for(RoundedImageView ec : eclipse){
                   ec.setVisibility(VISIBLE);
               }
               circle.setVisibility(VISIBLE);
               textView.setVisibility(VISIBLE);
            });
        }else {
            isVisible.set(false);
            activity.runOnUiThread(() -> {
                for(RoundedImageView ec : eclipse){
                    ec.setVisibility(INVISIBLE);
                }
                circle.setVisibility(INVISIBLE);
                textView.setVisibility(INVISIBLE);
            });
        }
    }
}

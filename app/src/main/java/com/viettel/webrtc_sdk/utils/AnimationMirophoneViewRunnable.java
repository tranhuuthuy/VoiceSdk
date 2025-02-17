package com.viettel.webrtc_sdk.utils;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import android.app.Activity;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AnimationMirophoneViewRunnable implements Runnable {
    private static long TS = 400;
    private Activity activity;
    private ArrayList<RoundedImageView> eclipse;
    private RoundedImageView circle;
    private TextView textView;
    private AtomicBoolean isVisible;
    private Thread thread;

    public AnimationMirophoneViewRunnable(Activity activity, ArrayList<RoundedImageView> eclipse, RoundedImageView circle, TextView textView) {
        this.activity = activity;
        this.eclipse = eclipse;
        this.circle = circle;
        this.textView = textView;
        this.thread = new Thread(this);
        this.isVisible = new AtomicBoolean(true);
    }

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
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public boolean getIsVisible() {
        return isVisible.get();
    }

    public void start(){
        thread.start();
    }

    public RoundedImageView getCircle() {
        return circle;
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
            BusinessUtils.playFromAssets(activity.getApplicationContext(), "sound.mp3");
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

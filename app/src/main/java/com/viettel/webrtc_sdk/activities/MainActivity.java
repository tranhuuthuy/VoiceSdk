package com.viettel.webrtc_sdk.activities;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.viettel.webrtc_sdk.R;
import com.viettel.webrtc_sdk.webrtc.MediaStreamWebRTC;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        try {
            MediaStreamWebRTC mediaStreamWebRTC = new MediaStreamWebRTC(
                    this,
//                    "wss://10.61.152.49:8002/voicebotv2",
//                    "wss://192.168.16.137:8002/voicebotv2",
                    "wss://171.244.195.82:8111/voicebotv2",
                    20000,
                    1280,
                    720,
                    30,
                    null, null,
                    "turn:171.244.195.82:8889", "thuyth2", "1qaz"
            );
            mediaStreamWebRTC.offer();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(MediaStreamWebRTC.TAG, "MediaStreamWebRTC websocket error");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
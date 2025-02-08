package com.viettel.webrtc_sdk.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.viettel.webrtc_sdk.R;
import com.viettel.webrtc_sdk.utils.VideoConfig;
import com.viettel.webrtc_sdk.webrtc.MediaStreamWebRTC;
import com.viettel.webrtc_sdk.webrtc.TurnServer;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private VideoConfig videoConfig;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


//        videoConfig = new VideoConfig(1280, 720, 30);

        // check permission
        if (arePermissionsGranted()) {
            process();
        } else {
            requestPermissions();
            Log.d("Permissions", "requestPermissions");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void process() {
        try {
            TurnServer turnServer = new TurnServer("171.244.195.82", 8889, "thuyth2", "1qaz");
            MediaStreamWebRTC mediaStreamWebRTC = new MediaStreamWebRTC(
                    this,
                    "wss://171.244.195.82:8111/voicebotv2",
                    20000,
                    videoConfig,
                    null, null,
                    turnServer
            );
            mediaStreamWebRTC.offer();
        } catch (Exception e) {
            Log.d(MediaStreamWebRTC.TAG, "MediaStreamWebRTC websocket error");
        }
    }

    // check permission app
    private boolean arePermissionsGranted() {
        if (videoConfig != null) {
            return ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (videoConfig != null) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            }, REQUEST_CODE_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO
            }, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (arePermissionsGranted()) {
                process();
            } else {
                Log.d("Permissions", "requestPermissions error");
                showAlertDialog();
            }
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo")
                .setMessage("Hãy đảm bảo cấp quyền Microphone/Camera cho ứng dụng")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle OK button click
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle Cancel button click
                        dialog.cancel();
                    }
                });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
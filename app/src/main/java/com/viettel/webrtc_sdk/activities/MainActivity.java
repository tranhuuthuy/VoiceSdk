package com.viettel.webrtc_sdk.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
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
import com.viettel.webrtc_sdk.utils.UserInfo;
import com.viettel.webrtc_sdk.utils.VideoConfig;
import com.viettel.webrtc_sdk.webrtc.MediaStreamWebRTC;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
            Properties properties = loadProperties(getApplicationContext());
            String signalingServer = properties.getProperty("signaling_server");
            int timeout = Integer.parseInt(properties.getProperty("timeout"));
            Log.d("SignalingServer", "Connect to signaling server " + signalingServer);
            UserInfo userInfo = new UserInfo();
            userInfo.getProperties().put("domain", "Viettel Home");

            MediaStreamWebRTC mediaStreamWebRTC = new MediaStreamWebRTC(
                    this,
                    signalingServer,
                    timeout,
                    videoConfig,
                    userInfo, null
            );
            mediaStreamWebRTC.connect();
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

    private Properties loadProperties(Context context) {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open("config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            Log.e("PROPERTIES", "Error loading properties file: " + e.getMessage());
            return null;
        }
        return properties;
    }
}
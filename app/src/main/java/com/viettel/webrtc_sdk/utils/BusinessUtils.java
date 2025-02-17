package com.viettel.webrtc_sdk.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class BusinessUtils {
    // load properties
    private static Properties properties;

    private static Properties loadProperties(Context context) {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open("config.properties")) {
            properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e("PROPERTIES", "Error loading properties file: " + e.getMessage());
            return null;
        }
        return properties;
    }

    public static void playFromAssets(Context context, String fileName) {
        try {
            // Initialize MediaPlayer
            MediaPlayer mediaPlayer = new MediaPlayer();

            // Get the AssetFileDescriptor for the audio file
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(fileName);
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength());
            assetFileDescriptor.close(); // Close the descriptor

            mediaPlayer.prepare(); // Prepare the player
            mediaPlayer.start(); // Start playback

            // Optionally, set a listener for when playback is complete
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release(); // Release the MediaPlayer
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(Context context, String key){
        if(properties == null){
            properties = loadProperties(context);
        }
        return properties.getProperty(key);
    }

    public static short[] byteArrayToShortArray(byte[] bytes, boolean bigEndian) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        short[] samples = new short[bytes.length / 2]; // 2 bytes per short
        buffer.asShortBuffer().get(samples);
        return samples;
    }

}

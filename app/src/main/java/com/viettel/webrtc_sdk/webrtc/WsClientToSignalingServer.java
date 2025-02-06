package com.viettel.webrtc_sdk.webrtc;

import android.util.Log;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import tech.gusavila92.apache.http.ssl.SSLContexts;
import tech.gusavila92.apache.http.ssl.TrustStrategy;
import tech.gusavila92.websocketclient.WebSocketClient;

public abstract class WsClientToSignalingServer {
    private static String TAG = "MediaWsClientToSignalingServer";
    private String uri;
    private int connectionTimeout;

    private WebSocketClient ws;

    public WsClientToSignalingServer(String uri, int timeout) throws URISyntaxException {
        this.uri = uri;
        this.connectionTimeout = timeout;
        initialize();
    }

    public abstract void onMessage(String message);

    private void initialize() throws URISyntaxException {
        Log.d(TAG, "[WsClientToSignalingServer::initialize] Starting init websocket client connect to " + uri);
        ws = new tech.gusavila92.websocketclient.WebSocketClient(new URI(uri)) {
            @Override
            public void onOpen() {
                Log.d(TAG, "[WsClientToSignalingServer::initialize] connected to " + uri);
            }

            @Override
            public void onTextReceived(String message) {
                    Log.d(TAG, "[connectToSignallingServer::onTextReceived] : " + message);
                    onMessage(message);
            }

            @Override
            public void onBinaryReceived(byte[] data) {

            }

            @Override
            public void onPingReceived(byte[] data) {

            }

            @Override
            public void onPongReceived(byte[] data) {

            }

            @Override
            public void onException(Exception e) {
                Log.e(TAG, "[WsClientToSignalingServer::initialize] onException " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onCloseReceived() {
                Log.d(TAG, "[WsClientToSignalingServer::initialize] onCloseReceived");
            }
        };
        ws.setConnectTimeout(connectionTimeout);
        ws.enableAutomaticReconnection(connectionTimeout);
        Log.d(TAG, "[WsClientToSignalingServer::initialize] End init websocket client connect to " + uri);
    }

    public void connect() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // by-pass https
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        ws.setSSLSocketFactory(sslContext.getSocketFactory());
        ws.connect();
    }

    public void sendMessage(String message){
        ws.send(message);
        Log.d(TAG,"[MediaWsClientToSignalingServer::sendMessage] " + message);
    }

    public void close(){
        ws.close();
        Log.d(TAG,"[WsClientToSignalingServer::close]");
    }
}

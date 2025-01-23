package com.viettel.webrtc_sdk.webrtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

public class PeerObserverImpl implements PeerConnection.Observer{
    private static final String TAG = "MediaPeerObserverImpl";
    private String label;

    private String logLabel;

    public PeerObserverImpl(String label) {
        this.label = label;
        this.logLabel = ", label : " + label;
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, "[PeerObserverImpl::onSignalingChange] : " + signalingState.name() + logLabel);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "[PeerObserverImpl::onIceConnectionChange] : " + iceConnectionState.name() + logLabel);
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG, "[PeerObserverImpl::onIceConnectionReceivingChange] : " + b + logLabel);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG, "[PeerObserverImpl::onIceGatheringChange] : " + iceGatheringState.name() +logLabel);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG, "[PeerObserverImpl::onIceCandidate] : " + iceCandidate +logLabel);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG, "[PeerObserverImpl::onIceCandidatesRemoved] : " + iceCandidates + logLabel);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "[PeerObserverImpl::onAddStream] : " + mediaStream.videoTracks.size() + logLabel);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "[PeerObserverImpl::onRemoveStream] : " + mediaStream.getId() + logLabel);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(TAG, "[PeerObserverImpl::onRemoveStream] : " + dataChannel.id() + logLabel);
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(TAG, "[PeerObserverImpl::onRenegotiationNeeded] : " + logLabel);
    }
}

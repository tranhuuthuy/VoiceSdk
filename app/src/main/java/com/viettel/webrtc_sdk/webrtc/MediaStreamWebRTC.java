package com.viettel.webrtc_sdk.webrtc;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.viettel.webrtc_sdk.R;
import com.viettel.webrtc_sdk.adapters.MessageAdapter;
import com.viettel.webrtc_sdk.models.Message;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class MediaStreamWebRTC {
    public static final String TAG = "MediaStreamWebRTC";
    private static final String VIDEO_TRACK_ID = "01";
    private static final String AUDIO_TRACK_ID = "02";
    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
    public static final int FPS = 30;

    private int videoResolutionWidth;
    private int videoResolutionHeight;
    private int fps;

    private String firstText;

    private JSONObject userInfo;

    private Activity activity;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    GifImageView gifImageView;
    private EglBase rootEglBase;
    private List<PeerConnection.IceServer> iceServers;
    private VideoTrack videoTrackFromCamera;
    private AudioTrack audioTrackFromMicrophone;
    private SurfaceViewRenderer localViewRender;
    private SurfaceViewRenderer remoteViewViewRender;
    private PeerConnectionFactory factory;
    private PeerConnection localPeerConnection, remotePeerConnection;
    private WsClientToSignalingServer wsClient;

    public MediaStreamWebRTC(Activity activity, String uriToSignalingServer, int timeout,
                             int width, int height, int fps,
                             JSONObject userInfo, String firstText) throws URISyntaxException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.activity = activity;
        this.iceServers = new ArrayList<>();
        this.videoResolutionWidth = width;
        this.videoResolutionHeight = height;
        this.fps = fps;
        this.userInfo = userInfo;

        this.firstText = firstText == null || firstText.isEmpty() ? "Xin chào, tôi có thể giúp gì được cho bạn !" : firstText;

        initViewText();

        wsClient = new WsClientToSignalingServer(uriToSignalingServer, timeout) {
            @Override
            public void onMessage(String message) {
                handlerTextMessage(message);
            }
        };
        wsClient.connect();


        initializeSurfaceViews();

        initializePeerConnectionFactory();

        createVideoTrackFromCameraAndShowIt();
        createAudioTrackFromMicrophone();

        initializePeerConnections();
    }
//
//
//    private void addIceServer(String iceServer) { // "stun:stun.l.google.com:19302"
//        this.iceServers.add(PeerConnection.IceServer.builder(iceServer).setUsername("thuyth2").setPassword("1qaz").createIceServer());
//        Log.d(TAG, "Add Ice Server " + iceServer);
//    }

    private void initViewText() {
        activity.runOnUiThread(() -> {
            recyclerView = activity.findViewById(R.id.chatRecyclerView);
            progressBar = activity.findViewById(R.id.progressBar);
            gifImageView = activity.findViewById(R.id.mic);
            Log.d(TAG, "progressBar " + progressBar);
            ArrayList<Message> messages = new ArrayList<>();
            MessageAdapter messageAdapter = new MessageAdapter(messages);
            recyclerView.setVisibility(RecyclerView.VISIBLE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(messageAdapter);
            if (firstText != null && !firstText.isEmpty()) {
                messages.add(new Message(firstText, Message.Type.BOT));
            }
        });
    }

    private void initializeSurfaceViews() {
        this.rootEglBase = EglBase.create();
        // Setup the local video track to be displayed in a SurfaceViewRenderer.
        this.localViewRender = activity.findViewById(R.id.local_video_view);
        this.localViewRender.init(this.rootEglBase.getEglBaseContext(), null);
        this.localViewRender.setEnableHardwareScaler(true);
        this.localViewRender.setZOrderMediaOverlay(true);
        this.localViewRender.setMirror(true);
        Log.d(TAG, "----> Create Local View Render");

        // Setup the remote video track to be displayed in a SurfaceViewRenderer.
        this.remoteViewViewRender = activity.findViewById(R.id.remote_video_view);
        this.remoteViewViewRender.init(this.rootEglBase.getEglBaseContext(), null);
        this.remoteViewViewRender.setEnableHardwareScaler(true);
        this.remoteViewViewRender.setZOrderMediaOverlay(true);
        this.remoteViewViewRender.setMirror(true);
        Log.d(TAG, "----> Create Remote View Render");
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(activity.getApplicationContext())
                        .createInitializationOptions();

        PeerConnectionFactory.initialize(initializationOptions);
        // Create a new PeerConnectionFactory instance.
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext(), true, true);
        VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

        this.factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        Log.d(TAG, "----> Create PeerConnectionFactory");
    }

    private void createVideoTrackFromCameraAndShowIt() {
        MediaUtils mediaUtils = new MediaUtils(activity);
        VideoSource videoSource = this.factory.createVideoSource(false);
        VideoCapturer videoCapturer = mediaUtils.createCameraCapturer(new Camera1Enumerator(false));

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
        videoCapturer.initialize(surfaceTextureHelper, activity.getApplicationContext(), videoSource.getCapturerObserver());
        Log.d(TAG, "VideoCapturer initialized");

        if (videoResolutionWidth <= 0 || videoResolutionHeight <= 0 || fps <= 0) {
            videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
            Log.d(TAG, "VideoCapturer started with resolution default " + VIDEO_RESOLUTION_WIDTH + "x" + VIDEO_RESOLUTION_HEIGHT + ", fps=" + FPS);
        } else {
            videoCapturer.startCapture(videoResolutionWidth, videoResolutionHeight, fps);
            Log.d(TAG, "VideoCapturer started with resolution " + videoResolutionWidth + "x" + videoResolutionHeight + ", fps=" + fps);

        }

        this.videoTrackFromCamera = this.factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        this.videoTrackFromCamera.setEnabled(true);
        Log.d(TAG, "Create video track from camera");

        this.videoTrackFromCamera.addSink(localViewRender);
        Log.d(TAG, "Video Track is assigned to Local View Render");
    }

    private void createAudioTrackFromMicrophone() {
        AudioSource audioSource = this.factory.createAudioSource(new MediaConstraints());
        this.audioTrackFromMicrophone = this.factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        this.audioTrackFromMicrophone.setEnabled(true);
    }

    private void initializePeerConnections() {
        this.localPeerConnection = createPeerConnection(factory, true, "local-connection");
        this.remotePeerConnection = createPeerConnection(factory, false, "remote-connection");
    }

    private void handlerTextMessage(String message) {
        try {
            Log.d(TAG, "[handlerTextMessage::onTextReceived] : " + message);
            JSONObject obj = new JSONObject(message);
            String id = obj.getString("id");
            switch (id) {
                case "PROCESS_SDP_ANSWER":
                    String sdpAnswer = obj.getString("sdpAnswer");
                    localPeerConnection.setRemoteDescription(new SimpleSdpObserver("local-peer-connection setRemoteDescription ANSWER"),
                            new SessionDescription(SessionDescription.Type.ANSWER, sdpAnswer));
                    Log.d(TAG, "[connectToSignallingServer::onTextReceived] Local Peer Connection set remote description");
                    break;
                case "ADD_ICE_CANDIDATE":
                    JSONObject candidateJson = new JSONObject(obj.getString("candidate"));
                    String sdpMid = candidateJson.getString("sdpMid");
                    int sdpMLineIndex = candidateJson.getInt("sdpMLineIndex");
                    String sdp = candidateJson.getString("candidate");
                    IceCandidate candidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);
                    remotePeerConnection.addIceCandidate(candidate);
                    Log.d(TAG, "remote-peer-connection add candidate");
                    break;
                case "ADD_MESSAGE":
                    activity.runOnUiThread(() -> {
                        try {
                            viewMessage(obj);
                        } catch (JSONException e) {
                            Log.e(TAG, "Add message to recycler view error " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                    break;
                case "OPEN_SPEECH":
                    invisibleProgressBar();
                    visibleMic();
                    break;
                case "CLOSE_SPEECH":
                    invisibleProgressBar();
                    invisibleMic();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "[handlerTextMessage::onTextReceived] process error" + e.getMessage());
        }
    }

    private void viewMessage(JSONObject message) throws JSONException {
        String type = message.getString("type");
        String content = message.getString("content");
        MessageAdapter messageAdapter = (MessageAdapter) recyclerView.getAdapter();
        if ("user".equals(type)) {
            messageAdapter.addMessage(new Message(content, Message.Type.USER));
        } else if ("bot".equals(type)) {
            messageAdapter.addMessage(new Message(content, Message.Type.BOT));
        }
        messageAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        Log.d(TAG, "ViewMessage type : " + type);
    }

    private void invisibleProgressBar() {
        activity.runOnUiThread(() -> {
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void invisibleMic() {
        activity.runOnUiThread(() -> {
            if (gifImageView.getVisibility() == View.VISIBLE) {
                gifImageView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void visibleMic() {
        activity.runOnUiThread(() -> {
            if (gifImageView.getVisibility() == View.INVISIBLE) {
                gifImageView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void offer() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(audioTrackFromMicrophone);
        this.localPeerConnection.addStream(mediaStream);

        // create offer from local peer connection
        this.localPeerConnection.createOffer(new SimpleSdpObserver("local-peer-connection create offer") {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                Log.d(TAG, "[generateOffer::onCreateSuccess] localPeerConnection " + sdp);
                // set info sdp
                localPeerConnection.setLocalDescription(new SimpleSdpObserver("local-peer-connection onCreateSuccess"), sdp);
                remotePeerConnection.setRemoteDescription(new SimpleSdpObserver("remote-peer-connection onCreateSuccess") {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        super.onCreateSuccess(sessionDescription);
                        remotePeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                        localPeerConnection.setRemoteDescription(new SimpleSdpObserver(), sessionDescription);
                        Log.d(TAG, "remotePeerConnection setRemoteDescription onCreateSuccess");
                    }
                }, sdp);

                //  gathering
                answer();

                // send offer to signaling server
                try {
                    JSONObject message = new JSONObject();
                    message.put("id", "PROCESS_SDP_OFFER");
                    message.put("sdpOffer", sdp.description);

                    JSONObject mediaConfig = new JSONObject();

                    JSONObject audio = new JSONObject();
                    audio.put("sampleRate", 16000);
                    mediaConfig.put("audio", audio);
                    mediaConfig.put("video", false);
                    message.put("mode", mediaConfig);
//                    message.put("info", userInfo);

                    Log.d(TAG, "MediaConfig : " + mediaConfig);
                    wsClient.sendMessage(message.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "[generateOffer::onCreateSuccess] error " + e.getMessage());
                }
            }
        }, mediaConstraints);
    }

    public void answer() {
        this.remotePeerConnection.createAnswer(new SimpleSdpObserver("remote-peer-connection create answer") {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                remotePeerConnection.setLocalDescription(new SimpleSdpObserver("remote-peer-connection setLocalDescription"), sdp);
            }
        }, new MediaConstraints());
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory, boolean isLocal, String label) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        PeerConnection peerConnection = factory.createPeerConnection(rtcConfig, new PeerObserverImpl(label) {
                    @Override
                    public void onIceCandidate(IceCandidate iceCandidate) {
                        super.onIceCandidate(iceCandidate);

                        // send information local candidate
                        if (isLocal) {
                            localPeerConnection.addIceCandidate(iceCandidate);
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("id", "ADD_ICE_CANDIDATE");

                                JSONObject candidate = new JSONObject();
                                candidate.put("candidate", iceCandidate.sdp);
                                candidate.put("sdpMid", iceCandidate.sdpMid);
                                candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);

                                obj.put("candidate", candidate);
                                wsClient.sendMessage(obj.toString());
                                Log.d(TAG, "Local peer connection send candidate");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "onIceCandidate error" + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onAddStream(MediaStream mediaStream) {
                        super.onAddStream(mediaStream);

                        // add media stream to remote peer connection
                        if (!isLocal) {
                            for (VideoTrack videoTrack : mediaStream.videoTracks) {
                                videoTrack.setEnabled(true);
                                videoTrack.addSink(remoteViewViewRender);
                            }
                        }
                    }
                }
        );

        return peerConnection;
    }

}

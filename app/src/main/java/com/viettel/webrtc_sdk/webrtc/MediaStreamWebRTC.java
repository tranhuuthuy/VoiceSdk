package com.viettel.webrtc_sdk.webrtc;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;
import com.viettel.webrtc_sdk.R;
import com.viettel.webrtc_sdk.adapters.MessageAdapter;
import com.viettel.webrtc_sdk.models.Message;
import com.viettel.webrtc_sdk.utils.MicrophoneRunnable;
import com.viettel.webrtc_sdk.utils.UserInfo;
import com.viettel.webrtc_sdk.utils.VideoConfig;

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
import java.util.Arrays;
import java.util.List;

public class MediaStreamWebRTC {
    public static final String TAG = "MediaStreamWebRTC";
    private static final String VIDEO_TRACK_ID = "01";
    private static final String AUDIO_TRACK_ID = "02";
    public static final int VIDEO_RESOLUTION_WIDTH_DEFAULT = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT_DEFAULT = 720;
    public static final int FPS_DEFAULT = 30;
    private VideoConfig videoConfig;
    private String firstText;
    private UserInfo userInfo;

    private Activity activity;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    RoundedImageView buttonKeyBoard;
    EditText inputText;
    AppCompatImageView inputSent;

    ConstraintLayout frameMicrophone;
    ConstraintLayout frameStop;
    MicrophoneRunnable micRunnable;
    TextView textStop;
    private EglBase rootEglBase;
    private List<PeerConnection.IceServer> iceServers;
    private VideoTrack videoTrackFromCamera;
    private AudioTrack audioTrackFromMicrophone;
    private SurfaceViewRenderer localViewRender;
    private SurfaceViewRenderer remoteViewViewRender;
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    private WsClientToSignalingServer wsClient;


    public MediaStreamWebRTC(Activity activity, String uriToSignalingServer, int timeout,
                             VideoConfig videoConfig,
                             UserInfo userInfo, String firstText) throws URISyntaxException{
        this.activity = activity;
        this.iceServers = new ArrayList<>();
        this.videoConfig = videoConfig;
        this.userInfo = userInfo;
        this.firstText = firstText == null || firstText.isEmpty() ? "Xin chào, tôi có thể giúp gì được cho bạn !" : firstText;
        initViewText();

        wsClient = new WsClientToSignalingServer(uriToSignalingServer, timeout) {
            @Override
            public void onMessage(String message) {
                handlerTextMessage(message);
            }

            @Override
            public void close() {
                super.close();
            }
        };


        initializeSurfaceViews();

        initializePeerConnectionFactory();

        if (videoConfig != null) {
            createVideoTrackFromCameraAndShowIt();
        }
        createAudioTrackFromMicrophone();
    }

    private void addIceServer(String iceServer, String user, String password) { // "stun:stun.l.google.com:19302"
        this.iceServers.add(PeerConnection.IceServer.builder(iceServer).setUsername(user).setPassword(password).createIceServer());
        Log.d(TAG, "Add Ice Server " + iceServer);
    }

    private void initViewText() {
        activity.runOnUiThread(() -> {
            recyclerView = activity.findViewById(R.id.chatRecyclerView);
            progressBar = activity.findViewById(R.id.progressBar);
            buttonKeyBoard = activity.findViewById(R.id.keyboard);
            inputText = activity.findViewById(R.id.inputMessage);
            inputSent = activity.findViewById(R.id.sent);
            frameMicrophone = activity.findViewById(R.id.mic_key);
            frameStop = activity.findViewById(R.id.stop_bg);
            textStop = activity.findViewById(R.id.text_stop);
            ArrayList<RoundedImageView> eclipse = new ArrayList<>(Arrays.asList(new RoundedImageView[]{
                    activity.findViewById(R.id.microphone_line),
                    activity.findViewById(R.id.microphone_line_2),
                    activity.findViewById(R.id.microphone_line_3),
            }));

            // check android version
            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            Log.d(TAG, "Android API SDK : " + sdkVersion);
            micRunnable = new MicrophoneRunnable(activity, eclipse,
                    activity.findViewById(R.id.microphone), activity.findViewById(R.id.hintSpeak));
            if (sdkVersion > Build.VERSION_CODES.P) {
                micRunnable.setAudioRecorder(true);
            } else {
                Log.w(TAG, "Android API SDK " + sdkVersion + " <= " + Build.VERSION_CODES.P);
            }
            micRunnable.start();

            initAction();

            ArrayList<Message> messages = new ArrayList<>();
            MessageAdapter messageAdapter = new MessageAdapter(messages);
            recyclerView.setVisibility(VISIBLE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(messageAdapter);
            if (firstText != null && !firstText.isEmpty()) {
                messages.add(new Message(firstText, Message.Type.BOT));
            }
        });
    }

    private void initAction() {
        frameStop.setOnClickListener(v -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", "STOP_PLAYING");
                wsClient.sendMessage(obj.toString());
                frameStop.setVisibility(INVISIBLE);
            } catch (Exception e) {
                Log.e(TAG, "Stop playing error");
            }
        });

//        //action keyboard on/off
//        KeyboardVisibilityEvent.setEventListener(activity, isOpen -> {
//            if (isOpen) {
//                onKeyboardShown();
//            } else {
//                onKeyboardHidden();
//            }
//        });
//
//        buttonKeyBoard.setOnClickListener(v -> {
//            inputText.setVisibility(VISIBLE);
//            inputSent.setVisibility(VISIBLE);
//            frameMicrophone.setVisibility(INVISIBLE);
//            inputText.requestFocus();
//
//            // Show the keyboard
//            InputMethodManager imm = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT);
//        });
    }

    private void onKeyboardShown() {
        // Action to take when the keyboard is shown
        Log.d(TAG, "Keyboard is open");
        inputText.setVisibility(VISIBLE);
        inputSent.setVisibility(VISIBLE);
        frameMicrophone.setVisibility(INVISIBLE);

    }

    private void onKeyboardHidden() {
        // Action to take when the keyboard is hidden
        Log.d(TAG, "Keyboard is hidden");
        inputText.setVisibility(INVISIBLE);
        inputSent.setVisibility(INVISIBLE);
        frameMicrophone.setVisibility(VISIBLE);

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

        if (videoConfig == null || videoConfig.getWidth() <= 0 || videoConfig.getHeight() <= 0 || videoConfig.getFps() <= 0) {
            videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH_DEFAULT, VIDEO_RESOLUTION_HEIGHT_DEFAULT, FPS_DEFAULT);
            Log.d(TAG, "VideoCapturer started with resolution default " + VIDEO_RESOLUTION_WIDTH_DEFAULT + "x" + VIDEO_RESOLUTION_HEIGHT_DEFAULT + ", fps=" + FPS_DEFAULT);
        } else {
            videoCapturer.startCapture(videoConfig.getWidth(), videoConfig.getHeight(), videoConfig.getFps());
            Log.d(TAG, "VideoCapturer started with resolution " + videoConfig.getWidth() + "x" + videoConfig.getHeight() + ", fps=" + videoConfig.getFps());

        }

        this.videoTrackFromCamera = this.factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        this.videoTrackFromCamera.setEnabled(true);
        Log.d(TAG, "Create video track from camera");

        this.videoTrackFromCamera.addSink(localViewRender);
        Log.d(TAG, "Video Track is assigned to Local View Render");
    }

    private void createAudioTrackFromMicrophone() {
        // set external speaker
        Context context = activity.getApplicationContext();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        Log.d(TAG, "Set SpeakerPhone On");
        // --------------------------------------

        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));

//        AudioSource audioSource = this.factory.createAudioSource(new MediaConstraints());
        AudioSource audioSource = this.factory.createAudioSource(audioConstraints);
        this.audioTrackFromMicrophone = this.factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        this.audioTrackFromMicrophone.setEnabled(true);

        Log.d(TAG, "NoiseSuppressor : " + NoiseSuppressor.isAvailable());
        Log.d(TAG, "AcousticEchoCanceler : " + AcousticEchoCanceler.isAvailable());
    }

    private void initializePeerConnections() {
        this.peerConnection = createPeerConnection(factory, true, "Peer-connection");
    }

    private void handlerTextMessage(String message) {
        try {
            Log.d(TAG, "[handlerTextMessage::onTextReceived] : " + message);
            JSONObject obj = new JSONObject(message);
            String id = obj.getString("id");
            switch (id) {
                case "TURN_SERVER":
                    TurnServer turnServer = new TurnServer(
                            obj.getString("host"),
                            obj.getInt("port"),
                            obj.getString("username"),
                            obj.getString("password")
                    );
                    addIceServer(String.format("turn:%s:%d", turnServer.getHost(), turnServer.getPort()), turnServer.getUsername(), turnServer.getPassword());
                    initializePeerConnections();
                    offer();
                case "PROCESS_SDP_ANSWER":
                    String sdpAnswer = obj.getString("sdpAnswer");
                    peerConnection.setRemoteDescription(new SimpleSdpObserver("Peer-connection setRemoteDescription ANSWER"),
                            new SessionDescription(SessionDescription.Type.ANSWER, sdpAnswer));
                    Log.d(TAG, "[connectToSignallingServer::onTextReceived] Peer Connection set remote description");
                    break;
                case "ADD_ICE_CANDIDATE":
                    JSONObject candidateJson = new JSONObject(obj.getString("candidate"));
                    String sdpMid = candidateJson.getString("sdpMid");
                    int sdpMLineIndex = candidateJson.getInt("sdpMLineIndex");
                    String sdp = candidateJson.getString("candidate");
                    IceCandidate candidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);
                    peerConnection.addIceCandidate(candidate);
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
                case "END_ANSWER_MESSAGE":
                    activity.runOnUiThread(() -> frameStop.setVisibility(INVISIBLE));
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
        messageAdapter.removeLastWaitMessage();

        if ("user".equals(type)) {
            messageAdapter.addMessage(new Message(content, Message.Type.USER));
            messageAdapter.addMessage(new Message(content, Message.Type.WAIT_ANSWER));
            activity.runOnUiThread(() -> frameStop.setVisibility(INVISIBLE));
        } else if ("bot".equals(type)) {
            messageAdapter.addMessage(new Message(content, Message.Type.BOT));
            activity.runOnUiThread(() -> frameStop.setVisibility(VISIBLE));
        }
        messageAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        Log.d(TAG, "ViewMessage type : " + type);
    }

    private void invisibleProgressBar() {
        activity.runOnUiThread(() -> {
            if (progressBar.getVisibility() == VISIBLE) {
                progressBar.setVisibility(INVISIBLE);
            }
        });
    }

    private void invisibleMic() {
        micRunnable.setVisible(false);
        if (frameStop.getVisibility() == VISIBLE) {
            activity.runOnUiThread(() -> frameStop.setVisibility(INVISIBLE));
        }
        Log.d(TAG, "Microphone Runnable is set invisible");
    }

    private void visibleMic() {
        micRunnable.setVisible(true);
    }

    private void offer() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        if (videoConfig != null) {
            mediaStream.addTrack(videoTrackFromCamera);
        }
        mediaStream.addTrack(audioTrackFromMicrophone);
        this.peerConnection.addStream(mediaStream);

        // create offer from local peer connection
        this.peerConnection.createOffer(new SimpleSdpObserver("Peer-connection create offer") {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                Log.d(TAG, "[generateOffer::onCreateSuccess] localPeerConnection " + sdp);
                // set info sdp
                peerConnection.setLocalDescription(new SimpleSdpObserver("Peer-connection onCreateSuccess"), sdp);

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
                    mediaConfig.put("video", videoConfig == null ? false : true);
                    message.put("mode", mediaConfig);
                    if (userInfo != null) {
                        Gson gson = new Gson();
                        message.put("userInfo", gson.toJson(userInfo));
                    }

                    Log.d(TAG, "MediaConfig : " + mediaConfig);
                    wsClient.sendMessage(message.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "[generateOffer::onCreateSuccess] error " + e.getMessage());
                }
            }
        }, mediaConstraints);
    }

    public void answer() {
        this.peerConnection.createAnswer(new SimpleSdpObserver("Peer-connection create answer") {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                peerConnection.setRemoteDescription(new SimpleSdpObserver("Peer-connection setLocalDescription"), sdp);
            }
        }, new MediaConstraints());
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory, boolean isLocal, String label) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        PeerConnection peerConn = factory.createPeerConnection(rtcConfig, new PeerObserverImpl(label) {
                    @Override
                    public void onIceCandidate(IceCandidate iceCandidate) {
                        super.onIceCandidate(iceCandidate);

                        // send information local candidate
                        if (isLocal) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("id", "ADD_ICE_CANDIDATE");

                                JSONObject candidate = new JSONObject();
                                candidate.put("candidate", iceCandidate.sdp);
                                candidate.put("sdpMid", iceCandidate.sdpMid);
                                candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);

                                obj.put("candidate", candidate);
                                wsClient.sendMessage(obj.toString());
                                Log.d(TAG, "Peer connection send candidate");
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

        return peerConn;
    }

    public void connect() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        wsClient.connect();
    }
}

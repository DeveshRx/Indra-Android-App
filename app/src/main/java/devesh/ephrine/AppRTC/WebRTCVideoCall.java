package devesh.ephrine.AppRTC;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
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
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import devesh.common.utils.CachePref;
import devesh.common.utils.EpochLib;
import devesh.ephrine.BuildConfig;
import devesh.ephrine.R;
import devesh.ephrine.util.AppAnalytics;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebRTCVideoCall extends Activity {

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    public static final int VIDEO_RESOLUTION_WIDTH = 1920;
    public static final int VIDEO_RESOLUTION_HEIGHT = 1080;
    public static final int FPS = 60;
    final String TAG = "WebRTCVideoCall: ";
    OkHttpClient client;
    String CallReceiverID;
    String CallerType;
    FirebaseDatabase database;
    boolean isHost;
    String URL_CallNow;
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    Gson gson;
    FirebaseUser fUser;
    FirebaseAuth mAuth;
    LinearLayout LLCallingLayer;
    // ImageView CallingIMG;
    TextView CallingNameTextView;
    TextView CallingStatusTextView;
    String CallerUserName = null;
    String CallerPhoneNo = null;
    boolean isMyKeyprefSSProtected;

    EglBase rootEglBase;
    PeerConnectionFactory factory;
    // AudioManager audioManager;
    PeerConnection peerConnection;

    MediaConstraints audioConstraints;
    VideoTrack videoTrackFromCamera;
    AudioSource audioSource;
    AudioTrack localAudioTrack;
    VideoCapturer videoCapturer;
    List<String> STUNList = Arrays.asList(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302",
            "stun:stun3.l.google.com:19302",
            "stun:stun4.l.google.com:19302",
            "stun:stun.vodafone.ro:3478",
            "stun:stun.samsungsmartcam.com:3478",
            "stun:stun.services.mozilla.com:3478",
            "stun:global.stun.twilio.com:3478?transport=udp"

    );

    List<String> TURNList = Arrays.asList(
            "turn:global.turn.twilio.com:3478?transport=udp",
            "turn:global.turn.twilio.com:3478?transport=tcp",
            "turn:global.turn.twilio.com:443?transport=tcp"
    );

    String TwilioUsername;
    String TwilioPassword;
    boolean useTURN_Server;
    boolean UserTURN_Config=true;


    DatabaseReference SendData;

    SurfaceViewRenderer remoteSurfaceView;
    SurfaceViewRenderer localSurfaceView;
    String PeerID = "room01";
    CachePref cachePref;
    DatabaseReference RootDB;
    ImageView GreenScreenImageView;

    // FloatingActionButton VideoMuteFAB;
    // FloatingActionButton MicMuteFAB;
    // FloatingActionButton CamSwitchFAB;

    boolean isVideoEnabled;
    boolean isAudioEnabled;
    boolean isCallConnected;
    boolean isDataSaverEnabled;
    TextView AudioMuteMSG;
    TextView VideoMuteMSG;
    boolean isFrontCamActive;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    ValueEventListener OfferValListner;
    ValueEventListener CandidateValListner;
    ValueEventListener AnswerValListner;
    ValueEventListener RootDBValListner;
    DatabaseReference OfferDB;
    DatabaseReference CandidateDB;
    DatabaseReference AnswerDB;
    AppRTCAudioManager audioManager;
    FirebaseAnalytics mFirebaseAnalytics;
    FloatingActionButton PipModeFAB;
    boolean isAppAlreadyOpen;
    String IS_APP_ALREADY_OPEN = "isAppAlreadyOpen";
    Ringtone ringtone;
    boolean isPictureInPictureModeEnabled = false;
    boolean CallReceiverSSProtected = true;
    AppAnalytics appAnalytics;
    LinearLayout LLController;
    NotificationManagerCompat notificationManagerCompat;

    LottieAnimationView loadingAnimationView;

    CardView AlertCardView;
    TextView AlertCVText;
    boolean isActivityRunnning;
    String CurrentUserStatus;
    DatabaseReference UserStatusRef;


    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity CREATED");

        AppCenter.start(getApplication(), getString(R.string.MS_AppCenter_API_Key), Analytics.class, Crashes.class);

        appAnalytics = new AppAnalytics(getApplication(), this);
        if (savedInstanceState != null) {
            isAppAlreadyOpen = savedInstanceState.getBoolean(IS_APP_ALREADY_OPEN);
        } else {
            isAppAlreadyOpen = false;
        }
        setContentView(R.layout.activity_apprtc_call);
        cachePref = new CachePref(this);
        useTURN_Server = cachePref.getBoolean(getString(R.string.Pref_UseTurnServer));
     //   UserTURN_Config = cachePref.getBoolean(getString(R.string.Pref_UserConfig_TurnServer));
        TwilioUsername = cachePref.getString(getString(R.string.Pref_Twilio_UN));
        TwilioPassword = cachePref.getString(getString(R.string.Pref_Twilio_PW));

        URL_CallNow = getString(R.string.URL_CallNow);

        Intent intent = getIntent();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());

        database = FirebaseDatabase.getInstance();
        if (!isAppAlreadyOpen) {

            remoteSurfaceView = findViewById(R.id.fullscreen_video_view);
            localSurfaceView = findViewById(R.id.pip_video_view);

        }

        CallingNameTextView = findViewById(R.id.CallingNameTextView2);
        CallingStatusTextView = findViewById(R.id.CallingStatustextView3);
        LLCallingLayer = findViewById(R.id.LLCallingLayer);
        LLController = findViewById(R.id.LLController);
        GreenScreenImageView = findViewById(R.id.GreenScreenImageView);
        AudioMuteMSG = findViewById(R.id.MicMuteTextView);
        VideoMuteMSG = findViewById(R.id.videoMuteTextView);
        loadingAnimationView = findViewById(R.id.loadingAnimationView);
        AlertCardView = findViewById(R.id.AlertCardView);
        AlertCVText = findViewById(R.id.AlertCVText);

        AlertCardView.setVisibility(View.GONE);
        if (!isAppAlreadyOpen) {
            GreenScreenImageView.setVisibility(View.VISIBLE);
        } else {
            GreenScreenImageView.setVisibility(View.GONE);
            LLCallingLayer.setVisibility(View.GONE);
        }


        PipModeFAB = findViewById(R.id.PipFAB);

        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();
        gson = new Gson();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        isDataSaverEnabled = cachePref.getBoolean(getString(R.string.settings_data_saver));


        // audioManager = ((AudioManager)getSystemService(Context.AUDIO_SERVICE));
        setAudioWebRTCConfig();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(120, TimeUnit.SECONDS);
        builder.readTimeout(120, TimeUnit.SECONDS);
        builder.writeTimeout(120, TimeUnit.SECONDS);
        // builder.followSslRedirects(true);
        //   builder.followRedirects(true);
        //  builder.dns(new DNSConfig());
        //   builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));
        if (!BuildConfig.DEBUG) {
            builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));
        }

        client = builder.build();

        CallerType = intent.getStringExtra(getString(R.string.call_type));

        if (CallerType.equals("0")) {
            isHost = true;

        } else if (CallerType.equals("1")) {
            isHost = false;
        }

        CallReceiverID = intent.getStringExtra(getString(R.string.WebRTC_CallReceiverID));
        PeerID = intent.getStringExtra(getString(R.string.WebRTC_PEER_ID));

        if (intent.getStringExtra(getString(R.string.intent_Caller_User_Name)) != null) {
            CallerUserName = intent.getStringExtra(getString(R.string.intent_Caller_User_Name));
            CallingNameTextView.setText(CallerUserName);
        }

        if (intent.getStringExtra(getString(R.string.intent_Caller_Phone)) != null) {
            CallerPhoneNo = intent.getStringExtra(getString(R.string.intent_Caller_Phone));
        }

        CallingStatusTextView.setText("Calling..");

        isMyKeyprefSSProtected = cachePref.getBoolean(getString(R.string.settings_screenshot_protection));

        String ssp = null;
        if (intent.getStringExtra(getString(R.string.WebRTC_SS_Protect)) != null) {
            ssp = intent.getStringExtra(getString(R.string.WebRTC_SS_Protect));
            if (ssp.equals("1")) {
                ProtectScreenShot();
            }
        }


        if(!isHost){
            if(intent.getStringExtra(getString(R.string.intent_twilio_un))!=null){
                TwilioUsername=intent.getStringExtra(getString(R.string.intent_twilio_un));
            }


            if(intent.getStringExtra(getString(R.string.intent_twilio_pw))!=null){
                TwilioPassword= intent.getStringExtra(getString(R.string.intent_twilio_pw));
            }

            Log.d(TAG, "onCreate: New Twilio TURN Config set TwilioUsername: "+TwilioUsername+"\nTwilioPassword: "+TwilioPassword);
        }

        RootDB = database.getReference("WebRTC/" + PeerID);


        if (!isAppAlreadyOpen) {

            isAudioEnabled = true;
            isVideoEnabled = true;
            isFrontCamActive = true;
            isCallConnected = false;

            OfferValListner = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue(String.class) != null) {
                        String value = dataSnapshot.getValue(String.class);
                        try {
                            JSONObject message = new JSONObject(value);
                            Log.d(TAG, "OFFER JSON: " + message.toString());

                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("sdp")));
                            doAnswerRun();
                        } catch (JSONException err) {
                            Log.d("Error", err.toString());
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            };
            CandidateValListner = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.getValue(String.class) != null) {
                                String value = ds.getValue(String.class);
                                try {
                                    JSONObject message = new JSONObject(value);
                                    Log.d(TAG, "CandidateDBListner(); " + message);

                                    IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
                                    peerConnection.addIceCandidate(candidate);
                                } catch (JSONException err) {
                                    Log.d("Error", err.toString());
                                }
                            }
                        }
                    }


                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            };
            AnswerValListner = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue(String.class) != null) {
                        String value = dataSnapshot.getValue(String.class);
                        Log.d(TAG, "Value is: " + value);
                        try {
                            JSONObject message = new JSONObject(value);
                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("sdp")));

                        } catch (JSONException err) {
                            Log.d("Error", err.toString());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            };
            RootDBValListner = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        disconnectCall(false);
                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            };


            initializeremoteSurfaceViews();
            initializePeerConnectionFactory();
            createVideoTrackFromCameraAndShowIt();

            initializePeerConnections();
            startStreamingVideo();
            Map<String, String> adata2 = new HashMap<>();

            if (isHost) {
                doCall();
                sendCallRequest();
                adata2.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), "WebRTC: isHost: TRUE");

            } else {
                doAnswer();
                adata2.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), "WebRTC: isHost:  FALSE");
            }
            setCallCancelTimer();

            Map<String, String> adata = new HashMap<>();
            adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW), "WebRTC Video Call Screen");
            appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW), adata);

            appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), adata2);


        }

       /* if (isAppAlreadyOpen) {
            if (isPictureInPictureModeEnabled) {
                pipModeEnableViews();
            } else {
                pipModeDisableViews();
            }
        }*/

        SetActiveNotification();
        isAppAlreadyOpen = true;

        UserStatusRef = database.getReference("users/" + fUser.getUid() + "/indra/UserStatus");

        CurrentUserStatus = getString(R.string.USER_STATUS_AVAILABLE);

        setOnCallUserStatus();
      /*     remoteSurfaceView.setOnClickListener(view -> {

          if (isCallConnected) {
               if (LLController.getVisibility() == View.VISIBLE) {
                    LLController.setVisibility(View.INVISIBLE);
                } else {
                    LLController.setVisibility(View.VISIBLE);
                }
            }

        });
*/

        // PlayCallingTone();

  /*      if(android.os.Build.VERSION.SDK_INT >=  android.os.Build.VERSION_CODES.N){
            PipModeFAB.setVisibility(View.VISIBLE);
            HomeWatcher mHomeWatcher = new HomeWatcher(this);
            mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onHomePressed() {
                    // do something here...
                    pipEnable();
                }
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onHomeLongPressed() {
                    pipEnable();
                }
            });
            mHomeWatcher.startWatch();

        }else{
            PipModeFAB.setVisibility(View.GONE);
        }
*/

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //  textView.setText(savedInstanceState.getString(TEXT_VIEW_KEY));
        Log.d(TAG, "onRestoreInstanceState: onRestoreInstanceState");
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_APP_ALREADY_OPEN, true);
        //  outState.putString(TEXT_VIEW_KEY, textView.getText());
        Log.d(TAG, "onSaveInstanceState: onSaveInstanceState");
        // call superclass to save any view hierarchy

        Map<String, String> adata = new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS), "WebRTC Video Call: onSaveInstanceState");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS), adata);


        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // StopCallingTone();

        if(isCallConnected){
            cachePref.setBoolean(getString(R.string.Pref_EligibleForRating),true);
        }

        notificationManagerCompat.cancel(10);
        removeOnCallUserStatus();

        isActivityRunnning = false;
        disconnectCall(true);
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ///pipEnable();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        isActivityRunnning = true;
        Log.d(TAG, "onResume: ");
    }

    private void initializeremoteSurfaceViews() {
        rootEglBase = EglBase.create();
        remoteSurfaceView.init(rootEglBase.getEglBaseContext(), null);
        remoteSurfaceView.setEnableHardwareScaler(true);
        // remoteSurfaceView.setMirror(true);

        localSurfaceView.init(rootEglBase.getEglBaseContext(), null);
        localSurfaceView.setEnableHardwareScaler(true);
        localSurfaceView.setMirror(true);


    }

    private void initializePeerConnectionFactory() {
        AudioDeviceModule audioDeviceModule = JavaAudioDeviceModule.builder(this)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .createAudioDeviceModule();


        VideoEncoderFactory encoderFactory;
        VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, false);
        decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());


        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions());
        factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .setAudioDeviceModule(audioDeviceModule)
                .createPeerConnectionFactory();

    }

    private void createVideoTrackFromCameraAndShowIt() {

        //VideoCapturer videoCapturer = createVideoCapturer();
        videoCapturer = getFrontCam();

        //VideoSource videoSource=null;
        //Create a VideoSource instance
        VideoSource videoSource;
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
        videoSource = factory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());


        VideoEncoderFactory videoEncoderFactory =
                new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext()
                        , true, true);
        for (int i = 0; i < videoEncoderFactory.getSupportedCodecs().length; i++) {
            Log.d(TAG, "Supported codecs: " + videoEncoderFactory.getSupportedCodecs()[i].name);
        }

        videoTrackFromCamera = factory.createVideoTrack("100", videoSource);

        WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(true);


        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = new MediaConstraints();

        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation2", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googDAEchoCancellation", "true"));

        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googTypingNoiseDetection", "true"));

        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl2", "true"));

        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression2", "true"));

        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAudioMirroring", "false"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));


        //create an AudioSource instance
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("101", audioSource);

        if (videoCapturer != null) {
            //   videoCapturer.startCapture(1024, 720, 30);
            if (isDataSaverEnabled) {
                videoCapturer.startCapture(640, 480, FPS);
            } else {
                //videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
                videoCapturer.startCapture(1920, 1080, 50);
            }


        }

        videoTrackFromCamera.setEnabled(isVideoEnabled);

        videoTrackFromCamera.addSink(localSurfaceView);


    }

    private void initializePeerConnections() {
        peerConnection = createPeerConnection(factory);

    }

    private void startStreamingVideo() {
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(localAudioTrack);
        peerConnection.addStream(mediaStream);

        //   sendMessage("got user media");
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        Logging.d(TAG, "Creating capturer using camera1 API.");
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        Log.d(TAG, "createCameraCapturer: Camera List:\n" + deviceNames);

        for (String deviceName : deviceNames) {
            Log.d(TAG, "createCameraCapturer: deviceName: " + deviceName);
        }

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private VideoCapturer getBackCam() {
        CameraEnumerator enumerator;
        if (useCamera2()) {
            enumerator = new Camera2Enumerator(this);
        } else {
            enumerator = new Camera1Enumerator(true);
        }

        final String[] deviceNames = enumerator.getDeviceNames();

        Log.d(TAG, "createCameraCapturer: Camera List:\n" + deviceNames.toString());
        // First, try to find front facing camera
        Logging.d(TAG, "Looking for back cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                Logging.d(TAG, "Creating back camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isBackFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private VideoCapturer getFrontCam() {
        CameraEnumerator enumerator;
        if (useCamera2()) {
            enumerator = new Camera2Enumerator(this);
        } else {
            enumerator = new Camera1Enumerator(true);
        }

        final String[] deviceNames = enumerator.getDeviceNames();

        Log.d(TAG, "createCameraCapturer: Camera List:\n" + deviceNames.toString());
        // First, try to find front facing camera
        Logging.d(TAG, "Looking for back cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating back camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        Log.d(TAG, "createPeerConnection: ");
        //==
        // Add ICE Servers
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();

        // STUN 1
//        PeerConnection.IceServer.Builder iceServerBuilder = PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302");
        //      iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK); //this does the magic.
        //    PeerConnection.IceServer iceServer =  iceServerBuilder.createIceServer();

        for (String i : STUNList) {
            PeerConnection.IceServer.Builder iceServerBuilder = PeerConnection.IceServer.builder(i);
            iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK); //this does the magic.
            PeerConnection.IceServer iceServer = iceServerBuilder.createIceServer();
            iceServers.add(iceServer);
        }

        // Add TURN Server

        Log.d(TAG, "createPeerConnection: useTURN_Server: " +useTURN_Server
                +" UserTURN_Config: "+UserTURN_Config);

        if (useTURN_Server) {
            if (UserTURN_Config) {
                for (String i : TURNList) {
                    PeerConnection.IceServer.Builder iceServerBuilder = PeerConnection.IceServer.builder(i).setUsername(TwilioUsername).setPassword(TwilioPassword);
                    iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK); //this does the magic.
                    PeerConnection.IceServer iceServer = iceServerBuilder.createIceServer();
                    iceServers.add(iceServer);
                }
            }
        }


        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                /*
                 * HAVE_LOCAL_OFFER
                 * HAVE_REMOTE_OFFER
                 */
                Log.d(TAG, "onSignalingChange: " + signalingState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: " + iceConnectionState);
                ConnectionStatus(iceConnectionState.toString());

            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange: " + b);
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: " + iceGatheringState);
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: " + iceCandidate);
                JSONObject message = new JSONObject();

                try {
                    message.put("type", "candidate");
                    message.put("label", iceCandidate.sdpMLineIndex);
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);

                    Log.d(TAG, "onIceCandidate: sending candidate " + message);
                    SendData2DB(message);
                    Log.d(TAG, "onIceCandidate: " + message.toString());
                } catch (JSONException e) {

                    Log.e(TAG, "onIceCandidate ERROR: " + e);
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: " + iceCandidates);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);

                remoteAudioTrack.setEnabled(true);
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addSink(remoteSurfaceView);

                // remoteAudioTrack.setVolume(10); // 0 - 10

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }

            @Override
            public void onStandardizedIceConnectionChange(PeerConnection.IceConnectionState newState) {
                Log.d(TAG, "onStandardizedIceConnectionChange: " + newState.toString());
            }
        };

        return factory.createPeerConnection(rtcConfig, pcObserver);
    }

    public void doCall() {
        Log.d(TAG, "doCall: ");
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "onCreateSuccess: ");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    SendData2DB(message);
                    Log.d(TAG, "onCreateSuccess: " + message.toString());
                    CandidateDBListner();
                } catch (JSONException e) {

                    Log.e(TAG, "onCreateSuccess ERROR: " + e);
                }
            }
        }, sdpMediaConstraints);

        AnswerDBListner();
    }

    public void doAnswer() {
        OfferDBListner();

    }

    public void doAnswerRun() {
        Log.d(TAG, "doAnswer: ");

        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    SendData2DB(message);
                    Log.d(TAG, "onCreateSuccess: " + message.toString());
                    CandidateDBListner();
                } catch (JSONException e) {
                    Log.e(TAG, "onCreateSuccess ERROR: " + e);
                }
            }
        }, new MediaConstraints());// new MediaConstraints()
    }

    void SendData2DB(JSONObject message) {
        // Create a new user with a first and last name
        String type = "unknown";

        Map<String, Object> data = new HashMap<>();
        try {
            type = message.get("type").toString();
            data.put(type, message.toString());
            if (type.equals("offer")) {
            }
            if (type.equals("answer")) {
            }
            if (type.equals("candidate")) {
            }
            Log.d(TAG, "SendData2DB: " + type);

            if (type.equals("candidate")) {
                SendData = database.getReference("WebRTC/" + PeerID + "/candidate");
                SendData.push().setValue(message.toString());
            } else {
                SendData = database.getReference("WebRTC/" + PeerID);
                SendData.updateChildren(data);
            }

            //  SendData.setValue(message.toString());


        } catch (Exception e) {
            Log.e(TAG, "SendData2DB: " + e);
        }


    }

    void OfferDBListner() {
        OfferDB = database.getReference("WebRTC/" + PeerID + "/offer");
        OfferDB.addValueEventListener(OfferValListner);
    }

    void CandidateDBListner() {
        CandidateDB = database.getReference("WebRTC/" + PeerID + "/candidate");
        CandidateDB.addValueEventListener(CandidateValListner);
    }

    void AnswerDBListner() {
        AnswerDB = database.getReference("WebRTC/" + PeerID + "/answer");
        AnswerDB.addValueEventListener(AnswerValListner);
    }

    public void sendCallRequest() {
        executorService.execute(new Runnable() {
            String ServerResData = null;
            HashMap<String, String> ResDataMap;

            @Override
            public void run() {
                EpochLib epochLib = new EpochLib();

                HashMap<String, String> dataHash = new HashMap<>();
                dataHash.put("fromUID", fUser.getUid());
                dataHash.put("toUID", CallReceiverID);
                dataHash.put("SessionId", PeerID);
                if (isMyKeyprefSSProtected) {
                    dataHash.put("SSProtected", "1");
                } else {
                    dataHash.put("SSProtected", "0");
                }
                dataHash.put("EPOCH", String.valueOf(epochLib.getEPOCH()));
                if (fUser.getDisplayName() != null) {
                    dataHash.put("UserName", fUser.getDisplayName());
                } else {
                    dataHash.put("UserName", fUser.getPhoneNumber());
                }
                dataHash.put("PhoneNo", fUser.getPhoneNumber());
                dataHash.put("voip", "0");

                if(useTURN_Server & UserTURN_Config){
                    dataHash.put("twilioun",TwilioUsername);
                    dataHash.put("twiliopw",TwilioPassword);
                }else{
                    dataHash.put("twilioun","x");
                    dataHash.put("twiliopw","x");
                }

                String jsonBody = gson.toJson(dataHash);

                try {
                    Log.d(TAG, "run: JSON Body: " + jsonBody);
                    ServerResData = post(URL_CallNow, jsonBody);
                    Log.d(TAG, "run: OKHTTP Received: " + ServerResData);

                    ResDataMap = new HashMap<>();
                    Type collectionType = new TypeToken<HashMap<String, String>>() {
                    }.getType();

                    ResDataMap = gson.fromJson(ServerResData, collectionType);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            if (ResDataMap.get("screen_protected").equals("1")) {
                                //Screen Protected
                                ProtectScreenShot();
                                CallReceiverSSProtected = true;
                            } else {
                                //Screen Not Protected
                                CallReceiverSSProtected = false;

                            }

                            String user_status = ResDataMap.get("user_status");
                            if (user_status.equals("ONCALL")) {
                                Toast.makeText(WebRTCVideoCall.this, "Person is Already on Another Call", Toast.LENGTH_LONG).show();
                                showAlertCard("Person is Already on Another Call");

                            } else if (user_status.equals("BUSY")) {
                                Toast.makeText(WebRTCVideoCall.this, "Person is Busy, Try Again Later", Toast.LENGTH_LONG).show();
                                showAlertCard("Person is Busy, Try Again Later !");

                            } else if (user_status.equals("DND")) {
                                Toast.makeText(WebRTCVideoCall.this, "Person is Unavailable at this moment. try again later", Toast.LENGTH_LONG).show();
                                showAlertCard("Person is Unavailable at this moment. try again later");

                            }

                            if (ResDataMap.get("isblocked").equals("YES")) {
                                Toast.makeText(WebRTCVideoCall.this, "This Person has Blocked you :(", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "run: ", e);
                }


            }

            String post(String url, String json) throws IOException {
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    return response.body().string();
                }
            }
        });
    }

    void ProtectScreenShot() {
        Log.d(TAG, "ProtectScreenShot()");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
    }

    void ConnectionStatus(String s) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (s.equals("CONNECTED")) {
                        //StopCallingTone();
                        // Toast.makeText(WebRTCVideoCall.this, "CONNECTED", Toast.LENGTH_SHORT).show();
                        LLCallingLayer.setVisibility(View.GONE);
                        GreenScreenImageView.setVisibility(View.GONE);
                        isCallConnected = true;
                        setAutoCallDisconnect();
                        try{
                            OfferDB.removeEventListener(OfferValListner);
                        }catch (Exception e){
                            Log.e(TAG, "run: error: "+e );
                        }

                        try{
                            AnswerDB.removeEventListener(AnswerValListner);
                        }catch (Exception e){
                            Log.e(TAG, "run: error: "+e );
                        }


                       /* if (LLController.getVisibility() == View.VISIBLE) {
                            LLController.setVisibility(View.INVISIBLE);
                        }*/

                        try {

                            loadingAnimationView.pauseAnimation();
                            loadingAnimationView.cancelAnimation();
                            loadingAnimationView.setVisibility(View.GONE);

                        } catch (Exception e) {
                            Log.e(TAG, "run: ERROR #635476 " + e);
                        }

                    }
                } catch (Exception e) {
                    Log.e(TAG, "ConnectionStatus: " + e);
                }

                Map<String, String> adata = new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS), "WebRTC Conn Status: " + s);
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS), adata);


            }
        });


    }

    public void disconnectCallButton(View v) {
        Map<String, String> adata = new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), "WebRTC call disconnect btton");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), adata);

        disconnectCall(false);
    }

    void disconnectCall(boolean onDestroyAct) {
        if (RootDB != null) {
            RootDB.removeValue();
        }

        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (Exception e) {
                Log.e(TAG, "disconnectCall: " + e);
            }
        }

        peerConnection.close();

        if (isActivityRunnning) {
            Toast.makeText(this, "Call Ended", Toast.LENGTH_SHORT).show();
        }

        if (RootDB != null) {
            if (RootDBValListner != null) {
                RootDB.removeEventListener(RootDBValListner);
            }
        }

        if (CandidateDB != null) {
            if (CandidateValListner != null) {
                CandidateDB.removeEventListener(CandidateValListner);
            }
        }

        isCallConnected = false;
        removeOnCallUserStatus();
        if (!onDestroyAct) {
            finish();
        }

    }

    public void toggleMic(View v) {
        if (isAudioEnabled) {
            isAudioEnabled = false;
            AudioMuteMSG.setVisibility(View.VISIBLE);


        } else {
            isAudioEnabled = true;
            AudioMuteMSG.setVisibility(View.GONE);

        }
        localAudioTrack.setEnabled(isAudioEnabled);

        // localAudioTrack.setVolume(0.0);
        //  localAudioTrack.setVolume(100);
        Map<String, String> adata = new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), "WebRTC Toggle Mic: " + isAudioEnabled);
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), adata);


    }

    public void toggleVideo(View v) {
        if (isVideoEnabled) {
            isVideoEnabled = false;
            VideoMuteMSG.setVisibility(View.VISIBLE);


        } else {
            isVideoEnabled = true;
            VideoMuteMSG.setVisibility(View.GONE);


        }
        videoTrackFromCamera.setEnabled(isVideoEnabled);

        Map<String, String> adata = new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), "WebRTC Toggle Video: " + isVideoEnabled);
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), adata);

    }

    public void CamSwitch(View v) {
        if (isFrontCamActive) {
            localSurfaceView.setMirror(true);
            //  remoteSurfaceView.setMirror(true);
            isFrontCamActive = false;

        } else {
            localSurfaceView.setMirror(false);
            //remoteSurfaceView.setMirror(false);
            isFrontCamActive = true;
        }

        executor.execute(() -> {
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
            cameraVideoCapturer.switchCamera(null);
        });

        Map<String, String> adata = new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), "WebRTC CamSwitch: isFrontCamActive:" + isFrontCamActive);
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), adata);


    }

    void setCallCancelTimer() {


        new Handler(Looper.getMainLooper()).postDelayed((new Runnable() {
            @Override
            public void run() {
                if (!isCallConnected) {
                    if (isActivityRunnning) {

                        Toast.makeText(WebRTCVideoCall.this, "Person is not responding to your call.\nTry Again Later :)", Toast.LENGTH_LONG).show();

                    }
                    // showAlertCard("Person is not responding to your call.\nTry Again Later");
                    disconnectCall(false);
                }
            }
        }), 120000);


    }

    void setAutoCallDisconnect() {

        RootDB.addValueEventListener(RootDBValListner);
    }

    void setAudioWebRTCConfig() {

        audioManager = AppRTCAudioManager.create(getApplicationContext());
// Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
        audioManager.setSpeakerphoneOn(true);

/*    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    audioManager.setSpeakerphoneOn(true);

    if (!audioManager.isBluetoothScoOn()) {
        audioManager.startBluetoothSco();
    }else if(isHeadsetOn(this)){
        Log.d(TAG, "setAudioWebRTCConfig: isHeadsetOn: TRUE");
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        audioManager.setSpeakerphoneOn(false);
    }
*/
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(true);
        WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);

    }

    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
        Map<String, String> adata = new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS), "WebRTC onAudioManagerDevicesChanged");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS), adata);
    }

    public void PlayCallingTone() {
        Log.d(TAG, "PlayRingtone: ");

        if (ringtone == null) {

            Uri notification = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.calling_ringtone);

            // Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, notification);
        }

        if (!ringtone.isPlaying()) {
            ringtone.play();
        }
    }

    public void StopCallingTone() {

        if (ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            isPictureInPictureModeEnabled = true;
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
            //  findViewById(R.id.LLController).setVisibility(View.INVISIBLE);
            //  findViewById(R.id.pip_video_view).setVisibility(View.INVISIBLE);
            //  findViewById(R.id.AppLogoWatermark).setVisibility(View.INVISIBLE);
            Log.d(TAG, "onPictureInPictureModeChanged: isInPictureInPictureMode: TRUE");
        } else {
            // Restore the full-screen UI.
            isPictureInPictureModeEnabled = false;
            //   findViewById(R.id.pip_video_view).setVisibility(View.VISIBLE);
            // findViewById(R.id.LLController).setVisibility(View.VISIBLE);
            //   findViewById(R.id.AppLogoWatermark).setVisibility(View.VISIBLE);
            Log.d(TAG, "onPictureInPictureModeChanged: isInPictureInPictureMode: FALSE");
        }
        Log.d(TAG, "onPictureInPictureModeChanged: isInPictureInPictureMode: " + isInPictureInPictureMode + " \n newConfig:" + newConfig);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void pipEnable() {
        //   Rational rational = new Rational(16, 9);
        //DisplayMetrics displayMetrics = new DisplayMetrics();
        //   getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //  int height = displayMetrics.heightPixels;
        //  int width = displayMetrics.widthPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            final PictureInPictureParams.Builder pictureInPictureParamsBuilder =
                    new PictureInPictureParams.Builder();
            Rational aspectRatio = new Rational(1, 1);
            pictureInPictureParamsBuilder.setAspectRatio(aspectRatio);
            enterPictureInPictureMode(pictureInPictureParamsBuilder.build());

        } else {
            enterPictureInPictureMode();
        }

        isPictureInPictureModeEnabled = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void PipEnableButton(View v) {
        pipEnable();
    }

    void pipModeEnableViews() {
        Log.d(TAG, "pipModeEnableViews: ");
        findViewById(R.id.LLController).setVisibility(View.INVISIBLE);
        findViewById(R.id.pip_video_view).setVisibility(View.INVISIBLE);
        findViewById(R.id.AppLogoWatermark).setVisibility(View.INVISIBLE);
    }

    void pipModeDisableViews() {
        Log.d(TAG, "pipModeDisableViews: ");
        findViewById(R.id.pip_video_view).setVisibility(View.VISIBLE);
        findViewById(R.id.LLController).setVisibility(View.VISIBLE);
        findViewById(R.id.AppLogoWatermark).setVisibility(View.VISIBLE);

    }

    void SetActiveNotification() {
        Intent intent;
        intent = new Intent(this, WebRTCVideoCall.class);

        intent.putExtra("isvoip", "0");

        intent.putExtra(getString(R.string.WebRTC_PEER_ID), PeerID);
        intent.putExtra(getString(R.string.call_type), CallerType);
        intent.putExtra(getString(R.string.WebRTC_CallReceiverID), CallReceiverID); //contact2Id

        if (CallerUserName != null) {
            intent.putExtra(getString(R.string.intent_Caller_User_Name), CallerUserName);
        }

        intent.putExtra(getString(R.string.intent_Caller_Phone), CallerPhoneNo);

        PendingIntent fullScreenPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        }else{

            fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

        }



        notificationManagerCompat = NotificationManagerCompat.from(this);

        String title = "Active Video Call";
        if (CallerUserName != null) {
            title = CallerUserName;
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_call))
                        .setSmallIcon(R.drawable.ic_baseline_videocam_30)
                        .setContentTitle(title)
                        .setContentText(CallerPhoneNo)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setColor(Color.GREEN)
                        .setColorized(true)
                        //.setSound(alarmSound,AudioManager.STREAM_RING)
                        .setContentIntent(fullScreenPendingIntent)
                        .setOngoing(true);
        //      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), AudioManager.STREAM_RING)
        //    .setVibrate(new long[]{500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500});
        //    .setFullScreenIntent(fullScreenPendingIntent, true);


        notificationManagerCompat.notify(10, notificationBuilder.build());

    }

    void showAlertCard(String text) {
        try {
            CallingStatusTextView.setVisibility(View.INVISIBLE);
            AlertCardView.setVisibility(View.VISIBLE);
            AlertCVText.setText(text);
            loadingAnimationView.cancelAnimation();
            loadingAnimationView.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "showAlertCard: " + e);
        }
    }

    void setOnCallUserStatus() {
        CurrentUserStatus = cachePref.getString(getString(R.string.PREF_USER_CURRENT_STATUS));
        cachePref.setString(getString(R.string.PREF_USER_CURRENT_STATUS), getString(R.string.USER_STATUS_ON_CALL));
        UserStatusRef.setValue(Integer.parseInt(getString(R.string.USER_STATUS_ON_CALL)));
    }

    void removeOnCallUserStatus() {
        cachePref.setString(getString(R.string.PREF_USER_CURRENT_STATUS), CurrentUserStatus);
        UserStatusRef.setValue(Integer.parseInt(CurrentUserStatus));
    }

    // TODO: RECORDING
    void MediaRecorder() {
        MediaRecorder recorder = new MediaRecorder();
        try {

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                recorder.setOutputFile(new File("/video.mp3"));
            }

            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
    }

}

/*
audioConstraints = new MediaConstraints();

* audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
* */
package devesh.ephrine;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import devesh.ephrine.AppRTC.WebRTCVideoCall;
import devesh.ephrine.AppRTC.WebRTCVoiceCall;
import devesh.ephrine.service.CallService;
import devesh.ephrine.util.AppAnalytics;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class CallingScreen extends AppCompatActivity {
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private static final String CHANNEL_ID = "channel_id";
    private static final String PRIORITY_CHANNEL_ID = "pr_channel_id";
    private static final int CONNECTION_REQUEST = 1;
    private static final int REMOVE_FAVORITE_INDEX = 0;
    private static boolean commandLineRun;
    public NotificationManager notificationManager;
    public String remoteid;
    // public String hostid;
    // public String shouldReconnect;
    public OkHttpClient client;
    public String MyPeerId; // Create Peer ID
    SharedPreferences sharedPref;
    String ReceiversPeerId;
    String CallReceiverID;
    String CallerType;
    boolean isVOIP;
    //String PEER_HOST;
    String TAG = "CallActivity_Old";
    String camMode;
    FirebaseDatabase database;
    //   DatabaseReference connectionDBref;
    boolean isHost;
    String URL_CallNow;
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    Gson gson;
    FirebaseUser fUser;
    String CallerUserName;
    String CallerPhoneNo;
    Ringtone ringtone;
    String CallingSSProtect = "1";
    // Bundle onCallBundle;
    // Fragment onCallFrag;
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AudioManager m_amAudioManager;
AppAnalytics appAnalytics;

    String TwilioUsername;
    String TwilioPassword;

//--

    public static PendingIntent getDismissIntent(int notificationId, Context context) {
        Intent intent = new Intent(context, CallingScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(String.valueOf(notificationId), notificationId);
        PendingIntent dismissIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dismissIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else{
            dismissIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        return dismissIntent;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        AppCenter.start(getApplication(),  getString(R.string.MS_AppCenter_API_Key), Analytics.class, Crashes.class);
appAnalytics=new AppAnalytics(getApplication(),this);

        setContentView(R.layout.activity_call);


        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().addFlags(flags);


    //    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
     //           | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
     //           | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        PreferenceManager.setDefaultValues(this, R.xml.apprtc_preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (fUser != null) {
//            init();
        } else {

        }

        PlayRingtone();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        camMode = "face";
        //     hideSystemUI();
        gson = new Gson();
        database = FirebaseDatabase.getInstance();

        URL_CallNow = getString(R.string.URL_CallNow);


        Intent intent = getIntent();
        CallerType = intent.getStringExtra(getString(R.string.call_type));
        if (CallerType.equals("0")) {
            MyPeerId = intent.getStringExtra(getString(R.string.create_room_id));
            ReceiversPeerId = "x";
            CallReceiverID = intent.getStringExtra(getString(R.string.contact2Id));
            isHost = true;
        } else if (CallerType.equals("1")) {
            MyPeerId = fUser.getUid();
            ReceiversPeerId = intent.getStringExtra(getString(R.string.join_room_id));
            CallReceiverID = intent.getStringExtra(getString(R.string.contact2Id));
            isHost = false;
        }

        if (intent.getStringExtra("isvoip").equals("1")) {
            isVOIP = true;
        } else {
            isVOIP = false;
        }

       /* onCallBundle = new Bundle();
        onCallBundle.putString(getString(R.string.create_room_id), MyPeerId);
        onCallBundle.putString(getString(R.string.join_room_id), ReceiversPeerId);
        onCallBundle.putString(getString(R.string.contact2Id), CallReceiverID);
        onCallBundle.putString(getString(R.string.call_type), CallerType);
        onCallBundle.putBoolean("isHost", isHost);

        onCallFrag = new OnCallScreenFragment();
        onCallFrag.setArguments(onCallBundle);
*/

        if (intent.getStringExtra(getString(R.string.intent_iscall_screen)) != null) {

            String isCallingScreen = intent.getStringExtra(getString(R.string.intent_iscall_screen));

            CallerUserName = intent.getStringExtra(getString(R.string.intent_Caller_User_Name));
            CallerPhoneNo = intent.getStringExtra(getString(R.string.intent_Caller_Phone));

            if (isCallingScreen.equals("1")) {
                Bundle bundle = new Bundle();

                bundle.putString(getString(R.string.intent_Caller_User_Name), CallerUserName);
                bundle.putString(getString(R.string.intent_Caller_Phone), CallerPhoneNo);
                bundle.putString(getString(R.string.contact2Id),CallReceiverID);
                if(isVOIP){
                    bundle.putString("isvoip","1");
                }else{
                    bundle.putString("isvoip","0");
                }

                Fragment CallingSCR = new CallingScreenFragment();
                CallingSCR.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        // .setReorderingAllowed(true)
                        .replace(R.id.fragment_container_view, CallingSCR)
                        .commit();
            } else {
                //  setOnCallScreen();
            }

        } else {

        }
        if (intent.getStringExtra(getString(R.string.WebRTC_SS_Protect)) != null) {
            CallingSSProtect = intent.getStringExtra(getString(R.string.WebRTC_SS_Protect));
            if (CallingSSProtect.equals("1")) {
            } else {
                CallingSSProtect = "0";
            }
        }

        if(intent.getStringExtra(getString(R.string.intent_twilio_un))!=null){
            TwilioUsername=intent.getStringExtra(getString(R.string.intent_twilio_un));
        }else{
            TwilioUsername="x";
        }

        if(intent.getStringExtra(getString(R.string.intent_twilio_pw))!=null){
            TwilioPassword= intent.getStringExtra(getString(R.string.intent_twilio_pw));
        }else{
            TwilioPassword="x";
        }






        //        setOnCallScreen();

        Log.d(TAG, "init: CallerType: " + CallerType +
                "\nMyPeerId: " + MyPeerId +
                "\nReceiversPeerId: " + ReceiversPeerId +
                "\nCallReceiverID: " + CallReceiverID +
                "\nisHost: " + isHost +
                "\nCallerUserName: " + CallerUserName +
                "\nCallerPhoneNo:" + CallerPhoneNo +
                "\nCallerScreenProtect: " + CallingSSProtect+
                "\nisVOIP: "+isVOIP);




        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),"Calling Screen");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);


    }



    @Override
    public void onBackPressed() {

       // super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        StopRingtone();
        super.onDestroy();

    }


    public void CallAccept(View v) {
        StopRingtone();
        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Call Accept");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);



        Intent intent;

        if (isVOIP) {
            intent = new Intent(this, WebRTCVoiceCall.class);
            intent.putExtra("isvoip", "1");

        } else {
            intent = new Intent(this, WebRTCVideoCall.class);
            intent.putExtra("isvoip", "0");

        }

        intent.putExtra(getString(R.string.WebRTC_PEER_ID), ReceiversPeerId);
        intent.putExtra(getString(R.string.call_type), "1");
        //intent.putExtra(getString(R.string.WebRTC_CallReceiverID), CallReceiverID); //contact2Id
        intent.putExtra(getString(R.string.WebRTC_CallReceiverID), CallReceiverID); //contact2Id

        if (CallerUserName != null) {
            intent.putExtra(getString(R.string.intent_Caller_User_Name), CallerUserName);
        }

        intent.putExtra(getString(R.string.intent_Caller_Phone), CallerPhoneNo);
        intent.putExtra(getString(R.string.intent_twilio_un), TwilioUsername);
        intent.putExtra(getString(R.string.intent_twilio_pw), TwilioPassword);

        startActivity(intent);

        Intent stopIntent = new Intent(this, CallService.class);
        stopIntent.setAction("STOP");
        startService(stopIntent);

        //connectToRoom(ReceiversPeerId, false, false, false, 0,CallReceiverID);
        notificationManager.cancel(10);
        CallingScreen.this.finish();
        /*    m_amAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        m_amAudioManager.setMode(AudioManager.MODE_NORMAL);
        m_amAudioManager.setSpeakerphoneOn(false);
    //PLAY ON EARPIECE
    mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
    audioManager.setMode(AudioManager.MODE_IN_CALL);
    audioManager.setSpeakerphoneOn(false);
    //PLAY ON SPEAKER
    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    audioManager.setMode(AudioManager.MODE_IN_CALL);
    audioManager.setSpeakerphoneOn(true);
    */

   /*     setOnCallScreen();
        notificationManager.cancel(10);
        Toast.makeText(this, "Connecting....", Toast.LENGTH_LONG).show();

   FragmentManager fm = getSupportFragmentManager();
        OnCallScreenFragment fragment =
                (OnCallScreenFragment)fm.findFragmentByTag("onCall");
        fragment.ConnectCallNow();*/

    }

    public void CancelCall(View v) {
        //NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //notificationManager.cancelAll();
        StopRingtone();
        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Call Denied");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);

        notificationManager.cancel(10);
        Intent stopIntent = new Intent(this, CallService.class);
        stopIntent.setAction("STOP");
        startService(stopIntent);
        CallingScreen.this.finish();

    }

    public void PlayRingtone() {
        Log.d(TAG, "PlayRingtone: ");
/*
        if (ringtone == null) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, notification);
        }

        if (!ringtone.isPlaying()) {
            ringtone.play();
        }
*/
    }

    public void StopRingtone() {
       /* if (ringtone.isPlaying()) {
            ringtone.stop();
        }*/
    }

    public static class CallingScreenFragment extends Fragment {
        TextView ReceiverNameTextView;
        TextView PhoneNoTextView;

        TextView VoiceCallTx;
        TextView VideoCallTx;

        String RName;
        String RPhoneNo;
        String CallerUID;

        boolean isVoip;

        String TAG = "CallingFrag";
CircleImageView CallProfilePic;
        public CallingScreenFragment() {
            super(R.layout.fragment_call_screen);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

            try {
                RName = requireArguments().getString(getString(R.string.intent_Caller_User_Name));
                RPhoneNo = requireArguments().getString(getString(R.string.intent_Caller_Phone));
CallerUID=requireArguments().getString(getString(R.string.contact2Id));
                if(requireArguments().getString("isvoip").equals("1")){
                    isVoip=true;
                }else {
                    isVoip=false;
                }

                ReceiverNameTextView = view.findViewById(R.id.ReceiverNameTextView);
                PhoneNoTextView = view.findViewById(R.id.PhoneNoTextView);

                VoiceCallTx=view.findViewById(R.id.VoiceCallTextView);
                VideoCallTx=view.findViewById(R.id.VideoCallTextView);
                CallProfilePic=view.findViewById(R.id.CallProfilePic);

                ReceiverNameTextView.setText(RName);
                PhoneNoTextView.setText(RPhoneNo);

                 if(isVoip){
                    VoiceCallTx.setVisibility(View.VISIBLE);
                    VideoCallTx.setVisibility(View.GONE);
                }else {
                     VideoCallTx.setVisibility(View.VISIBLE);

                     VoiceCallTx.setVisibility(View.GONE);
                 }

                setProfilePic(CallerUID);
                Log.d(TAG, "onViewCreated: INFO Fragment Call Screen: \n"+
                        "isvoip: "+ isVoip);
            } catch (Exception e) {
                Log.d(TAG, "onViewCreated: " + e);
            }


        }
        File ProfilePicFile;

        void setProfilePic(String callReceiverID){
String firebase_project_id = FirebaseApp.getInstance().getOptions().getProjectId();

            String DefaultPhotoURL = "https://storage.googleapis.com/" + firebase_project_id + ".appspot.com/indra/users/" + callReceiverID + "/app_user_photo.jpg";
            File myDir = new File(getActivity().getFilesDir(), "profilepics");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }

            ProfilePicFile = new File(myDir, callReceiverID + ".jpg");
            if(ProfilePicFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(ProfilePicFile.getAbsolutePath());
                CallProfilePic.setImageBitmap(myBitmap);

            }

            Glide.with(this).asBitmap().load(DefaultPhotoURL) .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            try {
                                Log.d(TAG, "ProfilePic onResourceReady: ");
                                // File file = new File(getActivity().getFilesDir(), FILE_PROFILE_PIC);
                                FileOutputStream out = new FileOutputStream(ProfilePicFile);
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                out.flush();
                                out.close();

                                CallProfilePic.setImageBitmap(resource);
                                Log.d(TAG, "ProfilePic onResourceReady: SAVED");

                            } catch( Exception e) {
                                Log.d(TAG, "Profile Pic ERROR #45643 "+e);
                            }



                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });


        }
    }
}


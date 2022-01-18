package devesh.ephrine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import devesh.common.utils.EpochLib;
import devesh.ephrine.notifications.room.NotifictionData;
import devesh.ephrine.notifications.workmanager.NotiWorkManager;
import devesh.ephrine.service.CallService;
import devesh.ephrine.util.AppAnalytics;
import devesh.ephrine.workmanager.ContactsUpdateWorkManager;
import devesh.ephrine.workmanager.CreateCallRecordWorker;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    Gson gson;
    String TAG = "My FCM: ";
    String ReceiversUID;
    String SessionId;
    String CallerUserName;
    String CallerPhoneNo;
    String ReceiverSSProtect;
    boolean isVOIP;
    private Bitmap bitmap;
    private NotificationManagerCompat notificationManagerCompat;
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;

    AppAnalytics appAnalytics;

    String TwilioUsername;
    String TwilioPassword;

    public MyFirebaseMessagingService() {

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        appAnalytics=new AppAnalytics(getApplication(),this);

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"FCM Receive");



        gson = new Gson();

        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String notificationType = remoteMessage.getData().get("type");
            if (notificationType.equals("call")) {
                notificationManagerCompat = NotificationManagerCompat.from(this);
                Log.d(TAG, "onMessageReceived: fromUID: " + remoteMessage.getData().get("fromUID"));
                ReceiversUID = remoteMessage.getData().get("fromUID");
                CallerUserName = remoteMessage.getData().get("CallerUserName");
                CallerPhoneNo = remoteMessage.getData().get("CallerPhoneNo");
                // CallReceiverTokenId=remoteMessage.getData().get("fromUID");
                SessionId = remoteMessage.getData().get("SessionId");
                ReceiverSSProtect = remoteMessage.getData().get("screen_protected");
                String EPOCH = remoteMessage.getData().get("EPOCH");

                TwilioUsername=remoteMessage.getData().get("twilioun");
                TwilioPassword=remoteMessage.getData().get("twiliopw");

                if (remoteMessage.getData().get("voip").equals("1")) {
                    isVOIP = true;
                    adata.put("CallType","VOIP");

                } else {
                    isVOIP = false;
                    adata.put("CallType","Video Call");
                }
                Log.d(TAG, "onMessageReceived: isvoip call: " + isVOIP);

                createNotificationChannels();

                if (isMissedCalled(Long.parseLong(EPOCH))) {

                    createMissCallNotification();
                    adata.put("CallType","Missed Call");

                } else {
                    // TODO: BETA : Calling Service

                    CreateCallNotification();
                    //  createFullScreenNotification();


                }

                CreateCallRecord(ReceiversUID, "i");

            } else if (notificationType.equals("csync")) {
                contactUpdateSync();
                adata.put("NotificationType","csync");

            } else if (notificationType.equals("ping")) {
                String name = remoteMessage.getData().get("PingerName");
                PingNotification(name);
                adata.put("NotificationType","ping");

            } else if (notificationType.equals("notification")) {
                Log.d(TAG, "onMessageReceived: general notification ");
                adata.put("NotificationType","normal");

                NotifictionData data = new NotifictionData();
                data.Title = remoteMessage.getData().get("title");
                data.Short_Message = remoteMessage.getData().get("short_summary");
                data.Long_Message = remoteMessage.getData().get("long_summary");
                data.icon = remoteMessage.getData().get("icon");
                data.img = remoteMessage.getData().get("img");
                data.Type = remoteMessage.getData().get("type");
                data.Url = remoteMessage.getData().get("url");
                data.id = Integer.parseInt(remoteMessage.getData().get("id"));
                // data.id=Integer.parseInt(String.valueOf(System.currentTimeMillis()));
                //data.time=Integer.parseInt(String.valueOf(System.currentTimeMillis()));
                data.time = System.currentTimeMillis();
                NormalNotification(data);

            } else if (notificationType.equals("app_update")) {
                Log.d(TAG, "onMessageReceived: general notification ");
                adata.put("NotificationType","app update");

                NotifictionData data = new NotifictionData();
                data.Title = remoteMessage.getData().get("title");
                data.Short_Message = remoteMessage.getData().get("short_summary");
                data.Long_Message = remoteMessage.getData().get("long_summary");
                data.icon = remoteMessage.getData().get("icon");
                data.img = remoteMessage.getData().get("img");
                data.Type = remoteMessage.getData().get("type");
                data.Url = remoteMessage.getData().get("url");
                data.id = Integer.parseInt(remoteMessage.getData().get("id"));
                // data.id=Integer.parseInt(String.valueOf(System.currentTimeMillis()));
                //data.time=Integer.parseInt(String.valueOf(System.currentTimeMillis()));
                data.time = System.currentTimeMillis();
                NormalNotification(data);

            }


            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                // scheduleJob();
            } else {
                // Handle message within 10 seconds
                // handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),adata);

    }

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        mAuth = FirebaseAuth.getInstance();
        sendToken2DB(token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        //sendRegistrationToServer(token);
    }

    void CreateCallRecord(String uid, String io) {
        WorkRequest createCallRecordWork = new OneTimeWorkRequest.Builder(CreateCallRecordWorker.class)
                .addTag("CallRecordCreated")
                .setInputData(
                        new Data.Builder()
                                .putString("uid", uid)
                                .putString("io", io)
                                .build())
                .build();

        WorkManager.getInstance(this)
                .enqueue(createCallRecordWork);
    }

    // Beta
    @RequiresApi(api = Build.VERSION_CODES.O)
    void CreateCallNotification(){
        Context context = getApplicationContext();
        Intent intent = new Intent(this, CallService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        intent.putExtra(getString(R.string.join_room_id), SessionId);
        intent.putExtra(getString(R.string.call_type), "1");
        intent.putExtra(getString(R.string.intent_iscall_screen), "1");
        intent.putExtra(getString(R.string.intent_Caller_User_Name), CallerUserName);
        intent.putExtra(getString(R.string.intent_Caller_Phone), CallerPhoneNo);
        intent.putExtra(getString(R.string.WebRTC_SS_Protect), ReceiverSSProtect);
        intent.putExtra(getString(R.string.contact2Id), ReceiversUID);
        intent.putExtra(getString(R.string.intent_twilio_un), TwilioUsername);
        intent.putExtra(getString(R.string.intent_twilio_pw), TwilioPassword);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isVOIP) {
            intent.putExtra("isvoip", "1");
        } else {
            intent.putExtra("isvoip", "0");
        }



        context.startForegroundService(intent);
    }

    public void createFullScreenNotification() {
        Intent fullScreenIntent;
        if (isVOIP) {
            fullScreenIntent = new Intent(this, CallingScreen.class);
            fullScreenIntent.putExtra("isvoip", "1");
        } else {
            fullScreenIntent = new Intent(this, CallingScreen.class);
            fullScreenIntent.putExtra("isvoip", "0");
        }
        fullScreenIntent.putExtra(getString(R.string.join_room_id), SessionId);
        fullScreenIntent.putExtra(getString(R.string.call_type), "1");
        fullScreenIntent.putExtra(getString(R.string.intent_iscall_screen), "1");
        fullScreenIntent.putExtra(getString(R.string.intent_Caller_User_Name), CallerUserName);
        fullScreenIntent.putExtra(getString(R.string.intent_Caller_Phone), CallerPhoneNo);
        fullScreenIntent.putExtra(getString(R.string.WebRTC_SS_Protect), ReceiverSSProtect);
        fullScreenIntent.putExtra(getString(R.string.contact2Id), ReceiversUID);

        fullScreenIntent.addFlags(Notification.FLAG_INSISTENT);
        fullScreenIntent.addFlags(Notification.PRIORITY_HIGH);
        fullScreenIntent.addFlags(Notification.FLAG_ONGOING_EVENT);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        fullScreenIntent.addFlags(Notification.DEFAULT_SOUND);

        PendingIntent fullScreenPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fullScreenPendingIntent = PendingIntent.getActivity(this, 0,fullScreenIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }else{
            fullScreenPendingIntent = PendingIntent.getActivity(this, 0,fullScreenIntent, PendingIntent.FLAG_ONE_SHOT );
        }



      Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_call))
                        .setSmallIcon(R.drawable.ic_baseline_call_end_30)
                        .setContentTitle("Incoming call")
                        .setContentText(CallerUserName)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setOngoing(true)
                        .setSound(alarmSound)
                        .setAutoCancel(false)
                        .setSilent(false)
                        .setVibrate(new long[]{500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500})
                        .setFullScreenIntent(fullScreenPendingIntent, true);


        notificationManagerCompat.notify(10, notificationBuilder.build());



        Intent call_intent = new Intent(this, CallingScreen.class);
        call_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        call_intent.putExtra(getString(R.string.join_room_id), SessionId);
        call_intent.putExtra(getString(R.string.call_type), "1");
        call_intent.putExtra(getString(R.string.intent_iscall_screen), "1");
        call_intent.putExtra(getString(R.string.intent_Caller_User_Name), CallerUserName);
        call_intent.putExtra(getString(R.string.intent_Caller_Phone), CallerPhoneNo);
        call_intent.putExtra(getString(R.string.WebRTC_SS_Protect), ReceiverSSProtect);
        call_intent.putExtra(getString(R.string.contact2Id), ReceiversUID);

        call_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isVOIP) {
            call_intent.putExtra("isvoip", "1");
        } else {
            call_intent.putExtra("isvoip", "0");
        }


        startActivity(call_intent);

    }

    public void createMissCallNotification() {


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_missed_call))
                        .setSmallIcon(R.drawable.ic_baseline_missed_video_call_30)
                        .setContentTitle("Missed Call")
                        .setContentText("" + CallerUserName + " Called you")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        //.setSound(alarmSound,AudioManager.STREAM_RING)
                        //  .setOngoing(true)
                        //      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), AudioManager.STREAM_RING)
                        .setVibrate(new long[]{500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500});


        notificationManagerCompat.notify(10, notificationBuilder.build());

        //Notification incomingCallNotification = notificationBuilder.build();
        //  incomingCallNotification.sound=alarmSound;
        // Provide a unique integer for the "notificationId" of each notification.
        //  startForeground(10, incomingCallNotification);


    }

    private void createNotificationChannels() {


        // Create Call Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel priorityNotificationChannel = new NotificationChannel(getString(R.string.notification_channel_id_call),
                    getString(R.string.notification_channel_id_call_text),
                    NotificationManager.IMPORTANCE_HIGH);

            priorityNotificationChannel.setSound(notification_sound,attributes);
            priorityNotificationChannel.enableVibration(true);
            priorityNotificationChannel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(priorityNotificationChannel);

        }


        // Create Missed Call Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            NotificationChannel missedCalledNotificationChannel = new NotificationChannel(getString(R.string.notification_channel_id_missed_call),
                    getString(R.string.notification_channel_id_missed_call_text),
                    NotificationManager.IMPORTANCE_DEFAULT);

            //missedCalledNotificationChannel.setName(getString(R.string.notification_channel_id_missed_call_text));
            missedCalledNotificationChannel.enableVibration(true);
            missedCalledNotificationChannel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(missedCalledNotificationChannel);

        }

        //
        // Create Ping Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence Channel_name = "Ping";
            String description = "Receive Ping from Friends";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_id_ping), Channel_name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }



    }

    void sendToken2DB(String token) {
        if (mAuth.getCurrentUser() != null) {
            DatabaseReference tokenDB = FirebaseDatabase.getInstance().getReference("users/" + mAuth.getCurrentUser().getUid() + "/indra/instanceid");
            tokenDB.setValue(token);
        }
    }

    void contactUpdateSync() {
        WorkRequest contactSyncWork = new OneTimeWorkRequest.Builder(ContactsUpdateWorkManager.class)
                .addTag("contact_update_sync").build();
        WorkManager.getInstance(this)
                .enqueue(contactSyncWork);
    }

    void PingNotification(String name) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else{
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_ping))
                .setSmallIcon(R.drawable.ic_baseline_waving_hand_30)
                .setContentTitle("" + name + " Pinged you !")
                //   .setContentText("Received a Ping")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create Channel
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence Channel_name = "Ping";
            String description = "Receive Ping from Friends";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_id_ping), Channel_name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }*/

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        Random rand = new Random();
        // Generate random integers in range 0 to 999
        int noti_id = rand.nextInt(999);
        notificationManager.notify(noti_id, builder.build());

    }

    void NormalNotification(NotifictionData data) {

        //NotiDataManager notiDataManager=new NotiDataManager(this);
        //notiDataManager.Save(data);

        WorkRequest notiWorkRequest =
                new OneTimeWorkRequest.Builder(NotiWorkManager.class)
                        .addTag(String.valueOf(data.id))
                        .setInputData(
                                new Data.Builder()
                                        .putString("title", data.Title)
                                        .putString("short_summary", data.Short_Message)
                                        .putString("long_summary", data.Long_Message)
                                        .putString("icon", data.icon)
                                        .putString("img", data.img)
                                        .putString("type", data.Type)
                                        .putString("url", data.Url)
                                        .putString("id", String.valueOf(data.id))
                                        .putString("time", String.valueOf(data.time))
                                        .putString("flavor", BuildConfig.FLAVOR)

                                        .build())
                        .build();

        WorkManager
                .getInstance(this)
                .enqueue(notiWorkRequest);


     /*   Intent intent = new Intent(this, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_ping))
                //.setSmallIcon(R.drawable.ic_baseline_apps_30)
                .setContentTitle(data.Title)
                .setContentText(data.Short_Message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence Channel_name = "News & Updates";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_id_general), Channel_name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        Random rand = new Random();
        // Generate random integers in range 0 to 999
        int noti_id = rand.nextInt(999);
        notificationManager.notify(noti_id, builder.build());*/

    }

    boolean isMissedCalled(long EPOCH) {
        final long MINUTES_2 = 120;
        EpochLib epochLib = new EpochLib();

        EPOCH = epochLib.convert2Seconds(EPOCH);
        long cEPOCH = epochLib.convert2Seconds(epochLib.getEPOCH());
        long t = cEPOCH - EPOCH;
        Log.d(TAG, "isMissedCalled: \nEPOCH:" + EPOCH + "\ncEPOCH:" + cEPOCH + "\nt:" + t + "\n");

        if (t >= MINUTES_2) {
            return true;
        } else {
            return false;
        }

    }

}
package devesh.ephrine.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.material.color.MaterialColors;

import devesh.ephrine.CallingScreen;
import devesh.ephrine.R;

public class CallService extends Service {
    String TAG = "mCallService:";
    Ringtone ringtone;

    public CallService() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent.getAction() != null) {
                if (intent.getAction().equals("STOP")) {
                    Log.i(TAG, "Received Stop Foreground Intent");
                 //   StopRingtone();

                    stopForeground(true);
                    stopSelfResult(startId);
stopSelf();
                }

            }

        } catch (Exception e) {
            Log.e(TAG, "onStartCommand: " + e);
        }

        Log.d(TAG, "onStartCommand: ");
        String UserName="";
        if(intent.getStringExtra(getString(R.string.intent_Caller_User_Name))!=null){
            UserName=intent.getStringExtra(getString(R.string.intent_Caller_User_Name));
        }else{
            UserName=intent.getStringExtra(getString(R.string.intent_Caller_Phone));
        }


        Intent notificationIntent = new Intent(this, CallingScreen.class);

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        notificationIntent.putExtra(getString(R.string.join_room_id), intent.getStringExtra(getString(R.string.join_room_id)));
        notificationIntent.putExtra(getString(R.string.call_type), intent.getStringExtra(getString(R.string.call_type)));
        notificationIntent.putExtra(getString(R.string.intent_iscall_screen), intent.getStringExtra(getString(R.string.intent_iscall_screen)));
        notificationIntent.putExtra(getString(R.string.intent_Caller_User_Name), intent.getStringExtra(getString(R.string.intent_Caller_User_Name)));
        notificationIntent.putExtra(getString(R.string.intent_Caller_Phone), intent.getStringExtra(getString(R.string.intent_Caller_Phone)));
        notificationIntent.putExtra(getString(R.string.WebRTC_SS_Protect), intent.getStringExtra(getString(R.string.WebRTC_SS_Protect)));
        notificationIntent.putExtra(getString(R.string.contact2Id), intent.getStringExtra(getString(R.string.contact2Id)));

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        notificationIntent.putExtra("isvoip", intent.getStringExtra("isvoip"));


        notificationIntent.putExtra(getString(R.string.intent_twilio_un), intent.getStringExtra(getString(R.string.intent_twilio_un)));
        notificationIntent.putExtra(getString(R.string.intent_twilio_pw), intent.getStringExtra(getString(R.string.intent_twilio_pw)));

        int icon=R.drawable.ic_baseline_phone_30;
        try{
            if( intent.getStringExtra("isvoip").equals("0")){
                icon=R.drawable.ic_baseline_videocam_30;
            }
        }catch (Exception e){

        }

        notificationIntent.addFlags(Notification.FLAG_INSISTENT);
        notificationIntent.addFlags(Notification.PRIORITY_HIGH);
        notificationIntent.addFlags(Notification.FLAG_ONGOING_EVENT);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      //  notificationIntent.addFlags(Notification.DEFAULT_SOUND);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);


        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        @SuppressLint("WrongConstant") Notification notification =
                new Notification.Builder(this, getString(R.string.notification_channel_id_call))
                        .setContentTitle(UserName+" is Calling You")
                       .setContentText("Tap to Respond")
                        .setSmallIcon(icon)
                        .setColor(Color.parseColor("#4caf50"))
                        .setColorized(true)
                     //   .setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent, true)
                        .setTicker(UserName+" is Calling You")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                       // .setOngoing(true)
                       // .setSound(alarmSound)
                       // .setAutoCancel(false)
                        .setVibrate(new long[]{500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500})
                        .build();


        startForeground(10, notification);
        PlayRingtone();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        StopRingtone();
        super.onDestroy();
    }

    public void PlayRingtone() {
        Log.d(TAG, "PlayRingtone: ");

        if (ringtone == null) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, notification);
        }

        if (!ringtone.isPlaying()) {
            ringtone.play();
        }

    }

    public void StopRingtone() {
        if (ringtone != null) {

            if (ringtone.isPlaying()) {
               ringtone.stop();
            }
        }
    }

    void ChangeStatusBarColor(){



         }

}
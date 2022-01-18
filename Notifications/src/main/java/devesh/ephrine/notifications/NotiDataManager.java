package devesh.ephrine.notifications;
/*
NOT USED
*/

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import java.util.Random;

import devesh.ephrine.notifications.room.NotificationAppDatabase;
import devesh.ephrine.notifications.room.NotifictionData;

public class NotiDataManager {
    NotificationAppDatabase NotiDB;
    Context mContext;
    String AppFlavor;

    public NotiDataManager(Context context){
        mContext = context;
        NotiDB = Room.databaseBuilder(context, NotificationAppDatabase.class, context.getString(R.string.DATABASE_NOTIFICATION))
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    public void Save(NotifictionData data){
        NotiDB.notificationDAO().insertAll(data);

        NotificationShoot(data);

    }
    void NotificationShoot(NotifictionData data) {
        Intent intent = new Intent(mContext, NotificationActivity.class);
        intent.putExtra("flavor",AppFlavor);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

             pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        }else{

            pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, mContext.getString(R.string.notification_channel_id_general))
                .setSmallIcon(R.drawable.ic_notifications_white_48dp)
                .setContentTitle(data.Title)
                .setContentText(data.Short_Message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence Channel_name = "News & Updates";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(mContext.getString(R.string.notification_channel_id_general), Channel_name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

// notificationId is a unique int for each notification that you must define
        Random rand = new Random();
        // Generate random integers in range 0 to 999
        int noti_id = rand.nextInt(999);
        notificationManager.notify(noti_id, builder.build());
    }
}

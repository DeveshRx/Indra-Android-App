package devesh.ephrine.notifications.workmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Random;

import devesh.ephrine.notifications.NotificationActivity;
import devesh.ephrine.notifications.R;
import devesh.ephrine.notifications.room.NotificationAppDatabase;
import devesh.ephrine.notifications.room.NotifictionData;

public class NotiWorkManager extends Worker {
    NotificationAppDatabase NotiDB;
    Context mContext;
    String AppFlavor;
String TAG="NotiWork: ";
    public NotiWorkManager(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
        NotiDB = Room.databaseBuilder(context, NotificationAppDatabase.class, context.getString(R.string.DATABASE_NOTIFICATION))
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

    }

    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: ");
        NotifictionData data=new NotifictionData();

        String title = getInputData().getString("title");
        if (title == null) {
            title="x";
        }
        data.Title=title;

        String short_summary = getInputData().getString("short_summary");
        if (short_summary == null) {
            short_summary="x";
        }
        data.Short_Message=short_summary;

        String long_summary = getInputData().getString("long_summary");
        if (long_summary == null) {
            long_summary = "x";
        }
        data.Long_Message=long_summary;

        String icon = getInputData().getString("icon");
        if (icon == null) {
            icon = "x";
        }
        data.icon=icon;

        String img = getInputData().getString("img");
        if (img == null) {
            img = "x";
        }
        data.img="x";

        String type = getInputData().getString("type");
        if (type == null) {
            type = "x";
        }
        data.Type=type;

        String url = getInputData().getString("url");
        if (url == null) {
            url = "x";
        }
        data.Url=url;

        String id = getInputData().getString("id");
        if (id == null) {
            id = "x";
        }
        data.id=Integer.parseInt(id);

        String time = getInputData().getString("time");
        if (time == null) {
            time = "x";
        }
        data.time=Double.valueOf(time);

        AppFlavor=getInputData().getString("flavor");

        NotiDB.notificationDAO().insertAll(data);

        NotificationShoot(data);

        return Result.success();
    }

    void NotificationShoot(NotifictionData data) {
        Intent intent = new Intent(mContext, NotificationActivity.class);
        intent.putExtra("flavor",AppFlavor);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

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

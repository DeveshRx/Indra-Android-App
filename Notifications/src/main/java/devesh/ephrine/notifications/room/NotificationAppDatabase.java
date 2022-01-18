package devesh.ephrine.notifications.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {NotifictionData.class},
        version = 1)
public abstract class NotificationAppDatabase extends RoomDatabase {
    public abstract NotificationDAO notificationDAO();
}

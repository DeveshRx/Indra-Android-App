package devesh.common.database.userdata;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class},
        version = 1)
public abstract class UserAppDatabase extends RoomDatabase {
    public abstract UserDAO userDao();
}
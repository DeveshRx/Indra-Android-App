package devesh.common.database.friends;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Friend.class},
        version = 1)
public abstract class FriendsListAppDatabase extends RoomDatabase {
    public abstract FriendsDAO friendsDao();
}
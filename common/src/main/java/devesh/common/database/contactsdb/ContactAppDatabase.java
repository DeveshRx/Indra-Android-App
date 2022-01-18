package devesh.common.database.contactsdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ContactUser.class},
        version = 1)
public abstract class ContactAppDatabase extends RoomDatabase {
    public abstract ContactsDAO contactDao();
}
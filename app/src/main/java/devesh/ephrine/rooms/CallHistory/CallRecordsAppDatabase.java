package devesh.ephrine.rooms.CallHistory;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CallRecord.class},
        version = 1)
public abstract class CallRecordsAppDatabase extends RoomDatabase {
    public abstract CallRecordsDAO callRecordsDAO();
}
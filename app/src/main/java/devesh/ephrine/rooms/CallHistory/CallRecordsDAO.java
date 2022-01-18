package devesh.ephrine.rooms.CallHistory;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;


@Dao
public interface CallRecordsDAO {
    @Insert
    void insertAll(CallRecord... callRecord);

    @Delete
    void delete(CallRecord callRecord);

    @Query("SELECT * FROM CallRecord")
    List<CallRecord> getAll();

    @Query("SELECT * FROM CallRecord WHERE uid = :uid ORDER BY time_epoch Desc")
    List<CallRecord> getRecords(String uid);

    @Query("DELETE FROM CallRecord")
    void nukeTable();

}
package devesh.ephrine.notifications.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;


@Dao
public interface NotificationDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(NotifictionData... msg);

    @Delete
    void delete(NotifictionData msg);

    @Query("SELECT * FROM NotifictionData")
    List<NotifictionData> getAll();

  //"SELECT * FROM User ORDER BY " + targetField + " ASC"

    @Query("SELECT * FROM NotifictionData ORDER BY time Desc")
    List<NotifictionData> getAllbyTime();

}
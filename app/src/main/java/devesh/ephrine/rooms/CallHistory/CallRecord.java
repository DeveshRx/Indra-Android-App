package devesh.ephrine.rooms.CallHistory;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class CallRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "uid")
    public String UID;

    @ColumnInfo(name = "time_epoch")
    public long time_epoch;

    @ColumnInfo(name = "time_formatted")
    public String time_formatted;

    /* Incoming-Outgoing
     * i = incoming
     * o = outgoing
     **/
    @ColumnInfo(name = "io")
    public String io;


}
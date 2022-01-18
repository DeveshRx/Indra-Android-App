package devesh.ephrine.notifications.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class NotifictionData {

    @PrimaryKey
    public int id;

    @ColumnInfo(name = "title")
    public String Title;

    @ColumnInfo(name = "short_message")
    public String Short_Message;

    @ColumnInfo(name = "url")
    public String Url;

    @ColumnInfo(name = "long_message")
    public String Long_Message;

    @ColumnInfo(name = "img")
    public String img;

    @ColumnInfo(name = "type")
    public String Type;

    @ColumnInfo(name = "icon")
    public String icon;


    @ColumnInfo(name = "time")
    public double time;
}
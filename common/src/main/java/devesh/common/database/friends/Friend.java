package devesh.common.database.friends;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "friends",indices = {@Index(value = {"uid"},
        unique = true)})
public class Friend {
    @PrimaryKey
    @NonNull
    public String phone;
    //  public int id;

    @ColumnInfo(name = "display_name")
    public String DisplayName;

    @ColumnInfo(name = "uid")
    public String UID;

    @ColumnInfo(name = "photo_url")
    public String photo;

     @ColumnInfo(name = "last_seen")
    public String LastSeen;

    @ColumnInfo(name = "activity_status")
    public String activity_status;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "bio")
    public String user_bio;

    @ColumnInfo(name = "isFav", defaultValue = "0")
    public int isFav;

//    @ColumnInfo(name = "is_app_user", defaultValue = "0")
//    public int isAppUser;



}

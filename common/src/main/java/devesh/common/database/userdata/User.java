package devesh.common.database.userdata;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "display_name")
    public String DisplayName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "photo_url")
    public String photoURL;


}
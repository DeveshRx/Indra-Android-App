package devesh.common.database.friends;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FriendsDAO {
    //  @Insert
    //void insertAll(Friend... users);

    @Delete
    void delete(Friend friend);

    @Query("SELECT * FROM friends")
    List<Friend> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRawContacts(Friend friend);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Friend> friend);

 //   @Query("SELECT * FROM friends WHERE is_app_user = 1 ORDER BY is_app_user ASC")
 //   List<Friend> getAppUsers();

    @Query("SELECT * FROM friends WHERE isFav = 1")
    List<Friend> getFavAppUsers();

  //  @Query("SELECT * FROM friends WHERE is_app_user = 0 ORDER BY is_app_user ASC")
 //   List<Friend> getNonAppUsers();

    @Query("UPDATE friends SET isFav=:isFav WHERE uid = :uid")
    void setFavUser(String uid, int isFav);

  //  @Query("UPDATE friends SET is_app_user=:is_app_user,uid=:uid WHERE phone = :phone")
   // void updateAppUser(String phone, String uid, int is_app_user);

    @Query("UPDATE friends SET last_seen=:last_seen WHERE phone = :phone")
    void updateAppUserLastSeen(String phone, String last_seen);

    @Query("UPDATE friends SET activity_status=:activity_status WHERE phone = :phone")
    void updateAppUserActivityStatus(String phone, String activity_status);

    @Query("UPDATE friends SET display_name=:display_name WHERE phone = :phone")
    void updateAppUserName(String phone, String display_name);

    @Query("UPDATE friends SET photo_url=:photoURL WHERE phone = :phone")
    void updateAppUserPhoto(String phone, String photoURL);

  //  @Query("UPDATE friends SET is_app_user=:is_app_user, display_name=:display_name, uid=:uid,last_seen=:last_seen,activity_status=:activity_status,photo_url=:photoURL WHERE phone = :phone")
  //  void updateAppUserFull(String phone, String display_name, String uid, int is_app_user, String last_seen, String activity_status, String photoURL);

    @Query("SELECT * FROM friends WHERE uid IN (:id)")
    List<Friend> getUserByUID(String id);

    @Query("SELECT * FROM friends WHERE phone IN (:phone)")
    List<Friend> getUserByPhone(String phone);

    /*

    @Query("SELECT * FROM product")
    List<Product> getAll();

    @Query("SELECT * FROM product WHERE product_id IN (:id)")
    List<Product> loadAllByIds(String id);

    /*@Query("SELECT * FROM product WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    Product findByName(String first, String last);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Product... products);

    @Query("UPDATE product SET inventory_stock=:inventory_stock WHERE product_id = :product_id")
    void update(String product_id, int inventory_stock);

    @Query("UPDATE product SET quantity=:qty, price=:price WHERE product_id = :product_id")
    void updateProductCart(String product_id, int qty, String price);

    @Delete
    void delete(Product product);

    @Query("DELETE FROM product")
    void nukeTable();*/
}
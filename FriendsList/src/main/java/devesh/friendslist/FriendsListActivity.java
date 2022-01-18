package devesh.friendslist;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import devesh.common.database.contactsdb.ContactAppDatabase;
import devesh.common.database.contactsdb.ContactUser;
import devesh.common.database.friends.Friend;
import devesh.common.database.friends.FriendsListAppDatabase;
import devesh.common.utils.CachePref;
import devesh.common.utils.HttpsReq;
import devesh.friendslist.adapters.MyContactAdapter;
import devesh.friendslist.adapters.MyFriendReqListAdapter;
import devesh.friendslist.adapters.MyFriendsListAdapter;
import devesh.friendslist.databinding.ActivityFriendsListBinding;
import devesh.friendslist.workmanager.FriendsSyncWorkManager;

public class FriendsListActivity extends AppCompatActivity {
    final static String HM_FRIEND_UID = "uid";
    String TAG = "FriendsList: ";
    ActivityFriendsListBinding mBinding;
    View mView;
    List<Friend> friendList;
    List<Friend> friendsReqList;
    List<ContactUser> contactUserList;
    FriendsListAppDatabase friendsListAppDatabase;
    FirebaseDatabase database;
    DatabaseReference FriendsRef;
    DatabaseReference FriendRequestListRef;
    FirebaseAuth mAuth;
    HttpsReq httpsReq;
    Gson gson;
    MyFriendsListAdapter myFavContactAdapter;
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    DatabaseReference contactsDBRef;
    ContactAppDatabase contactsDB;
    MyContactAdapter myContactAdapter;
    CachePref cachePref;
    ValueEventListener friendsListner;
    //Contact List Sync
    ValueEventListener contactListener;
    //Friend Requests
    ValueEventListener friendReqListListener;
    MyFriendReqListAdapter myFriendReqListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendList = new ArrayList<>();
        contactUserList = new ArrayList<>();

        mBinding = ActivityFriendsListBinding.inflate(getLayoutInflater());
        mView = mBinding.getRoot();
        setContentView(mView);

        database = FirebaseDatabase.getInstance();
        friendsListAppDatabase = Room.databaseBuilder(this, FriendsListAppDatabase.class, getString(R.string.DATABASE_FRIENDS_LIST_DB))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        contactsDB = Room.databaseBuilder(this, ContactAppDatabase.class, getString(R.string.DATABASE_CONTACTS_DB))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        mAuth = FirebaseAuth.getInstance();
        httpsReq = new HttpsReq(this);
        gson = new Gson();
        cachePref = new CachePref(this);

        friendList = friendsListAppDatabase.friendsDao().getAll();
        contactUserList = contactsDB.contactDao().getAppUsers();

        contactsDBRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/indra/contacts");

        FriendsRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/indra/friends");
        FriendRequestListRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/indra/friend_request");

        LoadFriendsRecycleView();
        if (friendList.isEmpty()) {
            FetchFriendsList();
        }

        LoadContactListRecycleView();
        if (contactUserList.isEmpty()) {
            FetchContactList();
        }

        mBinding.swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                        SyncContacts();
                        fetchFriendRequests();

                    }
                }
        );

        //Friend Request
        friendsReqList = new ArrayList<>();

        try {
            Type collectionType = new TypeToken<List<Friend>>() {
            }.getType();

            String frl = cachePref.getString(getString(R.string.Pref_cache_friend_request));
            friendsReqList = gson.fromJson(frl, collectionType);

            if (friendsReqList.isEmpty()) {
                Log.d(TAG, "onCreate: friendsReqList Downloading ");
                fetchFriendRequests();
            }
            Log.d(TAG, "onCreate: friendsReqList Loading from cache");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ERROR #3243 " + e);

            fetchFriendRequests();

        }

        LoadFriendReqListRecycleView();
        //  fetchFriendRequests();

        // LoadFriendReqListRecycleView();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (contactsDB.isOpen()) {
            contactsDB.close();
        }

        if (friendsListAppDatabase.isOpen()) {
            friendsListAppDatabase.close();
        }
        try {
            FriendsRef.removeEventListener(friendsListner);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: ERROR #4523 " + e);
        }
        try {
            contactsDBRef.removeEventListener(contactListener);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: ERROR #454 " + e);
        }

    }

    void LoadFriendsRecycleView() {
        Log.d(TAG, "LoadFriendsRecycleView: ");

        if(friendList!=null){

            mBinding.FriendsListRecycleview.removeAllViews();
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mBinding.FriendsListRecycleview.setLayoutManager(layoutManager);

            myFavContactAdapter = new MyFriendsListAdapter(this, friendList);
            mBinding.FriendsListRecycleview.setAdapter(myFavContactAdapter);

        }

    }

    void FetchFriendsList() {

        friendsListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {
                    List<String> FriendsUIDList = new ArrayList<>();
                    for (DataSnapshot DS : dataSnapshot.getChildren()) {
                        String friendUID = DS.getValue(String.class);
                        Log.d(TAG, "onDataChange: Friend UID: " + friendUID);
                        FriendsUIDList.add(friendUID);
                    }
                    ProcessFriendsList(FriendsUIDList);

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        };

        FriendsRef.addValueEventListener(friendsListner);

    }

    void ProcessFriendsList(List<String> list) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                friendList.clear();

                for (String friendUID : list) {
                    String data = null;
                    Friend friend = new Friend();
                    HashMap<String, String> dataHash = new HashMap<>();
                    dataHash.put("uid", friendUID);
                    Log.d(TAG, "run: UID: " + friendUID);

                    String jsonBody = gson.toJson(dataHash);
                    try {
                        Log.d(TAG, "run: JSON Body: " + jsonBody);
                        data = httpsReq.post(getString(R.string.URL_getProfile), jsonBody);
                        Log.d(TAG, "run: OKHTTP Received: " + data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "run: ", e);
                    }

                    try {

                        Type collectionType = new TypeToken<HashMap<String, String>>() {
                        }.getType();

                        HashMap<String, String> dataMap = new HashMap<>();
                        dataMap = gson.fromJson(data, collectionType);
                        if (dataMap.get("UserName") != null) {
                            friend.DisplayName = dataMap.get("UserName");
                        }
                        if (dataMap.get("UserEmail") != null) {
                            friend.email = dataMap.get("UserEmail");
                        }
                        if (dataMap.get("UserBio") != null) {
                            friend.user_bio = dataMap.get("UserBio");
                        }
                        if (dataMap.get("UserPhone") != null) {
                            friend.phone = dataMap.get("UserPhone");
                        }
                        if (dataMap.get("UserPic") != null) {
                            friend.photo = dataMap.get("UserPic");
                        }

                        friendList.add(friend);


                    } catch (Exception e) {
                        Log.e(TAG, "run: " + e);
                    }
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        friendsListAppDatabase.friendsDao().insertAll(friendList);
                        LoadFriendsRecycleView();
                    }
                });
            }
        });
    }

    void FetchContactList() {
        Log.d(TAG, "FetchContactList: ");
        contactListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                //Post post = dataSnapshot.getValue(Post.class);

                if (dataSnapshot != null) {
                    contactUserList.clear();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        ContactUser c = new ContactUser();
                        String phoneno = data.child("phone").getValue(String.class);
                        String uid = data.child("uid").getValue(String.class);
                        String name = null;
                        if (data.child("name").getValue(String.class) != null) {
                            name = data.child("name").getValue(String.class);
                        }

                        String photoURL = null;
                        if (data.child("photo").getValue(String.class) != null) {
                            photoURL = data.child("photo").getValue(String.class);
                        }
                        addToDB(name, uid, phoneno, photoURL);
                        contactUserList.add(c);
                    }

                    Log.d(TAG, "onDataChange: FINISHED: ");
                    LoadContactListRecycleView();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

            }
        };
        contactsDBRef.addValueEventListener(contactListener);

    }

    void addToDB(String name, String uid, String phone, String photo) {
        contactsDB.contactDao().updateAppUser(phone, uid, 1);
        if (name != null) {
            contactsDB.contactDao().updateAppUserName(phone, name);
        }
        if (phone != null) {
            contactsDB.contactDao().updateAppUserPhoto(phone, photo);
        }
    }

    void LoadContactListRecycleView() {

        Log.d(TAG, "LoadContactListRecycleView: ");

        if(contactUserList!=null){
            mBinding.ContactListRecycleview.removeAllViews();
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mBinding.ContactListRecycleview.setLayoutManager(layoutManager);

            myContactAdapter = new MyContactAdapter(this, contactUserList);
            mBinding.ContactListRecycleview.setAdapter(myContactAdapter);
        }


    }

    void SyncContacts() {
        WorkRequest friendSyncWork = new OneTimeWorkRequest.Builder(FriendsSyncWorkManager.class)
                .build();
        WorkManager.getInstance(this)
                .enqueue(friendSyncWork);
        mBinding.swiperefresh.setRefreshing(false);
    }

    public void SendFriendRequest(String FriendUID) {
        Log.d(TAG, "SendFriendRequest: " + FriendUID);
        HashMap<String, String> body = new HashMap<>();
        body.put("useruid", mAuth.getCurrentUser().getUid());
        body.put("frienduid", FriendUID);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = httpsReq.post("getString(R.string.URL_SendFriendRequest)", gson.toJson(body));
                    Log.d(TAG, "SendFriendRequest: RESPONSE: " + response);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FriendsListActivity.this, "Friend Request Sent :)", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "SendFriendRequest: ERROOR #7689 " + e);
                }
            }
        });

    }

    void fetchFriendRequests() {
        Log.d(TAG, "fetchFriendRequests: ");
        friendReqListListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {
                    Log.d(TAG, "onDataChange: friendReqListListener ");
                    List<String> FriendReqUIDList = new ArrayList<>();
                    for (DataSnapshot DS : dataSnapshot.getChildren()) {
                        String friendUID = DS.getValue(String.class);
                        Log.d(TAG, "onDataChange: Friend Req UID: " + friendUID);
                        FriendReqUIDList.add(friendUID);
                    }
                    ProcessFriendsReqList(FriendReqUIDList);

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        };
        FriendRequestListRef.addListenerForSingleValueEvent(friendReqListListener);

    }

    void ProcessFriendsReqList(List<String> list) {
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "run: ProcessFriendsReqList");
                if (!friendsReqList.isEmpty()) {
                    friendsReqList.clear();
                    friendsReqList = new ArrayList<>();
                }

                for (String friendUID : list) {
                    String data = null;
                    Friend friend = new Friend();

                    HashMap<String, String> dataHash = new HashMap<>();
                    dataHash.put("uid", friendUID);
                    Log.d(TAG, "run: UID: " + friendUID);
                    friend.UID = friendUID;
                    String jsonBody = gson.toJson(dataHash);

                    try {
                        Log.d(TAG, "run: ProcessFriendsReqList JSON Body: " + jsonBody);
                        data = httpsReq.post(getString(R.string.URL_getProfile), jsonBody);
                        Log.d(TAG, "run: OKHTTP Received: ProcessFriendsReqList " + data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "run: ", e);
                    }

                    try {
                        Type collectionType = new TypeToken<HashMap<String, String>>() {
                        }.getType();
                        HashMap<String, String> dataMap = new HashMap<>();
                        dataMap = gson.fromJson(data, collectionType);
                        if (dataMap.get("UserName") != null) {
                            friend.DisplayName = dataMap.get("UserName");
                        }
                        if (dataMap.get("UserEmail") != null) {
                            friend.email = dataMap.get("UserEmail");
                        }
                        if (dataMap.get("UserBio") != null) {
                            friend.user_bio = dataMap.get("UserBio");
                        }
                        if (dataMap.get("UserPhone") != null) {
                            friend.phone = dataMap.get("UserPhone");
                        }

                        if (dataMap.get("UserPic") != null) {
                            friend.photo = dataMap.get("UserPic");
                        }

                        friendsReqList.add(friend);
                    } catch (Exception e) {
                        Log.e(TAG, "run: " + e);
                    }
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        cachePref.setString(getString(R.string.Pref_cache_friend_request), gson.toJson(friendsReqList));
                        LoadFriendReqListRecycleView();

                    }
                });
            }

        });

    }

    void LoadFriendReqListRecycleView() {
        Log.d(TAG, "LoadFriendReqListRecycleView: friendsReqList.size ");



        if(friendsReqList!=null){
mBinding.LLFriendRequest.setVisibility(View.VISIBLE);
if(friendsReqList.isEmpty()){
    mBinding.LLFriendRequest.setVisibility(View.GONE);

}
            mBinding.FriendRequestRecycleview.removeAllViews();
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mBinding.FriendRequestRecycleview.setLayoutManager(layoutManager);

            myFriendReqListAdapter = new MyFriendReqListAdapter(this, friendsReqList);
            mBinding.FriendRequestRecycleview.setAdapter(myFriendReqListAdapter);

        }else{
            mBinding.LLFriendRequest.setVisibility(View.GONE);

        }

    }

    public void AcceptFriendReq(String FriendUID, int position) {
        HashMap<String, String> body = new HashMap<>();
        body.put("ownerUID", mAuth.getCurrentUser().getUid());
        body.put("friendUID", FriendUID);

        Toast.makeText(FriendsListActivity.this, "Adding Friend", Toast.LENGTH_SHORT).show();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = httpsReq.post("getString(R.string.URL_AcceptFriendReq)", gson.toJson(body));

                    Log.d(TAG, "SendFriendRequest: RESPONSE: " + response);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            friendsReqList.remove(position);

                            cachePref.setString(getString(R.string.Pref_cache_friend_request), gson.toJson(friendsReqList));

                            FriendRequestListRef.child(FriendUID).setValue(null);

                            myFriendReqListAdapter.notifyDataSetChanged();

                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "SendFriendRequest: ERROOR #7689 " + e);
                }
            }
        });


    }

    public void RejectFriendReq(String FriendUID, int position) {
        FriendRequestListRef.child(FriendUID).setValue(null);
        friendsReqList.remove(position);

        cachePref.setString(getString(R.string.Pref_cache_friend_request), gson.toJson(friendsReqList));

        myFriendReqListAdapter.notifyDataSetChanged();


    }

}


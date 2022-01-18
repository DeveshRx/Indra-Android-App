package devesh.ephrine.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import devesh.common.database.contactsdb.ContactAppDatabase;
import devesh.common.database.contactsdb.ContactUser;
import devesh.common.utils.CachePref;
import devesh.ephrine.BuildConfig;
import devesh.ephrine.MainActivity;
import devesh.ephrine.R;
import devesh.ephrine.adapters.CallRecordsAdapter;
import devesh.ephrine.rooms.CallHistory.CallRecord;
import devesh.ephrine.rooms.CallHistory.CallRecordsAppDatabase;
import devesh.ephrine.util.AppAnalytics;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ContactProfileFragment extends Fragment {

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    TextView UserBioTextView;
    ImageView ContactProfilePhoto;
    TextView UserNameTx;
    TextView UserPhoneTx;
    TextView UserBioTx;

    ContactAppDatabase contactsDB;

    ContactUser contactUser;

    Gson gson;
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    String URL_getProfile;
    OkHttpClient client;
    String TAG = "ProfileFrag: ";
    String uid;
    FloatingActionButton PingUserButton;
    FloatingActionButton CallUserButton;
    FloatingActionButton VOIPbutton;

    Button ClearHistoryButton;
    Button BlockButton;

    CallRecordsAppDatabase CallRecordDB;
    List<CallRecord> callRecordList;
    RecyclerView callLogsRecycleView;
    TextView NoCallHistoryText;
    AdView mAdView;
    FirebaseUser fUser;
    FirebaseAuth mAuth;

    FirebaseAnalytics mFirebaseAnalytics;

    AppAnalytics appAnalytics;
    FirebaseDatabase database;

    DatabaseReference myBlockListRef;
    boolean isFriendBlocked;
    ValueEventListener friendBlockEventListner;
    LinearLayout LLBlocked;
    LinearLayout LLCallControl;
    File ProfilePicFile;
    boolean AdsEnabled;
    CachePref cachePref;

    public ContactProfileFragment() {
        super(R.layout.fragment_contact_profile);
    }

    Activity mActivity;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        if (mActivity == null) {
            return;
        }

        cachePref=new CachePref(getActivity());
        AdsEnabled=cachePref.getBoolean(getString(R.string.Pref_AdsEnabled));


        appAnalytics = new AppAnalytics(mActivity.getApplication(), getActivity());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mActivity);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();


        ((AppCompatActivity) mActivity).getSupportActionBar().hide();
        ((MainActivity) mActivity).NavBarVisibility(0);

        uid = requireArguments().getString("uid");
        UserBioTextView = view.findViewById(R.id.UserBioTextView);
        ContactProfilePhoto = view.findViewById(R.id.ContactProfilePhoto);
        UserNameTx = view.findViewById(R.id.userNameTxeView);
        UserPhoneTx = view.findViewById(R.id.userPhoneTx);
        UserBioTx = view.findViewById(R.id.UserBioTextView);
        PingUserButton = view.findViewById(R.id.PingUserButton);
        callLogsRecycleView = view.findViewById(R.id.callLogsRecycleView);
        NoCallHistoryText = view.findViewById(R.id.NoCallHistoryText);
        CallUserButton = view.findViewById(R.id.CallUserButton);
        mAdView = view.findViewById(R.id.adViewContactProfile);
        VOIPbutton = view.findViewById(R.id.VOIPbutton);
        ClearHistoryButton = view.findViewById(R.id.ClearHistoryButton);
        BlockButton = view.findViewById(R.id.BlockButton);
        LLBlocked = view.findViewById(R.id.LLBlocked);
        LLCallControl = view.findViewById(R.id.LLCallControl);

        gson = new Gson();

        URL_getProfile = getString(R.string.URL_getProfile);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(120, TimeUnit.SECONDS);
        builder.readTimeout(120, TimeUnit.SECONDS);
        builder.writeTimeout(120, TimeUnit.SECONDS);
        // builder.followSslRedirects(true);
        //   builder.followRedirects(true);
        //  builder.dns(new DNSConfig());
        //   builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));
        if (!BuildConfig.DEBUG) {
            builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));
        }
        // client = new OkHttpClient();
        client = builder.build();

        contactUser = new ContactUser();

        contactsDB = Room.databaseBuilder(mActivity, ContactAppDatabase.class, getString(R.string.DATABASE_CONTACTS_DB))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        CallRecordDB = Room.databaseBuilder(mActivity, CallRecordsAppDatabase.class, getString(R.string.DATABASE_CALL_RECORDS))
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();


        callRecordList = CallRecordDB.callRecordsDAO().getRecords(uid);

        contactUser = contactsDB.contactDao().getUserByUID(uid).get(0);

        PingUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mActivity).PingUser(uid);
            }
        });

        CallUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mActivity).SelectContact2Call(uid);
            }
        });

        VOIPbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)mActivity).SelectContact2VoiceCall(uid);
            }
        });

        BlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFriendBlocked) {
                    // Task: Unblock him/her
                    myBlockListRef.removeValue();
                    isFriendBlocked = false;
                    LLBlocked.setVisibility(View.GONE);
                    LLCallControl.setVisibility(View.VISIBLE);
                } else {
                    // Task: Block him/her
                    myBlockListRef.setValue(uid);
                    isFriendBlocked = true;
                    LLBlocked.setVisibility(View.VISIBLE);
                    LLCallControl.setVisibility(View.GONE);
                }

            }
        });

        myBlockListRef = database.getReference("users/" + fUser.getUid() + "/indra/block_list/" + uid);
        friendBlockEventListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getValue(String.class).equals(uid)) {
                            isFriendBlocked = true;
                            BlockButton.setText("Unblock");
                            LLBlocked.setVisibility(View.VISIBLE);
                            LLCallControl.setVisibility(View.GONE);
                        } else {
                            isFriendBlocked = false;
                            BlockButton.setText("Block");
                            LLBlocked.setVisibility(View.GONE);
                            LLCallControl.setVisibility(View.VISIBLE);
                        }
                    } else {
                        isFriendBlocked = false;
                        BlockButton.setText("Block");
                        LLBlocked.setVisibility(View.GONE);
                        LLCallControl.setVisibility(View.VISIBLE);
                    }
                } else {
                    isFriendBlocked = false;
                    BlockButton.setText("Block");
                    LLBlocked.setVisibility(View.GONE);
                    LLCallControl.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        };
        myBlockListRef.addValueEventListener(friendBlockEventListner);

        File myDir = new File(mActivity.getFilesDir(), "profilepics");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        ProfilePicFile = new File(myDir, contactUser.UID + ".jpg");


        LoadProfile();
        getProfileRequest();
        LoadCallRecords();

        if(AdsEnabled){

            AdRequest adRequest = ((MainActivity) mActivity).getBannerAdRequest();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    // Code to be executed when an ad request fails.
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });


        }

        ClearHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> adata = new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), "Call History Deleted");
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW), adata);

                // ((MainActivity) getActivity()).SelectContact2VoiceCall(uid);
                CallRecordDB.callRecordsDAO().nukeTable();
                callRecordList.clear();
                Toast.makeText(getContext(), "Deleted Call Records", Toast.LENGTH_SHORT).show();
                LoadCallRecords();

            }

        });

        Map<String, String> adata = new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW), "Contact Profile Screen");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW), adata);

    }

    @Override
    public void onDestroy() {
        if(AdsEnabled){
            ((MainActivity) mActivity).newBannerAdRequest();
        }

        try {
            myBlockListRef.removeEventListener(friendBlockEventListner);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: ERROR #453455 " + e);
        }
        super.onDestroy();

    }

    public void LoadProfile() {

        UserNameTx.setText(contactUser.DisplayName);
        UserPhoneTx.setText(contactUser.phone);
        UserBioTx.setText(contactUser.user_bio);

        /*ViewGroup.MarginLayoutParams imageViewParams = new ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.MATCH_PARENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT);*/
        /*RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);*/
        DisplayMetrics displayMetrics = new DisplayMetrics();

        int dwidth= RelativeLayout.LayoutParams.WRAP_CONTENT;
        if(mActivity!=null){
           mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
             dwidth = displayMetrics.widthPixels;
        }


        //int dheight = displayMetrics.heightPixels;
        //int dwidth = displayMetrics.widthPixels;

        ViewGroup.LayoutParams imageViewParams = ContactProfilePhoto.getLayoutParams();
        imageViewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        imageViewParams.height = dwidth;


        //Glide.get(this).clearMemory();
        if (ProfilePicFile.exists()) {
            Log.d(TAG, "setProfileImage: ProfilePicFile.exists()");
            Bitmap myBitmap = BitmapFactory.decodeFile(ProfilePicFile.getAbsolutePath());
            ContactProfilePhoto.setImageBitmap(myBitmap);
       //     ContactProfilePhoto.setLayoutParams(imageViewParams);

            Log.d(TAG, "setProfileImage: ProfilePicFile.exists() APPLIED");

        }
        if (mActivity != null) {

  /*          Glide.with(this).load(contactUser.photo)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ContactProfilePhoto);
*/
            Glide.with(this).asBitmap().load(contactUser.photo).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    try {
                        Log.d(TAG, "ProfilePic onResourceReady: ");
                        //  File file = new File(getActivity().getFilesDir(), FILE_PROFILE_PIC);
                        FileOutputStream out = new FileOutputStream(ProfilePicFile);
                        resource.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();

                        ContactProfilePhoto.setImageBitmap(resource);
                     //   ContactProfilePhoto.setLayoutParams(imageViewParams);

                        Log.d(TAG, "ProfilePic onResourceReady: SAVED");

                    } catch (Exception e) {
                        Log.d(TAG, "Profile Pic ERROR #45643 " + e);
                    }


                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }
            });

        }
    }

    public void getProfileRequest() {
        executorService.execute(new Runnable() {
            String data;

            @Override
            public void run() {
                HashMap<String, String> dataHash = new HashMap<>();
                dataHash.put("uid", contactUser.UID);
                Log.d(TAG, "run: UID: " + contactUser.UID);

                String jsonBody = gson.toJson(dataHash);
                try {
                    Log.d(TAG, "run: JSON Body: " + jsonBody);
                    data = post(URL_getProfile, jsonBody);
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
                        contactUser.DisplayName = dataMap.get("UserName");
                    }
                    if (dataMap.get("UserEmail") != null) {
                        contactUser.email = dataMap.get("UserEmail");
                    }
                    if (dataMap.get("UserBio") != null) {
                        contactUser.user_bio = dataMap.get("UserBio");
                    }
                    if (dataMap.get("UserPic") != null) {
                        contactUser.photo = dataMap.get("UserPic");
                    }
                    contactsDB.contactDao().insertRawContacts(contactUser);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            UserNameTx.setText(contactUser.DisplayName);
                            UserPhoneTx.setText(contactUser.phone);
                            UserBioTx.setText(contactUser.user_bio);
                            LoadProfile();
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "run: " + e);
                }


            }

            String post(String url, String json) throws IOException {
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    return response.body().string();
                }
            }
        });
    }

    void LoadCallRecords() {
        if (callRecordList.isEmpty()) {
            NoCallHistoryText.setVisibility(View.VISIBLE);
            ClearHistoryButton.setVisibility(View.GONE);
        } else {
            NoCallHistoryText.setVisibility(View.GONE);
            ClearHistoryButton.setVisibility(View.VISIBLE);

            callLogsRecycleView.setHasFixedSize(true);
            //recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
            callLogsRecycleView.setLayoutManager(layoutManager);
            CallRecordsAdapter mAdapter = new CallRecordsAdapter(mActivity, callRecordList);
            callLogsRecycleView.setAdapter(mAdapter);
        }
    }


}
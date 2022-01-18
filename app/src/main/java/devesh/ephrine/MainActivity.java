package devesh.ephrine;

import static devesh.ephrine.util.ContactUtil.getContactName;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import devesh.common.utils.CachePref;
import devesh.ephrine.AppRTC.WebRTCVideoCall;
import devesh.ephrine.AppRTC.WebRTCVoiceCall;
import devesh.common.ViewModel.AppDataViewModel;
import devesh.ephrine.appreview.AppReviewTask;
import devesh.ephrine.fragments.ContactProfileFragment;
import devesh.ephrine.fragments.HomeFragment;
import devesh.ephrine.fragments.LoginFragment;
import devesh.ephrine.fragments.StartUpScreenFragment;
import devesh.ephrine.notifications.NotificationActivity;
import devesh.common.database.contactsdb.ContactAppDatabase;
import devesh.common.database.contactsdb.ContactUser;
import devesh.common.database.userdata.UserAppDatabase;
import devesh.ephrine.theme.AppThemePreferences;
import devesh.ephrine.util.AppAnalytics;
import devesh.common.utils.AppFlavour;
import devesh.common.utils.VerifyAppInstall;
import devesh.ephrine.workmanager.ContactSyncWorkManager;
import devesh.ephrine.workmanager.ContactsUpdateWorkManager;
import devesh.ephrine.workmanager.CreateCallRecordWorker;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.distribute.Distribute;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private static final int PERMISSION_REQUEST = 2;
    private static final int CONNECTION_REQUEST = 1;
    private static final int REMOVE_FAVORITE_INDEX = 0;
    private static boolean commandLineRun;
    public OkHttpClient client;
    //public FlutterEngine flutterEngine;
    String TAG = "indra: ";
    FirebaseUser fUser;
    FirebaseDatabase database;
    UserAppDatabase AppDB;
    //   RecyclerView contactsRecycleView;
    //  MyContactAdapter myContactAdapter;
    ContactAppDatabase contactsDB;
    AppDataViewModel appLiveModel;
    List<ContactUser> contactUserList;
    //BottomNavigationView bottomNavigationView;
    Fragment ScreenFragment;
    FirebaseAuth mAuth;
    boolean AdShowed;
    AdRequest adRequest;
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    boolean isKeyprefSSProtected;
    AppThemePreferences appThemePreferences;
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    private InterstitialAd mInterstitialAd;
    private SharedPreferences sharedPref;

    private FirebaseAnalytics mFirebaseAnalytics;
    Gson gson;

    AppAnalytics appAnalytics;
    VerifyAppInstall verifyAppInstall;
CachePref cachePref;
    boolean AdsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        /*if(BuildConfig.DEBUG){
            firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance());
        }else{
            firebaseAppCheck.installAppCheckProviderFactory(
                    SafetyNetAppCheckProviderFactory.getInstance());
        }*/


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        appThemePreferences = new AppThemePreferences(MainActivity.this);
        appThemePreferences.ThemeApply();

        setContentView(R.layout.activity_main);
        gson = new Gson();

        cachePref=new CachePref(this);

        AdsEnabled=cachePref.getBoolean(getString(R.string.Pref_AdsEnabled));
        AdShowed = false;
/*
        if(BuildConfig.FLAVOR.equals(AppFlavour.INTERNAL)){
            Distribute.setUpdateTrack(UpdateTrack.PRIVATE);
        }else if(BuildConfig.FLAVOR.equals(AppFlavour.UNIVERSAL)){
            Distribute.setUpdateTrack(UpdateTrack.PUBLIC);
        }*/

       if(BuildConfig.DEBUG){
           Distribute.setEnabledForDebuggableBuild(true);
       }
        // AppCenter.start(getApplication(),getString(R.string.MS_AppCenter_API_Key), Analytics.class, Crashes.class, Distribute.class);
        AppCenter.configure(getApplication(), getString(R.string.MS_AppCenter_API_Key));
        if (AppCenter.isConfigured()) {
            AppCenter.start(Analytics.class);
            AppCenter.start(Crashes.class);
          //  AppCenter.start(Distribute.class);
        }

        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();

        appAnalytics =new AppAnalytics(getApplication(),this);

        database = FirebaseDatabase.getInstance();
        contactUserList = new ArrayList<>();
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        //  bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (fUser != null) {
            init();
            createNotificationChannels();
            mFirebaseAnalytics.setUserId(fUser.getUid());

        } else {

            ShowStartScreen();
          // LoginNow();
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            Log.d(TAG, "onCreate: activityManager.isBackgroundRestricted() = " + activityManager.isBackgroundRestricted());
        }


        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),"Home Screen");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);
       
    }



    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        // Save the user's current game state
        //  savedInstanceState.putInt(STATE_SCORE, currentScore);
          // savedInstanceState.putInt("nav", ScreenFragment.getId());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_notification:
                openNotification();
                return true;
       //     case R.id.menu_friends:
               // Intent intent1 = new Intent(this, FriendsListActivity.class);
               // startActivity(intent1);
         //       return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        if(ScreenFragment!=null){
            ScreenFragment.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(ScreenFragment!=null){
            ScreenFragment.onPause();
        }

        super.onPause();
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
  /*      if (savedInstanceState.getInt("nav") == R.id.NavBottomBarHome) {
            ScreenFragment = new HomeScreenFragment();
        } else if (savedInstanceState.getInt("nav") == R.id.NavBottomBarShop) {
            ScreenFragment = new ViewProductsFragment();
        }
        // Restore state members from saved instance

        loadFragment(ScreenFragment, 1);
*/
    }

    @Override
    protected void onDestroy() {
        if (contactsDB != null) {
            if (contactsDB.isOpen()) {
                contactsDB.close();
            }
        }

        if (AppDB != null) {
            if (AppDB.isOpen()) {
                AppDB.close();
            }
        }
if(ScreenFragment!=null){
    ScreenFragment.onDestroy();

}

if(contactsDBRef!=null){
    contactsDBRef.removeEventListener(contactListener);
}

        super.onDestroy();
    }

    void LoadADS() {
        // MobileAds.initialize(this);
        if(AdsEnabled){
            Log.d(TAG, "LoadADS: Ads Enabled by Admin");
            try{
                MobileAds.initialize(this, new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {

                        //AdRequest adRequest = new AdRequest.Builder().build();
                        LoadInterstitialAd();
                        newBannerAdRequest();

                    }
                });


            }catch (Exception e){
                Log.e(TAG, "LoadADS: ERROR: "+e );
            }
        }else{
            Log.d(TAG, "LoadADS: Ads Disabled by Admin");
        }

    }

    void LoadInterstitialAd(){
        if(AdsEnabled){
            Log.d(TAG, "LoadADS: Ads Enabled by Admin");
            if(!BuildConfig.FLAVOR.equals(AppFlavour.PLAY_STORE)){
                AdRequest adRequest = new AdRequest.Builder().build();

                InterstitialAd.load(MainActivity.this, getString(R.string.AdMob_Int_Id1), adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                                AdShowed = true;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                                AdShowed = true;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                Log.d("TAG", "The ad was shown.");
                                AdShowed = true;
                            }
                        });

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });

            }
        }else{
            Log.d(TAG, "LoadADS: Ads Disabled by Admin");
        }

       }
    public AdRequest getBannerAdRequest() {
        if(AdsEnabled){
            Log.d(TAG, "LoadADS: Ads Enabled by Admin");
            if(adRequest!=null){
                return adRequest;
            }else{
                return new AdRequest.Builder().build();
            }
        }else{
            Log.d(TAG, "LoadADS: Ads Disabled by Admin");
            return null;
        }

    }

    public void newBannerAdRequest() {
        if(AdsEnabled){
            Log.d(TAG, "LoadADS: Ads Enabled by Admin");
            adRequest = new AdRequest.Builder().build();
        }else{
            Log.d(TAG, "LoadADS: Ads Disabled by Admin");
        }
    }

    DatabaseReference contactsDBRef;
    ValueEventListener contactListener;
    DatabaseReference UserStatusRef;
    public void init() {
        mFirebaseAnalytics.setUserId(mAuth.getCurrentUser().getUid());
        //  Bundle bundle = new Bundle();
        //   bundle.putString("some_int", "0");
       // LoadADS();


        requestPermissions();

        AppDB = Room.databaseBuilder(this, UserAppDatabase.class, getString(R.string.DATABASE_APP_USER_DB))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        contactsDB = Room.databaseBuilder(this, ContactAppDatabase.class, getString(R.string.DATABASE_CONTACTS_DB))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        PreferenceManager.setDefaultValues(this, R.xml.apprtc_preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        isKeyprefSSProtected = sharedPref.getBoolean(getString(R.string.settings_screenshot_protection), true);
        Log.d(TAG, "AppRTCInit: isKeyprefSSProtected : " + isKeyprefSSProtected);

        UserStatusRef = database.getReference("users/" + fUser.getUid() + "/indra/UserStatus");


      /*  bottomNavigationView.setVisibility(View.VISIBLE);
          bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int a = 0;
              switch (item.getItemId()) {
                  case R.id.nav_home:
                      ScreenFragment = new HomeFragment();
                      a = 2;
                      loadFragment(ScreenFragment, a);

                      if(CurrentFragmentScreen!=R.id.NavBottomBarHome){
                          CurrentFragmentScreen=item.getItemId();
                        }

                        break;
                  case R.id.nav_status:
                       if(CurrentFragmentScreen!=R.id.NavBottomBarShop){
                            ScreenFragment = new ViewProductsFragment();
                            a = 1;
                            loadFragment(ScreenFragment, a);
                            CurrentFragmentScreen=item.getItemId();
                        }

                        break;


                }
                return true;
            }
        });
      */
        //CurrentFragmentScreen=bottomNavigationView.getSelectedItemId();

        //  FragProfile.setVisibility(View.GONE);
        if (ScreenFragment == null) {
            loadFragment(new HomeFragment(), 1);
            //     CurrentFragmentScreen=R.id.NavBottomBarHome;

        } else {
            loadFragment(ScreenFragment, 1);
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, "FCM registration Token: " + msg);
                        sendToken2DB(msg);

                        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.notification_FCM_Topic_General))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        String msg = "FCM Topic msg_subscribed";
                                        if (!task.isSuccessful()) {
                                            msg = "FCM Topic msg_subscribe_failed";
                                        }
                                        Log.d(TAG, msg);
                                        //    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                });

  /*      getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, new HomeFragment(), null)
                .setReorderingAllowed(true)
              //  .addToBackStack(null)
                .commit();
*/



        appLiveModel = new ViewModelProvider(this).get(AppDataViewModel.class);

        getContactListData();
        getFavContactListData();

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

        FirebaseConfig();

        AppReviewProcess();

        verifyAppInstall=new VerifyAppInstall();
        boolean isPlayStoreApp = verifyAppInstall.verifyInstallerId(this);

        Log.d(TAG, "init: isPlayStoreApp:"+isPlayStoreApp+"\nSource:"+verifyAppInstall.getInstallSource(this)+"\ngetInstallSourceRAW: "+verifyAppInstall.getInstallSourceRAW(this));

        if(!BuildConfig.FLAVOR.equals(AppFlavour.INTERNAL)){
         
        if (AppCenter.isConfigured()) {
            if(!BuildConfig.FLAVOR.equals(AppFlavour.INTERNAL)){
                AppCenter.start(Distribute.class);

            }
        }
        }


        getCurrentUserStatus();

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: ");
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        Log.d(TAG, "onPermissionsGranted: ");
        init();
        WorkRequest contactSyncWork = new OneTimeWorkRequest.Builder(ContactSyncWorkManager.class)
                .build();
        WorkManager.getInstance(this)
                .enqueue(contactSyncWork);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        Log.d(TAG, "onPermissionsDenied: ");

    }

    void LoadFutterEngine() {
        /* EXPERIMENTAL
        flutterEngine = new FlutterEngine(this);

        // Start executing Dart code to pre-warm the FlutterEngine.
        flutterEngine.getDartExecutor().executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        );

        // Cache the FlutterEngine to be used by FlutterActivity.
        FlutterEngineCache
                .getInstance()
                .put("flutter_engine", flutterEngine);
*/
    }

    private boolean loadFragment(Fragment fragment, int anim) {
        if (fragment != null) {

            int enter;
            int exit;
            int popEnter;
            int popExit;

            if (anim == 0) {
                enter = R.anim.fade_in;
                exit = R.anim.fade_out;
                popEnter = R.anim.fade_in;
                //  popExit = R.anim.slide_up_out;
            } else if (anim == 1) {
                //Right to Left
                enter = R.anim.slide_right_to_left;
                exit = R.anim.slide_right_to_left_end;
                popEnter = R.anim.slide_left_to_right;
                popExit = R.anim.slide_right_to_left;
            } else if (anim == 2) {
                //Left to Right
                enter = R.anim.slide_left_to_right;
                exit = R.anim.slide_left_to_right_end;
                popEnter = R.anim.slide_right_to_left;
                popExit = R.anim.slide_left_to_right;
            } else {
                enter = R.anim.fade_in;
                exit = R.anim.fade_out;
                popEnter = R.anim.fade_in;
                //popExit = R.anim.slide_up_out;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(enter,  // enter
                            exit // exit
                    )
                    .replace(R.id.fragment_container_view, fragment, "home_frag")
                    .commit();

            return true;
        }
        return false;
    }

    public void SelectContact2Call(String contact2id) {
        isKeyprefSSProtected = sharedPref.getBoolean(getString(R.string.settings_screenshot_protection), true);


        Random rand = new Random();

        // Generate random integers in range 0 to 999
        int rand_int1 = rand.nextInt(10000);
        ContactUser contact = contactsDB.contactDao().getUserByUID(contact2id).get(0);
        CreateCallRecord(contact.UID, "o");

        Intent intent = new Intent(this, WebRTCVideoCall.class);
        intent.putExtra(getString(R.string.WebRTC_PEER_ID), "A" + String.valueOf(rand_int1));
        intent.putExtra(getString(R.string.call_type), "0");
        intent.putExtra(getString(R.string.WebRTC_CallReceiverID), contact2id);
        intent.putExtra(getString(R.string.intent_Caller_User_Name), contact.DisplayName);
        intent.putExtra(getString(R.string.intent_Caller_Phone), contact.phone);

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Video Call");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);


        startActivity(intent);

        // connectToRoom(String.valueOf(rand_int1), false, false, false, 0, contact2id, contact);


    }

    public void SelectContact2VoiceCall(String contact2id) {
        isKeyprefSSProtected = sharedPref.getBoolean(getString(R.string.settings_screenshot_protection), true);

        Random rand = new Random();

        // Generate random integers in range 0 to 999
        int rand_int1 = rand.nextInt(10000);
        ContactUser contact = contactsDB.contactDao().getUserByUID(contact2id).get(0);
        CreateCallRecord(contact.UID, "o");

        Intent intent = new Intent(this, WebRTCVoiceCall.class);
        intent.putExtra(getString(R.string.WebRTC_PEER_ID), "A" + String.valueOf(rand_int1));
        intent.putExtra(getString(R.string.call_type), "0");
        intent.putExtra(getString(R.string.WebRTC_CallReceiverID), contact2id);
        intent.putExtra(getString(R.string.intent_Caller_User_Name), contact.DisplayName);
        intent.putExtra(getString(R.string.intent_Caller_Phone), contact.phone);

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"VOIP Called");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);


        startActivity(intent);

        // connectToRoom(String.valueOf(rand_int1), false, false, false, 0, contact2id, contact);


    }

    public void LoginButton(View v){
        LoginNow();
    }
    void LoginNow() {
        NavBarVisibility(0);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        getSupportFragmentManager().beginTransaction()
                // .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, new LoginFragment(), null)
                .commit();

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"User Not Login");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

        Map<String,String> adata2=new HashMap<>();
        adata2.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),"Login Screen");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata2);

    //    Map<String,String> adata3=new HashMap<>();
    //    adata3.put("Install Source",verifyAppInstall.getInstallSourceRAW(this));
    //    appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),adata3);

    }
    void ShowStartScreen() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        getSupportFragmentManager().beginTransaction()
                // .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, new StartUpScreenFragment(), null)
                .commit();
        NavBarVisibility(0);

    }

    public void LoginComplete(FirebaseUser user) {
        fUser = user;
        requestPermissions();

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"User Login Success");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

        cachePref.setBoolean(getString(R.string.Pref_UserConfig_TurnServer),true);

    }

    void sendToken2DB(String token) {
        DatabaseReference tokenDB = database.getReference("users/" + fUser.getUid() + "/indra/instanceid");
        tokenDB.setValue(token);
        Log.d(TAG, "sendToken2DB: " + token);
    }

    private void createNotificationChannels() {
        //   Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          /*  NotificationChannel basicNotificationChanel = new NotificationChannel(CHANNEL_ID,
                    "basic notification chanel",
                    NotificationManager.IMPORTANCE_DEFAULT);
          */
            NotificationChannel priorityNotificationChannel = new NotificationChannel(getString(R.string.notification_channel_id_call),
                    getString(R.string.notification_channel_id_call_text),
                    NotificationManager.IMPORTANCE_HIGH);


            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            //      notificationManager.createNotificationChannel(basicNotificationChanel);
            notificationManager.createNotificationChannel(priorityNotificationChannel);
        }
    }

    public void Edit2Fav(String UID, int isFav) {
        Log.d(TAG, "Edit2Fav: UID:" + UID + "  isFav:" + isFav);
        contactsDB.contactDao().setFavUser(UID, isFav);
        appLiveModel.getAppContactsList().setValue(contactsDB.contactDao().getAppUsers());
        appLiveModel.getAppFavContactsList().setValue(contactsDB.contactDao().getFavAppUsers());

    }

    public void signout(View v) {

        FirebaseAuth.getInstance().signOut();
        LoginNow();

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"User Signed Out");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

    }

    /*void LoadContactList() {

        //    contactUserList=contactsDB.contactDao().getAppUsers();
        //   nonAppContactUserList=contactsDB.contactDao().getNonAppUsers();

        if (findViewById(R.id.recycleViewContactList) != null) {


            contactsRecycleView = findViewById(R.id.recycleViewContactList);
            contactsRecycleView.setHasFixedSize(true);
            //recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            contactsRecycleView.setLayoutManager(layoutManager);
            myContactAdapter = new MyContactAdapter(this, contactUserList);
            contactsRecycleView.setAdapter(myContactAdapter);

        }


    }
*/
    // Contact Sync
    void ContactSyncNow() {

  /*      WorkRequest contactSyncWork = new OneTimeWorkRequest.Builder(ContactSyncWorkManager.class)
                .build();
        WorkManager.getInstance(this)
                .enqueue(contactSyncWork);
*/

    /*    PeriodicWorkRequest contactSyncWork =
                new PeriodicWorkRequest.Builder(ContactSyncWorkManager.class, 6, TimeUnit.DAYS)
                        .addTag("contactsync")
                        .build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "contactsync",
                        ExistingPeriodicWorkPolicy.KEEP,
                        contactSyncWork);
*/
        OneTimeWorkRequest contactSyncWork=new OneTimeWorkRequest.Builder(ContactSyncWorkManager.class)
                .build();
        OneTimeWorkRequest contactSyncWorkDB=new OneTimeWorkRequest.Builder(ContactsUpdateWorkManager.class)
                .addTag("contact_update_sync").build();

        WorkManager.getInstance(this)
                .beginWith(contactSyncWork).then(contactSyncWorkDB).enqueue();


    }


    public void SyncContactsSingleTime(View v) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseReference contactsDBRef;

                contactsDBRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/indra/contacts/");

                ValueEventListener contactListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get Post object and use the values to update the UI
                        //Post post = dataSnapshot.getValue(Post.class);

                        if (dataSnapshot != null) {
                            List<ContactUser> mContactList=new ArrayList<>();
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                              //  Friend c=new Friend();

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

                                /*if(phoneno!=null){
                                    c.phone=phoneno;
                                    c.UID=uid;
                                    c.DisplayName=name;
                                    c.photo=photoURL;
                                    c.isAppUser=1;
                                    mContactList.add(c);
                                }
                                */
                                addToDB(name, uid, phoneno, photoURL);
                            }
                           // contactsDB.contactDao().insertAll(mContactList);

                            Log.d(TAG, "onDataChange: FINISHED: ");
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    appLiveModel.getAppContactsList().setValue(contactsDB.contactDao().getAppUsers());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

                    }
                };
                contactsDBRef.addListenerForSingleValueEvent(contactListener);



            }
            void addToDB(String name, String uid, String phone, String photo) {
                ContactUser c;//=new Friend();
                try{
     c=contactsDB.contactDao().getUserByPhone(phone).get(0);
                if(c!=null){

                    contactsDB.contactDao().updateAppUser(phone, uid, 1);
                    if (name != null) {
                        contactsDB.contactDao().updateAppUserName(phone, name);
                    }
                    if (phone != null) {
                        contactsDB.contactDao().updateAppUserPhoto(phone, photo);
                    }
                }else{
                    if (name != null) {
                        c.DisplayName=name;
                    }
                    c.UID=uid;
                    c.phone=phone;
                    c.photo=photo;
                    c.isAppUser=1;

                    contactsDB.contactDao().insertRawContacts(c);

                }
                }catch(Exception e) {
                     Log.e("TAG", "ERROR #ey454 "+e);

                }
           

            }

        });

    /*      WorkRequest contactSyncWork = new OneTimeWorkRequest.Builder(ContactSyncWorkManager.class)
                .build();
        WorkManager.getInstance(this)
                .enqueue(contactSyncWork).;


        WorkRequest contactSyncWorkDB = new OneTimeWorkRequest.Builder(ContactsUpdateWorkManager.class)
                .addTag("contact_update_sync").build();
        WorkManager.getInstance(this)
                .enqueue(contactSyncWorkDB);
*/

        OneTimeWorkRequest contactSyncWork=new OneTimeWorkRequest.Builder(ContactSyncWorkManager.class)
                .build();

        OneTimeWorkRequest contactSyncWorkDB=new OneTimeWorkRequest.Builder(ContactsUpdateWorkManager.class)
                .addTag("contact_update_sync").build();
        WorkManager.getInstance(this)
                .beginWith(contactSyncWork).then(contactSyncWorkDB).enqueue();

        Toast.makeText(this, "Syncing Contacts..\nIt might take few minutes", Toast.LENGTH_LONG).show();


    }



    public void getContactListData() {
        contactUserList = contactsDB.contactDao().getAppUsers();
        appLiveModel.getAppContactsList().postValue(contactUserList);

        if(!contactUserList.isEmpty()){
            LoadADS();
        }

        if(contactUserList.isEmpty()){
            ContactSyncNow();

            contactsDBRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/indra/contacts/");
            contactListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    //Post post = dataSnapshot.getValue(Post.class);

                    if (dataSnapshot != null) {
                        Log.d(TAG, "onDataChange: contactListener");
                        List<ContactUser> cuList=new ArrayList<>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            ContactUser c = new ContactUser();
                            c.phone = data.child("phone").getValue(String.class);
                            c.UID = data.child("uid").getValue(String.class);
                            c.DisplayName = null;
                            if (data.child("name").getValue(String.class) != null) {
                                c.DisplayName = data.child("name").getValue(String.class);
                            }else{
                                Log.d(TAG, "onDataChange: DB NAME NULL");
                                try {
                                    // phone must begin with '+'
                                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                                    Phonenumber.PhoneNumber numberProto = phoneUtil.parse(c.phone, "");
                                    int countryCode = numberProto.getCountryCode();
                                    long nationalNumber = numberProto.getNationalNumber();
                                    Log.d(TAG, "code " + countryCode);
                                    Log.d(TAG, "national number " + nationalNumber);

                                    String name=getContactName(MainActivity.this, c.phone);
                                    if(name.equals(null) ||name.equals("")){
                                        name= getContactName(MainActivity.this, String.valueOf(nationalNumber));
                                    }
                                    c.DisplayName = name;

                                } catch (Exception e) {
                                    Log.e(TAG, "onDataChange: ERROR #45453 "+e );
                                }


                            }

                            c.photo = null;
                            if (data.child("photo").getValue(String.class) != null) {
                                c.photo = data.child("photo").getValue(String.class);
                            }
                            c.isAppUser = 1;
                            cuList.add(c);
                            contactsDB.contactDao().insertRawContacts(c);
                        }

                        appLiveModel.getAppContactsList().postValue(cuList);
                        Log.d(TAG, "onDataChange: FINISHED: ");
                        // contactsLiveModel.getAppContactsList().setValue(contactsDB.contactDao().getAppUsers());


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




    }

    public void getFavContactListData() {
        appLiveModel.getAppFavContactsList().postValue(contactsDB.contactDao().getFavAppUsers());
    }

    public void getAllContacts(View v) {
        /*contactUserList.clear();
        contactUserList = contactsDB.contactDao().getAppUsers();
        contactUserList.addAll(contactsDB.contactDao().getNonAppUsers());
        appLiveModel.getAppContactsList().setValue(contactUserList);
        */
    }

    /*  public void ViewProfile(View v) {
          Intent intent = new Intent(this, ProfileActivity.class);

          startActivity(intent);
      }
  */
    public void ContactUserFrag(String uid) {

        ContactProfileFragment profileFrag = new ContactProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);

        profileFrag.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, profileFrag, null)
                .addToBackStack(null)
                .setReorderingAllowed(true)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.FragmentHomeScreen) != null) {
            if (mInterstitialAd != null) {
                if (AdShowed) {
                    super.onBackPressed();
                } else {
                    if(!BuildConfig.FLAVOR.equals(AppFlavour.PLAY_STORE)){
                        mInterstitialAd.show(MainActivity.this);
                    }else{
                       // finish();
                        super.onBackPressed();
                        //MainActivity.this.finish();
                    }

                }
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.");
               //finish();
                super.onBackPressed();
               // MainActivity.this.finish();
            }
        } else {
            super.onBackPressed();
           // MainActivity.this.finish();

        }


    }

    public void NavBarVisibility(int i) {
       /* if(i==0){
            bottomNavigationView.setVisibility(View.GONE);
        }else{
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        */
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Dynamic permissions are not required before Android M.
            //   onPermissionsGranted();
            return;
        }
        methodRequiresTwoPermission();

        String[] missingPermissions = getMissingPermissions();
        if (missingPermissions.length != 0) {
            requestPermissions(missingPermissions, PERMISSION_REQUEST);
        } else {
            // onPermissionsGranted();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private String[] getMissingPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new String[0];
        }

        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to retrieve permissions.");
            return new String[0];
        }

        if (info.requestedPermissions == null) {
            Log.w(TAG, "No requested permissions.");
            return new String[0];
        }

        ArrayList<String> missingPermissions = new ArrayList<>();
        for (int i = 0; i < info.requestedPermissions.length; i++) {
            if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                missingPermissions.add(info.requestedPermissions[i]);
            }
        }
        Log.d(TAG, "Missing permissions: " + missingPermissions);

        return missingPermissions.toArray(new String[missingPermissions.size()]);
    }

    @AfterPermissionGranted(PERMISSION_REQUEST)
    private void methodRequiresTwoPermission() {
        String[] perms = getMissingPermissions();
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Requires Permission",
                    PERMISSION_REQUEST, perms);
        }
    }

    public void PingUser(String uid) {
        Log.d(TAG, "PingUser: UID:" + uid);



        Toast toast = new Toast(this);


        Toast.makeText(this, "Sending Ping", Toast.LENGTH_SHORT).show();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> dataHash = new HashMap<>();
                dataHash.put("fromUID", fUser.getUid());
                dataHash.put("toUID", uid);
                dataHash.put("ping", "yes");
                if (fUser.getDisplayName() != null) {
                    dataHash.put("UserName", fUser.getDisplayName());
                } else {
                    dataHash.put("UserName", fUser.getPhoneNumber());
                }
                dataHash.put("PhoneNo", fUser.getPhoneNumber());

                String jsonBody = gson.toJson(dataHash);
                try {
                    Log.d(TAG, "run: JSON Body: " + jsonBody);
                    String data = post(getString(R.string.URL_ping_user), jsonBody);
                    Log.d(TAG, "run: OKHTTP Received: " + data);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "run: ", e);
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                        Toast.makeText(MainActivity.this, "Ping Send", Toast.LENGTH_SHORT).show();

                    }
                });

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


        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"User Pinged Friend");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

    }

    public void openNotification() {
        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Open Notification");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);


        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("flavor", BuildConfig.FLAVOR);
        startActivity(intent);

    }

    void CreateCallRecord(String uid, String io) {
        WorkRequest createCallRecordWork = new OneTimeWorkRequest.Builder(CreateCallRecordWorker.class)
                .addTag("CallRecordCreated")
                .setInputData(
                        new Data.Builder()
                                .putString("uid", uid)
                                .putString("io", io)
                                .build())
                .build();

        WorkManager.getInstance(this)
                .enqueue(createCallRecordWork);
    }

    void AppReviewProcess() {

        AppReviewTask appReviewTask = new AppReviewTask(this, MainActivity.this);
        if (!appReviewTask.isAppReviewed()) {

            boolean eligible2Review=cachePref.getBoolean(getString(R.string.Pref_EligibleForRating));
            if(eligible2Review){
                appReviewTask.requestAppReview();
            }
        }


    }

    boolean useTURN_Server;
    void FirebaseConfig() {

        int fetchIntrval=30;
        if(!BuildConfig.DEBUG){
            fetchIntrval=3600;
        }

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(fetchIntrval)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                            Log.d(TAG, "onComplete: Remote Config: Fetch and activate succeeded");

                        } else {
                            Log.d(TAG, "onComplete: Remote Config: Fetch failed");
                        }

                        Log.d(TAG, "onComplete: Config: " + mFirebaseRemoteConfig.getString("indra_build_no")+"\nads_enabled: "+mFirebaseRemoteConfig.getBoolean("ads_enabled"));

                        ConfigUpdateStatus(mFirebaseRemoteConfig.getString("indra_build_no"),
                                mFirebaseRemoteConfig.getString("indra_version"));

                        AdsEnabled=mFirebaseRemoteConfig.getBoolean("ads_enabled");
                        cachePref.setBoolean(getString(R.string.Pref_AdsEnabled),AdsEnabled);

                        useTURN_Server=mFirebaseRemoteConfig.getBoolean("use_turn_server");
                        cachePref.setBoolean(getString(R.string.Pref_UseTurnServer),useTURN_Server);

                        if(useTURN_Server){
                            getTURNServerConfig();
                        }

                    }
                });
        Log.d(TAG, "onComplete: Config: " + mFirebaseRemoteConfig.getString("indra_build_no"));
        ConfigUpdateStatus(mFirebaseRemoteConfig.getString("indra_build_no"),
                mFirebaseRemoteConfig.getString("indra_version"));

        AdsEnabled=mFirebaseRemoteConfig.getBoolean("ads_enabled");
cachePref.setBoolean(getString(R.string.Pref_AdsEnabled),AdsEnabled);


        useTURN_Server=mFirebaseRemoteConfig.getBoolean("use_turn_server");
        cachePref.setBoolean(getString(R.string.Pref_UseTurnServer),useTURN_Server);

        getTURNServerConfig();



    }

    void ConfigUpdateStatus(String BuildNumber, String AppVersion) {
        Log.d(TAG, "ConfigUpdateStatus: BuildNumber: " + BuildNumber + "\nAppVersion: " + AppVersion);
        if (BuildNumber != null) {
            if (AppVersion != null) {
                try {
                    int Build_Number = Integer.parseInt(BuildNumber);
                    int current_build = Integer.parseInt(getString(R.string.app_version_code));
                    HashMap<String, String> data = new HashMap<>();
                    data.put("build_number", BuildNumber);
                    data.put("app_version", AppVersion);

                    if (Build_Number > current_build) {
                        Log.d(TAG, "ConfigUpdateStatus: UPDATE AVAILABLE");
                        data.put("isUpdateAvailable", "yes");



                    } else {
                        Log.d(TAG, "ConfigUpdateStatus: ALREADY LATEST VERSION");
                        data.put("isUpdateAvailable", "no");
                    }

                    appLiveModel.getServerBuildConfig().setValue(data);
                } catch (Exception e) {
                    Log.d(TAG, "ConfigUpdateStatus: " + e);
                }


            }

        }


    }

    public void UpdateNow(View v) {
        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Update Button Clicked");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

        if (BuildConfig.FLAVOR.equals(getString(devesh.ephrine.notifications.R.string.FLAVOR_PLAY_STORE))) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(
                    "https://play.google.com/store/apps/details?id=devesh.ephrine.indra"));
            intent.setPackage("com.android.vending");
            startActivity(intent);

        } else if (BuildConfig.FLAVOR.equals(getString(devesh.ephrine.notifications.R.string.FLAVOR_GALAXY_STORE))) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(
                    "https://galaxystore.samsung.com/detail/devesh.ephrine.indra"));
            startActivity(intent);

        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.UPDATE_APK_WEBSITE)));
            startActivity(browserIntent);

        }
    }


  /*  private void onShareClicked() {

        String link = "https://play.google.com/store/apps/details?id=com.recharge2mePlay.recharge2me";

        Uri uri = Uri.parse(link);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, link);
        intent.putExtra(Intent.EXTRA_TITLE, "Recharge2me");

        startActivity(Intent.createChooser(intent, "Share Link"));
    }
*/
    private Spanned getSpannedText(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(text);
        }
    }

    public void ShareButton(View v) {
        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"App Share Clicked");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.SHARE_WITH_FRIENDS_TITLE));

        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.SHARE_WITH_FRIENDS_TEXT));
        sendIntent.setType("text/*");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);

    }

    public Activity getAct() {
        return MainActivity.this;
    }

    public void showChangeStatusPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_change_status, popup.getMenu());
        popup.show();
    }

    public void BlockUser(String uid){
        DatabaseReference myBlockListRef = database.getReference("users/"+fUser.getUid()+"/indra/block_list/"+uid);
        myBlockListRef.setValue(uid);


    }

    /* User Status */
    public void SetUserStatus(String status){
        //DatabaseReference UserStatusRef = database.getReference("users/" + fUser.getUid() + "/indra/UserStatus");

        if(status.equals(getString(R.string.USER_STATUS_AVAILABLE))){
            UserStatusRef.setValue(Integer.parseInt(getString(R.string.USER_STATUS_AVAILABLE)));
        } else if(status.equals(getString(R.string.USER_STATUS_BUSY))){
            UserStatusRef.setValue(Integer.parseInt(getString(R.string.USER_STATUS_BUSY)));
        } else if(status.equals(getString(R.string.USER_STATUS_DND))){
            UserStatusRef.setValue(Integer.parseInt(getString(R.string.USER_STATUS_DND)));
        }
        else if(status.equals(getString(R.string.USER_STATUS_ON_CALL))){ }
        else if(status.equals(getString(R.string.USER_STATUS_UNAVAILABLE))){ }

    }
    void getCurrentUserStatus(){
       // DatabaseReference UserStatusRef = database.getReference("users/" + fUser.getUid() + "/indra/UserStatus");
// Read from the database
        UserStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.getValue(Integer.class)!=null){
                    int value1 = dataSnapshot.getValue(Integer.class);
                    String value=String.valueOf(value1);

                    if(value.equals(getString(R.string.USER_STATUS_AVAILABLE))){
                        cachePref.setString(getString(R.string.PREF_USER_CURRENT_STATUS),value);

                    } else if(value.equals(getString(R.string.USER_STATUS_BUSY))){
                        cachePref.setString(getString(R.string.PREF_USER_CURRENT_STATUS),value);
                    } else if(value.equals(getString(R.string.USER_STATUS_DND))){
                        cachePref.setString(getString(R.string.PREF_USER_CURRENT_STATUS),value);
                    }
                    else if(value.equals(getString(R.string.USER_STATUS_ON_CALL))){
                        cachePref.setString(getString(R.string.PREF_USER_CURRENT_STATUS),getString(R.string.USER_STATUS_AVAILABLE));
                    }
                }


//                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    /**Get TURN Server Config*/


    public void getTURNServerConfig(){
        long Hours23=82800000;



        boolean shouldFetch=false;
        if(cachePref.getString(getString(R.string.Pref_Twilio_TTL_EPOCH))!=null){
            String epoch=cachePref.getString(getString(R.string.Pref_Twilio_TTL_EPOCH));
            long PrevEpoch=Long.parseLong(epoch);

            long diff= System.currentTimeMillis()-PrevEpoch;

            Log.d(TAG, "getTURNServerConfig: EPOCH calc= PrevEpoch: "+PrevEpoch+" diff:"+diff);
            if(diff>=Hours23){
                Log.d(TAG, "getTURNServerConfig: Twilio TURN Config Timeout !");
                shouldFetch=true;
            }

        }else{
            shouldFetch=true;

        }


        if(shouldFetch){
            fUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                // Send token to your backend via HTTPS
                                Log.d(TAG, "Firebase idToken: " + idToken);

                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        HashMap<String, String> dataHash = new HashMap<>();
                                        dataHash.put("token", idToken);

                                        String jsonBody = gson.toJson(dataHash);
                                        try {
                                            Log.d(TAG, "run: JSON Body: " + jsonBody);
                                            String data = post(getString(R.string.URL_getTURNConfig), jsonBody);
                                            Log.d(TAG, "run: OKHTTP Received: " + data);


                                            HashMap<String, String> ResDataMap = new HashMap<>();
                                            Type collectionType = new TypeToken<HashMap<String, String>>() {
                                            }.getType();

                                            ResDataMap = gson.fromJson(data, collectionType);

                                            String Username=ResDataMap.get("username");
                                            String Password=ResDataMap.get("password");
                                            cachePref.setString(getString(R.string.Pref_Twilio_UN),Username);
                                            cachePref.setString(getString(R.string.Pref_Twilio_PW),Password);
                                            cachePref.setString(getString(R.string.Pref_Twilio_TTL_EPOCH),String.valueOf(System.currentTimeMillis()));


                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Log.e(TAG, "run: ", e);
                                        }

                                    /*
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {


                                        }
                                    });
                                    */

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


                            } else {
                                // Handle error -> task.getException();
                            }
                        }
                    });


        }




    }

}



/*

 */
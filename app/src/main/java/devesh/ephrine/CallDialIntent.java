package devesh.ephrine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.room.Room;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import devesh.ephrine.AppRTC.WebRTCVideoCall;
import devesh.ephrine.AppRTC.WebRTCVoiceCall;
import devesh.common.database.contactsdb.ContactAppDatabase;
import devesh.common.database.contactsdb.ContactUser;
import devesh.ephrine.util.AppAnalytics;
import devesh.ephrine.workmanager.CreateCallRecordWorker;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class CallDialIntent extends AppCompatActivity {

    static String TAG = "CallDialIntent: ";
    // private static boolean commandLineRun;
    ContactAppDatabase contactsDB;
    String toCallNo;
    ContactUser mContact;
    ImageView ProfilePicIMG;
    TextView UserNameTx;
    TextView UserPhoneNoTx;
    boolean isKeyprefSSProtected;
    private FirebaseAnalytics mFirebaseAnalytics;
    private NativeAd nativeAd;
    private SharedPreferences sharedPref;
CardView UserInviteCardView;
LinearLayout LLCallView;

AppAnalytics appAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_dial_intent);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AppCenter.start(getApplication(),  getString(R.string.MS_AppCenter_API_Key), Analytics.class, Crashes.class);
        appAnalytics=new AppAnalytics(getApplication(),this);
        getSupportActionBar().hide();

        ProfilePicIMG = findViewById(R.id.ProfilePhotoimageView4);
        UserNameTx = findViewById(R.id.UserNametextView3);
        UserPhoneNoTx = findViewById(R.id.UserPhonetextView5);
        UserInviteCardView=findViewById(R.id.UserInviteCardView);
        LLCallView=findViewById(R.id.LLCallView);

        if (getIntent() != null) {
            Intent intent = getIntent();
            if (intent.getData() != null) {
                Uri data = intent.getData();
                Log.d(TAG, "onCreate: RECEIVED INTENT: " + data.toString());
                toCallNo = data.toString().replace("tel:", "");
            }
        } else {
            finish();
            Toast.makeText(this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
        }

        contactsDB = Room.databaseBuilder(this, ContactAppDatabase.class, getString(R.string.DATABASE_CONTACTS_DB))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        List<ContactUser> listContact = contactsDB.contactDao().getUserByPhone(toCallNo);
        if (listContact.isEmpty()) {
            //User is not available
            UserInviteCardView.setVisibility(View.VISIBLE);
            LLCallView.setVisibility(View.GONE);
        } else {
            UserInviteCardView.setVisibility(View.GONE);
            LLCallView.setVisibility(View.VISIBLE);

            mContact = listContact.get(0);
            UserNameTx.setText(mContact.DisplayName);
            UserPhoneNoTx.setText(mContact.phone);
            Glide.with(this).load(mContact.photo)
                    //.diskCacheStrategy(DiskCacheStrategy.NONE)
                    //  .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(ProfilePicIMG);


            AppRTCInit();
            //loadNativeAd();

        }


        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),"Call Dial Screen");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);


        loadNativeAd();
    }

    void loadNativeAd() {

        AdLoader adLoader = new AdLoader.Builder(this, getString(R.string.AdMob_NativeAd1))
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd NativeAd) {
                        // Show the ad.
                        Log.d(TAG, "onNativeAdLoaded: ");
                       /* if (adLoader.isLoading()) {
                            // The AdLoader is still loading ads.
                            // Expect more adLoaded or onAdFailedToLoad callbacks.
                        } else {
                            // The AdLoader has finished loading ads.
                        }*/
                        boolean isDestroyed = false;
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            isDestroyed = isDestroyed();
                        }
                        if (isDestroyed || isFinishing() || isChangingConfigurations()) {
                            NativeAd.destroy();
                            return;
                        }*/
                        // You must call destroy on old ads when you are done with them,
                        // otherwise you will have a memory leak.
                        if (nativeAd != null) {
                            nativeAd.destroy();
                        }
                        nativeAd = NativeAd;
                        FrameLayout frameLayout = findViewById(R.id.nativeAdLayout);
                        NativeAdView adView =
                                (NativeAdView) getLayoutInflater()
                                        .inflate(R.layout.nativead1, null);
                        populateUnifiedNativeAdView(NativeAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                        Log.e(TAG, "onAdFailedToLoad: " + adError);
                    }

                    @Override
                    public void onAdClicked() {
                        // Log the click event or other custom behavior.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .setVideoOptions(new VideoOptions.Builder()
                                .setStartMuted(true)
                                .build())
                        .build())
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());

        //adLoader.loadAds(new AdRequest.Builder().build(), 3);
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        //adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        // adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);


        MediaView mediaView = adView.findViewById(R.id.ad_media);

        if (nativeAd.getMediaContent() != null) {
            adView.setMediaView(mediaView);

            mediaView.setMediaContent(nativeAd.getMediaContent());
            if (nativeAd.getMediaContent().hasVideoContent()) {
                Log.d(TAG, "populateUnifiedNativeAdView: nativeAd.getMediaContent().hasVideoContent() YES");
            } else {
                Log.d(TAG, "populateUnifiedNativeAdView: nativeAd.getMediaContent().hasVideoContent() NO");
                adView.findViewById(R.id.ad_media).setVisibility(View.GONE);
            }

            Log.d(TAG, "populateUnifiedNativeAdView: nativeAd.getMediaContent()");
        } else {
            Log.d(TAG, "populateUnifiedNativeAdView: nativeAd.getMediaContent() NULL");
        }

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
  /*      VideoController vc = nativeAd.getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
            videoStatus.setText(String.format(Locale.getDefault(),
                    "Video status: Ad contains a %.2f:1 video asset.",
                    vc.getAspectRatio()));

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    refresh.setEnabled(true);
                    videoStatus.setText("Video status: Video playback has ended.");
                    super.onVideoEnd();
                }
            });
        } else {
            videoStatus.setText("Video status: Ad does not contain a video asset.");
            refresh.setEnabled(true);
        }*/
    }

    public void CallNow(View v) {
        SelectContact2Call();
    }

    public void CallNowVoip(View v){
        isKeyprefSSProtected = sharedPref.getBoolean(getString(R.string.settings_screenshot_protection), true);

        Random rand = new Random();

        // Generate random integers in range 0 to 999
        int rand_int1 = rand.nextInt(10000);
        //   Friend contact = contactsDB.contactDao().getUserByPhone(contactNo).get(0);
        CreateCallRecord(mContact.UID, "o");
        Intent intent = new Intent(this, WebRTCVoiceCall.class);
        intent.putExtra(getString(R.string.WebRTC_PEER_ID), "A" + String.valueOf(rand_int1));
        intent.putExtra(getString(R.string.call_type), "0");
        intent.putExtra(getString(R.string.WebRTC_CallReceiverID), mContact.UID);
        intent.putExtra(getString(R.string.intent_Caller_User_Name), mContact.DisplayName);
        intent.putExtra(getString(R.string.intent_Caller_Phone), mContact.phone);

        startActivity(intent);

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"VOIP Called");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

    }

    public void SelectContact2Call() {
        isKeyprefSSProtected = sharedPref.getBoolean(getString(R.string.settings_screenshot_protection), true);

        Random rand = new Random();

        // Generate random integers in range 0 to 999
        int rand_int1 = rand.nextInt(10000);
        //   Friend contact = contactsDB.contactDao().getUserByPhone(contactNo).get(0);
        CreateCallRecord(mContact.UID, "o");
        Intent intent = new Intent(this, WebRTCVideoCall.class);
        intent.putExtra(getString(R.string.WebRTC_PEER_ID), "A" + String.valueOf(rand_int1));
        intent.putExtra(getString(R.string.call_type), "0");
        intent.putExtra(getString(R.string.WebRTC_CallReceiverID), mContact.UID);
        intent.putExtra(getString(R.string.intent_Caller_User_Name), mContact.DisplayName);
        intent.putExtra(getString(R.string.intent_Caller_Phone), mContact.phone);

        startActivity(intent);

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Video Call");
        adata.put("Method","Call Dial Intent");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);


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

    public void AppRTCInit() {
        // Get setting keys.
        PreferenceManager.setDefaultValues(this, R.xml.apprtc_preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);


        isKeyprefSSProtected = sharedPref.getBoolean(getString(R.string.settings_screenshot_protection), true);
        Log.d(TAG, "AppRTCInit: isKeyprefSSProtected : " + isKeyprefSSProtected);

    }

    public void onShareClicked(View v) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.SHARE_WITH_FRIENDS_TITLE));
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.SHARE_WITH_FRIENDS_TEXT));
        sendIntent.setType("text/*");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);

    }

}
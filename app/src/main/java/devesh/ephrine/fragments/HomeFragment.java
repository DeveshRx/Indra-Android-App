package devesh.ephrine.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MuteThisAdListener;
import com.google.android.gms.ads.MuteThisAdReason;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devesh.common.utils.CachePref;
import devesh.ephrine.BuildConfig;
import devesh.ephrine.MainActivity;
import devesh.ephrine.R;
import devesh.common.ViewModel.AppDataViewModel;
import devesh.ephrine.adapters.MyContactAdapter;
import devesh.ephrine.adapters.MyFavContactAdapter;
import devesh.common.database.contactsdb.ContactUser;
import devesh.ephrine.util.AppAnalytics;
import devesh.common.utils.AppFlavour;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class HomeFragment extends Fragment {
    //ContactAppDatabase contactsDB;
    RecyclerView contactsRecycleView;
    MyContactAdapter myContactAdapter;
    List<ContactUser> contactUserList;
    List<ContactUser> favContactUserList;

    AppDataViewModel appLiveModel;

    RecyclerView favContactsRecycleView;
    MyFavContactAdapter myFavContactAdapter;

    String TAG = "ContactsListFrag: ";
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    LinearLayout LLUpdateBanner;

    LinearLayout LLFavList;
    LinearLayout LLNoContacts;

    View mView;
     FirebaseAnalytics mFirebaseAnalytics;
     NativeAd nativeAd;
    AdLoader adLoader;
CachePref cachePref;
    AppAnalytics appAnalytics;
    boolean AdsEnabled;

    public HomeFragment() {
        super(R.layout.fragment_home);

        contactUserList = new ArrayList<>();
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
    public void onDestroy() {
        if(nativeAd!=null){
            nativeAd.destroy();
        }

        super.onDestroy();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        //  int someInt = requireArguments().getInt("some_int");
        // contactUserList.addAll(contactsDB.contactDao().getNonAppUsers());

        cachePref=new CachePref(getActivity());
        AdsEnabled=cachePref.getBoolean(getString(R.string.Pref_AdsEnabled));


        adLoader=null;
        appAnalytics=new AppAnalytics(mActivity.getApplication(),mActivity);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mActivity);

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),"Home Screen Fragment");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);

        mView = view;
        ((AppCompatActivity) mActivity).getSupportActionBar().show();
        ((MainActivity) mActivity).NavBarVisibility(1);
        ((AppCompatActivity) mActivity).getSupportActionBar().setElevation(0f);
        ((AppCompatActivity) mActivity).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mActivity.getColor(R.color.BG_Blank)));

        contactsRecycleView = view.findViewById(R.id.recycleViewContactList);
        contactsRecycleView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2));
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        contactsRecycleView.setLayoutManager(layoutManager);

        itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Log.d(TAG, "onMove: ");
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Row is swiped from recycler view
                // remove it from adapter
                final int position = viewHolder.getAdapterPosition();
                Log.d(TAG, "onSwiped: " + position);
                LoadRecycleView(true);
                String uid = myContactAdapter.getItemData(position);
                Map<String,String> adata=new HashMap<>();
String mDirection="";
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        //VOIP
                        if (uid != null) {
                            //((MainActivity) mActivity).ContactUserFrag(uid);
                            ((MainActivity) mActivity).SelectContact2VoiceCall(uid);
                        } else {
                            Toast.makeText(mActivity, "User has not installed App", Toast.LENGTH_SHORT).show();
                        }
                        mDirection="LEFT";
                        break;
                    case ItemTouchHelper.RIGHT:
                        // Call
                        if (uid != null) {
                            ((MainActivity) mActivity).SelectContact2Call(uid);
                        } else {
                            Toast.makeText(mActivity, "User has not installed App", Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "onSwiped: UID:" + uid);
                        mDirection="RIGHT";

                        break;
                }


                adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Swipe2Call "+mDirection);
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // view the background view
                Log.d(TAG, "onChildDraw: dX:" + dX + " dY:" + dY);
                //  final View foregroundView = ((MyContactAdapter.MyViewHolder) viewHolder).viewForeground;

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(mActivity, R.color.Call_Green))
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_phone_30)
                        //.addSwipeLeftLabel("Profile")
                        .setSwipeLeftLabelColor(R.color.white)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(mActivity, R.color.Call_Green))
                        .addSwipeRightActionIcon(R.drawable.ic_baseline_videocam_30)
                        //.addSwipeRightLabel("Video Call")
                        .setSwipeRightLabelColor(R.color.white)
                        //.setSwipeLeftLabelTextSize(25,TypedValue.COMPLEX_UNIT_SP)
                        //.setActionIconTint(R.color.white)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };


        LLUpdateBanner = view.findViewById(R.id.LLUpdateBanner);
        favContactsRecycleView = view.findViewById(R.id.myFavContactList);
        favContactsRecycleView.setHasFixedSize(true);
        //  favContactsRecycleView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        //LinearLayoutManager layoutManager2 = new LinearLayoutManager(mActivity);
        //favContactsRecycleView.setLayoutManager(layoutManager2);
        LLFavList = view.findViewById(R.id.LLFavList);
        LLNoContacts = view.findViewById(R.id.LLNoContacts);


        LoadRecycleView(true);

        Observer<List<ContactUser>> ContactDataLiveModel = new Observer<List<ContactUser>>() {
            @Override
            public void onChanged(@Nullable final List<ContactUser> newContacts) {
                // Update the UI, in mActivity case, a TextView.
                //  nameTextView.setText(newName);
                Log.d(TAG, "onChanged: Friend: newContacts " + newContacts);
                if(contactUserList!=null){
                    contactUserList.clear();
                }

                contactUserList =new ArrayList<>(newContacts);
                Log.d(TAG, "onChanged: Friend: contactUserList " + newContacts);

                Map<String,String> adata=new HashMap<>();

                if (contactUserList.isEmpty()) {
                    LLNoContacts.setVisibility(View.VISIBLE);
                    adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Zero_Indra_Friends");
                } else {
                    LLNoContacts.setVisibility(View.GONE);
                    adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Indra_Friends");
                }

                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

                LoadRecycleView(true);
            }
        };

        Observer<List<ContactUser>> FavContactDataLiveModel = new Observer<List<ContactUser>>() {
            @Override
            public void onChanged(@Nullable final List<ContactUser> newContacts) {
                // Update the UI, in mActivity case, a TextView.
                //  nameTextView.setText(newName);
                Log.d(TAG, "onChanged: FavFriend: " + newContacts);
                favContactUserList = newContacts;
                if (favContactUserList.isEmpty()) {
                    LLFavList.setVisibility(View.GONE);
                } else {
                    LLFavList.setVisibility(View.VISIBLE);
                }
                LoadFavRecycleView();
            }
        };
        Observer<HashMap<String, String>> AppServerConfigDataLiveModel = new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(@Nullable final HashMap<String, String> data) {
                // Update the UI, in mActivity case, a TextView.
                //  nameTextView.setText(newName);
                Log.d(TAG, "onChanged: AppServerConfigDataLiveModel: " + data);
                if (data.get("isUpdateAvailable").equals("yes")) {
                    UpdateAvailable(data);
                } else {
                    UpdateUnAvailable();
                }

            }
        };
        if(getActivity()!=null){
            appLiveModel = new ViewModelProvider(getActivity()).get(AppDataViewModel.class);

            appLiveModel.getAppContactsList().observe(getActivity(), ContactDataLiveModel);
            // appLiveModel.getAppContactsList().setValue(contactUserList);
            appLiveModel.getAppFavContactsList().observe(getActivity(), FavContactDataLiveModel);
            appLiveModel.getServerBuildConfig().observe(getActivity(), AppServerConfigDataLiveModel);

        }else{
            Log.e(TAG, "onViewCreated:ERROR #3454  NULL ACTIVITY IN FRAG" );
        }



    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }



    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    void LoadRecycleView(boolean isSwipable) {
        Log.d(TAG, "LoadRecycleView: ");
     //   contactsRecycleView.removeAllViews();
        if(!contactUserList.isEmpty()){
            if(adLoader==null){
             if(!BuildConfig.FLAVOR.equals(AppFlavour.PLAY_STORE)){
                 loadNativeAd();
             }

            }
        }

        if (isSwipable) {
            ItemTouchHelper ith = new ItemTouchHelper(itemTouchHelperCallback);
            ith.attachToRecyclerView(contactsRecycleView);
        }
        Log.d(TAG, "LoadRecycleView: "+contactUserList);
        myContactAdapter = new MyContactAdapter(mActivity, contactUserList);

        contactsRecycleView.setAdapter(myContactAdapter);



    }

    void LoadFavRecycleView() {
    //    favContactsRecycleView.removeAllViews();
        myFavContactAdapter = new MyFavContactAdapter(mActivity, favContactUserList);
        favContactsRecycleView.setAdapter(myFavContactAdapter);
        if (favContactUserList.isEmpty()) {

        }
    }

    public void Edit2Fav(String UID, int isFav) {
        Log.d(TAG, "Edit2Fav: UID:" + UID + "  isFav:" + isFav);
        //contactsDB.contactDao().setFavUser(UID,isFav);
        ((MainActivity) mActivity).Edit2Fav(UID, isFav);

    }



    void loadNativeAd() {
if(AdsEnabled){
    adLoader = new AdLoader.Builder(getContext(), getString(R.string.AdMob_NativeAd1))
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
                    FrameLayout frameLayout = mView.findViewById(R.id.nativeAdLayout);
                    NativeAdView adView =
                            (NativeAdView) getLayoutInflater()
                                    .inflate(R.layout.nativead1, null);
                    populateUnifiedNativeAdView(NativeAd, adView);

                    nativeAd.setMuteThisAdListener(new MuteThisAdListener() {
                        @Override
                        public void onAdMuted() {
                            Toast.makeText(mActivity, "Ad muted", Toast.LENGTH_SHORT).show();
                        }
                    });
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
                    .setRequestCustomMuteThisAd(true)
                    .build())
            .build();
    adLoader.loadAd(new AdRequest.Builder().build());

}

        //adLoader.loadAds(new AdRequest.Builder().build(), 3);
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
if(AdsEnabled){
    if (nativeAd.isCustomMuteThisAdEnabled()) {
        enableCustomMuteWithReasons(nativeAd.getMuteThisAdReasons());
    }else {
        hideCustomMute();
    }
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
    private void enableCustomMuteWithReasons(List<MuteThisAdReason> reasons) {
        //TODO: This method should show your custom mute button and provide the list
        // of reasons to the interface that are to be displayed when the user mutes
        // the ad.

        Log.d(TAG, "enableCustomMuteWithReasons: ");
    }

    private void hideCustomMute() {
        //TODO: Remove / hide the custom mute button from your user interface.
        Log.d(TAG, "hideCustomMute: ");
    }
    private void muteAdDialogDidSelectReason(MuteThisAdReason reason) {
        // Report the mute action and reason to the ad.
        nativeAd.muteThisAd(reason);
        muteAd();
    }

    private void muteAd() {
        //TODO: Mute / hide the ad in your preferred manner.
    }
    void UpdateUnAvailable() {
        LLUpdateBanner.setVisibility(View.GONE);
    }
    void UpdateAvailable(HashMap<String, String> data) {
        LLUpdateBanner.setVisibility(View.VISIBLE);

    }
}
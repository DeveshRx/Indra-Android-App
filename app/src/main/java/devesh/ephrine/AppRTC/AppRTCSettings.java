package devesh.ephrine.AppRTC;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import devesh.ephrine.R;
import devesh.ephrine.util.AppAnalytics;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.util.HashMap;
import java.util.Map;

public class AppRTCSettings extends AppCompatActivity {
    View AppHeader;
    private FirebaseAnalytics mFirebaseAnalytics;
AppAnalytics appAnalytics;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appAnalytics=new AppAnalytics(getApplication(),this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AppCenter.start(getApplication(),  getString(R.string.MS_AppCenter_API_Key), Analytics.class, Crashes.class);

        setContentView(R.layout.settings_activity);

        AppHeader = findViewById(R.id.appheader);
        AppHeader.setVisibility(View.GONE);

        //  getSupportActionBar().hide();


        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new AppRTCSettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0f);
            actionBar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.BG_Blank)));
        }
        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),"AppRTC Setting Screen");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);

    }

    public static class AppRTCSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.apprtc_preferences, rootKey);
/*
            Preference getserTURNConfig = findPreference(getString(R.string.Pref_UserConfig_TurnServer));
            getserTURNConfig.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here


                    return true;
                }
            });/*

  /*          Preference myprofile = findPreference("myprofile");
            myprofile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);

                    startActivity(intent);

                    return true;
                }
            });

            Preference SyncContact = findPreference("SyncContacts");
            SyncContact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    WorkRequest contactSyncWork = new OneTimeWorkRequest.Builder(ContactSyncWorkManager.class)
                            .build();

                    WorkRequest contactSyncWorkDB = new OneTimeWorkRequest.Builder(ContactsUpdateWorkManager.class)
                            .addTag("contact_update_sync").build();
                    //WorkManager.getInstance(this)
                    //      .enqueue(contactSyncWorkDB);

                    ArrayList<WorkRequest> wr = new ArrayList<>();
                    wr.add(contactSyncWork);
                    wr.add(contactSyncWorkDB);

                    WorkManager.getInstance(getActivity()).enqueue(wr);
                    Toast.makeText(getActivity(), "Syncing Contacts", Toast.LENGTH_LONG).show();
                    return true;
                }
            });


            Preference PrefNotifi = findPreference("notif");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PrefNotifi.setVisible(true);
                PrefNotifi.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        String mPackageName = getActivity().getPackageName();
                        Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                // .putExtra(Settings.EXTRA_CHANNEL_ID, "001")
                                .putExtra(Settings.EXTRA_APP_PACKAGE, mPackageName);

                        startActivity(settingsIntent);

                        return true;
                    }
                });

            } else {
                PrefNotifi.setVisible(false);
            }

            Preference VidAdv = findPreference("vidadv");
            VidAdv.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {


                    return true;
                }
            });


            Preference ThemeSetting = findPreference("theme");
            ThemeSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(getActivity(), ThemeSettingActivity.class);
                    startActivity(intent);

                    return true;
                }
            });
*/

        }
    }
}
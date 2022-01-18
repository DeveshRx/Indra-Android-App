package devesh.ephrine;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import devesh.common.ui.PrivacyPolicyActivity;
import devesh.ephrine.AppRTC.AppRTCSettings;
import devesh.ephrine.profile.ProfileActivity;
import devesh.ephrine.theme.ThemeSettingActivity;
import devesh.ephrine.util.AppAnalytics;
import devesh.ephrine.workmanager.ContactSyncWorkManager;
import devesh.ephrine.workmanager.ContactsUpdateWorkManager;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
AppAnalytics appAnalytics;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   
        AppCenter.start(getApplication(),  getString(R.string.MS_AppCenter_API_Key), Analytics.class, Crashes.class);

   appAnalytics=new AppAnalytics(getApplication(),this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        setContentView(R.layout.settings_activity);

        getSupportActionBar().hide();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0f);
            actionBar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.BG_Blank)));
        }

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),"App Setting Screen");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);


    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
       AppAnalytics appAnalytics;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
appAnalytics=new AppAnalytics(getActivity().getApplication(),getActivity());

            Preference myprofile = findPreference("myprofile");
            myprofile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Map<String,String> adata=new HashMap<>();
                    adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),"Profile Screen");
                    appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);

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
                    Toast.makeText(getActivity(), "Syncing Contacts..\nIt might take few minutes", Toast.LENGTH_LONG).show();

                    Map<String,String> adata=new HashMap<>();
                    adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Contact Sync init");
                    appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);


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

                        Map<String,String> adata=new HashMap<>();
                        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Sys Notification");
                        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

                        return true;
                    }
                });

            } else {
                PrefNotifi.setVisible(false);
            }

            Preference VidAdv = findPreference("vidadv");
            VidAdv.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    Map<String,String> adata=new HashMap<>();
                    adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Advance Video Setting Screen");
                    appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

                    Intent intent = new Intent(getActivity(), AppRTCSettings.class);
                    startActivity(intent);

                    return true;
                }
            });

            Preference SecPri = findPreference("secpri");
            SecPri.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(getActivity(), PrivacyPolicyActivity.class);
                    startActivity(intent);
                    //open browser or intent here
                   /* startActivity(
                            FlutterActivity
                                    .createDefaultIntent(getActivity())

                    );*/

      /*            startActivity(
                            FlutterActivity
                                    .withCachedEngine("prisec")
                                    .build(getActivity())
                    );
*/
                    Map<String,String> adata=new HashMap<>();
                    adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Privacy & Security Flutter");
                    appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

                   /* startActivity(
                            FlutterActivity
                                    .withNewEngine()
                                    .initialRoute("/prisec")
                                    .build(getActivity())
                    );*/


                    return true;
                }
            });

            Preference ThemeSetting = findPreference("theme");

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                // Do something for android 10 and above versions
                ThemeSetting.setVisible(false);

  /*              ThemeSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        //open browser or intent here

                        Map<String,String> adata=new HashMap<>();
                        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Theme Setting Screen");
                        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

                        Intent intent = new Intent(getActivity(), ThemeSettingActivity.class);
                        startActivity(intent);

                        return true;
                    }
                });
*/
            } else{
                // do something for phones running an SDK before lollipop
                ThemeSetting.setVisible(false);
            }

        }
    }
}
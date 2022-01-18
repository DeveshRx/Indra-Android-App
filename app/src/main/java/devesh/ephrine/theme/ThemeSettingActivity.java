package devesh.ephrine.theme;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import devesh.ephrine.R;


public class ThemeSettingActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    String TAG = "ThemeSetting: ";
    ThemeSettingFragment themeSettingFragment;
    AppThemePreferences appThemePreferences;
    View header;
    private FirebaseAnalytics mFirebaseAnalytics;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setContentView(R.layout.settings_activity);
        header = findViewById(R.id.appheader);
        header.setVisibility(View.GONE);
        themeSettingFragment = new ThemeSettingFragment();
        appThemePreferences = new AppThemePreferences(ThemeSettingActivity.this);
        if (savedInstanceState == null) {


            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, themeSettingFragment)
                    .commit();


        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0f);
            actionBar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.BG_Blank)));
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set summary to be the user-description for the selected value
        if (themeSettingFragment != null) {
            SharedPreferences sharedPreferences =
                    themeSettingFragment.getPreferenceScreen().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (themeSettingFragment != null) {
            SharedPreferences sharedPreferences =
                    themeSettingFragment.getPreferenceScreen().getSharedPreferences();
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        }


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged: " + key);
        if (key.equals(getString(R.string.Pref_ThemeMode))) {
            String value = sharedPreferences.getString(getString(R.string.Pref_ThemeMode), "");
            Log.d(TAG, "onSharedPreferenceChanged: " + value);
            Preference ThemeSetting = themeSettingFragment.findPreference(getString(R.string.Pref_ThemeMode));
            ThemeSetting.setSummary("Current: " + value + "\nPlease Restart App to Apply Changes");
            appThemePreferences.setThemePref(value);
            Toast.makeText(this, "Please Restart to Apply Changes", Toast.LENGTH_SHORT).show();
        }
    }


    public static class ThemeSettingFragment extends PreferenceFragmentCompat {
        String TAG = "ThemeSetting: ";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.theme_preferences, rootKey);

            Preference ThemeSetting = findPreference(getString(R.string.Pref_ThemeMode));
            String val = ThemeSetting.getSharedPreferences().getString(getString(R.string.Pref_ThemeMode), "");
            ThemeSetting.setSummary("Current: " + val);


        }
    }
}
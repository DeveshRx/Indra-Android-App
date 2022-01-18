package devesh.ephrine.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import devesh.ephrine.R;

public class AppThemePreferences {
    final static String SYSTEM_DEFAULT = "System Default";
    final static String DARK_MODE = "Dark Mode";
    final static String LIGHT_MODE = "Light Mode";
    //Context mContext;
    final static String TAG = "AppThemePref: ";
    SharedPreferences sharedPref;
    Activity mActivity;

    public AppThemePreferences(Activity activity) {
        //mContext=context;
        mActivity = activity;
        sharedPref = activity.getSharedPreferences(
                activity.getString(R.string.Pref_ThemeMode), Context.MODE_PRIVATE);
    }

    public String getTheme() {
        return sharedPref.getString(mActivity.getString(R.string.Pref_ThemeMode), SYSTEM_DEFAULT);
    }

    public String getCurrentTheme() {
        int m = AppCompatDelegate.getDefaultNightMode();
        Log.d(TAG, "getCurrentTheme: " + m);
        if (m == AppCompatDelegate.MODE_NIGHT_NO) {
            return LIGHT_MODE;
        } else if (m == AppCompatDelegate.MODE_NIGHT_YES) {
            return DARK_MODE;
        } else if (m == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            return SYSTEM_DEFAULT;
        } else if (m == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            return SYSTEM_DEFAULT;
        } else {
            return SYSTEM_DEFAULT;
        }
    }

    public void ThemeApply() {

        String setting = getTheme();
        String current = getCurrentTheme();
        if (setting != current) {
            updateTheme(setting);

        }
        Log.d(TAG, "ThemeApply: setting: " + setting + "\ncurrent: " + current);
    }

    public void updateTheme(String mode) {
        if (mode.equals(AppThemePreferences.SYSTEM_DEFAULT)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            Log.d(TAG, "updateTheme: MODE_NIGHT_FOLLOW_SYSTEM Applied");
        } else if (mode.equals(AppThemePreferences.LIGHT_MODE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Log.d(TAG, "updateTheme: MODE_NIGHT_NO applied");
        } else if (mode.equals(AppThemePreferences.DARK_MODE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Log.d(TAG, "updateTheme: MODE_NIGHT_YES applied");
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            Log.d(TAG, "updateTheme: MODE_NIGHT_FOLLOW_SYSTEM applied #");
        }
    }

    public void setThemePref(String s) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mActivity.getString(R.string.Pref_ThemeMode), s);
        editor.apply();
    }
}

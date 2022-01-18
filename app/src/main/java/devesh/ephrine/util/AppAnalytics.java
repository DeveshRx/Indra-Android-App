package devesh.ephrine.util;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.util.Map;

import devesh.common.utils.AppFlavour;
import devesh.ephrine.BuildConfig;
import devesh.ephrine.R;


public class AppAnalytics {
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;
    String UserUID = "x";
    FirebaseAuth mAuth;

    public AppAnalytics(Application application,Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            UserUID = mAuth.getUid();
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        mFirebaseAnalytics.setUserId(UserUID);
        AppCenter.configure(application, application.getString(R.string.MS_AppCenter_API_Key));
        if (AppCenter.isConfigured()) {
            if(!BuildConfig.FLAVOR.equals(AppFlavour.INTERNAL)) {
                AppCenter.start(Analytics.class);
            }
            AppCenter.start(Crashes.class);
        }
    }

    public void logEvent(String EventName,Map<String, String> properties) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        if(!BuildConfig.FLAVOR.equals(AppFlavour.INTERNAL)){
            // Firebase Analytics
            mFirebaseAnalytics.logEvent(EventName, bundle);
            mFirebaseAnalytics.setUserId(UserUID);
            mFirebaseAnalytics.setUserProperty("User_UID",UserUID);

            //App Center
            Analytics.trackEvent(EventName, properties);
        }


    }



   /* public void logEvent(Bundle bundle) {
       // Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "App_Flow");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "HomeScreen");
        mFirebaseAnalytics.logEvent("Indra", bundle);

        //App Center
        Analytics.trackEvent("HomeScreen");

    }*/


    /* public void logEvent(String Key, String Value) {
        // Bundle bundle = new Bundle();
         Bundle bundle = new Bundle();
        bundle.putString(Key,Value);
        mFirebaseAnalytics.logEvent("Indra", bundle);

        //App Center
        Analytics.trackEvent("HomeScreen");

    }*/
}

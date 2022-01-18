package devesh.common.utils;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerifyAppInstall {
    public final static int INSTALLED_FROM_UNKNOWN=001;
    public final static int INSTALLED_FROM_GOOGLE=002;
    public final static int INSTALLED_FROM_AMAZON=003;
    public final static int INSTALLED_FROM_GALAXY=004;
final static String TAG="VerifyAppInstall: ";
   public boolean verifyInstallerId(Context context) {
        // A list with valid installers package name
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));

        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }

    public int getInstallSource(Context context){
        List<String> GoogleInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));
        String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        Log.d(TAG, "getInstallSource: installer : "+installer);
        if(GoogleInstallers.contains(installer)){
            return INSTALLED_FROM_GOOGLE;
        }else {
            return INSTALLED_FROM_UNKNOWN;
        }
    }

    public String getInstallSourceRAW(Context context){
        String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
return  installer;
    }

}

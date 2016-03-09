package cl.tidchile.antennagpstracker.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;

/**
 * Created by benjamin on 3/3/16.
 */
public class CommonHelper {

    public static final String SETTINGS_NAME = "mysettings";
    public static final String TOKEN_PREFERENCE = "API_TOKEN";
    public static final String PHONE_PREFERENCE = "PHONE";
    public static final String USERNAME = "guarisnake";
    public static final String PASSWORD = "lacachaelaespasandiacala";

    public static void setPhonePreference(Context context, String phone){
        SharedPreferences.Editor editor = context.getSharedPreferences(
                CommonHelper.SETTINGS_NAME, context.MODE_PRIVATE).edit();
        editor.putString(PHONE_PREFERENCE, phone);
        editor.commit();
    }
    public static String getPhonePreference(Context context){
        SharedPreferences settings = context.getSharedPreferences(
                CommonHelper.SETTINGS_NAME, context.MODE_PRIVATE);
        return settings.getString(PHONE_PREFERENCE, "");
    }

    public static boolean isGPSEnabled (Context context){
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}

package cl.tidchile.antennagpstracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import cl.tidchile.antennagpstracker.util.LocationHelper;


public class TrackerService extends Service {

    private Handler mHandler;
    private Runnable runnable;
    private final int SEND_INTERVAL = 5 * 60 * 1000;//5 minutes
    private final int INTERVAL = 5000;

    private GoogleApiClient mGoogleApiClient;
    private String TAG = "GPS-ANTENNA Tracking service";
    private LocationHelper mLocationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "onCreate");
        mHandler = new Handler();
        runnable = new Runnable() {
            public void run() {
                getCurrentStatus();
                mHandler.postDelayed(runnable, INTERVAL);
            }
        };
        mLocationHelper = new LocationHelper(getApplicationContext());


    }

    private void getCurrentStatus() {
        if (mLocationHelper.hasLocationPermission()) {
            if (mLocationHelper.getCurrentLocation() != null) {
                double lat = mLocationHelper.getCurrentLocation().getLatitude();
                double lon = mLocationHelper.getCurrentLocation().getLatitude();
                String locationTimeStamp = mLocationHelper.getLastUpdateTime();
                Toast.makeText(getApplicationContext(), "Location: (" + lat + "," + lon + ") on " + locationTimeStamp, Toast.LENGTH_SHORT).show();
            } else{
                mLocationHelper.startLocationUpdates();
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "onStartCommand");
        Toast.makeText(this, TAG + " started", Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(runnable, INTERVAL);
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        Log.d("Service", "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "onDestroy");
        mHandler.removeCallbacks(runnable);
        Toast.makeText(this, TAG + " terminated", Toast.LENGTH_SHORT).show();
        mLocationHelper.disconnectApiClient();
    }

}
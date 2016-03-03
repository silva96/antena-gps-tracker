package cl.tidchile.antennagpstracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import cl.tidchile.antennagpstracker.models.CellConnection;
import cl.tidchile.antennagpstracker.models.Movement;
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
        if (mLocationHelper.hasLocationPermission() && mLocationHelper.getConnectivityHelper().hasPhonePermission()) {
            Movement m = mLocationHelper.getCurrentMovement();
            if (m != null) {
                String toast = "Phone: "+ m.getPhone()+
                        ",\nLocation: (" + m.getLat() + "," + m.getLon() + ") with " + m.getLocation_accuracy() +"m accuracy\n";
                int counter = 0;
                for(CellConnection cc : m.getCell_connections()){
                    counter++;
                    if(cc.getNetwork_type().equals("LTE")){
                        toast += "Connection "+ counter +": LTE|TAC:"+cc.getTac()+"|PCI:"+cc.getPci()+"|CI:"+cc.getCi()+"|SS:"+cc.getSs()+"|SSL:"+cc.getSsl()+"|REGISTERED:"+cc.is_registered()+"\n\n";
                    }
                    else if(cc.getNetwork_type().contains("GSM") || cc.getNetwork_type().equals("LTE_OLD") || cc.getNetwork_type().equals("WCDMA")){
                        toast += "Connection "+ counter +": "+cc.getNetwork_type()+"|LAC:"+cc.getLac()+"|CID:"+cc.getCid()+"|SS:"+cc.getSs()+"|SSL:"+cc.getSsl()+"|REGISTERED:"+cc.is_registered()+"\n\n";
                    }
                }
                //Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                sendBroadcastMessage(toast);
            } else{
                mLocationHelper.startLocationUpdates();
            }
        }

    }

    private void sendBroadcastMessage(String tv) {
            Intent intent = new Intent("reportValues");
            intent.putExtra("result", tv);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
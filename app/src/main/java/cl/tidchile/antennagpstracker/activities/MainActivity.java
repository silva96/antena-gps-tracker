package cl.tidchile.antennagpstracker.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import cl.tidchile.antennagpstracker.R;
import cl.tidchile.antennagpstracker.services.TrackerService;

public class MainActivity extends AppCompatActivity implements Switch.OnCheckedChangeListener{

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private Switch mEnableTrackerSwitch;
    private final String TAG = "MainActivity";
    private boolean mPermissionGranted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();
        checkForPermissions();



    }

    private void setViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mEnableTrackerSwitch = (Switch) findViewById(R.id.tracking_switch);
        mEnableTrackerSwitch.setChecked(isTrackerServiceRunning());
        mEnableTrackerSwitch.setOnCheckedChangeListener(this);

    }

    private boolean isTrackerServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TrackerService.class.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "Service is running");
                return true;
            }
        }
        Log.d(TAG, "Service is not running");
        return false;
    }

    private void checkForPermissions() {
        int coarse_permission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (coarse_permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Switch sw = (Switch) buttonView;
        if(isChecked && !isTrackerServiceRunning() ){
            if(mPermissionGranted) {
                buttonView.setText(sw.getTextOff());
                Intent intent = new Intent(this, TrackerService.class);
                startService(intent);
            }
            else{
                checkForPermissions();
            }
        }
        else{
            buttonView.setText(sw.getTextOn());
            Intent intent = new Intent(this, TrackerService.class);
            stopService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                       if(!mPermissionGranted){
                           Intent intent = new Intent(this, TrackerService.class);
                           startService(intent);
                           mPermissionGranted = true;
                       }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "The app has no location permission", Toast.LENGTH_SHORT).show();
                    mPermissionGranted = false;
                }
                return;
        }
    }
}

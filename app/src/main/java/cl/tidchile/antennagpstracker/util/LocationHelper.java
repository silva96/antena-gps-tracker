package cl.tidchile.antennagpstracker.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import cl.tidchile.antennagpstracker.models.CellConnection;
import cl.tidchile.antennagpstracker.models.Movement;


public class LocationHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private long mLastUpdateTime;
    private final int GPS_INTERVAL = 1*60*1000;//1 minute
    private boolean hasLocationPermission;
    private ConnectivityHelper mConnectivityHelper;
    private Movement mCurrentMovement = null;

    public LocationHelper(Context context) {
        this.context = context;
        this.hasLocationPermission = true;
        mConnectivityHelper = new ConnectivityHelper(context);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(context, "Could not connect to Google Play Services", Toast.LENGTH_SHORT).show();

    }

    public void startLocationUpdates() {
        mConnectivityHelper.startPhoneUpdates();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M){
            hasLocationPermission = checkLocationPermission();
            if (!hasLocationPermission) {
                Toast.makeText(context, "The app has no location permission", Toast.LENGTH_SHORT).show();
            }
            else{
                createLocationRequest();
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
            }
        } else{
            createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = System.currentTimeMillis()/1000;

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(GPS_INTERVAL);
        mLocationRequest.setFastestInterval(GPS_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public Movement getCurrentMovement(){
        if (mCurrentLocation != null){
            double lat = mCurrentLocation.getLatitude();
            double lon = mCurrentLocation.getLongitude();
            int accuracy = (int)mCurrentLocation.getAccuracy();
            ArrayList<CellConnection> cc = mConnectivityHelper.getCell_connections();

            return new Movement(CommonHelper.getPhonePreference(context), lat, lon, accuracy, mLastUpdateTime, cc);
        }
        else return null;

    }

    public long getLastUpdateTime() {
        return mLastUpdateTime;
    }

    public void disconnectApiClient(){
        mGoogleApiClient.disconnect();
    }

    private boolean checkLocationPermission(){
        int coarse_permission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return coarse_permission == PackageManager.PERMISSION_GRANTED;
    }
    public boolean hasLocationPermission() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            hasLocationPermission = checkLocationPermission();
        }
        return hasLocationPermission;
    }

    public ConnectivityHelper getConnectivityHelper() {
        return mConnectivityHelper;
    }
}

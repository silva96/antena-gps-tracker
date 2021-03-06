package cl.tidchile.antennagpstracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import cl.tidchile.antennagpstracker.models.CellConnection;
import cl.tidchile.antennagpstracker.models.Movement;
import cl.tidchile.antennagpstracker.models.Token;
import cl.tidchile.antennagpstracker.util.CommonHelper;
import cl.tidchile.antennagpstracker.util.ConnectivityHelper;
import cl.tidchile.antennagpstracker.util.RestHelper;
import cl.tidchile.antennagpstracker.util.rest_request_models.PostMovementRequest;
import cl.tidchile.antennagpstracker.util.rest_response_models.PostMovementResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TrackerService extends Service {

    private Handler mHandler;
    private Runnable update_runnable;
    private Runnable send_runnable;
    private final int SEND_INTERVAL = 5 * 60 * 1000;//5 minutes
    private final int INTERVAL = 5 * 1000;
    private boolean gpsEnabled = false;

    private String TAG = "GPS-ANTENNA Tracking service";
    private ConnectivityHelper mConnectivityHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "onCreate");
        mHandler = new Handler();
        gpsEnabled = CommonHelper.isGPSEnabled(getApplicationContext());
        update_runnable = new Runnable() {
            public void run() {
                updateUiWithCurrentStatus();
                updateGpsStatus();
                mHandler.postDelayed(update_runnable, INTERVAL);
            }
        };
        send_runnable = new Runnable() {
            public void run() {
                sendData();
                mHandler.postDelayed(send_runnable, SEND_INTERVAL);
            }
        };
        //mLocationHelper = new LocationHelper(getApplicationContext());
        mConnectivityHelper = new ConnectivityHelper(getApplicationContext());


    }

    private void updateGpsStatus() {
        if(!gpsEnabled && CommonHelper.isGPSEnabled(getApplicationContext())){
            //if it was disabled and now is enabled
            gpsEnabled = true;
            mConnectivityHelper.getLocationHelper().startLocationUpdates();
        }
        else if(gpsEnabled && !CommonHelper.isGPSEnabled(getApplicationContext())){
            sendBroadcastMessage("El GPS está desactivado");
            gpsEnabled = false;
        }
    }

    private void updateUiWithCurrentStatus() {
        if (mConnectivityHelper.hasPhonePermission() && mConnectivityHelper.getLocationHelper().hasLocationPermission()) {
            ArrayList<Movement> storedMovements = mConnectivityHelper.getStoredMovements();
            if(storedMovements != null && storedMovements.size()>0){
                Movement m = mConnectivityHelper.getStoredMovements().get(mConnectivityHelper.getStoredMovements().size()-1);
                if (m != null) {
                    String message = "Phone: " + CommonHelper.getPhonePreference(getApplicationContext()) +
                            ",\nLocation: (" + m.getLat() + "," + m.getLon() + ") with " + m.getLocation_accuracy() + "m accuracy\n";
                    int counter = 0;
                    for (CellConnection cc : m.getCell_connections()) {
                        counter++;
                        if (cc.getNetwork_type().equals("LTE")) {
                            message += "Connection " + counter + ": LTE|TAC:" + cc.getTac() + "|PCI:" + cc.getPci() + "|CI:" + cc.getCi() + "|SS:" + cc.getSs() + "|SSL:" + cc.getSsl() + "|REGISTERED:" + cc.is_registered() + "\n\n";
                        } else if (cc.getNetwork_type().contains("GSM") || cc.getNetwork_type().equals("LTE_OLD") || cc.getNetwork_type().contains("UMTS")) {
                            message += "Connection " + counter + ": " + cc.getNetwork_type() + "|LAC:" + cc.getLac() + "|CID:" + cc.getCid() + "|SS:" + cc.getSs() + "|SSL:" + cc.getSsl() + "|REGISTERED:" + cc.is_registered() + "\n\n";
                        }
                    }
                    Log.d(TAG, "UPDATING UI");
                    sendBroadcastMessage(message);
                } else {
                    mConnectivityHelper.startPhoneUpdates();
                }
            }
            else {
                mConnectivityHelper.startPhoneUpdates();
            }

        }

    }

    private void sendData() {
        if (mConnectivityHelper.hasPhonePermission() && mConnectivityHelper.getLocationHelper().hasLocationPermission()) {
            if (RestHelper.token == null) {
                HashMap<String, String> params = new HashMap<>();
                params.put("username", CommonHelper.USERNAME);
                params.put("password", CommonHelper.PASSWORD);
                Call<Token> call = RestHelper.getService().getUserToken(params);
                call.enqueue(tokenCallback());
            } else {
                ArrayList<Movement> ma = mConnectivityHelper.getStoredMovements();
                if (ma != null && ma.size() > 0) {
                    PostMovementRequest r = new PostMovementRequest(ma);
                    Call<PostMovementResponse> call = RestHelper.getService().postMovements(CommonHelper.getPhonePreference(getApplicationContext()), r);
                    call.enqueue(postMovementsResponseCallback());
                } else {
                    mConnectivityHelper.startPhoneUpdates();
                }
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
        mHandler.postDelayed(update_runnable, INTERVAL);
        mHandler.postDelayed(send_runnable, SEND_INTERVAL);
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
        mHandler.removeCallbacks(update_runnable);
        mHandler.removeCallbacks(send_runnable);
        Toast.makeText(this, TAG + " terminated", Toast.LENGTH_SHORT).show();
        mConnectivityHelper.getLocationHelper().disconnectApiClient();
    }

    public Callback<Token> tokenCallback() {
        return new Callback<Token>() {

            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccess()) {
                    // tasks available
                    RestHelper.setToken(response.body().token);
                    Log.d(TAG, "Token set to: " + response.body().token);
                    sendData();
                } else {
                    Log.d(TAG, "Onresponse but response not success");
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Log.d(TAG, t.getMessage());
                Toast.makeText(getApplicationContext(), "Username or password not found", Toast.LENGTH_LONG).show();
            }
        };
    }

    public Callback<PostMovementResponse> postMovementsResponseCallback() {
        return new Callback<PostMovementResponse>() {

            @Override
            public void onResponse(Call<PostMovementResponse> call, Response<PostMovementResponse> response) {
                if (response.isSuccess()) {
                    // tasks available
                    if (response.body().status != null && response.body().status.equals("ok")) {
                        Toast.makeText(getApplicationContext(), "GPS-Antenna data sent ("+mConnectivityHelper.getStoredMovements().size()+" items)", Toast.LENGTH_LONG).show();
                        mConnectivityHelper.clearStoredMovements();
                    }
                    else if (response.body().error != null) {
                        Toast.makeText(getApplicationContext(), "Something went wrong while sending GPS-Antenna data, error: " +response.body().error, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "Onresponse but response not success");
                }
            }

            @Override
            public void onFailure(Call<PostMovementResponse> call, Throwable t) {
                Log.d(TAG, t.getMessage());
                Toast.makeText(getApplicationContext(), "Something went wrong while sending GPS-Antenna data, error: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

}
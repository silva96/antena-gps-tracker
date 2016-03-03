package cl.tidchile.antennagpstracker.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cl.tidchile.antennagpstracker.R;
import cl.tidchile.antennagpstracker.services.TrackerService;
import cl.tidchile.antennagpstracker.util.CommonHelper;

public class MainActivity extends AppCompatActivity implements Switch.OnCheckedChangeListener {

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private Switch mEnableTrackerSwitch;
    private final String TAG = "MainActivity";
    private EditText mPhoneEditText;
    private RelativeLayout mPhoneEditTextWrapper;
    private RelativeLayout mSwitchWrapper;
    private Button mSavePhoneButton;
    private TextView mResultTextView;
    private String phoneNumber;
    private TelephonyManager mTelephonyManager;
    private boolean mPermissionsGranted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            mPermissionsGranted = false;
            checkForPermissions();
        }else{
            setPhoneNumber();
        }


    }

    private void setViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPhoneEditTextWrapper = (RelativeLayout) findViewById(R.id.phone_number_wrapper);
        mSwitchWrapper = (RelativeLayout) findViewById(R.id.switch_wrapper);
        mPhoneEditText = (EditText) findViewById(R.id.phone_number);
        mResultTextView = (TextView) findViewById(R.id.result_tv);
        mSavePhoneButton = (Button) findViewById(R.id.save_phone);
        mEnableTrackerSwitch = (Switch) findViewById(R.id.tracking_switch);
        mEnableTrackerSwitch.setChecked(isTrackerServiceRunning());
        mEnableTrackerSwitch.setOnCheckedChangeListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String tv = intent.getStringExtra("result");
                        mResultTextView.setText(tv);
                    }
                }, new IntentFilter("reportValues")
        );

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
        int location_permission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int sms_permission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS);
        int phone_permission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE);
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        if (location_permission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (phone_permission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_SMS);
        }
        if (sms_permission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[permissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return;
        }
        mPermissionsGranted=true;
        setPhoneNumber();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Switch sw = (Switch) buttonView;
        if (isChecked && !isTrackerServiceRunning()) {
            if (mPermissionsGranted) {
                if (TextUtils.isEmpty(CommonHelper.getPhonePreference(this))) {
                    Toast.makeText(this, "Por favor ingresa tu teléfono antes de habilitar el servicio", Toast.LENGTH_LONG).show();
                } else {
                    buttonView.setText(sw.getTextOff());
                    Intent intent = new Intent(this, TrackerService.class);
                    startService(intent);
                }
            } else {
                buttonView.setChecked(false);
                checkForPermissions();
            }
        } else {
            buttonView.setText(sw.getTextOn());
            Intent intent = new Intent(this, TrackerService.class);
            stopService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_SMS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);

                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        perms.put(permissions[i], grantResults[i]);
                    }
                    if (perms.get(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = true;
                    }
                    else {
                        mPermissionsGranted = false;
                    }
                    setPhoneNumber();
                }
        }
    }

    private void setPhoneNumber() {
        if (CommonHelper.getPhonePreference(this).equals("")) {
            if(mPermissionsGranted)
                phoneNumber = mTelephonyManager.getLine1Number();
            else phoneNumber = "";
            if (TextUtils.isEmpty(phoneNumber)) {
                mPhoneEditTextWrapper.setVisibility(View.VISIBLE);
                mSavePhoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPhoneEditText.getText().toString().length() != 8) {
                            mPhoneEditText.setError("Ingresa un número válido");
                        } else {
                            mPhoneEditText.setError(null);
                            CommonHelper.setPhonePreference(MainActivity.this, "569"+mPhoneEditText.getText().toString());
                            Toast.makeText(MainActivity.this, "Teléfono guardado", Toast.LENGTH_SHORT).show();
                            //animate out
                            mPhoneEditTextWrapper.animate().translationY(0).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mPhoneEditTextWrapper.setVisibility(View.GONE);
                                    //animate entrance
                                    mSwitchWrapper.setVisibility(View.VISIBLE);
                                    mSwitchWrapper.setAlpha(0.0f);
                                    mSwitchWrapper.animate().translationY(mSwitchWrapper.getHeight()).alpha(1.0f);
                                }
                            });
                        }


                    }
                });
            } else {
                CommonHelper.setPhonePreference(this, phoneNumber);
                Toast.makeText(MainActivity.this, "Teléfono " + phoneNumber + " guardado", Toast.LENGTH_SHORT).show();
                mSwitchWrapper.setVisibility(View.VISIBLE);
            }
        }
        else{
            mSwitchWrapper.setVisibility(View.VISIBLE);
        }
    }
}

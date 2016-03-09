package cl.tidchile.antennagpstracker.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cl.tidchile.antennagpstracker.models.CellConnection;
import cl.tidchile.antennagpstracker.models.Movement;

/**
 * Created by benjamin on 3/3/16.
 */
public class ConnectivityHelper extends PhoneStateListener {
    private final LocationHelper mLocationHelper;
    private TelephonyManager mTelephonyManager;
    private Context context;
    private boolean hasPhonePermission = true;
    private ArrayList<CellConnection> cell_connections = new ArrayList<>();
    private long mLastUpdateTime;
    private ArrayList<Movement> movements = new ArrayList<>();

    public ConnectivityHelper(Context context) {
        this.context = context;
        mLocationHelper = new LocationHelper(context);
        this.mTelephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
        startPhoneUpdates();
    }

    public void startPhoneUpdates() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            hasPhonePermission = checkPhonePermission();
            if (!hasPhonePermission) {
                Toast.makeText(context, "The app has no READ_PHONE_STATE permission", Toast.LENGTH_SHORT).show();
            } else {
                mTelephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION);
            }
        } else {
            mTelephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION);
        }

    }

    private boolean checkPhonePermission() {
        int coarse_permission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE);
        return coarse_permission == PackageManager.PERMISSION_GRANTED;

    }

    public boolean hasPhonePermission() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            hasPhonePermission = checkPhonePermission();
        }
        return hasPhonePermission;
    }

    public Movement getCurrentMovement() {
        if (mLocationHelper.getCurrentLocation() != null) {
            double lat = mLocationHelper.getCurrentLocation().getLatitude();
            double lon = mLocationHelper.getCurrentLocation().getLongitude();
            int accuracy = (int) mLocationHelper.getCurrentLocation().getAccuracy();
            ArrayList<CellConnection> cc = getCell_connections();

            return new Movement(CommonHelper.getPhonePreference(context), lat, lon, accuracy, mLastUpdateTime, cc);
        } else return null;

    }

    @Override
    public void onCellLocationChanged(CellLocation location) {
        super.onCellLocationChanged(location);
        //each time location of any antenna changes, get all new connections.
        List<CellInfo> cellInfoList = mTelephonyManager.getAllCellInfo();
        mLastUpdateTime = System.currentTimeMillis() / 1000;
        cell_connections.clear();
        if (cellInfoList != null) {
            Log.d("CELLINFO", cellInfoList.size() + "");
            for (CellInfo cellInfo : cellInfoList) {

                if (cellInfo instanceof CellInfoLte) {
                    int pci = ((CellInfoLte) cellInfo).getCellIdentity().getPci();
                    int ci = ((CellInfoLte) cellInfo).getCellIdentity().getCi();
                    int tac = ((CellInfoLte) cellInfo).getCellIdentity().getTac();
                    int ss = ((CellInfoLte) cellInfo).getCellSignalStrength().getAsuLevel();
                    int ssl = ((CellInfoLte) cellInfo).getCellSignalStrength().getLevel();
                    boolean is_registered = cellInfo.isRegistered();
                    CellConnection cc = new CellConnection(pci, ci, tac, ss, ssl, is_registered, "LTE");
                    cell_connections.add(cc);
                } else if (cellInfo instanceof CellInfoGsm) {
                    int cid = ((CellInfoGsm) cellInfo).getCellIdentity().getCid() & 0xffff;
                    int lac = ((CellInfoGsm) cellInfo).getCellIdentity().getLac() & 0xffff;
                    int ss = ((CellInfoGsm) cellInfo).getCellSignalStrength().getAsuLevel();
                    int ssl = ((CellInfoGsm) cellInfo).getCellSignalStrength().getLevel();
                    boolean is_registered = cellInfo.isRegistered();
                    if (cid != 65535 && lac != 65535) {
                        CellConnection cc = new CellConnection(cid, lac, ss, ssl, is_registered, "GSM");
                        cell_connections.add(cc);
                    }
                }
                if (cellInfo instanceof CellInfoWcdma) {
                    int cid = ((CellInfoWcdma) cellInfo).getCellIdentity().getCid() & 0xffff;
                    int lac = ((CellInfoWcdma) cellInfo).getCellIdentity().getLac() & 0xffff;
                    int ss = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getAsuLevel();
                    int ssl = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getLevel();
                    boolean is_registered = cellInfo.isRegistered();
                    if (cid != 65535 && lac != 65535) {
                        CellConnection cc = new CellConnection(cid, lac, ss, ssl, is_registered, "UMTS");
                        cell_connections.add(cc);
                    }
                }
            }
        } else {
            GsmCellLocation cl = (GsmCellLocation) location;
            int cid = cl.getCid() & 0xffff;
            int lac = cl.getLac() & 0xffff;
            int nt = mTelephonyManager.getNetworkType();
            String network_type = "GSM_OLD";
            if (nt == TelephonyManager.NETWORK_TYPE_HSDPA || nt == TelephonyManager.NETWORK_TYPE_HSPAP
                    || nt == TelephonyManager.NETWORK_TYPE_HSPA || nt == TelephonyManager.NETWORK_TYPE_HSUPA || nt == TelephonyManager.NETWORK_TYPE_UMTS)
                network_type = "UMTS_OLD";
            else if (nt == TelephonyManager.NETWORK_TYPE_LTE) network_type = "LTE_OLD";
            boolean is_registered = true;
            CellConnection cc = new CellConnection(cid, lac, is_registered, network_type);
            cell_connections.add(cc);
        }
        Movement m = getCurrentMovement();
        if (m != null) movements.add(m);

    }

    private ArrayList<CellConnection> getCell_connections() {
        return cell_connections;
    }

    public LocationHelper getLocationHelper() {
        return mLocationHelper;
    }

    public ArrayList<Movement> getStoredMovements() {
        return movements;
    }

    public void clearStoredMovements() {
        movements.clear();
    }
}

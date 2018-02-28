package com.beneville.grandfatherclock.helpers;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

/**
 * Created by joeja on 11/14/2017.
 */
public class BleDeviceScanner {

    private final static String TAG = BleDeviceScanner.class.getSimpleName();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 2000;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private ScanStoppedCallback mScanStopped;

    public BleDeviceScanner() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler();
    }

    public void onStop(ScanStoppedCallback callback) {
        mScanStopped = callback;
    }

    public void scanLeDevice(final boolean enable, final BluetoothAdapter.LeScanCallback callback) {
        if (enable == true && mScanning == false) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Turn off scanning
                    scanLeDevice(false, callback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(callback);
        } else if (enable == false && mScanning == true) {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(callback);
            if (mScanStopped != null) {
                mScanStopped.onStopped();
            }
        }
    }

    public boolean isScanning() {
        return mScanning;
    }

    public interface ScanStoppedCallback {
        void onStopped();
    }
}

package com.beneville.grandfatherclock.helpers;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

/**
 * Created by joeja on 11/14/2017.
 */

public class BluetoothHelper {

    public static final String TAG = BluetoothHelper.class.getSimpleName();
    public static final int PERMISSION_REQUEST_BLUETOOTH = 100;
    public static final int REQUEST_ENABLE_BT = 100;
    private BluetoothAdapter mBluetoothAdapter;
    private Activity mActivity;
    private DeviceController mController;
    private BluetoothStateChangedCallback mCallback;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // It means the user has changed his bluetooth state.
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                mCallback.onStateChanged(mBluetoothAdapter.getState());
            }
        }
    };

    public BluetoothHelper(Activity activity, DeviceController controller, BluetoothStateChangedCallback callback) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mActivity = activity;
        mCallback = callback;
        mController = controller;
        mActivity.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    public static boolean checkPermissions(Activity activity) {
        boolean hasPerm = true;
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_DENIED) {
            requestLocationPermission(activity);
            hasPerm = false;
        }

        return hasPerm;
    }

    public static void requestLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_BLUETOOTH);
        }
    }

    public static boolean requestEnabled(Activity activity) {
        boolean wait = false;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            wait = true;
        }

        return wait;
    }

    public static boolean isEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public static boolean isBluetoothSupported() {
        return (BluetoothAdapter.getDefaultAdapter() != null);
    }

    public void unregisterBroadCastReceiver() {
        mActivity.unregisterReceiver(mReceiver);
    }

    public STATUS getStatus() {
        STATUS status = STATUS.NONE;

        if (!isBluetoothSupported()) {
            status = STATUS.UNSUPPORTED;
        } else if (!isEnabled()) {
            status = STATUS.ENABLE_BLUETOOTH;
        } else if (!checkPermissions(mActivity)) {
            status = STATUS.ENABLE_PERMISSIONS;
        } else if (mController.getStatus() == BluetoothProfile.STATE_DISCONNECTED) {
            status = STATUS.SCAN;
        } else if (mController.getStatus() == BluetoothProfile.STATE_CONNECTING) {
            status = STATUS.CONNECTING;
        } else if (mController.isReady() == false) {
            status = STATUS.DOWNLOADING;
        }

        return status;
    }

    public enum STATUS {
        NONE,
        UNSUPPORTED,
        ENABLE_PERMISSIONS,
        ENABLE_BLUETOOTH,
        SCAN,
        CONNECTING,
        DOWNLOADING,
    }

    public interface BluetoothStateChangedCallback {
        void onStateChanged(int state);
    }


}
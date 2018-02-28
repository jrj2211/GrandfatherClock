package com.beneville.grandfatherclock;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.beneville.grandfatherclock.fragments.BaseFragment;
import com.beneville.grandfatherclock.fragments.ControlFragment;
import com.beneville.grandfatherclock.helpers.BluetoothHelper;
import com.beneville.grandfatherclock.helpers.DeviceController;
import com.beneville.grandfatherclock.views.BatteryView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    BluetoothHelper mBleHelper;
    DeviceController mDeviceController;

    private BluetoothHelper.BluetoothStateChangedCallback mBluetoothStateChanged = new BluetoothHelper.BluetoothStateChangedCallback() {
        @Override
        public void onStateChanged(int state) {
            BaseFragment.showSetupFragment(MainActivity.this, mBleHelper);
        }
    };

    private DeviceController.DeviceStatusChangeListener mDeviceStatusChange = new DeviceController.DeviceStatusChangeListener() {
        @Override
        public void onChange(int state) {
            BaseFragment.showSetupFragment(MainActivity.this, mBleHelper);
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case BluetoothHelper.REQUEST_ENABLE_BT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    BaseFragment.showSetupFragment(MainActivity.this, mBleHelper);
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make it full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        mDeviceController = new DeviceController(this, mDeviceStatusChange);
        mBleHelper = new BluetoothHelper(this, mDeviceController, mBluetoothStateChanged);

        if (mBleHelper.getStatus() == BluetoothHelper.STATUS.NONE) {
            BaseFragment.startFragment(this, new ControlFragment());
        } else {
            BaseFragment.showSetupFragment(this, mBleHelper);
        }

        // Setup a battery view
        new BatteryView((TextView) findViewById(R.id.battery_level), this);

        lockDevice();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    public DeviceController getDeviceController() {
        return mDeviceController;
    }

    private void lockDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // get policy manager
            DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            // get this app package name
            ComponentName mDPM = new ComponentName(this, MainActivity.class);

            if (myDevicePolicyManager.isDeviceOwnerApp(this.getPackageName())) {
                // get this app package name
                String[] packages = {this.getPackageName()};
                // mDPM is the admin package, and allow the specified packages to lock task
                myDevicePolicyManager.setLockTaskPackages(mDPM, packages);
                startLockTask();
                Log.d(TAG, "Device owner so pinned app");
            } else {
                Log.d(TAG, "Not a device owner so app cant be pinned");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBleHelper.unregisterBroadCastReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDeviceController.registerGattReceiver(this);
    }

}

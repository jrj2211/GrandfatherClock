package com.beneville.grandfatherclock;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.beneville.grandfatherclock.fragments.BaseFragment;
import com.beneville.grandfatherclock.fragments.ControlFragment;
import com.beneville.grandfatherclock.helpers.BluetoothHelper;
import com.beneville.grandfatherclock.helpers.DeviceController;
import com.beneville.grandfatherclock.helpers.KioskModeReceiver;
import com.beneville.grandfatherclock.views.BatteryView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    BluetoothHelper mBleHelper;
    DeviceController mDeviceController;
    Context mContext = this;

    private int unlockKioskCount = 0;
    private long unlockKioskTime=0;

    private BluetoothHelper.BluetoothStateChangedCallback mBluetoothStateChanged = new BluetoothHelper.BluetoothStateChangedCallback() {
        @Override
        public void onStateChanged(int state) {
            onStateChangeFragmentCheck();
        }
    };

    private DeviceController.DeviceStatusChangeListener mDeviceStatusChange = new DeviceController.DeviceStatusChangeListener() {
        @Override
        public void onChange(int state) {
            onStateChangeFragmentCheck();
        }
    };

    private DeviceController.DataDownloadedListener mDataDownloadedStatusChange = new DeviceController.DataDownloadedListener() {
        @Override
        public void onDownloaded() {
            onStateChangeFragmentCheck();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case BluetoothHelper.REQUEST_ENABLE_BT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onStateChangeFragmentCheck();
                }
                break;
            }
        }
    }

    private void onStateChangeFragmentCheck() {
        if (mBleHelper.needsSetupStep()) {
            BaseFragment.showSetupFragment(mContext, mBleHelper);
        } else if (BaseFragment.isSetupStepShowing(mContext)) {
            BaseFragment.startFragment(mContext, new ControlFragment());
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

        mDeviceController.setDataDownloadedListener(mDataDownloadedStatusChange);

        onStateChangeFragmentCheck();

        // Setup a battery view
        new BatteryView((TextView) findViewById(R.id.battery_level), this);
    }

    public DeviceController getDeviceController() {
        return mDeviceController;
    }

    private void lockDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // get policy manager
            DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            // get this app package name
            ComponentName mDPM = new ComponentName(this, KioskModeReceiver.class);

            if (myDevicePolicyManager != null && myDevicePolicyManager.isDeviceOwnerApp(this.getPackageName())) {
                // get this app package name
                String[] packages = {this.getPackageName()};
                // mDPM is the admin package, and allow the specified packages to lock task
                myDevicePolicyManager.setLockTaskPackages(mDPM, packages);
                startLockTask();
                Log.d(TAG, "Device owner so pinned app");

                muteDevice();
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

        lockDevice();

        findViewById(R.id.clock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long time= System.currentTimeMillis();

                if (unlockKioskTime==0 || (time-unlockKioskTime> 3000) ) {
                    unlockKioskTime=time;
                    unlockKioskCount=1;
                } else{
                    unlockKioskCount++;
                }

                if (unlockKioskCount==10) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        stopLockTask();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //do something or nothing in your case
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void muteDevice() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        }
    }
}

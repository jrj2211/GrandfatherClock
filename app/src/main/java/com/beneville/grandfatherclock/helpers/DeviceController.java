package com.beneville.grandfatherclock.helpers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.services.BleService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by joeja on 1/26/2018.
 */

public class DeviceController {

    private static final String TAG = DeviceController.class.getSimpleName();

    private BleService mBleService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(TAG, "Service Connected");
            mBleService = ((BleService.LocalBinder) service).getService();

            if (!mBleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };
    private List<BluetoothGattService> mGattServices = new ArrayList<>();
    private DeviceStatusChangeListener mStatusChangeListener;
    private BluetoothGattService mGattPlaybackService;
    private BluetoothGattService mGattFileNameService;
    private BluetoothGattService mGattCalibrationService;
    private Context mContext;
    private int curVolume = 0;
    private SyncSongs syncSongs;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BleService.ACTION_GATT_CONNECTING.equals(action)) {
                Log.d(TAG, "Bluetooth Connecting!");
                notifyStatusChange();
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "Bluetooth Connected!");
                notifyStatusChange();
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "Bluetooth Disconnected!");
                notifyStatusChange();
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "Bluetooth Discovered!");
                getServices();
                notifyStatusChange();
            } else if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_playback_mode_characteristic))) {

                }

                if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_count_characteristic))) {
                    byte[] bytes = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                    if (bytes.length > 0) {
                        syncSongs.setNumFiles(bytes[0]);
                        syncSongs.getSongInfo(DeviceController.this);
                    }
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_characteristic))) {
                    byte[] bytes = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                    syncSongs.setTitle(bytes.toString(), DeviceController.this);
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_artist_characteristic))) {
                    byte[] bytes = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                    syncSongs.setArtist(bytes.toString(), DeviceController.this);
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_metadata_genre_characteristic))) {
                    byte[] bytes = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                    syncSongs.setGenre(bytes.toString(), DeviceController.this);
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_disable_read_characteristic))) {
                    byte[] bytes = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                    if (bytes.length > 0) {
                        syncSongs.setDisabled((bytes[0] == 1 ? true : false), DeviceController.this);
                    }
                }
            }
        }
    };

    public DeviceController(Context context, DeviceStatusChangeListener statusChangeListener) {
        Intent intent = new Intent(context, BleService.class);
        context.bindService(intent, mServiceConnection, context.BIND_AUTO_CREATE);
        mContext = context;
        mStatusChangeListener = statusChangeListener;
        syncSongs = new SyncSongs(context);
    }

    public void registerGattReceiver(Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleService.ACTION_DESCRIPTOR_WROTE);

        context.getApplicationContext().registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    private void getServices() {
        mGattServices = mBleService.getSupportedGattServices();
        for (BluetoothGattService service : mGattServices) {
            if (service.getUuid().toString().equals(mContext.getString(R.string.gatt_calibration_service))) {
                mGattCalibrationService = service;
            } else if (service.getUuid().toString().equals(mContext.getString(R.string.gatt_playback_service))) {
                mGattPlaybackService = service;
            } else if (service.getUuid().toString().equals(mContext.getString(R.string.gatt_file_name_service))) {
                mGattFileNameService = service;
            }
        }

        subscribePlaybackMode(true);
        getSongCount();

        FakeDatabase db = new FakeDatabase(mContext);

        // TODO : REMOVE
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyStatusChange();
            }
        }, 2000);
    }

    private BluetoothGattCharacteristic getCharacteristicFromService(int resource, BluetoothGattService service) {
        BluetoothGattCharacteristic characteristic = null;
        if (service != null) {
            service.getCharacteristic(UUID.fromString(mContext.getString(resource)));
        }

        return characteristic;
    }

    public void setAudioVolume(int volume) {
        if (volume < 0) {
            volume = 0;
        } else if (volume > 100) {
            volume = 100;
        }

        Log.e(TAG, "Setting volume to " + volume);

        if (curVolume != volume) {
            BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_volume_characteristic, mGattPlaybackService);
            mBleService.queueWriteCharacteristic(characteristic, volume);
            curVolume = volume;
        }
    }

    public void setPlayControl(boolean toggled) {
        Log.e(TAG, "Playback  " + toggled);
        if (toggled) {
            BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_start_characteristic, mGattPlaybackService);
            mBleService.queueWriteCharacteristic(characteristic, true);
        } else {
            BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_stop_characteristic, mGattPlaybackService);
            mBleService.queueWriteCharacteristic(characteristic, false);
        }
    }

    public void setPlaybackMode(PlaybackMode mode) {
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_stop_characteristic, mGattPlaybackService);
        mBleService.queueWriteCharacteristic(characteristic, mode.getValue());
    }

    public void subscribePlaybackMode(boolean subscribe) {
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_mode_characteristic, mGattPlaybackService);
        mBleService.queueSubscribeCharacteristic(characteristic, subscribe);
    }

    public void getSongCount() {
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_count_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void getSongTitle() {
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void getSongArtist() {
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_artist_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void getSongGenre() {
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_metadata_genre_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void getSongDisabled() {
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_disable_read_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void setNextSongIndex(int index) {
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_index_characteristic, mGattFileNameService);
        mBleService.queueWriteCharacteristic(characteristic, index);
    }

    public void connectGatt(BluetoothDevice device) {
        connectGatt(device.getAddress());
    }

    public void connectGatt(String address) {
        // Connect to the device
        mBleService.connect(address);
    }

    public void disconnectGatt() {
        mBleService.disconnect();
    }

    public int getStatus() {
        if (mBleService != null) {
            return mBleService.getDeviceState();
        }
        return BluetoothProfile.STATE_DISCONNECTED;
    }

    public boolean isReady() {
        return syncSongs.isComplete();
    }

    private void notifyStatusChange() {
        if (mStatusChangeListener != null) {
            mStatusChangeListener.onChange(getStatus());
        }
    }

    public enum PlaybackMode {
        SHUFFLE(0),
        MOVIE(1),
        MUSIC(2),
        BOOK(3),
        BELL(4),
        MUTE(5);

        private final int value;

        private PlaybackMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public interface DeviceStatusChangeListener {
        public void onChange(int status);
    }
}

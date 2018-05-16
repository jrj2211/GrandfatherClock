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
import android.support.v4.math.MathUtils;
import android.util.Log;

import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.services.BleService;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by joeja on 1/26/2018.
 */

public class DeviceController {

    private static final String TAG = DeviceController.class.getSimpleName();

    private Context mContext;

    // Services
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
    private BluetoothGattService mGattPlaybackService;
    private BluetoothGattService mGattFileNameService;
    private BluetoothGattService mGattCalibrationService;
    // Song Downloading
    private Queue<PlaybackMode> playbackModesToLoad = new ArrayDeque<PlaybackMode>();
    private SyncSongs syncSongs;
    // Current Device State
    private int numberOfSongs = 0;
    private PlaybackMode currentMode = PlaybackMode.ALL;
    private int currentSongIndex = -1;
    private boolean currentPlaybackState = false;
    private int currentVolume = 70;
    // Setup listeners
    private DeviceStatusChangeListener mStatusChangeListener;
    private DataDownloadedListener mDataDownloadedListener;
    private SongChangedListener mSongChangedListener;
    private VolumeChangedListener mVolumeChangedListener;
    private PlaybackStateListener mPlaybackStateListener;
    private ModeChangedListener mModeChangedListener;
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
                if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_count_characteristic))) {
                    numberOfSongs = GetIntFromIntent(intent);
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_playback_mode_first_index_characteristic))) {
                    Log.e(TAG, "Got file first index: " + GetIntFromIntent(intent));
                    ModeIndexes.setPlaybackStartIndex(currentMode, GetIntFromIntent(intent));
                    if (playbackModesToLoad.size() > 0) {
                        // Loading playback modes to download songs
                        getGenreIndex(playbackModesToLoad.remove());
                    } else {
                        if (!AppSettings.getInstance(mContext).getSongsDownloaded()) {
                            // No songs so download if necessary
                            if (!syncSongs.isDownloading()) {
                                // Start downloading the songs
                                syncSongs.startSync(numberOfSongs, DeviceController.this);
                            }
                        }
                    }
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_fragment_count_characteristic))) {
                    syncSongs.getTitle(GetIntFromIntent(intent), DeviceController.this);
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_artist_fragment_count_characteristic))) {
                    syncSongs.getArtist(GetIntFromIntent(intent), DeviceController.this);
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_characteristic))) {
                    byte[] bytes = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                    syncSongs.setTitle(new String(bytes), DeviceController.this);
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_artist_characteristic))) {
                    byte[] bytes = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                    syncSongs.setArtist(new String(bytes), DeviceController.this);
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_playback_volume_characteristic))) {
                    setCurrentVolume(GetIntFromIntent(intent));
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_playback_mode_characteristic))) {
                    setCurrentPlaybackMode(PlaybackMode.fromInteger(GetIntFromIntent(intent)));
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_file_name_index_characteristic))) {
                    setCurrentSongIndex(GetIntFromIntent(intent));
                    Log.e(TAG, "Got song index: " + GetIntFromIntent(intent));
                } else if (intent.getStringExtra(BleService.CHARACTERISTIC).equals(context.getString(R.string.gatt_playback_start_characteristic))) {
                    boolean isPlaying = GetIntFromIntent(intent) % 100 == 1;
                    setCurrentPlaybackState(isPlaying);
                }
            }
        }
    };
    private SyncSongs.CompleteListener mSongsDownloadedListener = new SyncSongs.CompleteListener() {
        @Override
        public void OnComplete() {
            GetDefaultsAndSubscribe();

            if (mDataDownloadedListener != null) {
                mDataDownloadedListener.onDownloaded();
            }
        }
    };

    public DeviceController(Context context, DeviceStatusChangeListener statusChangeListener) {
        Intent intent = new Intent(context, BleService.class);
        context.bindService(intent, mServiceConnection, context.BIND_AUTO_CREATE);
        mContext = context;
        mStatusChangeListener = statusChangeListener;
        syncSongs = new SyncSongs(context);
        syncSongs.setCompleteListener(mSongsDownloadedListener);
    }

    public SyncSongs getSongSync() {
        return syncSongs;
    }

    public PlaybackMode getCurrentMode() {
        return currentMode;
    }

    public int GetIntFromIntent(Intent intent) {
        try {
            byte[] bytes = intent.getByteArrayExtra(BleService.EXTRA_DATA);
            switch (bytes.length) {
                case 0:
                    return 0;
                case 1:
                    return bytes[0];
                case 2:
                    return ByteBuffer.wrap(bytes).getShort();
                case 4:
                    return ByteBuffer.wrap(bytes).getInt();
                default:
                    throw new Exception("Cant get int from " + bytes.length + " bytes");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get int from characteristic");
        }
        return 0;
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
                Log.d(TAG, "Found calibration service");
            } else if (service.getUuid().toString().equals(mContext.getString(R.string.gatt_playback_service))) {
                mGattPlaybackService = service;
                Log.d(TAG, "Found playback service");
            } else if (service.getUuid().toString().equals(mContext.getString(R.string.gatt_file_name_service))) {
                mGattFileNameService = service;
                Log.d(TAG, "Found file name service");
            }
        }

        readSongCount();
        startSongDownload();

        // TODO : REMOVE
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyStatusChange();
            }
        }, 2000);
    }

    private void GetDefaultsAndSubscribe() {
        // Subscribe to changes
        mBleService.queueSubscribeCharacteristic(mGattPlaybackService.getCharacteristic(UUID.fromString(mContext.getString(R.string.gatt_playback_mode_characteristic))), true);
        mBleService.queueSubscribeCharacteristic(mGattPlaybackService.getCharacteristic(UUID.fromString(mContext.getString(R.string.gatt_playback_volume_characteristic))), true);
        mBleService.queueSubscribeCharacteristic(mGattPlaybackService.getCharacteristic(UUID.fromString(mContext.getString(R.string.gatt_playback_start_characteristic))), true);
        mBleService.queueSubscribeCharacteristic(mGattFileNameService.getCharacteristic(UUID.fromString(mContext.getString(R.string.gatt_file_name_index_characteristic))), true);

        // Read Defaults
        mBleService.queueReadCharacteristic(mGattPlaybackService.getCharacteristic(UUID.fromString(mContext.getString(R.string.gatt_playback_mode_characteristic))));
        mBleService.queueReadCharacteristic(mGattPlaybackService.getCharacteristic(UUID.fromString(mContext.getString(R.string.gatt_playback_volume_characteristic))));
        mBleService.queueReadCharacteristic(mGattPlaybackService.getCharacteristic(UUID.fromString(mContext.getString(R.string.gatt_playback_start_characteristic))));
        mBleService.queueReadCharacteristic(mGattFileNameService.getCharacteristic(UUID.fromString(mContext.getString(R.string.gatt_file_name_index_characteristic))));
    }

    private BluetoothGattCharacteristic getCharacteristicFromService(int resource, BluetoothGattService service) {
        BluetoothGattCharacteristic characteristic = null;

        if (service != null) {
            characteristic = service.getCharacteristic(UUID.fromString(mContext.getString(resource)));
        }

        return characteristic;
    }

    private void getGenreIndex(PlaybackMode mode) {
        if (mode != null) {
            writePlaybackMode(mode);
            readModeFirstIndex();
        }
    }

    public void playSong(int index) {
        currentSongIndex = index;
        writeSongIndex(index);
        writePlayControl(true);
    }

    public void startSongDownload() {
        if (AppSettings.getInstance(mContext).getSongsDownloaded()) {
            // Songs already downloaded so no need to resync
            syncSongs.setComplete();
            GetDefaultsAndSubscribe();
        } else {
            // Load the indexes for each genre
            playbackModesToLoad.add(PlaybackMode.BOOK);
            playbackModesToLoad.add(PlaybackMode.MOVIE);
            playbackModesToLoad.add(PlaybackMode.MUSIC);

            getGenreIndex(playbackModesToLoad.remove());
        }
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    private void setCurrentSongIndex(int index) {
        if (index != currentSongIndex) {
            currentSongIndex = index;
            Log.w(TAG, "Changing song to " + currentSongIndex);
            if (mSongChangedListener != null) {
                mSongChangedListener.onChange(currentSongIndex);
            }
        }
    }

    public int getCurrentVolume() {
        return currentVolume;
    }

    private void setCurrentVolume(int volume) {
        if (volume != currentVolume) {
            currentVolume = volume;
            Log.w(TAG, "Changing volume to " + currentVolume);
            if (mVolumeChangedListener != null) {
                mVolumeChangedListener.onChange(currentVolume);
            }
        }
    }

    public boolean getPlaybackControl() {
        return currentPlaybackState;
    }

    private void setCurrentPlaybackMode(PlaybackMode mode) {
        if (mode != currentMode) {
            currentMode = mode;
            Log.w(TAG, "Changing mode to " + currentMode);
            if (mModeChangedListener != null) {
                mModeChangedListener.onChange(currentMode);
            }
        }
    }

    private void setCurrentPlaybackState(boolean playing) {
        if (playing != currentPlaybackState) {
            currentPlaybackState = playing;

            if (mPlaybackStateListener != null) {
                mPlaybackStateListener.onChange(playing);
            }
        }
    }

    public void writeNext(boolean play) {
        if (currentMode != PlaybackMode.MUTE && currentMode != PlaybackMode.BELL) {
            Log.w(TAG, "Writing next command");
            BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_next_characteristic, mGattPlaybackService);
            mBleService.queueWriteCharacteristic(characteristic, true);
            writePlayControl(play);
        }
    }

    public void writePrevious(boolean play) {
        if (currentMode != PlaybackMode.MUTE && currentMode != PlaybackMode.BELL) {
            Log.w(TAG, "Writing previous command");
            BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_previous_characteristic, mGattPlaybackService);
            mBleService.queueWriteCharacteristic(characteristic, true);
            writePlayControl(play);
        }
    }

    public void writeAudioVolume(int volume) {
        // Keep volume from 0 to 100
        volume = MathUtils.clamp(volume, 0, 100);

        Log.w(TAG, "Writing audio volume " + currentVolume);
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_volume_characteristic, mGattPlaybackService);
        mBleService.queueWriteCharacteristic(characteristic, volume);
        setCurrentVolume(volume);
    }

    public void writePlayControl(boolean toggled) {
        BluetoothGattCharacteristic characteristic;

        if (toggled) {
            characteristic = getCharacteristicFromService(R.string.gatt_playback_start_characteristic, mGattPlaybackService);
        } else {
            characteristic = getCharacteristicFromService(R.string.gatt_playback_stop_characteristic, mGattPlaybackService);
        }

        Log.w(TAG, "Writing playback " + toggled);
        mBleService.queueWriteCharacteristic(characteristic, true);
        setCurrentPlaybackState(toggled);
    }

    public void writePlaybackMode(PlaybackMode mode) {
        Log.w(TAG, "Writing mode: " + mode.getValue());
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_mode_characteristic, mGattPlaybackService);
        mBleService.queueWriteCharacteristic(characteristic, mode.getValue());
        readCurrentSongIndex();
        setCurrentPlaybackMode(mode);
        setCurrentPlaybackState(false);
    }

    public void readModeFirstIndex() {
        Log.w(TAG, "Reading mode first index");
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_playback_mode_first_index_characteristic, mGattPlaybackService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void readCurrentSongIndex() {
        Log.w(TAG, "Reading current song index");
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_index_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void readSongCount() {
        Log.w(TAG, "Reading file count");
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_count_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void readSongTitleFragmentCount() {
        Log.w(TAG, "Reading title fragment count");
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_fragment_count_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void readSongArtistFragmentCount() {
        Log.w(TAG, "Reading artist fragment count");
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_artist_fragment_count_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void readSongTitle() {
        Log.w(TAG, "Reading title");
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void readSongArtist() {
        Log.w(TAG, "Reading artist");
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_artist_characteristic, mGattFileNameService);
        mBleService.queueReadCharacteristic(characteristic);
    }

    public void writeSongIndex(int index) {
        Log.w(TAG, "Writing index " + index);
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_index_characteristic, mGattFileNameService);
        mBleService.queueWriteCharacteristic(characteristic, index);
        setCurrentSongIndex(index);
    }

    public void disableSong(int index, boolean disable) {
        Log.w(TAG, "Disabling index " + index);
        writeSongIndex(index);
        BluetoothGattCharacteristic characteristic = getCharacteristicFromService(R.string.gatt_file_name_disable_set_characteristic, mGattFileNameService);
        mBleService.queueWriteCharacteristic(characteristic, disable);
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

    public boolean songsDownloaded() {
        return syncSongs.isComplete();
    }

    private void notifyStatusChange() {
        if (mStatusChangeListener != null) {
            mStatusChangeListener.onChange(getStatus());
        }
    }

    public void setSongChangedListener(SongChangedListener listener) {
        mSongChangedListener = listener;
    }

    public void setDataDownloadedListener(DataDownloadedListener listener) {
        mDataDownloadedListener = listener;
    }

    public void setVolumeChangedListener(VolumeChangedListener listener) {
        mVolumeChangedListener = listener;
    }

    public void setPlaybackStateChanged(PlaybackStateListener listener) {
        mPlaybackStateListener = listener;
    }

    public void setModeChangedListener(ModeChangedListener listener) {
        mModeChangedListener = listener;
    }

    public enum PlaybackMode {
        ALL(0),
        BELL(1),
        MUTE(2),
        MUSIC(3),
        MOVIE(4),
        BOOK(5);

        private final int value;

        PlaybackMode(int value) {
            this.value = value;
        }

        public static PlaybackMode fromInteger(int x) {
            if (x == BELL.getValue()) return BELL;
            else if (x == MUTE.getValue()) return MUTE;
            else if (x == MUSIC.getValue()) return MUSIC;
            else if (x == MOVIE.getValue()) return MOVIE;
            else if (x == BOOK.getValue()) return BOOK;
            return ALL;
        }

        public int getValue() {
            return value;
        }
    }

    public interface DataDownloadedListener {
        void onDownloaded();
    }

    public interface DeviceStatusChangeListener {
        void onChange(int status);
    }

    public interface ModeChangedListener {
        void onChange(PlaybackMode mode);
    }

    public interface SongChangedListener {
        void onChange(int index);
    }

    public interface VolumeChangedListener {
        void onChange(int volume);
    }

    public interface PlaybackStateListener {
        void onChange(boolean playing);
    }
}

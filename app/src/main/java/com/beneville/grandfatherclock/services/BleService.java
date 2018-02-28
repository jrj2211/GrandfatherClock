package com.beneville.grandfatherclock.services;

/**
 * Created by joeja on 11/14/2017.
 */

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.beneville.grandfatherclock.R;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class BleService extends Service {
    public final static String ACTION_GATT_CONNECTING =
            "ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";
    public final static String ACTION_DESCRIPTOR_WROTE =
            "ACTION_DESCRIPTOR_WROTE";
    public final static String EXTRA_DATA =
            "EXTRA_DATA";
    public final static String CHARACTERISTIC =
            "CHARACTERISTIC";
    public final static String DESCRIPTOR =
            "DESCRIPTOR";
    public final static String DESCRIPTOR_STATUS =
            "DESCRIPTOR_STATUS";
    private final static String TAG = BleService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    private boolean processingCommands = false;
    private Queue<BleCommand> commandQueue = new LinkedList<BleCommand>();
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                mConnectionState = BluetoothProfile.STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i(TAG, "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }

            processCommandQueue();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onCharacteristicWrite Success!");
            } else {
                Log.i(TAG, "onCharacteristicWrite Failed!");
            }

            processCommandQueue();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "onCharacteristicChanged");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorWrite");
            broadcastUpdate(ACTION_DESCRIPTOR_WROTE, descriptor, status);

            processCommandQueue();
        }
    };

    public int getDeviceState() {
        return mConnectionState;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();

        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, data);
            intent.putExtra(CHARACTERISTIC, characteristic.getUuid().toString());
        }
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattDescriptor descriptor,
                                 final int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(DESCRIPTOR, descriptor.getUuid().toString());
        intent.putExtra(DESCRIPTOR_STATUS, status);

        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();

        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        Log.i(TAG, "initialize");

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        Log.i(TAG, "connect: " + address);

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = BluetoothProfile.STATE_CONNECTING;
                broadcastUpdate(ACTION_GATT_CONNECTING);
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = BluetoothProfile.STATE_CONNECTING;

        mConnectionState = BluetoothProfile.STATE_CONNECTING;
        broadcastUpdate(ACTION_GATT_CONNECTING);
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        Log.i(TAG, "disconnect");

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        Log.i(TAG, "close");

        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            processCommandQueue();
            return;
        }

        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0) {
            Log.w(TAG, "Characteristic isn't readable" + characteristic.getUuid());
            processCommandQueue();
            return;
        }

        boolean read = mBluetoothGatt.readCharacteristic(characteristic);
    }

    private void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            processCommandQueue();
            return;
        }

        characteristic.setValue(data);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        if (characteristic != null) {
            // Check if characteristic is a notify property
            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(getBaseContext().getString(R.string.characteristic_update_notification_descriptor_uuid)));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            } else {
                Log.e(TAG, "Provided characteristic is not NOTIFY - " + characteristic.getUuid().toString());
            }
        } else {
            Log.e(TAG, "Failed to write to characteristic because it is null");
        }
    }

    public void queueWriteCharacteristic(BluetoothGattCharacteristic characteristic, byte[] bytes) {
        if (characteristic != null) {
            BleCommand command = new BleCommand();
            command.characteristic = characteristic;
            command.dataToWrite = bytes;
            command.type = BleCommand.CommandType.WriteCharacteristic;

            addCommandToQueue(command);
        } else {
            Log.e(TAG, "Failed to write to characteristic because it is null");
        }
    }

    public void queueWriteCharacteristic(BluetoothGattCharacteristic characteristic, int data) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) data;
        queueWriteCharacteristic(characteristic, bytes);
    }

    public void queueWriteCharacteristic(BluetoothGattCharacteristic characteristic, boolean data) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (data ? 1 : 0);
        queueWriteCharacteristic(characteristic, bytes);
    }

    public void queueReadCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null) {
            BleCommand command = new BleCommand();
            command.characteristic = characteristic;
            command.type = BleCommand.CommandType.ReadCharacteristic;

            addCommandToQueue(command);
        } else {
            Log.e(TAG, "Failed to write to characteristic because it is null");
        }
    }

    public void queueSubscribeCharacteristic(BluetoothGattCharacteristic characteristic, boolean enabled) {
        BleCommand command = new BleCommand();
        command.characteristic = characteristic;
        command.enabled = enabled;
        command.type = BleCommand.CommandType.SubscribeCharacteristic;

        addCommandToQueue(command);
    }

    private void addCommandToQueue(BleCommand command) {
        commandQueue.add(command);

        if (processingCommands == false) {
            processCommandQueue();
        }
    }

    private void processCommandQueue() {
        if (commandQueue.size() <= 0) {
            processingCommands = false;
            return;
        }

        processingCommands = true;
        BleCommand command = commandQueue.remove();

        switch (command.type) {
            case WriteCharacteristic:
                writeCharacteristic(command.characteristic, command.dataToWrite);
                break;
            case SubscribeCharacteristic:
                setCharacteristicNotification(command.characteristic, command.enabled);
                break;
            case ReadCharacteristic:
                readCharacteristic(command.characteristic);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

    enum BleCommandType {
        SubscribeCharacteristic,
        ReadCharacteristic,
        WriteCharacteristic
    }

    private static class BleCommand {

        BluetoothGattCharacteristic characteristic;
        byte[] dataToWrite; // Only used for characteristic write
        boolean enabled; // Only used for characteristic notification subscription
        CommandType type;

        enum CommandType {
            SubscribeCharacteristic,
            ReadCharacteristic,
            WriteCharacteristic
        }

    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

}
package com.beneville.grandfatherclock.fragments.setup;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.beneville.grandfatherclock.MainActivity;
import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.adapters.ListAdapterBluetooth;
import com.beneville.grandfatherclock.helpers.BleDeviceScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joeja on 1/22/2018.
 */

public class ScanDevicesFragment extends Fragment {

    private List<BluetoothDevice> mDevices = new ArrayList<>();
    private BleDeviceScanner mScanner;
    private ListView mDeviceList;
    private ListAdapterBluetooth mDeviceListAdapter;

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            foundDevice(bluetoothDevice);
        }
    };

    private BleDeviceScanner.ScanStoppedCallback scanStoppedCallback = new BleDeviceScanner.ScanStoppedCallback() {
        @Override
        public void onStopped() {
            getView().findViewById(R.id.scanning).setVisibility(View.GONE);
            ((Button) getView().findViewById(R.id.setup_button)).setText("Rescan Devices");

            if (mDevices.size() > 1) {
                // Multiple devices so ask which to connect to
                ((TextView) getView().findViewById(R.id.scan_text)).setText("Multiple devices found");
                getView().findViewById(R.id.scan_list).setVisibility(View.VISIBLE);
                mDeviceListAdapter.setData(mDevices);
            } else if (mDevices.size() == 1) {
                // Only one so just connect to it
                ((MainActivity) getActivity()).getDeviceController().connectGatt(mDevices.get(0));
            } else {
                // No devices found
                ((TextView) getView().findViewById(R.id.scan_text)).setText("No devices found\nCheck that your device is on and then try scanning again");
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.setup_scan_devices, container, false);

        // Create scanner object
        mScanner = new BleDeviceScanner();

        // Setup list for multiple devices found
        mDeviceList = (ListView) view.findViewById(R.id.scan_list);
        mDeviceListAdapter = new ListAdapterBluetooth(getActivity());
        mDeviceList.setAdapter(mDeviceListAdapter);

        mDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = mDeviceListAdapter.getItem(i);
                ((MainActivity) getActivity()).getDeviceController().connectGatt(device);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startScan();

        view.findViewById(R.id.setup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
    }

    private void startScan() {
        // Enable spinner
        getView().findViewById(R.id.scanning).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.scan_list).setVisibility(View.GONE);
        ((Button) getView().findViewById(R.id.setup_button)).setText("Scanning...");
        ((TextView) getView().findViewById(R.id.scan_text)).setText("Scanning for devices");


        // Clear all previously found devices
        clearDevices();
        mDeviceListAdapter.setData(mDevices);

        // Start the new scan
        mScanner.scanLeDevice(true, leScanCallback);
        mScanner.onStop(scanStoppedCallback);
    }

    public void foundDevice(BluetoothDevice device) {
        if (!mDevices.contains(device)) {
            // Find only grandfather clock devices
            //if (getActivity().getString(R.string.bluetooth_device_name).equalsIgnoreCase(device.getName())) {
                // Store the device
                mDevices.add(device);
            //}
        }
    }

    public void clearDevices() {
        mDevices.clear();
    }

}

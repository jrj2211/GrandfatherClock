package com.beneville.grandfatherclock.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.beneville.grandfatherclock.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joeja on 1/22/2018.
 */

public class ListAdapterBluetooth extends BaseAdapter {

    private Context mContext;
    private List<BluetoothDevice> mDevices = new ArrayList<>();

    public ListAdapterBluetooth(Context context) {
        super();

        // Store the context
        mContext = context;
    }

    public void setData(List<BluetoothDevice> devices) {
        mDevices = devices;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothDevice device = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bluetooth_list_item, parent, false);
        }

        // Lookup view for data population
        TextView bluetoothName = (TextView) convertView.findViewById(R.id.device_name);
        TextView bluetoothMac = (TextView) convertView.findViewById(R.id.device_mac);

        // Populate the data into the template view using the data object
        bluetoothName.setText(device.getName() != null ? device.getName() : "Uknown Device");
        bluetoothMac.setText(device.getAddress());

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
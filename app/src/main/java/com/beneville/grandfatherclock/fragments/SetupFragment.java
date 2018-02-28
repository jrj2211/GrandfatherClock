package com.beneville.grandfatherclock.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.fragments.setup.BluetoothUnsupportedFragment;
import com.beneville.grandfatherclock.fragments.setup.ConnectingDeviceFragment;
import com.beneville.grandfatherclock.fragments.setup.DownloadingDataFragment;
import com.beneville.grandfatherclock.fragments.setup.EnableBluetoothFragment;
import com.beneville.grandfatherclock.fragments.setup.EnablePermissionsFragment;
import com.beneville.grandfatherclock.fragments.setup.ScanDevicesFragment;
import com.beneville.grandfatherclock.helpers.BluetoothHelper;

/**
 * Created by joeja on 11/14/2017.
 */

public class SetupFragment extends Fragment {

    BluetoothHelper.STATUS mStatus = BluetoothHelper.STATUS.NONE;

    BluetoothHelper bluetoothHelper;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        super.onCreate(savedInstance);
        View view = inflater.inflate(R.layout.setup, container, false);

        if (bluetoothHelper != null) {
            mStatus = bluetoothHelper.getStatus();
        }

        if (mStatus == BluetoothHelper.STATUS.NONE) {
            BaseFragment.startFragment(getContext(), new ControlFragment(), ControlFragment.class.getSimpleName());
        }

        setStep(view);

        return view;
    }

    private void setStep(View view) {
        switch (mStatus) {
            case UNSUPPORTED:
                showFragment(new BluetoothUnsupportedFragment());
                break;
            case ENABLE_PERMISSIONS:
                showFragment(new EnablePermissionsFragment());
                break;
            case ENABLE_BLUETOOTH:
                showFragment(new EnableBluetoothFragment());
                break;
            case SCAN:
                showFragment(new ScanDevicesFragment());
                break;
            case CONNECTING:
                showFragment(new ConnectingDeviceFragment());
                break;
            case DOWNLOADING:
                showFragment(new DownloadingDataFragment());
                break;
        }
    }

    public void setBluetoothHelper(BluetoothHelper bluetoothHelper) {
        this.bluetoothHelper = bluetoothHelper;
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragManager = ((AppCompatActivity) getActivity()).getSupportFragmentManager();

        // Start the transactions
        FragmentTransaction transaction = fragManager.beginTransaction();
        transaction.add(R.id.setup_fragment, fragment);

        // Show it
        transaction.commitAllowingStateLoss();
    }
}

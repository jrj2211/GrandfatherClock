package com.beneville.grandfatherclock.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.helpers.BluetoothHelper;

/**
 * Created by joeja on 11/7/2017.
 */

public class BaseFragment {

    public static Fragment startFragment(Context context, Fragment newFragment) {
        return startFragment(context, newFragment, null);
    }

    public static Fragment startFragment(Context context, Fragment newFragment, String tag) {
        FragmentManager fragManager = ((AppCompatActivity) context).getSupportFragmentManager();
        Fragment currentFragment = (Fragment) fragManager.findFragmentById(R.id.fragment_container);

        // Start the transactions
        FragmentTransaction transaction = fragManager.beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);

        // If there is already a fragment then we want it on the backstack
        if (currentFragment != null) {
            transaction.addToBackStack(tag);
        }

        // Show it
        transaction.commitAllowingStateLoss();

        return currentFragment;
    }

    public static Fragment showSetupFragment(Context context, BluetoothHelper bluetoothHelper) {
        FragmentManager fragManager = ((AppCompatActivity) context).getSupportFragmentManager();

        SetupFragment setupFragment = new SetupFragment();
        setupFragment.setBluetoothHelper(bluetoothHelper);

        // Start the transactions
        FragmentTransaction transaction = fragManager.beginTransaction();
        transaction.replace(R.id.fragment_container, setupFragment, SetupFragment.class.getSimpleName());
        transaction.addToBackStack(SetupFragment.class.getSimpleName());

        // Show it
        transaction.commitAllowingStateLoss();

        return setupFragment;
    }
}

package com.beneville.grandfatherclock.fragments.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.helpers.BluetoothHelper;

/**
 * Created by joeja on 1/22/2018.
 */

public class EnablePermissionsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.setup_enable_permissions, container, false);

        view.findViewById(R.id.setup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothHelper.checkPermissions(getActivity());
            }
        });

        return view;
    }
}

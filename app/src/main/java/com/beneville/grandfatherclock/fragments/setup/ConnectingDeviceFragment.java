package com.beneville.grandfatherclock.fragments.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beneville.grandfatherclock.MainActivity;
import com.beneville.grandfatherclock.R;

/**
 * Created by joeja on 1/26/2018.
 */

public class ConnectingDeviceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.setup_connecting_device, container, false);

        view.findViewById(R.id.setup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).getDeviceController().disconnectGatt();
            }
        });

        return view;
    }

}

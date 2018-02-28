package com.beneville.grandfatherclock.fragments.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beneville.grandfatherclock.R;

/**
 * Created by joeja on 1/30/2018.
 */

public class DownloadingDataFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.setup_download_data, container, false);
        return view;
    }

}

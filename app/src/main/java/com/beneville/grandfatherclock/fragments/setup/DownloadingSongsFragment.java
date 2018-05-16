package com.beneville.grandfatherclock.fragments.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beneville.grandfatherclock.MainActivity;
import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.helpers.SyncSongs;

/**
 * Created by joeja on 1/30/2018.
 */

public class DownloadingSongsFragment extends Fragment {

    private TextView infoText;

    SyncSongs.SongDownloadingListener songDownloadingListener = new SyncSongs.SongDownloadingListener() {
        @Override
        public void OnDownloading(int current, int max) {
            if (infoText != null) {
                infoText.setText("Downloading song list...\n\n" + current + " / " + max + "\n\nThis may take a few minutes.");
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.setup_download, container, false);
        infoText = view.findViewById(R.id.downloadText);
        infoText.setText("Checking song list...\n");

        ((MainActivity) getActivity()).getDeviceController().getSongSync().setSongDownloadingListener(songDownloadingListener);

        return view;
    }

}

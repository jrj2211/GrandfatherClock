package com.beneville.grandfatherclock.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.beneville.grandfatherclock.MainActivity;
import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.helpers.DeviceController;
import com.beneville.grandfatherclock.helpers.DeviceController.PlaybackMode;
import com.beneville.grandfatherclock.views.MenuIconButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joeja on 11/7/2017.
 */

public class ControlFragment extends Fragment {

    private DeviceController mController;
    private HashMap<PlaybackMode, MenuIconButton> mModeButtons = new HashMap<>();

    private SeekBar.OnSeekBarChangeListener mVolumeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            mController.setAudioVolume(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private MenuIconButton.StateChangeListener playbackControlListener = new MenuIconButton.StateChangeListener() {
        @Override
        public void OnChange(boolean toggled) {
            mController.setPlayControl(toggled);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control, container, false);

        mController = ((MainActivity) getActivity()).getDeviceController();

        setModeButton(PlaybackMode.SHUFFLE, (MenuIconButton) view.findViewById(R.id.menu_icon_shuffle));
        setModeButton(PlaybackMode.MOVIE, (MenuIconButton) view.findViewById(R.id.menu_icon_movie));
        setModeButton(PlaybackMode.MUSIC, (MenuIconButton) view.findViewById(R.id.menu_icon_music));
        setModeButton(PlaybackMode.BOOK, (MenuIconButton) view.findViewById(R.id.menu_icon_book));
        setModeButton(PlaybackMode.BELL, (MenuIconButton) view.findViewById(R.id.menu_icon_bell));
        setModeButton(PlaybackMode.MUTE, (MenuIconButton) view.findViewById(R.id.menu_icon_mute));

        // Setup Volume Control
        SeekBar volumeSeekBar = (SeekBar) view.findViewById(R.id.volume_seekbar);
        volumeSeekBar.setMax(100);
        volumeSeekBar.setOnSeekBarChangeListener(mVolumeChangeListener);

        // Setup Playback Control
        MenuIconButton playbackControl = (MenuIconButton) view.findViewById(R.id.control_play);
        playbackControl.setOnChangeListener(playbackControlListener);

        // Setup Library Menu Button
        view.findViewById(R.id.library_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseFragment.startFragment(getContext(), new LibraryFragment());
            }
        });

        // Setup Library Menu Button
        view.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LibraryFragment searchFrag =  new LibraryFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("search", true);
                searchFrag.setArguments(bundle);

                BaseFragment.startFragment(getContext(), searchFrag);
            }
        });

        return view;
    }

    private void setModeButton(final PlaybackMode mode, final MenuIconButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Map.Entry<PlaybackMode, MenuIconButton> modeButton : mModeButtons.entrySet()) {
                    modeButton.getValue().setToggled(false);
                }

                mController.setPlaybackMode(mode);
                button.setToggled(true);
            }
        });

        mModeButtons.put(mode, button);
    }

}

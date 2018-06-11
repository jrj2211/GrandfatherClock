package com.beneville.grandfatherclock.fragments;

import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.beneville.grandfatherclock.MainActivity;
import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.database.AppDatabase;
import com.beneville.grandfatherclock.database.Song;
import com.beneville.grandfatherclock.helpers.DeviceController;
import com.beneville.grandfatherclock.helpers.DeviceController.PlaybackMode;
import com.beneville.grandfatherclock.views.MenuIconButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joeja on 11/7/2017.
 */

public class ControlFragment extends Fragment {

    private static String TAG = ControlFragment.class.getSimpleName();

    private DeviceController mController;
    private AppDatabase mDatabase;
    private TextView songTitle;
    private TextView songArtist;
    private MenuIconButton mPlaybackControl;
    private SeekBar mVolumeSeekBar;
    private HashMap<PlaybackMode, MenuIconButton> mModeButtons = new HashMap<>();
    private LoadSongAsyncTask mLoadSongAsyncTask;

    private SeekBar.OnSeekBarChangeListener mVolumeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b) {
                mController.writeAudioVolume(i);
            }
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
            mController.writePlayControl(toggled);
        }
    };

    public void setCurrentSongInfo(final int index) {
        Log.e(TAG, "Loading song info " + index);
        mLoadSongAsyncTask = new LoadSongAsyncTask(mDatabase, index);
        mLoadSongAsyncTask.setListener(new LoadSongAsyncTask.LoadSongAsyncTaskListener() {
            @Override
            public void onTaskFinished(Song song) {
                if (song != null) {
                    Log.e(TAG, "Loaded song info " + song.getTitle());
                    songTitle.setText((song != null && mController.getCurrentMode() != PlaybackMode.BELL && mController.getCurrentMode() != PlaybackMode.MUTE ? song.getTitle() : ""));
                    songArtist.setText((song != null && mController.getCurrentMode() != PlaybackMode.BELL && mController.getCurrentMode() != PlaybackMode.MUTE ? song.getArtist() : ""));
                    songTitle.setVisibility(songTitle.getText().toString().isEmpty() ? View.GONE : View.VISIBLE);
                    songArtist.setVisibility(songArtist.getText().toString().isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    Log.e(TAG, "Song doesn't exist.");
                    songTitle.setVisibility(View.GONE);
                    songArtist.setVisibility(View.GONE);
                }
            }
        });
        mLoadSongAsyncTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        songTitle = view.findViewById(R.id.songTitle);
        songArtist = view.findViewById(R.id.songArtist);

        // Get a reference to the database
        mDatabase = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "database-name").build();

        mController = ((MainActivity) getActivity()).getDeviceController();

        // Add mode buttons
        setModeButton(PlaybackMode.ALL, (MenuIconButton) view.findViewById(R.id.menu_icon_shuffle));
        setModeButton(PlaybackMode.MOVIE, (MenuIconButton) view.findViewById(R.id.menu_icon_movie));
        setModeButton(PlaybackMode.MUSIC, (MenuIconButton) view.findViewById(R.id.menu_icon_music));
        setModeButton(PlaybackMode.BOOK, (MenuIconButton) view.findViewById(R.id.menu_icon_book));
        setModeButton(PlaybackMode.BELL, (MenuIconButton) view.findViewById(R.id.menu_icon_bell));
        setModeButton(PlaybackMode.MUTE, (MenuIconButton) view.findViewById(R.id.menu_icon_mute));

        // Setup Playback Control
        mPlaybackControl = (MenuIconButton) view.findViewById(R.id.control_play);
        mPlaybackControl.setOnChangeListener(playbackControlListener);

        // Setup Volume Seek Bar
        mVolumeSeekBar = (SeekBar) view.findViewById(R.id.volume_seekbar);
        mVolumeSeekBar.setOnSeekBarChangeListener(mVolumeChangeListener);
        mVolumeSeekBar.setMax(100);

        view.findViewById(R.id.control_reverse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.writePrevious(mPlaybackControl.isToggled());
            }
        });

        view.findViewById(R.id.control_forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.writeNext(mPlaybackControl.isToggled());
            }
        });

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

    @Override
    public void onDestroy() {
        mLoadSongAsyncTask.setListener(null); // PREVENT LEAK AFTER ACTIVITY DESTROYED
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Handle song changes
        setCurrentSongInfo(mController.getCurrentSongIndex());
        mController.setSongChangedListener(new DeviceController.SongChangedListener() {
            @Override
            public void onChange(int index) {
                setCurrentSongInfo(index);
            }
        });

        // Handle playback state changes
        mPlaybackControl.setToggled(mController.getPlaybackControl());
        mController.setPlaybackStateChanged(new DeviceController.PlaybackStateListener() {
            @Override
            public void onChange(boolean playing) {
                mPlaybackControl.setToggled(playing);
            }
        });

        // Handle volume changes
        mVolumeSeekBar.setProgress(mController.getCurrentVolume());
        mController.setVolumeChangedListener(new DeviceController.VolumeChangedListener() {
            @Override
            public void onChange(int volume) {
                if (mVolumeSeekBar != null) {
                    mVolumeSeekBar.setProgress(volume);
                }
            }
        });

        // Handle mode changes
        highlightModeButton(mModeButtons.get(mController.getCurrentMode()));
        mController.setModeChangedListener(new DeviceController.ModeChangedListener() {
            @Override
            public void onChange(PlaybackMode mode) {
                highlightModeButton(mModeButtons.get(mode));
            }
        });
    }

    private void setModeButton(final PlaybackMode mode, final MenuIconButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.writePlaybackMode(mode);
                highlightModeButton(button);
            }
        });

        mModeButtons.put(mode, button);
    }

    private void highlightModeButton(MenuIconButton button) {
        for (Map.Entry<PlaybackMode, MenuIconButton> modeButton : mModeButtons.entrySet()) {
            modeButton.getValue().setToggled(false);
        }
        button.setToggled(true);
    }

    static class LoadSongAsyncTask extends AsyncTask<Void, Void, Song> {
        private LoadSongAsyncTaskListener listener;

        private AppDatabase mDatabase;
        private int mIndex;

        public LoadSongAsyncTask(AppDatabase db, int index) {
            mDatabase = db;
            mIndex = index;
        }

        @Override
        protected Song doInBackground(Void... voids) {
            if (mDatabase != null) {
                final Song.Dao songDao = mDatabase.songDao();
                return songDao.getSongByIndex(mIndex);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Song song) {
            super.onPostExecute(song);
            if (listener != null) {
                listener.onTaskFinished(song);
            }
        }

        public void setListener(LoadSongAsyncTaskListener listener) {
            this.listener = listener;
        }

        public interface LoadSongAsyncTaskListener {
            void onTaskFinished(Song song);
        }
    }

}

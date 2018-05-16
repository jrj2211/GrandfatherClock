package com.beneville.grandfatherclock.helpers;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.util.Log;

import com.beneville.grandfatherclock.database.AppDatabase;
import com.beneville.grandfatherclock.database.Song;

/**
 * Created by joeja on 1/30/2018.
 */

public class SyncSongs {

    private static String TAG = SyncSongs.class.getSimpleName();

    private boolean mIsDownloading = false;
    private int mCurIndex = 0;
    private int mNumFiles = 0;
    private Song mSong = new Song();
    private boolean mComplete = false;
    private int numReadsLeft = 0;
    private AppDatabase mDatabase;
    private Context mContext;
    private CompleteListener mCompleteListener;
    private SongDownloadingListener mSongDownloadingListener;

    public SyncSongs(Context context) {
        mContext = context;
        mDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "database-name").build();
    }

    public void setSongDownloadingListener(SongDownloadingListener listener) {
        mSongDownloadingListener = listener;
    }

    public void setCompleteListener(CompleteListener listener) {
        mCompleteListener = listener;
    }

    public boolean isComplete() {
        return mComplete;
    }

    public boolean isDownloading() {
        return mIsDownloading;
    }

    public void setComplete() {
        mComplete = true;
        mIsDownloading = false;
        AppSettings.getInstance(mContext).setSongsDownloaded(true);
        if (mCompleteListener != null) {
            mCompleteListener.OnComplete();
        }
    }

    public void startSync(int files, DeviceController controller) {
        if (mIsDownloading == false) {
            mIsDownloading = true;
            mNumFiles = files;
            mDatabase.deleteAllSongs();
            getSongInfo(controller);
        }
    }

    private void getSongInfo(DeviceController controller) {
        if (mCurIndex < mNumFiles) {
            mSong = new Song();
            Log.e(TAG, "Setting mode to " + ModeIndexes.getModeForIndex(mCurIndex) + " for index " + mCurIndex);
            mSong.setMode(ModeIndexes.getModeForIndex(mCurIndex));
            mSong.setBoardIndex(mCurIndex);

            // Set the song to get
            controller.writeSongIndex(mCurIndex);
            controller.readSongTitleFragmentCount();

            // Notify that a song has started downloading
            if (mSongDownloadingListener != null) {
                mSongDownloadingListener.OnDownloading(mCurIndex + 1, mNumFiles);
            }
        } else {
            setComplete();
        }
    }

    public void getTitle(int numReadCount, DeviceController controller) {
        // Queue up to read each fragment
        numReadsLeft = numReadCount;
        if (numReadCount > 0) {
            for (int x = 0; x < numReadCount; x++) {
                controller.readSongTitle();
            }
        } else {
            titleRead(controller);
        }
    }

    public void setTitle(String title, DeviceController controller) {
        mSong.appendTitle(title);
        numReadsLeft--;

        if (numReadsLeft == 0) {
            titleRead(controller);
        }
    }

    private void titleRead(DeviceController controller) {
        controller.readSongArtistFragmentCount();
    }

    public void getArtist(int numReadCount, DeviceController controller) {
        // Queue up to read each fragment
        numReadsLeft = numReadCount;
        if (numReadCount > 0) {
            for (int x = 0; x < numReadCount; x++) {
                controller.readSongArtist();
            }
        } else {
            artistRead(controller);
        }
    }

    public void setArtist(String artist, DeviceController controller) {
        mSong.appendArtist(artist);
        numReadsLeft--;

        if (numReadsLeft == 0) {
            artistRead(controller);
        }
    }

    private void artistRead(DeviceController controller) {
        nextSong();
        getSongInfo(controller);
    }

    private void nextSong() {
        // All info received for song so add it to the database
        mDatabase.insertSong(mSong);
        mCurIndex++;
    }

    public interface CompleteListener {
        public void OnComplete();
    }

    public interface SongDownloadingListener {
        public void OnDownloading(int current, int max);
    }

}

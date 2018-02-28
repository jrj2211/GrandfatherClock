package com.beneville.grandfatherclock.helpers;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.beneville.grandfatherclock.database.AppDatabase;
import com.beneville.grandfatherclock.database.Song;

/**
 * Created by joeja on 1/30/2018.
 */

public class SyncSongs {

    private int mCurIndex = 0;
    private int mNumFiles = 0;
    private Song mSong = new Song();
    private boolean mComplete = true;
    private AppDatabase mDatabase;

    public SyncSongs(Context context) {
        mDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "database-name").build();
        mDatabase.deleteAllSongs();
    }

    public boolean isComplete() {
        return mComplete;
    }

    public void setNumFiles(int files) {
        mNumFiles = files;
    }

    public void getSongInfo(DeviceController controller) {
        if (mCurIndex < mNumFiles) {
            controller.getSongTitle();
        } else {
            mComplete = true;
        }
    }

    public void setTitle(String title, DeviceController controller) {
        mSong.setTitle(title);
        controller.getSongArtist();
    }

    public void setArtist(String artist, DeviceController controller) {
        mSong.setArtist(artist);
        controller.getSongGenre();
    }

    public void setGenre(String genre, DeviceController controller) {
        mSong.setGenre(genre);
        controller.getSongDisabled();
    }

    public void setDisabled(boolean disabled, DeviceController controller) {
        mSong.setDisabled(disabled);

        // All info received for song so add it to the database
        mDatabase.insertSong(mSong);
        mSong = new Song();

        // Move to the next song
        controller.setNextSongIndex(mCurIndex++);

        // Start getting the next song
        getSongInfo(controller);
    }

}

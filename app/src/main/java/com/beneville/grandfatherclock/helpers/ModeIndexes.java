package com.beneville.grandfatherclock.helpers;

import android.util.SparseArray;

import com.beneville.grandfatherclock.database.Song;

/**
 * Created by joeja on 3/27/2018.
 */

public class ModeIndexes {

    private static SparseArray<DeviceController.PlaybackMode> modeIndexes = new SparseArray();

    public static void setPlaybackStartIndex(DeviceController.PlaybackMode mode, int startIndex) {
        modeIndexes.append(startIndex, mode);
    }

    public static Song.ModeType getModeForIndex(int index) {
        for (int i = modeIndexes.size() - 1; i >= 0; i--) {
            int startIndex = modeIndexes.keyAt(i);
            if (index >= startIndex) {
                return DeviceModeToSongType(modeIndexes.get(startIndex));
            }
        }
        return DeviceModeToSongType(DeviceController.PlaybackMode.MUSIC);
    }

    public static Song.ModeType DeviceModeToSongType(DeviceController.PlaybackMode mode) {
        switch (mode) {
            case BOOK:
                return Song.ModeType.BOOK;
            case MOVIE:
                return Song.ModeType.MOVIE;
            default:
                return Song.ModeType.SONG;
        }
    }

    public class Range {

        private int low;
        private int high;

        public Range(int low, int high) {
            this.low = low;
            this.high = high;
        }

        public boolean contains(int number) {
            return (number >= low && number <= high);
        }
    }
}

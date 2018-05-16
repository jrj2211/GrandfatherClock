package com.beneville.grandfatherclock.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.os.AsyncTask;

/**
 * Created by joeja on 1/30/2018.
 */

@Database(entities = {Song.class}, version = 1, exportSchema = false)
@TypeConverters({AppDatabase.Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract Song.Dao songDao();

    public void deleteAllSongs() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                songDao().deleteAll();
            }
        });
    }

    public void insertSong(final Song song) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                songDao().insert(song);
            }
        });
    }

    public void updateDisabled(final Song song) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                songDao().setDisabled(song.getBoardIndex(), song.isDisabled());
            }
        });
    }

    static public class Converters {
        @TypeConverter
        public Song.ModeType toMode(int mode) {
            if (mode == Song.ModeType.SONG.getMode()) {
                return Song.ModeType.SONG;
            } else if (mode == Song.ModeType.MOVIE.getMode()) {
                return Song.ModeType.MOVIE;
            } else if (mode == Song.ModeType.BOOK.getMode()) {
                return Song.ModeType.BOOK;
            } else {
                throw new IllegalArgumentException("Could not recognize status");
            }
        }

        @TypeConverter
        public int fromMode(Song.ModeType mode) {
            return mode.getMode();
        }
    }

}

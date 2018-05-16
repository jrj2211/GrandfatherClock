package com.beneville.grandfatherclock.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import com.beneville.grandfatherclock.helpers.DeviceController;

import java.util.List;

/**
 * Created by joeja on 1/30/2018.
 */

@Entity
public class Song {

    public final static String TABLE = "song";
    @PrimaryKey(autoGenerate = true)
    private int sid;
    @ColumnInfo(name = "boardIndex")
    private int boardIndex;
    @ColumnInfo(name = "title")
    private String title = "";
    @ColumnInfo(name = "artist")
    private String artist = "";
    @ColumnInfo(name = "genre")
    private String genre = "";
    @ColumnInfo(name = "mode")
    @TypeConverters(AppDatabase.Converters.class)
    private ModeType mode = ModeType.SONG;
    @ColumnInfo(name = "disabled")
    private boolean disabled;

    public Song() {
    }

    @Ignore
    public Song(String title, String artist, String genre, ModeType mode, boolean disabled) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.mode = mode;
        this.disabled = disabled;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getBoardIndex() {
        return this.boardIndex;
    }

    public void setBoardIndex(int boardIndex) {
        this.boardIndex = boardIndex;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public ModeType getMode() {
        return mode;
    }

    public void setMode(ModeType mode) {
        this.mode = mode;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void appendArtist(String artist) {
        this.artist += artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void appendTitle(String title) {
        this.title += title;
    }

    public enum ModeType {
        ALL(0),
        SONG(2),
        MOVIE(3),
        BOOK(4);

        private int mode;

        ModeType(int i) {
            mode = i;
        }

        public static ModeType GetFromPlaybackMode(DeviceController.PlaybackMode playbackMode) {
            switch (playbackMode) {
                case BOOK:
                    return Song.ModeType.BOOK;
                case MOVIE:
                    return Song.ModeType.MOVIE;
                default:
                    return Song.ModeType.SONG;
            }
        }

        public int getMode() {
            return mode;
        }
    }

    @android.arch.persistence.room.Dao
    public interface Dao {

        @Query("SELECT * FROM " + Song.TABLE + " ORDER BY artist, title ASC")
        List<Song> getAll();

        @Query("SELECT * FROM " + Song.TABLE + " WHERE mode=:modeType ORDER BY artist, title ASC")
        List<Song> getAllByType(ModeType modeType);

        @Query("SELECT * FROM " + Song.TABLE + " WHERE title LIKE :text OR artist LIKE :text ORDER BY artist, title ASC")
        List<Song> search(String text);

        @Query("SELECT * FROM " + Song.TABLE + " WHERE boardIndex = :boardIndex LIMIT 1")
        Song getSongByIndex(int boardIndex);

        @Query("UPDATE " + Song.TABLE + " SET disabled=:disabled WHERE boardIndex=:index")
        void setDisabled(int index, boolean disabled);

        @Insert
        void insertAll(Song... songs);

        @Insert
        void insert(Song song);

        @Delete
        void delete(Song song);

        @Query("DELETE FROM " + Song.TABLE)
        void deleteAll();
    }

}

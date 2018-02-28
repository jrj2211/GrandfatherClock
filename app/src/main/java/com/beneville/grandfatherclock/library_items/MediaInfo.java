package com.beneville.grandfatherclock.library_items;

import com.beneville.grandfatherclock.database.Song;

/**
 * Created by joeja on 11/8/2017.
 */

public class MediaInfo extends ListItem {
    private Song song;

    public MediaInfo(Song song) {
        super(null, song.getTitle());
        this.song = song;
    }

    @Override
    public int getType() {
        return ListItem.VIEW_TYPE_MEDIA;
    }

    public Song getSong() {
        return song;
    }
}

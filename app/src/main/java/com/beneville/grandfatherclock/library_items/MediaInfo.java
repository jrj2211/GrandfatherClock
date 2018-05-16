package com.beneville.grandfatherclock.library_items;

import android.view.View;

import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.database.AppDatabase;
import com.beneville.grandfatherclock.database.Song;
import com.beneville.grandfatherclock.helpers.DeviceController;

/**
 * Created by joeja on 11/8/2017.
 */

public class MediaInfo extends ListItem {
    private Song song;

    public MediaInfo(final Song song, final DeviceController controller, final AppDatabase appDatabase) {
        super(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.writePlaybackMode(DeviceController.PlaybackMode.ALL);
                controller.playSong(song.getBoardIndex());
            }
        }, song.getTitle());

        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                boolean newDisabledState = !song.isDisabled();
                song.setDisabled(newDisabledState);
                controller.disableSong(song.getBoardIndex(), newDisabledState);
                view.findViewById(R.id.library_item_disabled).setVisibility(newDisabledState ? View.VISIBLE : View.GONE);
                appDatabase.updateDisabled(song);
                return true;
            }
        });

        this.song = song;
    }

    @Override
    public String getName() {
        return song.getArtist();
    }

    @Override
    public int getType() {
        return ListItem.VIEW_TYPE_MEDIA;
    }

    public Song getSong() {
        return song;
    }
}

package com.beneville.grandfatherclock.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.database.Song;
import com.beneville.grandfatherclock.library_items.ListItem;
import com.beneville.grandfatherclock.library_items.MediaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joeja on 11/7/2017.
 */

public class LibraryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SectionIndexer {

    public static String[] ALPHABET = new String[]{"#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private List<ListItem> mDataArray = new ArrayList<ListItem>();

    public void setListItems(List<ListItem> items) {
        mDataArray = items;
        notifyDataSetChanged();
    }

    public void addListItem(ListItem item) {
        mDataArray.add(item);
    }

    public void clearList() {
        mDataArray.clear();
        notifyDataSetChanged();
    }

    public void notifyChange() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataArray.size() > position && position >= 0) {
            return mDataArray.get(position).getType();
        }

        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        int section = 0;
        if (mDataArray.size() > position && position >= 0) {
            for (String letter : ALPHABET) {
                if (mDataArray.get(position).getName().startsWith(letter)) {
                    break;
                }
                section++;
            }
        }

        return section;
    }

    @Override
    public Object[] getSections() {
        return ALPHABET;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex < 0) {
            sectionIndex = 0;
        }
        if (sectionIndex >= ALPHABET.length) {
            sectionIndex = ALPHABET.length - 1;
        }

        int position = 0;
        for (ListItem info : mDataArray) {
            if (info instanceof MediaInfo) {
                if (((MediaInfo) info).getName().charAt(0) >= ALPHABET[sectionIndex].charAt(0)) {
                    break;
                }
            }
            position++;
        }

        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item;

        switch (viewType) {
            case ListItem.VIEW_TYPE_MEDIA:
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_media_item, parent, false);
                return new MediaItemViewHolder(item);
            case ListItem.VIEW_TYPE_MENU_ITEM:
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_menu_item, parent, false);
                return new MenuItemViewHolder(item);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(mDataArray.get(position).getOnClickListener());

        switch (holder.getItemViewType()) {
            case ListItem.VIEW_TYPE_MEDIA:
                MediaItemViewHolder holder_media = (MediaItemViewHolder) holder;
                holder_media.setTitle(mDataArray.get(position).getName());
                holder_media.setArtist(((MediaInfo) mDataArray.get(position)).getSong().getArtist());
                holder_media.setType(((MediaInfo) mDataArray.get(position)).getSong().getMode());
                break;
            case ListItem.VIEW_TYPE_MENU_ITEM:
                MenuItemViewHolder holder_menu = (MenuItemViewHolder) holder;
                holder_menu.setName(mDataArray.get(position).getName());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataArray.size();
    }

    public static class MediaItemViewHolder extends RecyclerView.ViewHolder {

        public TextView mTitleView;
        public TextView mArtistView;
        public ImageView mImageView;

        public MediaItemViewHolder(View v) {
            super(v); // done this way instead of view tagging
            mTitleView = v.findViewById(R.id.library_item_title);
            mArtistView = v.findViewById(R.id.library_item_artist);
            mImageView = v.findViewById(R.id.library_item_icon);
        }

        public void setTitle(String title) {
            mTitleView.setText(title);
        }

        public void setArtist(String artist) {
            mArtistView.setText(artist);
        }

        public void setType(Song.ModeType type) {
            switch (type) {
                case BOOK:
                    mImageView.setImageResource(R.drawable.ic_book);
                    break;
                case MOVIE:
                    mImageView.setImageResource(R.drawable.ic_movie);
                    break;
                case SONG:
                    mImageView.setImageResource(R.drawable.ic_music);
                    break;
            }
        }
    }

    public static class MenuItemViewHolder extends RecyclerView.ViewHolder {

        public TextView mNameView;

        public MenuItemViewHolder(View v) {
            super(v); // done this way instead of view tagging
            mNameView = v.findViewById(R.id.name);
        }

        public void setName(String name) {
            mNameView.setText(name);
        }
    }
}

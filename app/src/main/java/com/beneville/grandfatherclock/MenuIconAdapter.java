package com.beneville.grandfatherclock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

/**
 * Created by joeja on 11/1/2017.
 */

class MenuIconAdapter extends BaseAdapter {
    private Context mContext;

    public MenuIconAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View icon;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            icon = inflater.inflate(R.layout.menu_icon, parent, false);
        } else {
            icon = convertView;
        }

        ((ImageView) icon.findViewById(R.id.menu_icon_image)).setImageResource(mThumbIds[position]);

        return icon;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.ic_menu_repeat,
            R.drawable.ic_menu_movie,
            R.drawable.ic_menu_music,
            R.drawable.ic_menu_book,
            R.drawable.ic_menu_bell,
            R.drawable.ic_menu_sound
    };
}

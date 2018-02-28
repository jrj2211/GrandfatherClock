package com.beneville.grandfatherclock.library_items;

import android.view.View;

/**
 * Created by joeja on 11/11/2017.
 */

public abstract class ListItem {
    public static final int VIEW_TYPE_MEDIA = 0;
    public static final int VIEW_TYPE_SECTION_HEADER = 1;
    public static final int VIEW_TYPE_MENU_ITEM = 2;

    protected String name;
    protected View.OnClickListener onClickListener;

    public ListItem(View.OnClickListener onClickListener, String name) {
        setName(name);
        setOnClickListener(onClickListener);
    }

    public abstract int getType();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public View.OnClickListener getOnClickListener() {
        return this.onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}

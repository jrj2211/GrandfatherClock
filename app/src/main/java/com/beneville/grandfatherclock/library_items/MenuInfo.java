package com.beneville.grandfatherclock.library_items;

import android.view.View;

/**
 * Created by joeja on 11/12/2017.
 */

public class MenuInfo extends ListItem {

    public MenuInfo(View.OnClickListener onClickListener, String name) {
        super(onClickListener, name);
    }

    @Override
    public int getType() {
        return ListItem.VIEW_TYPE_MENU_ITEM;
    }

}
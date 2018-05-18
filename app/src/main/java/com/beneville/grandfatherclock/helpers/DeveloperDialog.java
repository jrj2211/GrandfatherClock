package com.beneville.grandfatherclock.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.database.AppDatabase;

public class DeveloperDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity activity;

    public DeveloperDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_developer_options);
        findViewById(R.id.delete_data).setOnClickListener(this);
        findViewById(R.id.disable_kiosk).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_data:
                // Get a reference to the database
                AppDatabase appDatabase = Room.databaseBuilder(getContext(),
                        AppDatabase.class, "database-name").build();
                appDatabase.deleteAllSongs();

                AppSettings settings = AppSettings.getInstance(getContext());
                settings.reset();
                dismiss();
                break;
            case R.id.disable_kiosk:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.stopLockTask();
                }
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}

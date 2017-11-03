package com.beneville.grandfatherclock.Views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.beneville.grandfatherclock.R;

/**
 * Created by joeja on 11/3/2017.
 */

public class MenuIconButton extends RelativeLayout implements RelativeLayout.OnClickListener {

    protected Drawable menuIcon;
    protected ImageView menuIconView;

    private boolean toggled = false;

    public MenuIconButton(Context context) {
        super(context);
    }

    public MenuIconButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MenuIconButton);

        menuIcon = a.getDrawable(R.styleable.MenuIconButton_menu_icon);

        a.recycle();

        init();
    }

    public MenuIconButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MenuIconButton);

        menuIcon = a.getDrawable(R.styleable.MenuIconButton_menu_icon);

        a.recycle();

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.menu_icon, this);

        menuIconView = ((ImageView) findViewById(R.id.menu_icon_image));
        menuIconView.setImageDrawable(menuIcon);

        this.setOnClickListener(this);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        android.view.ViewGroup.LayoutParams layoutParams = menuIconView.getLayoutParams();
        layoutParams.width = (int) (w*.5f);
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        menuIconView.setLayoutParams(layoutParams);

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    @Override
    public void onClick(View v) {
        toggled = !toggled;

        if(toggled) {
            findViewById(R.id.button_container).setBackgroundResource(R.drawable.circle_border_on);
        } else {
            findViewById(R.id.button_container).setBackgroundResource(R.drawable.circle_border_off);
        }
    }
}

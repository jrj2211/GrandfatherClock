package com.beneville.grandfatherclock.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.beneville.grandfatherclock.R;

/**
 * Created by joeja on 11/3/2017.
 */

public class MenuIconButton extends RelativeLayout implements RelativeLayout.OnClickListener {

    protected Drawable menuIcon;
    protected Drawable menuIconOn;
    protected ImageView menuIconView;
    protected StateChangeListener mListener;

    private boolean toggled = false;

    public MenuIconButton(Context context) {
        super(context);
    }

    public MenuIconButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MenuIconButton);

        menuIcon = a.getDrawable(R.styleable.MenuIconButton_menu_icon);
        menuIconOn = a.getDrawable(R.styleable.MenuIconButton_menu_icon_on);
        a.recycle();

        init();
    }

    public MenuIconButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MenuIconButton);

        menuIcon = a.getDrawable(R.styleable.MenuIconButton_menu_icon);
        menuIconOn = a.getDrawable(R.styleable.MenuIconButton_menu_icon_on);

        a.recycle();

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.menu_icon, this);

        menuIconView = ((ImageView) findViewById(R.id.menu_icon_image));
        menuIconView.setImageDrawable(menuIcon);

        this.setOnClickListener(this);
    }

    public void setToggled(boolean t) {
        toggled = t;
        updateButtonView();
    }

    public void setOnChangeListener(StateChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        android.view.ViewGroup.LayoutParams layoutParams = menuIconView.getLayoutParams();
        layoutParams.width = (int) (w * .5f);
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

    private void updateButtonView() {
        if (menuIconOn != null) {
            if (toggled) {
                menuIconView.setImageDrawable(menuIconOn);
            } else {
                menuIconView.setImageDrawable(menuIcon);
            }
        } else {
            if (toggled) {
                findViewById(R.id.button_container).setBackgroundResource(R.drawable.circle_border_on);
            } else {
                findViewById(R.id.button_container).setBackgroundResource(R.drawable.circle_border_off);
            }
        }

    }

    @Override
    public void onClick(View v) {
        toggled = !toggled;

        updateButtonView();

        if (mListener != null) {
            mListener.OnChange(toggled);
        }
    }

    public interface StateChangeListener {
        public void OnChange(boolean toggled);
    }
}

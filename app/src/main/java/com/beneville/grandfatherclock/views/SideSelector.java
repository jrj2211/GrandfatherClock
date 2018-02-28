package com.beneville.grandfatherclock.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SectionIndexer;

import com.beneville.grandfatherclock.R;

/**
 * Created by joeja on 11/7/2017.
 */

public class SideSelector extends View {
    public static final int BOTTOM_PADDING = 10;
    private static String TAG = SideSelector.class.getCanonicalName();
    private SectionIndexer selectionIndexer = null;
    private RecyclerView list;
    private Paint offTextPaint;
    private Paint onTextPaint;
    private String[] sections = new String[0];
    private int curIndex = 0;

    public SideSelector(Context context) {
        super(context);
        init(context);
    }

    public SideSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SideSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    private void init(Context context) {
        setBackgroundColor(Color.TRANSPARENT);
        offTextPaint = new Paint();
        offTextPaint.setColor(ContextCompat.getColor(context, R.color.grayText));
        offTextPaint.setTextSize(convertDpToPixel(12, getContext()));
        offTextPaint.setTextAlign(Paint.Align.CENTER);

        onTextPaint = new Paint();
        onTextPaint.setColor(ContextCompat.getColor(context, R.color.selectorHighlight));
        onTextPaint.setTextSize(convertDpToPixel(12, getContext()));
        onTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setView(RecyclerView _list) {
        list = _list;
        selectionIndexer = (SectionIndexer) _list.getAdapter();

        Object[] sectionsArr = selectionIndexer.getSections();
        sections = new String[sectionsArr.length];
        for (int i = 0; i < sectionsArr.length; i++) {
            sections[i] = sectionsArr[i].toString();
        }

    }

    public void setCurrentSection(int section) {
        if (section >= 0 || sections.length < section) {
            curIndex = section;
            invalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int y = (int) event.getY();
        float selectedIndex = ((float) y / (float) getPaddedHeight()) * sections.length;

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (selectionIndexer == null) {
                selectionIndexer = (SectionIndexer) list.getAdapter();
            }
            int pos = selectionIndexer.getPositionForSection((int) selectedIndex);
            ((LinearLayoutManager) list.getLayoutManager()).scrollToPositionWithOffset(pos, 0);
            curIndex = selectionIndexer.getSectionForPosition(pos);
            invalidate();
        }
        return true;
    }

    protected void onDraw(Canvas canvas) {

        int viewHeight = getPaddedHeight();
        float charHeight = ((float) viewHeight) / (float) sections.length;
        float widthCenter = getMeasuredWidth() / 2;
        for (int i = 0; i < sections.length; i++) {
            canvas.drawText(String.valueOf(sections[i]), widthCenter, charHeight + (i * charHeight), (curIndex == i ? onTextPaint : offTextPaint));
        }
        super.onDraw(canvas);
    }

    private int getPaddedHeight() {
        return getHeight() - BOTTOM_PADDING;
    }
}
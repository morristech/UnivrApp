package com.cellasoft.univrapp.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.utils.Lists;
import com.cellasoft.univrapp.utils.UIUtils;

import java.util.ArrayList;

public class ChannelView extends LinearLayout {

    private static final int COLOR_SELECT_AREA = Color.TRANSPARENT;
    private static final int COLOR_STAR_AREA = COLOR_SELECT_AREA;
    private static int TOUCH_ADDITION = 90;
    private final ArrayList<TouchDelegateRecord> mTouchDelegateRecords = Lists
            .newArrayList();
    private final Paint mPaint = new Paint();
    private ImageButton mSelectButton;
    private ImageButton mStarButton;
    private TextView mTextView;
    private TouchDelegateGroup mTouchDelegateGroup;
    private OnChannelViewListener channelListener;
    private int mTouchAddition;
    private boolean mIsStarred;
    private boolean mIsSelected;
    private int mPreviousWidth = -1;
    private int mPreviousHeight = -1;
    public ChannelView(Context context) {
        super(context);
        init(context);
    }

    public ChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        setOrientation(LinearLayout.HORIZONTAL);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        mTouchDelegateGroup = new TouchDelegateGroup(this);
        mPaint.setStyle(Style.FILL);

        // Determine screen size
        switch (UIUtils.getScreenSize(context)) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                TOUCH_ADDITION = 90;
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                TOUCH_ADDITION = 85;
                break;
            default:
                TOUCH_ADDITION = 90;
        }

        final float density = context.getResources().getDisplayMetrics().density;
        mTouchAddition = (int) (density * TOUCH_ADDITION + 0.5f);

        LayoutInflater.from(context).inflate(R.layout.channel, this);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mSelectButton = (ImageButton) findViewById(R.id.channel_chek);
        mSelectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setItemViewSelected(!mIsSelected);
                if (channelListener != null) {
                    channelListener.onSelected(ChannelView.this, mIsSelected);
                }
            }
        });

        mStarButton = (ImageButton) findViewById(R.id.channel_star);
        mStarButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setItemViewStarred(!mIsStarred);
                if (channelListener != null) {
                    channelListener.onStarred(ChannelView.this, mIsStarred);
                }
            }
        });

        mTextView = (TextView) findViewById(R.id.channel_updated);
        // description = (TextView) findViewById(R.id.channel_description);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        final int width = r - l;
        final int height = b - t;

		/*
         * We can't use onSizeChanged here as this is called before the layout
		 * of child View is actually done ... Because we need the size of some
		 * child children we need to check for View size change manually
		 */
        if (width != mPreviousWidth || height != mPreviousHeight) {

            mPreviousWidth = width;
            mPreviousHeight = height;

            mTouchDelegateGroup.clearTouchDelegates();

            // @formatter:off
            addTouchDelegate(new Rect(0, 0, mSelectButton.getWidth()
                    + mTouchAddition, height), COLOR_SELECT_AREA, mSelectButton);

            addTouchDelegate(new Rect(width - mStarButton.getWidth()
                            - mTouchAddition, 0, width, height), COLOR_STAR_AREA,
                    mStarButton);
            // @formatter:on

            setTouchDelegate(mTouchDelegateGroup);
        }
    }

    private void addTouchDelegate(Rect rect, int color, View delegateView) {
        mTouchDelegateGroup.addTouchDelegate(new TouchDelegate(rect,
                delegateView));
        mTouchDelegateRecords.add(new TouchDelegateRecord(rect, color));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        for (TouchDelegateRecord record : mTouchDelegateRecords) {
            mPaint.setColor(record.color);
            canvas.drawRect(record.rect, mPaint);
        }
        super.dispatchDraw(canvas);
    }

    /**
     * Register a listener to be notified of changes on this item view.
     *
     * @param listener The listener to set
     */
    public void setChannelListener(OnChannelViewListener listener) {
        channelListener = listener;
    }

    /**
     * Returns the underlying {@link TextView}.
     */
    public TextView getTitleView() {
        return mTextView;
    }

    /**
     * Select/unselect the view.
     *
     * @param selected The new selection state
     */
    public void setItemViewSelected(boolean selected) {
        if (mIsSelected != selected) {
            mIsSelected = selected;
            mSelectButton
                    .setImageResource(mIsSelected ? R.drawable.btn_check_on_normal
                            : R.drawable.btn_check_off_normal);
            this.setBackgroundColor(mIsSelected ? Color.parseColor("#33B5E5") : Color.WHITE);
            mTextView.setTextColor(mIsSelected ? Color.WHITE : Color.parseColor("#7b7b7b"));
        }
    }

    /**
     * Star/unstar the view.
     *
     * @param starred The new starred state
     */
    public void setItemViewStarred(boolean starred) {
        if (mIsStarred != starred) {
            mIsStarred = starred;
            mStarButton
                    .setImageResource(mIsStarred ? R.drawable.btn_star_on_normal
                            : R.drawable.btn_star_off_normal);
        }
    }

    private static class TouchDelegateRecord {
        public Rect rect;
        public int color;

        public TouchDelegateRecord(Rect _rect, int _color) {
            rect = _rect;
            color = _color;
        }
    }
}

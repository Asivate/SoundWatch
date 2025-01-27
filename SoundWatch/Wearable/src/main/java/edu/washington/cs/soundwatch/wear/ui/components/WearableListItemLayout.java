package edu.washington.cs.soundwatch.wear.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.wear.widget.WearableRecyclerView;

import edu.washington.cs.soundwatch.wear.R;

public class WearableListItemLayout extends LinearLayout {
    private float mUnselectedAlpha;
    private float mSelectedAlpha;
    private TextView mName;

    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mUnselectedAlpha = 0.4f;
        mSelectedAlpha = 1.0f;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mName = findViewById(R.id.name);
    }

    public void onCenterPosition() {
        setAlpha(mSelectedAlpha);
    }

    public void onNonCenterPosition() {
        setAlpha(mUnselectedAlpha);
    }
}

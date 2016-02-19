package com.fenchtose.contactsdemo.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class RobotThinTextView extends TextView {
    public RobotThinTextView(Context context) {
        super(context);
        setTypeface(Fonts.getTypeface(Fonts.FONT_ROBOTO_THIN));
    }

    public RobotThinTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Fonts.getTypeface(Fonts.FONT_ROBOTO_THIN));
    }

    public RobotThinTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(Fonts.getTypeface(Fonts.FONT_ROBOTO_THIN));
    }
}

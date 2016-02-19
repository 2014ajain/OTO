package com.fenchtose.contactsdemo.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class RobotLightTextView extends TextView {
    public RobotLightTextView(Context context) {
        super(context);
        setTypeface(Fonts.getTypeface(Fonts.FONT_ROBOTO_LIGHT));
    }

    public RobotLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Fonts.getTypeface(Fonts.FONT_ROBOTO_LIGHT));
    }

    public RobotLightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(Fonts.getTypeface(Fonts.FONT_ROBOTO_LIGHT));
    }
}

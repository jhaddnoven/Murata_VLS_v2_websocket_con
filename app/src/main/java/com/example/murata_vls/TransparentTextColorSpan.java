package com.example.murata_vls;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;

public class TransparentTextColorSpan extends ForegroundColorSpan {
    public TransparentTextColorSpan() {
        super(Color.TRANSPARENT);
    }

    public TransparentTextColorSpan(int color) {
        super(color);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setAlpha(128); // Set the alpha value here (0 to 255)
    }
}

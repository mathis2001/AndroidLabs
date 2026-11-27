package org.firecracker.insurelab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    static final int INK = 0xFF172A3A;
    static final int MUTED = 0xFF657483;
    static final int CORAL = 0xFFEF674F;
    static final int PALE = 0xFFF5F7FA;
    static final int LINE = 0xFFE4EAF0;

    static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static LinearLayout vertical(Context context) {
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    static LinearLayout horizontal(Context context) {
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.HORIZONTAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    static TextView text(Context context, String value, int sp, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    static GradientDrawable shape(Context context, int fill, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(context, radius));
        return drawable;
    }

    static GradientDrawable outline(Context context, int fill, int stroke, int radius) {
        GradientDrawable drawable = shape(context, fill, radius);
        drawable.setStroke(dp(context, 1), stroke);
        return drawable;
    }

    static TextView button(Context context, String value, int fill, int color) {
        TextView view = text(context, value, 15, color, true);
        view.setGravity(Gravity.CENTER);
        view.setMinHeight(dp(context, 52));
        view.setPadding(dp(context, 18), dp(context, 12), dp(context, 18), dp(context, 12));
        view.setBackground(shape(context, fill, 16));
        return view;
    }

    static void gap(Context context, LinearLayout parent, int height) {
        parent.addView(new View(context), new LinearLayout.LayoutParams(1, dp(context, height)));
    }

    static TextView chip(Context context, String value, int fill, int color) {
        TextView view = text(context, value, 11, color, true);
        view.setPadding(dp(context, 11), dp(context, 6), dp(context, 11), dp(context, 6));
        view.setBackground(shape(context, fill, 40));
        return view;
    }

    static LinearLayout card(Context context) {
        LinearLayout view = vertical(context);
        view.setPadding(dp(context, 20), dp(context, 20), dp(context, 20), dp(context, 20));
        view.setBackground(outline(context, Color.WHITE, LINE, 20));
        return view;
    }

    private Ui() { }
}

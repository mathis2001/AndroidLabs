package org.hidenseek.lab;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

final class Ui {
    static final int INK = Color.rgb(28, 33, 56);
    static final int MUTED = Color.rgb(121, 127, 149);
    static final int PURPLE = Color.rgb(102, 89, 213);
    static final int BACKGROUND = Color.rgb(247, 248, 252);
    static final int LINE = Color.rgb(233, 235, 244);

    private Ui() { }

    static int dp(Context context, float value) {
        return (int) (context.getResources().getDisplayMetrics().density * value + 0.5f);
    }

    static GradientDrawable shape(Context context, int color, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radius));
        return drawable;
    }

    static GradientDrawable outlined(Context context, int color, int stroke, float radius) {
        GradientDrawable drawable = shape(context, color, radius);
        drawable.setStroke(dp(context, 1), stroke);
        return drawable;
    }

    static TextView text(Context context, String value, int size, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    static LinearLayout vertical(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    static LinearLayout horizontal(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    static LinearLayout screen(Activity activity) {
        LinearLayout root = vertical(activity);
        root.setBackgroundColor(BACKGROUND);
        activity.setContentView(root);
        return root;
    }

    static ScrollView scroll(Context context, LinearLayout parent) {
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        return scroll;
    }

    static void gap(Context context, LinearLayout parent, int height) {
        View spacer = new View(context);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, dp(context, height)));
    }

    static TextView pill(Context context, String label, int background, int foreground) {
        TextView view = text(context, label, 12, foreground, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(context, 12), dp(context, 6), dp(context, 12), dp(context, 6));
        view.setBackground(shape(context, background, 20));
        return view;
    }

    static TextView button(Context context, String label) {
        TextView view = text(context, label, 15, Color.WHITE, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(context, 16), dp(context, 15), dp(context, 16), dp(context, 15));
        view.setBackground(shape(context, PURPLE, 16));
        return view;
    }

    static TextView avatar(Context context, String initials, int color, int size) {
        TextView view = text(context, initials, size > 48 ? 19 : 15, Color.WHITE, true);
        view.setGravity(Gravity.CENTER);
        view.setBackground(shape(context, color, size / 2f));
        view.setLayoutParams(new LinearLayout.LayoutParams(dp(context, size), dp(context, size)));
        return view;
    }

    static void footer(Context context, LinearLayout root, boolean chats, Runnable openChats, Runnable openSettings) {
        LinearLayout nav = horizontal(context);
        nav.setBackgroundColor(Color.WHITE);
        nav.setPadding(dp(context, 24), dp(context, 9), dp(context, 24), dp(context, 10));
        root.addView(nav, new LinearLayout.LayoutParams(-1, dp(context, 66)));
        TextView chat = text(context, "◉  Chats", 14, chats ? PURPLE : MUTED, chats);
        chat.setGravity(Gravity.CENTER);
        nav.addView(chat, new LinearLayout.LayoutParams(0, -1, 1));
        chat.setOnClickListener(v -> openChats.run());
        TextView settings = text(context, "⚙  Settings", 14, chats ? MUTED : PURPLE, !chats);
        settings.setGravity(Gravity.CENTER);
        nav.addView(settings, new LinearLayout.LayoutParams(0, -1, 1));
        settings.setOnClickListener(v -> openSettings.run());
    }
}

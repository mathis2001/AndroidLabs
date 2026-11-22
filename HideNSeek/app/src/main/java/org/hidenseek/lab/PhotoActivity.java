package org.hidenseek.lab;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class PhotoActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        int slot = getIntent().getIntExtra("slot", -1);
        DemoData.Media media = DemoData.media(slot);
        if (media == null) { finish(); return; }

        LinearLayout root = Ui.vertical(this);
        root.setBackgroundColor(Color.rgb(20, 22, 34));
        setContentView(root);

        LinearLayout bar = Ui.horizontal(this);
        bar.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 20), Ui.dp(this, 14));
        root.addView(bar);
        TextView close = Ui.text(this, "‹", 36, Color.WHITE, false);
        close.setGravity(Gravity.CENTER);
        bar.addView(close, new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 46)));
        close.setOnClickListener(v -> finish());
        TextView title = Ui.text(this, media.title, 18, Color.WHITE, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -2, 1);
        titleParams.leftMargin = Ui.dp(this, 10);
        bar.addView(title, titleParams);

        FrameLayout stage = new FrameLayout(this);
        root.addView(stage, new LinearLayout.LayoutParams(-1, 0, 1));
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Bitmap bitmap = MediaPreview.load(this, slot, true);
        if (bitmap != null) image.setImageBitmap(bitmap);
        stage.addView(image, new FrameLayout.LayoutParams(-1, -1));
        stage.setOnClickListener(v -> finish());

        if (media.identity) {
            TextView note = Ui.text(this, "Private document · preview only", 13, Color.rgb(194, 197, 211), false);
            note.setGravity(Gravity.CENTER);
            note.setPadding(0, 0, 0, Ui.dp(this, 24));
            root.addView(note);
        }
    }
}

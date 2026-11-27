package org.firecracker.insurelab;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class PortalActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = Ui.vertical(this);
        root.setBackgroundColor(Ui.PALE);
        setContentView(root);
        ScrollView scroll = new ScrollView(this);
        root.addView(scroll);
        LinearLayout content = Ui.vertical(this);
        content.setPadding(Ui.dp(this, 24), Ui.dp(this, 25), Ui.dp(this, 24), Ui.dp(this, 30));
        scroll.addView(content);
        TextView back = Ui.text(this, "‹  Back", 16, Ui.CORAL, true);
        content.addView(back);
        back.setOnClickListener(v -> finish());
        Ui.gap(this, content, 37);
        TextView mark = Ui.text(this, "✓", 32, Color.WHITE, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(Ui.shape(this, Ui.CORAL, 17));
        content.addView(mark, new LinearLayout.LayoutParams(Ui.dp(this, 58), Ui.dp(this, 58)));
        Ui.gap(this, content, 21);
        content.addView(Ui.text(this, "Your quote is ready to start", 28, Ui.INK, true));
        Ui.gap(this, content, 10);
        content.addView(Ui.text(this, "We have carried your mileage estimate into the next step. An adviser can confirm your details and final premium.",
            15, Ui.MUTED, false));
        Ui.gap(this, content, 25);
        LinearLayout summary = Ui.card(this);
        content.addView(summary, new LinearLayout.LayoutParams(-1, -2));
        summary.addView(Ui.text(this, "QUOTE SUMMARY", 11, Ui.MUTED, true));
        Ui.gap(this, summary, 20);
        summary.addView(Ui.text(this, "2021 Toyota Corolla Hybrid", 18, Ui.INK, true));
        Ui.gap(this, summary, 9);
        int miles = getIntent().getIntExtra("mileage", 6000);
        summary.addView(Ui.text(this, miles + " expected miles per year", 14, Ui.MUTED, false));
        Ui.gap(this, content, 24);
        TextView request = Ui.button(this, "Request a call back", Ui.INK, Color.WHITE);
        content.addView(request, new LinearLayout.LayoutParams(-1, -2));
        request.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("Request received")
            .setMessage("This is a fictional insurance demo. No details were sent.")
            .setPositiveButton("Done", null).show());
    }
}

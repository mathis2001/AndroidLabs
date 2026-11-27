package org.firecracker.insurelab;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public final class QuoteActivity extends Activity {
    private static final String DEMO_PORTAL = "https://quote.f1recr4ck3r.example/payd";
    private LinearLayout content;
    private TextView estimate, mileage;
    private RemoteConfig config;
    private int annualMiles = 6000;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Uri link = getIntent().getData();
        String next = link == null ? null : link.getQueryParameter("next");
        if (next != null) {
            if (!PolicyStore.signedIn(this)) {
                Intent login = new Intent(this, LoginActivity.class);
                login.putExtra("pending_offer_link", link.toString());
                startActivity(login);
                finish();
                return;
            }
            Uri target = Uri.parse(next);
            if (!"f1recr4ck3r-internal".equals(target.getScheme()) ||
                !"policy".equals(target.getHost()) ||
                !"/apply-vip".equals(target.getPath())) {
                OfferResultUi.show(this, "Offer unavailable", "This destination is unavailable.", false);
                return;
            }
            startActivity(new Intent(this, VipOfferActivity.class).setData(target));
            finish();
            return;
        }
        config = RemoteConfig.defaults();
        LinearLayout root = Ui.vertical(this);
        root.setBackgroundColor(Ui.PALE);
        setContentView(root);
        ScrollView scroll = new ScrollView(this);
        root.addView(scroll);
        content = Ui.vertical(this);
        content.setPadding(Ui.dp(this, 24), Ui.dp(this, 24), Ui.dp(this, 24), Ui.dp(this, 30));
        scroll.addView(content);
        render();
        RemoteConfig.fetch(this, (result, live) -> { config = result; render(); });
    }

    private void render() {
        content.removeAllViews();
        TextView back = Ui.text(this, "‹  Back", 16, Ui.CORAL, true);
        content.addView(back);
        back.setOnClickListener(v -> finish());
        Ui.gap(this, content, 30);
        content.addView(Ui.chip(this, "PAY AS YOU DRIVE", 0xFFE7F1F1, Ui.INK));
        Ui.gap(this, content, 19);
        content.addView(Ui.text(this, config.card.longTitle, 28, Ui.INK, true));
        Ui.gap(this, content, 12);
        content.addView(Ui.text(this, config.card.longDescription, 15, Ui.MUTED, false));
        Ui.gap(this, content, 25);

        LinearLayout form = Ui.card(this);
        content.addView(form, new LinearLayout.LayoutParams(-1, -2));
        form.addView(Ui.text(this, "Your indicative price", 18, Ui.INK, true));
        Ui.gap(this, form, 7);
        form.addView(Ui.text(this, "For your 2021 Toyota Corolla Hybrid", 13, Ui.MUTED, false));
        Ui.gap(this, form, 23);
        form.addView(Ui.text(this, "EXPECTED ANNUAL MILEAGE", 11, Ui.MUTED, true));
        Ui.gap(this, form, 9);
        mileage = Ui.text(this, annualMiles + " miles", 23, Ui.INK, true);
        form.addView(mileage);
        SeekBar slider = new SeekBar(this);
        slider.setMax(190);
        slider.setProgress((annualMiles - 1000) / 100);
        form.addView(slider, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean user) {
                annualMiles = 1000 + value * 100;
                updateEstimate();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        Ui.gap(this, form, 15);
        LinearLayout price = Ui.horizontal(this);
        form.addView(price);
        price.addView(Ui.text(this, "Estimated monthly", 14, Ui.MUTED, false),
            new LinearLayout.LayoutParams(0, -2, 1));
        estimate = Ui.text(this, "", 23, Ui.INK, true);
        price.addView(estimate);
        updateEstimate();
        Ui.gap(this, form, 9);
        form.addView(Ui.text(this, "Illustrative price only. Final cover and price are confirmed in a full quote.",
            11, Ui.MUTED, false));

        Ui.gap(this, content, 22);
        TextView continueButton = Ui.button(this, "Continue to full quote  →", Ui.CORAL, Color.WHITE);
        content.addView(continueButton, new LinearLayout.LayoutParams(-1, -2));
        continueButton.setOnClickListener(v -> continueQuote());
        Ui.gap(this, content, 15);
        TextView note = Ui.text(this, "No purchase is made in this demo.", 11, Ui.MUTED, false);
        note.setGravity(Gravity.CENTER);
        content.addView(note);
    }

    private void updateEstimate() {
        if (mileage != null) mileage.setText(annualMiles + " miles");
        if (estimate != null) estimate.setText("€" + (21 + annualMiles / 1000 * 2) + "/mo");
    }

    private void continueQuote() {
        String destination = config.card.quotationUrl;
        if (DEMO_PORTAL.equals(destination)) {
            Intent intent = new Intent(this, PortalActivity.class);
            intent.putExtra("mileage", annualMiles);
            startActivity(intent);
            return;
        }
        Uri uri = Uri.parse(destination);
        if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
            catch (ActivityNotFoundException error) {
                Toast.makeText(this, "No browser is available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Quotation service is unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}

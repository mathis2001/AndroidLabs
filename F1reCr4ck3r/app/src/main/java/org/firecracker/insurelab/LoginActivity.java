package org.firecracker.insurelab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class LoginActivity extends Activity {
    private LinearLayout offerHost;
    private TextView syncStatus;
    private RemoteConfig config;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        config = RemoteConfig.defaults();
        LinearLayout root = Ui.vertical(this);
        root.setBackgroundColor(Ui.PALE);
        setContentView(root);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root.addView(scroll);
        LinearLayout content = Ui.vertical(this);
        content.setPadding(Ui.dp(this, 24), Ui.dp(this, 26), Ui.dp(this, 24), Ui.dp(this, 28));
        scroll.addView(content);

        LinearLayout brand = Ui.horizontal(this);
        content.addView(brand);
        TextView mark = Ui.button(this, "F", Ui.INK, Color.WHITE);
        mark.setTextSize(24);
        mark.setMinWidth(Ui.dp(this, 54));
        mark.setMinHeight(Ui.dp(this, 54));
        brand.addView(mark);
        LinearLayout wordmark = Ui.vertical(this);
        LinearLayout.LayoutParams wordParams = new LinearLayout.LayoutParams(-2, -2);
        wordParams.leftMargin = Ui.dp(this, 12);
        brand.addView(wordmark, wordParams);
        wordmark.addView(Ui.text(this, "firecracker", 21, Ui.INK, true));
        wordmark.addView(Ui.text(this, "MOTOR INSURANCE", 10, Ui.CORAL, true));

        Ui.gap(this, content, 36);
        content.addView(Ui.text(this, "Good to see you.", 31, Ui.INK, true));
        Ui.gap(this, content, 7);
        content.addView(Ui.text(this, "Your car, your cover, all in one place.", 15, Ui.MUTED, false));
        Ui.gap(this, content, 26);

        LinearLayout login = Ui.card(this);
        content.addView(login, new LinearLayout.LayoutParams(-1, -2));
        login.addView(Ui.text(this, "Sign in to your policy", 19, Ui.INK, true));
        Ui.gap(this, login, 17);
        login.addView(Ui.text(this, "EMAIL ADDRESS", 11, Ui.MUTED, true));
        Ui.gap(this, login, 7);
        EditText email = field("alex.morgan@example.com", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        login.addView(email, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        Ui.gap(this, login, 16);
        login.addView(Ui.text(this, "POLICY PIN", 11, Ui.MUTED, true));
        Ui.gap(this, login, 7);
        EditText pin = field("2468", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        login.addView(pin, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        Ui.gap(this, login, 18);
        TextView signIn = Ui.button(this, "Sign in  →", Ui.INK, Color.WHITE);
        login.addView(signIn, new LinearLayout.LayoutParams(-1, -2));
        signIn.setOnClickListener(v -> {
            if ("alex.morgan@example.com".equalsIgnoreCase(email.getText().toString().trim()) &&
                "2468".equals(pin.getText().toString())) {
                PolicyStore.signIn(this);
                String pending = getIntent().getStringExtra("pending_offer_link");
                Uri link = pending == null ? null : Uri.parse(pending);
                if (link != null && "f1recr4ck3r".equals(link.getScheme()) &&
                    "offers".equals(link.getHost()) && "/employee".equals(link.getPath())) {
                    startActivity(new Intent(this, EmployeeOfferActivity.class)
                        .setAction(Intent.ACTION_VIEW).setData(link));
                    finish();
                } else if (link != null && "f1recr4ck3r".equals(link.getScheme()) &&
                    "payd".equals(link.getHost()) && "/quote".equals(link.getPath())) {
                    startActivity(new Intent(this, QuoteActivity.class)
                        .setAction(Intent.ACTION_VIEW).setData(link));
                    finish();
                } else {
                    startActivity(new Intent(this, HomeActivity.class));
                }
            } else {
                Toast.makeText(this, "Check your email and policy PIN", Toast.LENGTH_SHORT).show();
            }
        });
        Ui.gap(this, login, 12);
        TextView demo = Ui.text(this, "Demo policy: alex.morgan@example.com · PIN 2468", 11, Ui.MUTED, false);
        demo.setGravity(Gravity.CENTER);
        login.addView(demo);

        Ui.gap(this, content, 16);
        TextView support = Ui.text(this, "Checking claims support availability…", 12, Ui.MUTED, false);
        support.setGravity(Gravity.CENTER);
        content.addView(support);
        SupportRemoteConfig.fetch(this, label -> {
            if (!isFinishing() && !isDestroyed()) support.setText(label);
        });

        Ui.gap(this, content, 29);
        LinearLayout offerHeading = Ui.horizontal(this);
        content.addView(offerHeading);
        offerHeading.addView(Ui.text(this, "A better fit for your drive", 17, Ui.INK, true),
            new LinearLayout.LayoutParams(0, -2, 1));
        TextView refresh = Ui.text(this, "↻  Refresh", 12, Ui.CORAL, true);
        refresh.setPadding(Ui.dp(this, 7), Ui.dp(this, 8), 0, Ui.dp(this, 8));
        offerHeading.addView(refresh);
        refresh.setOnClickListener(v -> refresh());
        Ui.gap(this, content, 12);
        offerHost = Ui.vertical(this);
        content.addView(offerHost);
        Ui.gap(this, content, 10);
        syncStatus = Ui.text(this, "Checking current offers…", 11, Ui.MUTED, false);
        content.addView(syncStatus);
        renderOffer();
        refresh();
    }

    private EditText field(String value, int inputType) {
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setTextSize(14);
        field.setTextColor(Ui.INK);
        field.setInputType(inputType);
        field.setText(value);
        field.setPadding(Ui.dp(this, 13), 0, Ui.dp(this, 13), 0);
        field.setBackground(Ui.outline(this, Color.WHITE, Ui.LINE, 12));
        return field;
    }

    private void refresh() {
        syncStatus.setText("Checking current offers…");
        RemoteConfig.fetch(this, (result, live) -> {
            config = result;
            renderOffer();
            syncStatus.setText(live ? "Offer updated just now" : "Showing the most recent available offer");
        });
    }

    private void renderOffer() {
        offerHost.removeAllViews();
        if (config.maintenanceMode) {
            TextView notice = Ui.chip(this, "Service notice: some features may be delayed", 0xFFFFE9DD, Ui.CORAL);
            offerHost.addView(notice);
            Ui.gap(this, offerHost, 10);
        }
        if (!config.paydEnabled || !config.card.enabled) {
            TextView unavailable = Ui.text(this, "There are no new offers right now.", 14, Ui.MUTED, false);
            offerHost.addView(unavailable);
            return;
        }
        LinearLayout card = Ui.vertical(this);
        card.setPadding(Ui.dp(this, 21), Ui.dp(this, 20), Ui.dp(this, 21), Ui.dp(this, 20));
        card.setBackground(Ui.shape(this, 0xFFE7F1F1, 20));
        offerHost.addView(card, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout top = Ui.horizontal(this);
        card.addView(top);
        TextView icon = Ui.text(this, config.card.icon, 29, Ui.INK, false);
        top.addView(icon, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(Ui.chip(this, "MILEAGE-BASED", Color.WHITE, Ui.INK));
        Ui.gap(this, card, 14);
        card.addView(Ui.text(this, config.card.shortTitle, 22, Ui.INK, true));
        Ui.gap(this, card, 6);
        card.addView(Ui.text(this, config.card.shortDescription, 14, Ui.MUTED, false));
        Ui.gap(this, card, 15);
        card.addView(Ui.text(this, "Explore cover  →", 13, Ui.CORAL, true));
        card.setOnClickListener(v -> openOffer());
    }

    private void openOffer() {
        OfferNavigation.open(this, config.card.deeplink);
    }
}

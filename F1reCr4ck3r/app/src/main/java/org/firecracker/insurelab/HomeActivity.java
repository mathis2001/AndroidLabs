package org.firecracker.insurelab;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class HomeActivity extends Activity {
    private LinearLayout content;
    private RemoteConfig config;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (!PolicyStore.signedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
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
        content.setPadding(Ui.dp(this, 24), Ui.dp(this, 26), Ui.dp(this, 24), Ui.dp(this, 30));
        scroll.addView(content);
        render();
        RemoteConfig.fetch(this, (result, live) -> { config = result; render(); });
    }

    @Override protected void onResume() {
        super.onResume();
        if (content != null) render();
    }

    private void render() {
        content.removeAllViews();
        LinearLayout header = Ui.horizontal(this);
        content.addView(header);
        LinearLayout title = Ui.vertical(this);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        title.addView(Ui.text(this, "Good morning, Alex", 27, Ui.INK, true));
        Ui.gap(this, title, 5);
        title.addView(Ui.text(this, "Your cover at a glance", 14, Ui.MUTED, false));
        TextView avatar = Ui.button(this, "AM", Ui.INK, Color.WHITE);
        avatar.setMinWidth(Ui.dp(this, 52));
        header.addView(avatar);
        avatar.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("Alex Morgan")
            .setMessage("Demo policy holder\nMember since 2022")
            .setNegativeButton("Sign out", (dialog, which) -> {
                PolicyStore.signOut(this);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            })
            .setPositiveButton("Close", null).show());

        if (config.maintenanceMode) {
            Ui.gap(this, content, 20);
            content.addView(Ui.chip(this, "Service notice · some tools are temporarily unavailable",
                0xFFFFE9DD, Ui.CORAL));
        }
        Ui.gap(this, content, 27);
        LinearLayout policy = Ui.vertical(this);
        policy.setPadding(Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 23));
        policy.setBackground(Ui.shape(this, Ui.INK, 21));
        content.addView(policy, new LinearLayout.LayoutParams(-1, -2));
        int discount = PolicyStore.discountPercent(this);
        String status = discount >= 50 ? "VIP OFFER APPLIED" :
            discount > 0 ? "EMPLOYEE DISCOUNT APPLIED" : "ACTIVE POLICY";
        policy.addView(Ui.chip(this, status, 0xFF2C4656, Color.WHITE));
        Ui.gap(this, policy, 28);
        policy.addView(Ui.text(this, "Comprehensive cover", 23, Color.WHITE, true));
        Ui.gap(this, policy, 7);
        policy.addView(Ui.text(this, "2021 Toyota Corolla Hybrid", 15, 0xFFC9D8DF, false));
        Ui.gap(this, policy, 13);
        policy.addView(Ui.text(this,
            discount > 0 ? "€" + (64 * (100 - discount) / 100) + "/mo · " + discount + "% saving" : "€64/mo",
            16, Color.WHITE, true));
        Ui.gap(this, policy, 25);
        LinearLayout detail = Ui.horizontal(this);
        policy.addView(detail);
        detail.addView(Ui.text(this, "Policy  FM-2084-7712", 12, Color.WHITE, false),
            new LinearLayout.LayoutParams(0, -2, 1));
        detail.addView(Ui.text(this, "Renewal  12 Mar", 12, Color.WHITE, false));

        Ui.gap(this, content, 28);
        content.addView(Ui.text(this, "Quick actions", 19, Ui.INK, true));
        Ui.gap(this, content, 12);
        action("⊞", "Policy documents", "View your cover summary", () -> show("Policy documents",
            "Your comprehensive cover is active. Your policy summary is ready in this demo account."));
        Ui.gap(this, content, 10);
        action("◎", "Make a claim", config.claimsEnabled ? "Start or track a claim" : "Temporarily unavailable",
            () -> show("Claims", config.claimsEnabled ?
                "No open claims. To make a claim, have your registration and incident details ready." :
                "Claims are temporarily unavailable. Please try again later."));
        Ui.gap(this, content, 10);
        action("✦", "Roadside assistance", config.roadsideEnabled ? "Help when you need it" : "Temporarily unavailable",
            () -> show("Roadside assistance", config.roadsideEnabled ?
                "Your policy includes 24/7 roadside assistance. This is a demonstration account." :
                "Roadside assistance is temporarily unavailable in the app."));

        if (config.paydEnabled && config.card.enabled) {
            Ui.gap(this, content, 29);
            content.addView(Ui.text(this, "Explore more cover", 19, Ui.INK, true));
            Ui.gap(this, content, 12);
            LinearLayout offer = Ui.card(this);
            content.addView(offer, new LinearLayout.LayoutParams(-1, -2));
            offer.addView(Ui.text(this, config.card.icon + "  " + config.card.shortTitle, 18, Ui.INK, true));
            Ui.gap(this, offer, 8);
            offer.addView(Ui.text(this, config.card.shortDescription, 14, Ui.MUTED, false));
            Ui.gap(this, offer, 12);
            offer.addView(Ui.text(this, "Get an indicative quote  →", 13, Ui.CORAL, true));
            offer.setOnClickListener(v -> OfferNavigation.open(this, config.card.deeplink));
        }
        Ui.gap(this, content, 26);
        TextView note = Ui.text(this, "Firecracker Motor · Demo insurance account", 11, Ui.MUTED, false);
        note.setGravity(Gravity.CENTER);
        content.addView(note);
    }

    private void action(String icon, String title, String description, Runnable action) {
        LinearLayout row = Ui.horizontal(this);
        row.setPadding(Ui.dp(this, 17), Ui.dp(this, 16), Ui.dp(this, 17), Ui.dp(this, 16));
        row.setBackground(Ui.outline(this, Color.WHITE, Ui.LINE, 17));
        content.addView(row, new LinearLayout.LayoutParams(-1, -2));
        TextView symbol = Ui.text(this, icon, 27, Ui.CORAL, false);
        row.addView(symbol, new LinearLayout.LayoutParams(Ui.dp(this, 43), -2));
        LinearLayout labels = Ui.vertical(this);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        labels.addView(Ui.text(this, title, 15, Ui.INK, true));
        Ui.gap(this, labels, 3);
        labels.addView(Ui.text(this, description, 12, Ui.MUTED, false));
        row.addView(Ui.text(this, "›", 28, Ui.MUTED, false));
        row.setOnClickListener(v -> action.run());
    }

    private void show(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("Close", null).show();
    }
}

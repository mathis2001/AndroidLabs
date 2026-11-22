package org.hidenseek.lab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public final class SettingsActivity extends Activity {
    private static final int CREATE_BACKUP = 21;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = Ui.screen(this);
        ScrollView scroll = Ui.scroll(this, root);
        LinearLayout content = Ui.vertical(this);
        content.setPadding(Ui.dp(this, 21), Ui.dp(this, 23), Ui.dp(this, 21), Ui.dp(this, 22));
        scroll.addView(content);

        content.addView(Ui.text(this, "Settings", 27, Ui.INK, true));
        Ui.gap(this, content, 4);
        content.addView(Ui.text(this, "Make your space yours", 13, Ui.MUTED, false));
        Ui.gap(this, content, 25);

        LinearLayout profile = Ui.horizontal(this);
        profile.setPadding(Ui.dp(this, 17), Ui.dp(this, 18), Ui.dp(this, 17), Ui.dp(this, 18));
        profile.setBackground(Ui.shape(this, Color.WHITE, 18));
        content.addView(profile);
        profile.addView(Ui.avatar(this, "LM", Ui.PURPLE, 55));
        LinearLayout user = Ui.vertical(this);
        LinearLayout.LayoutParams userParams = new LinearLayout.LayoutParams(0, -2, 1);
        userParams.leftMargin = Ui.dp(this, 14);
        profile.addView(user, userParams);
        user.addView(Ui.text(this, "Lena Moreau", 17, Ui.INK, true));
        user.addView(Ui.text(this, "Your secure account", 12, Ui.MUTED, false));
        profile.addView(Ui.pill(this, "✓", Color.rgb(234, 249, 243), Color.rgb(42, 145, 101)));

        section(content, "PRIVACY & NOTIFICATIONS");
        option(content, "Read receipts", "Let contacts know when you've read messages", "read_receipts");
        Ui.gap(this, content, 8);
        option(content, "Quiet hours", "Pause notifications after 10 PM", "quiet_hours");
        Ui.gap(this, content, 8);
        option(content, "Private previews", "Hide message text in notifications", "lock_previews");
        Ui.gap(this, content, 8);
        option(content, "Auto-download media", "Save incoming media automatically", "auto_download");

        section(content, "DATA & STORAGE");
        action(content, "▣", "Create backup", "Export your chats and preferences", () -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "hidenseek-backup.json");
            startActivityForResult(intent, CREATE_BACKUP);
        });
        Ui.gap(this, content, 8);
        action(content, "↺", "Restore backup", "Import a saved chat backup", () -> {
            Intent intent = new Intent(this, RestoreActivity.class);
            intent.putExtra("return_intent", new Intent(this, SettingsActivity.class).toUri(Intent.URI_INTENT_SCHEME));
            startActivity(intent);
        });
        Ui.gap(this, content, 25);
        TextView caption = Ui.text(this, "Hide'N Seek  ·  Version 1.0", 12, Ui.MUTED, false);
        caption.setGravity(Gravity.CENTER);
        content.addView(caption);
        Ui.footer(this, root, false,
            () -> { startActivity(new Intent(this, ContactsActivity.class)); finish(); },
            () -> { });
    }

    private void section(LinearLayout parent, String title) {
        Ui.gap(this, parent, 25);
        TextView heading = Ui.text(this, title, 11, Ui.MUTED, true);
        heading.setLetterSpacing(0.1f);
        heading.setPadding(Ui.dp(this, 5), 0, 0, Ui.dp(this, 10));
        parent.addView(heading);
    }

    private void option(LinearLayout parent, String name, String detail, String key) {
        LinearLayout row = Ui.horizontal(this);
        row.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        row.setBackground(Ui.shape(this, Color.WHITE, 17));
        parent.addView(row);
        LinearLayout labels = Ui.vertical(this);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        labels.addView(Ui.text(this, name, 15, Ui.INK, true));
        Ui.gap(this, labels, 3);
        labels.addView(Ui.text(this, detail, 12, Ui.MUTED, false));
        Switch toggle = new Switch(this);
        toggle.setChecked(getSharedPreferences("settings", 0).getBoolean(key, BackupStore.defaultValue(key)));
        toggle.setOnCheckedChangeListener((view, checked) ->
            getSharedPreferences("settings", 0).edit().putBoolean(key, checked).apply());
        row.addView(toggle);
    }

    private void action(LinearLayout parent, String icon, String name, String detail, Runnable run) {
        LinearLayout row = Ui.horizontal(this);
        row.setPadding(Ui.dp(this, 16), Ui.dp(this, 15), Ui.dp(this, 16), Ui.dp(this, 15));
        row.setBackground(Ui.shape(this, Color.WHITE, 17));
        parent.addView(row);
        TextView symbol = Ui.text(this, icon, 23, Ui.PURPLE, true);
        symbol.setGravity(Gravity.CENTER);
        symbol.setBackground(Ui.shape(this, Color.rgb(239, 237, 252), 12));
        row.addView(symbol, new LinearLayout.LayoutParams(Ui.dp(this, 43), Ui.dp(this, 43)));
        LinearLayout labels = Ui.vertical(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, -2, 1);
        labelParams.leftMargin = Ui.dp(this, 13);
        row.addView(labels, labelParams);
        labels.addView(Ui.text(this, name, 15, Ui.INK, true));
        Ui.gap(this, labels, 3);
        labels.addView(Ui.text(this, detail, 12, Ui.MUTED, false));
        row.addView(Ui.text(this, "›", 27, Ui.MUTED, false));
        row.setOnClickListener(v -> run.run());
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == CREATE_BACKUP && result == RESULT_OK && data != null && data.getData() != null) {
            try {
                BackupStore.write(this, data.getData());
                Toast.makeText(this, "Backup saved", Toast.LENGTH_SHORT).show();
            } catch (Exception error) {
                Toast.makeText(this, "Could not save backup", Toast.LENGTH_LONG).show();
            }
        }
    }
}

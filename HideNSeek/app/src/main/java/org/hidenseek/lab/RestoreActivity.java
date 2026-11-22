package org.hidenseek.lab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class RestoreActivity extends Activity {
    private static final int PICK_BACKUP = 32;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = Ui.screen(this);
        LinearLayout content = Ui.vertical(this);
        content.setPadding(Ui.dp(this, 23), Ui.dp(this, 24), Ui.dp(this, 23), Ui.dp(this, 23));
        root.addView(content);
        TextView back = Ui.text(this, "‹  Settings", 16, Ui.PURPLE, true);
        content.addView(back);
        back.setOnClickListener(v -> finish());
        Ui.gap(this, content, 40);
        TextView icon = Ui.text(this, "↺", 40, Ui.PURPLE, true);
        icon.setPadding(Ui.dp(this, 18), Ui.dp(this, 10), Ui.dp(this, 18), Ui.dp(this, 10));
        icon.setBackground(Ui.shape(this, Color.rgb(238, 236, 252), 20));
        content.addView(icon, new LinearLayout.LayoutParams(-2, -2));
        Ui.gap(this, content, 22);
        content.addView(Ui.text(this, "Restore your backup", 26, Ui.INK, true));
        Ui.gap(this, content, 8);
        content.addView(Ui.text(this,
            "Choose a Hide'N Seek backup to bring your chats and preferences back to this device.",
            15, Ui.MUTED, false));
        Ui.gap(this, content, 28);
        TextView choose = Ui.button(this, "Choose backup file");
        content.addView(choose, new LinearLayout.LayoutParams(-1, -2));
        choose.setOnClickListener(v -> {
            Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            picker.addCategory(Intent.CATEGORY_OPENABLE);
            picker.setType("*/*");
            startActivityForResult(picker, PICK_BACKUP);
        });
        Ui.gap(this, content, 17);
        status = Ui.text(this, "Backups include chats and preferences. Secure media stays on this device.", 13, Ui.MUTED, false);
        content.addView(status);

        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            String json = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (json != null) {
                apply(json);
            } else {
                Uri stream = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                if (stream != null) importUri(stream);
            }
        }
    }

    private void importUri(Uri uri) {
        try {
            apply(BackupStore.read(this, uri));
        } catch (Exception error) {
            status.setText("Could not read this backup file.");
        }
    }

    private void apply(String json) {
        try {
            BackupStore.restore(this, json);
            status.setText("Backup restored successfully.");
            String destination = getIntent().getStringExtra("return_intent");
            if (destination != null) {
                Intent next = Intent.parseUri(destination,
                    Intent.URI_INTENT_SCHEME | Intent.URI_ALLOW_UNSAFE);
                startActivity(next);
                finish();
            }
        } catch (Exception error) {
            status.setText("This backup cannot be restored.");
        }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == PICK_BACKUP && result == RESULT_OK && data != null && data.getData() != null) {
            importUri(data.getData());
        }
    }
}

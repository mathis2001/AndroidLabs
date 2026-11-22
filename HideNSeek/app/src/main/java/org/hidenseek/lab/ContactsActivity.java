package org.hidenseek.lab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class ContactsActivity extends Activity {
    private LinearLayout list;
    private String currentFilter = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = Ui.screen(this);
        LinearLayout header = Ui.vertical(this);
        header.setPadding(Ui.dp(this, 24), Ui.dp(this, 23), Ui.dp(this, 24), Ui.dp(this, 12));
        root.addView(header);

        LinearLayout top = Ui.horizontal(this);
        header.addView(top);
        LinearLayout titles = Ui.vertical(this);
        top.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
        titles.addView(Ui.text(this, "Hide'N Seek", 27, Ui.INK, true));
        Ui.gap(this, titles, 3);
        titles.addView(Ui.text(this, "Private conversations, close connections", 13, Ui.MUTED, false));
        TextView compose = Ui.pill(this, "+", Ui.PURPLE, Color.WHITE);
        compose.setTextSize(24);
        top.addView(compose);
        compose.setOnClickListener(v -> {
            String[] names = new String[DemoData.CONVERSATIONS.length];
            for (int index = 0; index < names.length; index++) names[index] = DemoData.CONVERSATIONS[index].name;
            new android.app.AlertDialog.Builder(this)
                .setTitle("New message")
                .setItems(names, (dialog, index) -> {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("thread", DemoData.CONVERSATIONS[index].id);
                    startActivity(intent);
                }).show();
        });

        Ui.gap(this, header, 22);
        EditText search = new EditText(this);
        search.setId(1901);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setHint("Search conversations");
        search.setCompoundDrawablePadding(Ui.dp(this, 8));
        search.setPadding(Ui.dp(this, 18), Ui.dp(this, 12), Ui.dp(this, 18), Ui.dp(this, 12));
        search.setBackground(Ui.outlined(this, Color.WHITE, Ui.LINE, 17));
        header.addView(search, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));

        ScrollView scroll = Ui.scroll(this, root);
        list = Ui.vertical(this);
        list.setPadding(Ui.dp(this, 17), Ui.dp(this, 12), Ui.dp(this, 17), Ui.dp(this, 18));
        scroll.addView(list);
        render("");
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentFilter = s.toString();
                render(currentFilter);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        Ui.footer(this, root, true, () -> { },
            () -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override protected void onResume() {
        super.onResume();
        if (list != null) render(currentFilter);
    }

    private void render(String filter) {
        list.removeAllViews();
        TextView heading = Ui.text(this, "CONVERSATIONS", 11, Ui.MUTED, true);
        heading.setLetterSpacing(0.11f);
        heading.setPadding(Ui.dp(this, 9), Ui.dp(this, 5), 0, Ui.dp(this, 12));
        list.addView(heading);
        int matches = 0;
        for (DemoData.Conversation thread : DemoData.CONVERSATIONS) {
            if (!thread.name.toLowerCase().contains(filter.toLowerCase())) continue;
            matches++;
            addConversation(thread);
            Ui.gap(this, list, 9);
        }
        if (matches == 0) {
            TextView empty = Ui.text(this, "No conversations found", 15, Ui.MUTED, false);
            empty.setGravity(Gravity.CENTER);
            list.addView(empty, new LinearLayout.LayoutParams(-1, Ui.dp(this, 130)));
        }
    }

    private void addConversation(DemoData.Conversation thread) {
        LinearLayout row = Ui.horizontal(this);
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16));
        row.setBackground(Ui.shape(this, Color.WHITE, 18));
        row.setElevation(Ui.dp(this, 2));
        list.addView(row, new LinearLayout.LayoutParams(-1, -2));
        row.addView(Ui.avatar(this, thread.initials, thread.color, 54));
        LinearLayout content = Ui.vertical(this);
        LinearLayout.LayoutParams body = new LinearLayout.LayoutParams(0, -2, 1);
        body.leftMargin = Ui.dp(this, 14);
        row.addView(content, body);
        LinearLayout first = Ui.horizontal(this);
        content.addView(first);
        first.addView(Ui.text(this, thread.name, 16, Ui.INK, true), new LinearLayout.LayoutParams(0, -2, 1));
        java.util.ArrayList<DemoData.Message> saved = ChatStore.messages(this, thread.id);
        DemoData.Message latest = saved.isEmpty() ? null : saved.get(saved.size() - 1);
        first.addView(Ui.text(this, latest == null ? thread.time : latest.time, 11, Ui.MUTED, false));
        Ui.gap(this, content, 5);
        String snippet = latest == null ? thread.preview :
            latest.mediaId >= 0 ? "You sent a photo" : latest.body;
        TextView preview = Ui.text(this, snippet, 13, Ui.MUTED, false);
        preview.setSingleLine(true);
        preview.setEllipsize(android.text.TextUtils.TruncateAt.END);
        content.addView(preview);
        row.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("thread", thread.id);
            startActivity(intent);
        });
    }
}

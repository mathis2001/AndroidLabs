package org.hidenseek.lab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class ChatActivity extends Activity {
    private static final int PICK_IMAGE = 41;
    private LinearLayout messages;
    private ScrollView scroll;
    private DemoData.Conversation thread;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        thread = DemoData.conversation(getIntent().getStringExtra("thread"));
        LinearLayout root = Ui.screen(this);
        LinearLayout bar = Ui.horizontal(this);
        bar.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        bar.setBackgroundColor(Color.WHITE);
        root.addView(bar);
        TextView back = Ui.text(this, "‹", 34, Ui.PURPLE, false);
        back.setGravity(Gravity.CENTER);
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 32), Ui.dp(this, 44)));
        back.setOnClickListener(v -> finish());
        bar.addView(Ui.avatar(this, thread.initials, thread.color, 42));
        LinearLayout titles = Ui.vertical(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -2, 1);
        titleParams.leftMargin = Ui.dp(this, 12);
        bar.addView(titles, titleParams);
        titles.addView(Ui.text(this, thread.name, 17, Ui.INK, true));
        titles.addView(Ui.text(this, "End-to-end encrypted", 11, Ui.MUTED, false));
        bar.addView(Ui.pill(this, "⋮", Ui.BACKGROUND, Ui.INK));

        scroll = Ui.scroll(this, root);
        messages = Ui.vertical(this);
        messages.setPadding(Ui.dp(this, 17), Ui.dp(this, 16), Ui.dp(this, 17), Ui.dp(this, 19));
        scroll.addView(messages);
        TextView day = Ui.text(this, "TODAY", 11, Ui.MUTED, true);
        day.setGravity(Gravity.CENTER);
        day.setPadding(0, 0, 0, Ui.dp(this, 18));
        messages.addView(day);
        for (DemoData.Message message : thread.messages) addBubble(message);
        for (DemoData.Message message : ChatStore.messages(this, thread.id)) addBubble(message);
        scroll.post(() -> scroll.fullScroll(ScrollView.FOCUS_DOWN));

        LinearLayout composer = Ui.horizontal(this);
        composer.setBackgroundColor(Color.WHITE);
        composer.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));
        root.addView(composer);
        TextView plus = Ui.text(this, "+", 25, Ui.PURPLE, false);
        plus.setGravity(Gravity.CENTER);
        composer.addView(plus, new LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 42)));
        plus.setOnClickListener(v -> {
            Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            picker.addCategory(Intent.CATEGORY_OPENABLE);
            picker.setType("image/*");
            startActivityForResult(picker, PICK_IMAGE);
        });
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(14);
        input.setHint("Message " + thread.name);
        input.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        input.setBackground(Ui.shape(this, Ui.BACKGROUND, 21));
        composer.addView(input, new LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1));
        TextView send = Ui.pill(this, "➤", Ui.PURPLE, Color.WHITE);
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(-2, -2);
        sendParams.leftMargin = Ui.dp(this, 10);
        composer.addView(send, sendParams);
        send.setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) return;
            ChatStore.add(this, thread.id, value);
            addBubble(new DemoData.Message(value, "Now", true));
            input.setText("");
            scroll.post(() -> scroll.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != PICK_IMAGE || result != RESULT_OK || data == null) return;
        Uri selected = data.getData();
        if (selected == null) return;
        try {
            int mediaId = MediaUploads.save(this, selected);
            addBubble(ChatStore.addMedia(this, thread.id, mediaId));
            scroll.post(() -> scroll.fullScroll(ScrollView.FOCUS_DOWN));
        } catch (Exception error) {
            android.widget.Toast.makeText(this, "Could not attach this image", android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void addBubble(DemoData.Message message) {
        LinearLayout line = Ui.horizontal(this);
        line.setGravity(message.mine ? Gravity.RIGHT : Gravity.LEFT);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(-1, -2);
        lineParams.bottomMargin = Ui.dp(this, 12);
        messages.addView(line, lineParams);
        LinearLayout bubble = Ui.vertical(this);
        bubble.setPadding(Ui.dp(this, 15), Ui.dp(this, 11), Ui.dp(this, 15), Ui.dp(this, 10));
        bubble.setBackground(Ui.shape(this, message.mine ? Ui.PURPLE : Color.WHITE, 17));
        line.addView(bubble, new LinearLayout.LayoutParams(-2, -2));
        if (message.mediaId >= 0) {
            FrameLayout preview = new FrameLayout(this);
            preview.setBackground(Ui.shape(this, Ui.BACKGROUND, 12));
            preview.setClipToOutline(true);
            bubble.addView(preview, new LinearLayout.LayoutParams(Ui.dp(this, 175), Ui.dp(this, 112)));
            Bitmap bitmap = MediaPreview.load(this, message.mediaId);
            if (bitmap != null) {
                ImageView image = new ImageView(this);
                image.setImageBitmap(bitmap);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                preview.addView(image, new FrameLayout.LayoutParams(-1, -1));
            } else {
                TextView icon = Ui.text(this, "▧", 43, Ui.PURPLE, false);
                icon.setGravity(Gravity.CENTER);
                preview.addView(icon, new FrameLayout.LayoutParams(-1, -1));
            }
            DemoData.Media media = DemoData.media(message.mediaId);
            if (media != null && media.identity) {
                TextView label = Ui.text(this, "PRIVATE DOCUMENT", 10, Color.WHITE, true);
                label.setGravity(Gravity.CENTER);
                label.setBackground(Ui.shape(this, 0xCC293552, 8));
                FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(-1, Ui.dp(this, 30), Gravity.BOTTOM);
                preview.addView(label, labelParams);
            }
            preview.setOnClickListener(v -> {
                Intent intent = new Intent(this, PhotoActivity.class);
                intent.putExtra("slot", message.mediaId);
                startActivity(intent);
            });
            Ui.gap(this, bubble, 8);
        }
        TextView body = Ui.text(this, message.body, 14, message.mine ? Color.WHITE : Ui.INK, false);
        body.setMaxWidth(Ui.dp(this, 235));
        bubble.addView(body);
        TextView time = Ui.text(this, message.time, 10,
            message.mine ? Color.rgb(219, 215, 255) : Ui.MUTED, false);
        time.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(-1, -2);
        timeParams.topMargin = Ui.dp(this, 6);
        bubble.addView(time, timeParams);
    }
}

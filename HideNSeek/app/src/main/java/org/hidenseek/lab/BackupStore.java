package org.hidenseek.lab;

import android.content.Context;
import android.net.Uri;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

final class BackupStore {
    static final String[] OPTIONS = { "read_receipts", "quiet_hours", "lock_previews", "auto_download" };

    static String create(Context context) throws JSONException {
        JSONObject preferences = new JSONObject();
        for (String key : OPTIONS) {
            preferences.put(key, context.getSharedPreferences("settings", 0).getBoolean(key, defaultValue(key)));
        }
        JSONObject document = new JSONObject();
        document.put("format", "hns-backup");
        document.put("version", 1);
        document.put("saved_at", System.currentTimeMillis());
        document.put("preferences", preferences);
        JSONObject conversations = new JSONObject();
        for (DemoData.Conversation thread : DemoData.CONVERSATIONS) {
            String raw = context.getSharedPreferences("chat", 0).getString(thread.id, "[]");
            conversations.put(thread.id, new JSONArray(raw));
        }
        document.put("conversations", conversations);
        return document.toString(2);
    }

    static void restore(Context context, String json) throws JSONException {
        JSONObject document = new JSONObject(json);
        if (!"hns-backup".equals(document.optString("format")) || document.optInt("version") != 1) {
            throw new JSONException("Unsupported backup format");
        }
        JSONObject preferences = document.optJSONObject("preferences");
        if (preferences == null) throw new JSONException("Missing preferences");
        android.content.SharedPreferences.Editor editor = context.getSharedPreferences("settings", 0).edit();
        for (String key : OPTIONS) {
            if (preferences.has(key)) editor.putBoolean(key, preferences.optBoolean(key));
        }
        editor.apply();
        JSONObject conversations = document.optJSONObject("conversations");
        if (conversations != null) {
            android.content.SharedPreferences.Editor chats = context.getSharedPreferences("chat", 0).edit();
            for (DemoData.Conversation thread : DemoData.CONVERSATIONS) {
                JSONArray saved = conversations.optJSONArray(thread.id);
                if (saved != null && saved.length() <= 100) chats.putString(thread.id, saved.toString());
            }
            chats.apply();
        }
    }

    static boolean defaultValue(String key) {
        return !"auto_download".equals(key);
    }

    static String read(Context context, Uri uri) throws Exception {
        try (InputStream input = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            if (input == null) throw new java.io.IOException("Backup unavailable");
            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) != -1) {
                bytes.write(buffer, 0, length);
                if (bytes.size() > 256 * 1024) throw new java.io.IOException("Backup is too large");
            }
            return bytes.toString("UTF-8");
        }
    }

    static void write(Context context, Uri uri) throws Exception {
        try (OutputStream output = context.getContentResolver().openOutputStream(uri, "w")) {
            if (output == null) throw new java.io.IOException("Could not create backup");
            output.write(create(context).getBytes(StandardCharsets.UTF_8));
        }
    }

    private BackupStore() { }
}

package org.hidenseek.lab;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

final class ChatStore {
    static ArrayList<DemoData.Message> messages(Context context, String thread) {
        ArrayList<DemoData.Message> result = new ArrayList<>();
        String raw = context.getSharedPreferences("chat", 0).getString(thread, "[]");
        try {
            JSONArray saved = new JSONArray(raw);
            for (int index = 0; index < saved.length(); index++) {
                JSONObject item = saved.getJSONObject(index);
                result.add(new DemoData.Message(item.optString("body"), item.optString("time"),
                    true, item.optInt("media_id", -1)));
            }
        } catch (Exception ignored) { }
        return result;
    }

    static void add(Context context, String thread, String body) {
        addEntry(context, thread, body, -1);
    }

    static DemoData.Message addMedia(Context context, String thread, int mediaId) {
        return addEntry(context, thread, "Photo", mediaId);
    }

    private static DemoData.Message addEntry(Context context, String thread, String body, int mediaId) {
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        JSONArray saved;
        try { saved = new JSONArray(context.getSharedPreferences("chat", 0).getString(thread, "[]")); }
        catch (Exception error) { saved = new JSONArray(); }
        JSONObject item = new JSONObject();
        try {
            item.put("body", body);
            item.put("time", time);
            if (mediaId >= 0) item.put("media_id", mediaId);
            saved.put(item);
            context.getSharedPreferences("chat", 0).edit().putString(thread, saved.toString()).apply();
        } catch (Exception ignored) { }
        return new DemoData.Message(body, time, true, mediaId);
    }

    private ChatStore() { }
}

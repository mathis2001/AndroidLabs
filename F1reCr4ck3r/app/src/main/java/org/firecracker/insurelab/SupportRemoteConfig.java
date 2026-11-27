package org.firecracker.insurelab;

import android.content.Context;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import org.json.JSONObject;

final class SupportRemoteConfig {
    interface Callback { void onResult(String label); }

    static void fetch(Context context, Callback callback) {
        FirebaseRemoteConfig remote;
        try {
            remote = FirebaseRemoteConfig.getInstance(FirebaseClient.app(context));
        } catch (RuntimeException error) {
            callback.onResult("Claims support status unavailable");
            return;
        }
        remote.fetch(0).addOnCompleteListener(fetch -> {
            if (!fetch.isSuccessful()) {
                callback.onResult("Claims support status unavailable");
                return;
            }
            remote.activate().addOnCompleteListener(activation -> {
                if (!activation.isSuccessful() ||
                    remote.getValue("support_operations").getSource() != FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                    callback.onResult("Claims support status unavailable");
                    return;
                }
                try {
                    JSONObject value = new JSONObject(remote.getString("support_operations"));
                    callback.onResult(value.optBoolean("support_enabled")
                        ? value.optString("support_label", "Claims support is available")
                        : "Claims support is currently unavailable");
                } catch (Exception error) {
                    callback.onResult("Claims support status unavailable");
                }
            });
        });
    }

    private SupportRemoteConfig() { }
}

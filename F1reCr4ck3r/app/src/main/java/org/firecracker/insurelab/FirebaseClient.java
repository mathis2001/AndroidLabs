package org.firecracker.insurelab;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

final class FirebaseClient {
    static synchronized FirebaseApp app(Context context) {
        try {
            return FirebaseApp.getInstance("firecracker-config");
        } catch (IllegalStateException missing) {
            String appId = context.getString(R.string.firebase_app_id);
            String apiKey = context.getString(R.string.firebase_api_key);
            String projectId = context.getString(R.string.firebase_project_id);
            if (appId.isEmpty() || apiKey.isEmpty()) {
                throw new IllegalStateException("Firebase client is not configured");
            }
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(appId)
                .setApiKey(apiKey)
                .setProjectId(projectId)
                .build();
            return FirebaseApp.initializeApp(context.getApplicationContext(), options, "firecracker-config");
        }
    }

    private FirebaseClient() { }
}

package org.firecracker.insurelab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;

final class OfferResultUi {
    static void show(Activity activity, String title, String description, boolean success) {
        render(activity, title, description, success, true);
    }

    static void loading(Activity activity, String title, String description) {
        render(activity, title, description, false, false);
    }

    private static void render(Activity activity, String title, String description,
                               boolean success, boolean showButton) {
        LinearLayout root = Ui.vertical(activity);
        root.setBackgroundColor(Ui.PALE);
        root.setPadding(Ui.dp(activity, 24), Ui.dp(activity, 54), Ui.dp(activity, 24), Ui.dp(activity, 24));
        activity.setContentView(root);
        root.addView(Ui.text(activity, success ? "✓" : "◎", 42, success ? Ui.CORAL : Ui.INK, true));
        Ui.gap(activity, root, 20);
        root.addView(Ui.text(activity, title, 28, Ui.INK, true));
        Ui.gap(activity, root, 12);
        root.addView(Ui.text(activity, description, 15, Ui.MUTED, false));
        if (!showButton) return;
        Ui.gap(activity, root, 28);
        TextView next = Ui.button(activity, PolicyStore.signedIn(activity) ? "View policy" : "Sign in",
            Ui.INK, Color.WHITE);
        root.addView(next, new LinearLayout.LayoutParams(-1, -2));
        next.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity,
                PolicyStore.signedIn(activity) ? HomeActivity.class : LoginActivity.class));
            activity.finish();
        });
    }

    private OfferResultUi() { }
}

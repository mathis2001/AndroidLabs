package org.firecracker.insurelab;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

final class OfferNavigation {
    static void open(Activity activity, String link) {
        Uri uri = Uri.parse(link);
        if ("f1recr4ck3r".equals(uri.getScheme()) && "payd".equals(uri.getHost()) &&
            "/quote".equals(uri.getPath())) {
            activity.startActivity(new Intent(activity, QuoteActivity.class).setData(uri));
        } else if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null) {
            try { activity.startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
            catch (ActivityNotFoundException error) {
                Toast.makeText(activity, "No browser is available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "This offer is unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private OfferNavigation() { }
}

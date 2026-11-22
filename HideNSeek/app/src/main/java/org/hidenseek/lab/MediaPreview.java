package org.hidenseek.lab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.InputStream;

final class MediaPreview {
    static Bitmap load(Context context, int slot) {
        return load(context, slot, false);
    }

    static Bitmap load(Context context, int slot, boolean fullSize) {
        int id = context.getSharedPreferences("vault-index", 0).getInt("media_" + slot, -1);
        if (id < 0) {
            context.getContentResolver().getType(Uri.parse("content://org.hidenseek.lab.vault/media"));
            id = context.getSharedPreferences("vault-index", 0).getInt("media_" + slot, -1);
        }
        if (id < 0) return null;
        Uri uri = Uri.parse("content://org.hidenseek.lab.vault/media/" + id);
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = fullSize ? 1 : 2;
            Bitmap original = BitmapFactory.decodeStream(stream, null, options);
            if (original == null) return null;
            DemoData.Media media = DemoData.media(slot);
            if (media == null || !media.identity) return original;
            Bitmap tiny = Bitmap.createScaledBitmap(original, 18, 12, true);
            Bitmap preview = Bitmap.createScaledBitmap(tiny,
                fullSize ? 720 : 360, fullSize ? 480 : 240, false);
            original.recycle();
            tiny.recycle();
            return preview;
        } catch (Exception error) {
            return null;
        }
    }

    private MediaPreview() { }
}

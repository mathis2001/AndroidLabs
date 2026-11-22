package org.hidenseek.lab;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

final class MediaUploads {
    static int save(Context context, Uri source) throws Exception {
        context.getContentResolver().getType(Uri.parse("content://org.hidenseek.lab.vault/media"));
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = context.getContentResolver().openInputStream(source)) {
            if (input == null) throw new IOException("Image unavailable");
            BitmapFactory.decodeStream(input, null, bounds);
        }
        if (bounds.outWidth < 1 || bounds.outHeight < 1) throw new IOException("Unsupported image");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        while (Math.max(bounds.outWidth / options.inSampleSize,
                        bounds.outHeight / options.inSampleSize) > 1600) {
            options.inSampleSize *= 2;
        }
        Bitmap image;
        try (InputStream input = context.getContentResolver().openInputStream(source)) {
            if (input == null) throw new IOException("Image unavailable");
            image = BitmapFactory.decodeStream(input, null, options);
        }
        if (image == null) throw new IOException("Unsupported image");

        File folder = new File(context.getFilesDir(), "media-vault");
        if (!folder.exists() && !folder.mkdirs()) {
            image.recycle();
            throw new IOException("Media storage unavailable");
        }
        SharedPreferences index = context.getSharedPreferences("vault-index", 0);
        int slot = Math.max(7, index.getInt("next_upload_slot", 7));
        SecureRandom random = new SecureRandom();
        int id = -1;
        int start = random.nextInt(100);
        for (int attempt = 0; attempt < 100; attempt++) {
            int candidate = 1000 + (start + attempt) % 100;
            if (!new File(folder, candidate + ".png").exists()) {
                id = candidate;
                break;
            }
        }
        if (id < 0) {
            image.recycle();
            throw new IOException("Media storage is full");
        }

        File target = new File(folder, id + ".png");
        try (FileOutputStream output = new FileOutputStream(target)) {
            if (!image.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                throw new IOException("Could not save image");
            }
        } catch (Exception error) {
            target.delete();
            throw error;
        } finally {
            image.recycle();
        }
        index.edit().putInt("media_" + slot, id).putInt("next_upload_slot", slot + 1).apply();
        return slot;
    }

    private MediaUploads() { }
}

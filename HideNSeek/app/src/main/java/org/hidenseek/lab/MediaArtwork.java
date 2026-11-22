package org.hidenseek.lab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

final class MediaArtwork {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 640;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Canvas canvas;

    static void save(Context context, DemoData.Media media, File destination) throws Exception {
        if (!media.identity) {
            try (InputStream source = context.getAssets().open("photos/" + photoName(media.id));
                 FileOutputStream output = new FileOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = source.read(buffer)) != -1) output.write(buffer, 0, length);
            }
            return;
        }
        MediaArtwork artwork = new MediaArtwork();
        Bitmap image = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        artwork.canvas = new Canvas(image);
        artwork.identityCard();
        try (FileOutputStream output = new FileOutputStream(destination)) {
            if (!image.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                throw new java.io.IOException("Image encoding failed");
            }
        } finally {
            image.recycle();
        }
    }

    private static String photoName(int slot) {
        switch (slot) {
            case 0: return "mountain.png";
            case 1: return "concert.png";
            case 2: return "cafe.png";
            case 3: return "family.png";
            case 4: return "paella.png";
            case 6: return "flowers.png";
            default: throw new IllegalArgumentException("Unknown media");
        }
    }

    private void fill(int color) {
        canvas.drawColor(color);
    }

    private void round(float left, float top, float right, float bottom, float radius, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint);
    }

    private void text(String value, float x, float y, float size, int color, boolean bold) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(size);
        paint.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        canvas.drawText(value, x, y, paint);
    }

    private void circle(float x, float y, float radius, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, radius, paint);
    }

    private void identityCard() {
        fill(0xFFEBEDF3);
        round(42, 46, 958, 594, 31, Color.WHITE);
        round(42, 46, 958, 160, 31, 0xFF33436C);
        round(42, 129, 958, 160, 0, 0xFF33436C);
        text("CIVIC REPUBLIC OF NORTHSTAR", 81, 99, 32, Color.WHITE, true);
        text("FICTIONAL NATIONAL ID  •  CTF PROP", 82, 139, 20, 0xFFD9DFF3, false);

        round(78, 194, 330, 478, 17, 0xFFDDE3EE);
        circle(204, 280, 57, 0xFF9AA9C4);
        round(125, 348, 285, 455, 70, 0xFF9AA9C4);
        text("DEMO PORTRAIT", 100, 511, 18, 0xFF65728C, true);

        text("SURNAME", 373, 218, 18, 0xFF7E879B, true);
        text("MOREAU", 373, 259, 38, 0xFF293552, true);
        text("GIVEN NAME", 373, 307, 18, 0xFF7E879B, true);
        text("LENA", 373, 349, 38, 0xFF293552, true);
        text("DATE OF BIRTH", 373, 400, 18, 0xFF7E879B, true);
        text("18 JUN 1994", 373, 436, 31, 0xFF293552, true);
        text("DOCUMENT NO.", 373, 487, 18, 0xFF7E879B, true);
        text("NSR-2048-51", 373, 523, 30, 0xFF293552, true);
        text(decode(), 82, 565, 26, 0xFF725CB5, true);
        text("NOT VALID IDENTIFICATION", 610, 565, 18, 0xFFAD4861, true);
    }

    private String decode() {
        int[] encoded = {60,54,59,61,33,9,105,105,49,5,14,50,105,5,9,105,57,47,40,105,5,12,110,47,54,46,39};
        StringBuilder value = new StringBuilder();
        for (int number : encoded) value.append((char) (number ^ 0x5A));
        return value.toString();
    }
}

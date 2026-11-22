package org.hidenseek.lab;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.SecureRandom;
import java.util.HashSet;

public final class SecureImageVault extends ContentProvider {
    private static final int FIRST_ID = 1000;
    private static final int ID_COUNT = 100;
    private static final String AUTHORITY = "org.hidenseek.lab.vault";
    private File folder;

    @Override public boolean onCreate() {
        folder = new File(getContext().getFilesDir(), "media-vault");
        if (!folder.exists() && !folder.mkdirs()) return false;
        try {
            populate();
            return true;
        } catch (Exception error) {
            return false;
        }
    }

    private void populate() throws Exception {
        SharedPreferences preferences = getContext().getSharedPreferences("vault-index", 0);
        HashSet<Integer> used = new HashSet<>();
        SecureRandom random = new SecureRandom();
        SharedPreferences.Editor editor = preferences.edit();
        boolean refreshArtwork = preferences.getInt("artwork_version", 0) < 3;
        for (DemoData.Media item : DemoData.MEDIA) {
            String key = "media_" + item.id;
            int id = preferences.getInt(key, -1);
            if (id < FIRST_ID || id >= FIRST_ID + ID_COUNT || used.contains(id)) {
                do { id = FIRST_ID + random.nextInt(ID_COUNT); } while (used.contains(id));
                editor.putInt(key, id);
            }
            used.add(id);
            File target = new File(folder, id + ".png");
            if (refreshArtwork || !target.exists()) MediaArtwork.save(getContext(), item, target);
        }
        editor.putInt("artwork_version", 3);
        editor.apply();
    }

    private File resolve(Uri uri) throws FileNotFoundException {
        if (!AUTHORITY.equals(uri.getAuthority()) || uri.getPathSegments().size() != 2 ||
            !"media".equals(uri.getPathSegments().get(0))) throw new FileNotFoundException("Unknown item");
        final int id;
        try { id = Integer.parseInt(uri.getLastPathSegment()); }
        catch (NumberFormatException error) { throw new FileNotFoundException("Unknown item"); }
        if (id < FIRST_ID || id >= FIRST_ID + ID_COUNT) throw new FileNotFoundException("Unknown item");
        File file = new File(folder, id + ".png");
        if (!file.isFile()) throw new FileNotFoundException("Unknown item");
        return file;
    }

    @Override public String getType(Uri uri) {
        try { resolve(uri); return "image/png"; }
        catch (FileNotFoundException error) { return null; }
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) throw new FileNotFoundException("Read only");
        return ParcelFileDescriptor.open(resolve(uri), ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection,
                                  String[] selectionArgs, String sortOrder) {
        try {
            File file = resolve(uri);
            String[] columns = projection != null ? projection :
                new String[] { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };
            MatrixCursor cursor = new MatrixCursor(columns);
            Object[] row = new Object[columns.length];
            for (int i = 0; i < columns.length; i++) {
                if (OpenableColumns.DISPLAY_NAME.equals(columns[i])) row[i] = "private-image.png";
                else if (OpenableColumns.SIZE.equals(columns[i])) row[i] = file.length();
                else if ("_id".equals(columns[i])) row[i] = Integer.parseInt(uri.getLastPathSegment());
            }
            cursor.addRow(row);
            return cursor;
        } catch (FileNotFoundException error) {
            return null;
        }
    }

    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String selection, String[] args) { throw new UnsupportedOperationException(); }
}

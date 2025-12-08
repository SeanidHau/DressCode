package com.dresscode.app.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static File uriToFile(Context context, Uri uri) throws IOException {
        if ("file".equals(uri.getScheme())) {
            return new File(uri.getPath());
        }

        ContentResolver resolver = context.getContentResolver();
        String fileName = getFileName(resolver, uri);
        File cacheDir = context.getCacheDir();
        File file = new File(cacheDir, fileName);

        try (InputStream in = resolver.openInputStream(uri);
             FileOutputStream out = new FileOutputStream(file)) {
            if (in == null) throw new IOException("InputStream is null");
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }

        return file;
    }

    private static String getFileName(ContentResolver resolver, Uri uri) {
        String name = "temp_image";
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return name;
    }
}

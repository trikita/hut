package trikita.hut.apps;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import trikita.hut.AppsProvider;

public class Apps extends ContentProvider {

    public static final String AUTHORITY = "trikita.hut.apps";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/actions");

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/trikita.hut.actions";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> actions = pm.queryIntentActivities(launcherIntent, 0);
        Collections.sort(actions, new ResolveInfo.DisplayNameComparator(pm));

        MatrixCursor cursor = new MatrixCursor(AppsProvider.CURSOR_COLUMNS, actions.size());
        for (int i = 0; i < actions.size(); i++) {
            MatrixCursor.RowBuilder row = cursor.newRow();

            ResolveInfo info = actions.get(i);
            String component = info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.name;

            // id, icon, title, description
            row.add(component);
            Bitmap bitmap = drawableToBitmap(info.loadIcon(pm));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            row.add(baos.toByteArray());
            row.add(info.loadLabel(pm).toString());
            row.add(null);

            // Primary action: launch app (action, data, component, category, mime)
            row.add(Intent.ACTION_MAIN);
            row.add(null);
            row.add(component);
            row.add(Intent.CATEGORY_LAUNCHER);
            row.add(null);

            // Secondary action: open app details screen (action, data, component, category, mime)
            row.add(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            row.add("package:" + info.activityInfo.applicationInfo.packageName);
            row.add(null);
            row.add(null);
            row.add(null);
        }
        return cursor;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public String getType(Uri uri) {
        return CONTENT_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new RuntimeException("insert() is not supported: provider is read-only");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new RuntimeException("delete() is not supported: provider is read-only");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("update() is not supported: provider is read-only");
    }
}

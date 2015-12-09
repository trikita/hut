package trikita.hut.apps;

import android.content.ComponentName;
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
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import trikita.hut.ActionsProvider;

public class Apps extends ContentProvider {
    private static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/trikita.hut.actions";

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

        MatrixCursor cursor = new MatrixCursor(ActionsProvider.CURSOR_COLUMNS, actions.size());
        for (int i = 0; i < actions.size(); i++) {
            MatrixCursor.RowBuilder row = cursor.newRow();

            ResolveInfo info = actions.get(i);
            String component = info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.name;

            // id, icon, title, description
            row.add(component.hashCode());
            row.add(Uri.parse("android.resource://" + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.icon));
            row.add(info.loadLabel(pm).toString());
            row.add(null);

            // main action = launch app, settings action = open app details in settings
            row.add(new Intent(Intent.ACTION_MAIN)
                    .setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name))
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .toUri(0));
            row.add(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + info.activityInfo.applicationInfo.packageName))
                    .toUri(0));
        }
        return cursor;
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

package trikita.hut;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import trikita.hut.apps.Apps;

public class ActionsProvider {

    public static class ActionInfo {
        public final String id;
        public final Drawable icon;
		public final String title;
        public final String description;
		public final PendingIntent primaryAction;
		public final PendingIntent settingsAction;

        public ActionInfo(Context c, String id, Drawable icon, String title, String description, String actionUri, String settingsUri) throws URISyntaxException {
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.primaryAction = PendingIntent.getActivity(c, 0,
                    Intent.parseUri(actionUri, Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            this.settingsAction = PendingIntent.getActivity(c, 0,
                    Intent.parseUri(settingsUri, Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    public final static String COLUMN_ID = "id";
    public final static String COLUMN_ICON = "icon";
    public final static String COLUMN_TITLE = "title";
    public final static String COLUMN_DESCRIPTION = "description";

    public final static String COLUMN_ACTION = "action";
    public final static String COLUMN_SETTINGS = "settings";

    public final static String[] CURSOR_COLUMNS = new String[]{
            COLUMN_ID, COLUMN_ICON, COLUMN_TITLE, COLUMN_DESCRIPTION,
            COLUMN_ACTION, COLUMN_SETTINGS
    };

    private final static String PREFS_BLACKLIST = "blacklist";

    private Context mContext;
    private List<ActionInfo> mCache = null;

    public ActionsProvider(Context context) {
        mContext = context;
    }

	public synchronized void refresh() {
        mCache = getActions(mContext, Apps.CONTENT_URI);
	}

    public void blacklist(ActionInfo action, boolean state) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(PREFS_BLACKLIST, 0).edit();
        if (state) {
            editor.putBoolean(action.id, true);
        } else {
            editor.remove(action.id);
        }
        editor.apply();
    }

    public boolean isBlacklisted(ActionInfo action) {
        return mContext.getSharedPreferences(PREFS_BLACKLIST, 0).getBoolean(action.id, false);
    }

    public synchronized List<ActionInfo> getAll() {
        if (mCache == null) {
            refresh();
        }
		return mCache;
	}

    public synchronized List<ActionInfo> getWhitelisted() {
        List<ActionInfo> filtered = new ArrayList<>();
        for (ActionInfo app : getAll()) {
            if (!isBlacklisted(app)) {
                filtered.add(app);
            }
        }
        return filtered;
    }

    private List<ActionInfo> getActions(Context context, Uri uri) {
        ArrayList<ActionInfo> actions = new ArrayList<>();
        Cursor c = context.getContentResolver().query(uri, CURSOR_COLUMNS, null, null, null);
        if (c == null) {
            return actions;
        }
        c.moveToFirst();
        while (c.moveToNext()) {
            try {
                ActionInfo action = new ActionInfo(context,
                        c.getString(c.getColumnIndex(COLUMN_ID)),
                        scaleIcon(context, c.getBlob(c.getColumnIndex(COLUMN_ICON))),
                        c.getString(c.getColumnIndex(COLUMN_TITLE)),
                        c.getString(c.getColumnIndex(COLUMN_DESCRIPTION)),
                        c.getString(c.getColumnIndex(COLUMN_ACTION)),
                        c.getString(c.getColumnIndex(COLUMN_SETTINGS)));
                actions.add(action);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        c.close();
        return actions;
    }

    private Drawable scaleIcon(Context c, byte[] bytes) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Drawable icon = new BitmapDrawable(c.getResources(), bmp);
        Rect mOldBounds = new Rect();
        int iconSize = (int)
                c.getResources().getDimension(android.R.dimen.app_icon_size);
        int iconWidth = icon.getIntrinsicWidth();
        int iconHeight = icon.getIntrinsicHeight();

        if (iconSize > 0 && (iconSize < iconWidth || iconSize < iconHeight)) {
            float ratio = (float) iconWidth / iconHeight;

            if (iconWidth > iconHeight) {
                iconHeight = (int) (iconSize / ratio);
            } else if (iconHeight > iconWidth) {
                iconWidth = (int) (iconSize * ratio);
            }

            Bitmap.Config bc = icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            bmp = Bitmap.createBitmap(iconWidth, iconHeight, bc);
            Canvas canvas = new Canvas(bmp);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));

            mOldBounds.set(icon.getBounds());
            icon.setBounds(0, 0, iconWidth, iconHeight);
            icon.draw(canvas);
            icon.setBounds(mOldBounds);
            icon = new BitmapDrawable(c.getResources(), bmp);
        }
        return icon;
    }
}


package trikita.hut;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
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
import android.util.Base64;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActionsProvider {

    public static class ActionInfo {
        public final String id;
        public final Drawable icon;
		public final String title;
        public final String description;
		public final String actionUri;
		public final String settingsUri;

        public ActionInfo(String id, Drawable icon, String title, String description, String actionUri, String settingsUri) {
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.actionUri = actionUri;
            this.settingsUri = settingsUri;
        }
        public void run(Activity a) {
            run(a, actionUri);
        }
        public void settings(Activity a) {
            run(a, settingsUri);
        }
        private void run(Activity a, String uri) {
            if (uri != null) {
                try {
                    a.startActivity(Intent.parseUri(uri, 0)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final static String COLUMN_ID = "id";
    private final static String COLUMN_ICON = "icon";
    private final static String COLUMN_TITLE = "title";
    private final static String COLUMN_DESCRIPTION = "description";

    private final static String COLUMN_ACTION = "action";
    private final static String COLUMN_SETTINGS = "settings";

    public final static String[] CURSOR_COLUMNS = new String[]{
            COLUMN_ID, COLUMN_ICON, COLUMN_TITLE, COLUMN_DESCRIPTION,
            COLUMN_ACTION, COLUMN_SETTINGS
    };

    public final static String AUTHORITY_PREFIX = "trikita.hut.";

    private final static String PREFS_BLACKLIST = "blacklist";
    private final static String PREFS_SHORTCUTS = "shortcuts";

    public final static String SHORTCUT_SWIPE_LEFT = "swipe-left";
    public final static String SHORTCUT_SWIPE_RIGHT = "swipe-right";
    public final static String SHORTCUT_SWIPE_UP = "swipe-up";
    public final static String SHORTCUT_SWIPE_DOWN = "swipe-down";
    public final static String SHORTCUT_TAP = "tap";

    private final Context mContext;
    private List<ActionInfo> mCache = null;

    public ActionsProvider(Context context) {
        mContext = context;
    }

	public synchronized void refresh() {
        List<ActionInfo> actions = new ArrayList<>();
        for (PackageInfo pack : mContext.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
            ProviderInfo[] providers = pack.providers;
            if (providers != null) {
                for (ProviderInfo provider : providers) {
                    if (provider.authority.startsWith(AUTHORITY_PREFIX)) {
                        actions.addAll(getActions(mContext, Uri.parse("content://" + provider.authority + "/actions")));
                    }
                }
            }
        }
        Collections.sort(actions, new Comparator<ActionInfo>() {
            public int compare(ActionInfo lhs, ActionInfo rhs) {
                return notNull(lhs.title).compareTo(notNull(rhs.title));
            }
            private String notNull(String s) {
                return s == null ? "" : s;
            }
        });
        mCache = actions;
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

    public synchronized List<ActionInfo> getShortcutActions() {
        List<ActionInfo> shortcuts = new ArrayList<>();
        shortcuts.addAll(getAll());
        shortcuts.add(0, new ActionInfo("trikita.hut/trikita.hut.do_nothing",
                null,
                "Do nothing",
                null,
                null,
                null));
        return shortcuts;
    }

    public void setShortcut(String shortcut, String actionUri) {
        mContext.getSharedPreferences(PREFS_SHORTCUTS, 0).edit()
                .putString(shortcut, actionUri)
                .apply();
    }

    public ActionInfo getShortcut(String shortcut) {
        String uri = mContext.getSharedPreferences(PREFS_SHORTCUTS, 0).getString(shortcut, null);
        return new ActionInfo(null, null, null, null, uri, null);
    }

    private List<ActionInfo> getActions(Context context, Uri uri) {
        ArrayList<ActionInfo> actions = new ArrayList<>();
        Cursor c = context.getContentResolver().query(uri, CURSOR_COLUMNS, null, null, null);
        if (c == null) {
            return actions;
        }
        c.moveToFirst();
        while (c.moveToNext()) {
            ActionInfo action = new ActionInfo(c.getString(c.getColumnIndex(COLUMN_ID)),
                    scaleIcon(context, Base64.decode(c.getString(c.getColumnIndex(COLUMN_ICON)), 0)),
                    c.getString(c.getColumnIndex(COLUMN_TITLE)),
                    c.getString(c.getColumnIndex(COLUMN_DESCRIPTION)),
                    c.getString(c.getColumnIndex(COLUMN_ACTION)),
                    c.getString(c.getColumnIndex(COLUMN_SETTINGS)));
            actions.add(action);
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


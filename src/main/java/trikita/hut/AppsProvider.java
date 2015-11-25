package trikita.hut;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

import trikita.hut.apps.Apps;

public class AppsProvider {

	public static class ActionInfo {
        public final String id;
        public final Drawable icon;
		public final String title;
        public final String description;
		public final PendingIntent primaryAction;
		public final PendingIntent settingsAction;

        public ActionInfo(String id, Drawable icon, String title, String description, PendingIntent primaryAction, PendingIntent settingsAction) {
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.primaryAction = primaryAction;
            this.settingsAction = settingsAction;
        }
    }

    public final static String COLUMN_ID = "id";
    public final static String COLUMN_ICON = "icon";
    public final static String COLUMN_TITLE = "title";
    public final static String COLUMN_DESCRIPTION = "description";

    public final static String COLUMN_ACTION = "action";
    public final static String COLUMN_DATA = "data";
    public final static String COLUMN_COMPONENT = "component";
    public final static String COLUMN_CATEGORY = "category";
    public final static String COLUMN_MIME = "mime";

    public final static String COLUMN_SECONDARY_ACTION = "action2";
    public final static String COLUMN_SECONDARY_DATA = "data2";
    public final static String COLUMN_SECONDARY_COMPONENT = "component2";
    public final static String COLUMN_SECONDARY_CATEGORY = "category2";
    public final static String COLUMN_SECONDARY_MIME = "mime2";

    public final static String[] CURSOR_COLUMNS = new String[] {
            COLUMN_ID, COLUMN_ICON, COLUMN_TITLE, COLUMN_DESCRIPTION,
            COLUMN_ACTION, COLUMN_DATA, COLUMN_COMPONENT, COLUMN_CATEGORY, COLUMN_MIME,
            COLUMN_SECONDARY_ACTION, COLUMN_SECONDARY_DATA, COLUMN_SECONDARY_COMPONENT,
            COLUMN_SECONDARY_CATEGORY, COLUMN_SECONDARY_MIME,
    };

	private List<ActionInfo> mCache = new ArrayList<>();

	public AppsProvider update(Context c) {
		mCache.clear();
        mCache.addAll(getActions(c, Apps.CONTENT_URI));
		return this;
	}

    private List<ActionInfo> getActions(Context context, Uri uri) {
        ArrayList<ActionInfo> actions = new ArrayList<>();
        Cursor c = context.getContentResolver().query(uri, CURSOR_COLUMNS, null, null, null);
        if (c == null || c.getCount() == 0) {
            return actions;
        }
        c.moveToFirst();
        while (c.moveToNext()) {
            ActionInfo action = new ActionInfo(
                    c.getString(c.getColumnIndex(COLUMN_ID)),
                    scaleIcon(context, c.getBlob(c.getColumnIndex(COLUMN_ICON))),
                    c.getString(c.getColumnIndex(COLUMN_TITLE)),
                    c.getString(c.getColumnIndex(COLUMN_DESCRIPTION)),
                    pendingIntent(context, c.getString(c.getColumnIndex(COLUMN_ACTION)), c.getString(c.getColumnIndex(COLUMN_DATA)),
                            c.getString(c.getColumnIndex(COLUMN_COMPONENT)), c.getString(c.getColumnIndex(COLUMN_CATEGORY)),
                            c.getString(c.getColumnIndex(COLUMN_MIME))),
                    pendingIntent(context, c.getString(c.getColumnIndex(COLUMN_SECONDARY_ACTION)),
                            c.getString(c.getColumnIndex(COLUMN_SECONDARY_DATA)),
                            c.getString(c.getColumnIndex(COLUMN_SECONDARY_COMPONENT)),
                            c.getString(c.getColumnIndex(COLUMN_SECONDARY_CATEGORY)),
                            c.getString(c.getColumnIndex(COLUMN_SECONDARY_MIME))));
            actions.add(action);
        }
        return actions;
    }

    private PendingIntent pendingIntent(Context c, String action, String data, String component, String category, String mime) {
        if (action == null) {
            return null;
        }
        Intent intent = new Intent(action);
        if (data != null) {
            intent.setData(Uri.parse(data));
        }
        if (component != null) {
            String[] parts = component.split("/", 2);
            intent.setComponent(new ComponentName(parts[0], parts[1]));
        }
        if (category != null) {
            intent.addCategory(category);
        }
        if (mime != null) {
            intent.setType(mime);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

	public List<ActionInfo> apps() {
		return mCache;
	}
}


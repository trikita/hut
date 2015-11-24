package trikita.hut;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppsProvider {

	public static class AppInfo {
		public String title;
		public Drawable icon;
		public String id;
		public PendingIntent primaryAction;
		public PendingIntent settingsAction;
	}

	private Context mContext;
	private List<AppInfo> mCache = new ArrayList<>();

	public AppsProvider update(Context c) {
		mContext = c;
		Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null)
			.addCategory(Intent.CATEGORY_LAUNCHER);

		PackageManager pm = c.getPackageManager();
		List<ResolveInfo> apps = pm.queryIntentActivities(launcherIntent, 0);
		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm));

		mCache.clear();
		for (int i = 0; i < apps.size(); i++) {
			ResolveInfo info = apps.get(i);
			String pkgName = info.activityInfo.applicationInfo.packageName;
			Intent intent = new Intent(Intent.ACTION_MAIN)
				.addCategory(Intent.CATEGORY_LAUNCHER)
				.setComponent(new ComponentName(pkgName, info.activityInfo.name))
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			AppInfo app = new AppInfo();
			app.id = pkgName + "/" + info.activityInfo.name;
			app.title = info.loadLabel(pm).toString();
			app.primaryAction = PendingIntent.getActivity(c, 0, intent,
					Intent.FLAG_ACTIVITY_NEW_TASK);
			app.settingsAction = PendingIntent.getActivity(c, 0,
					new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
					.setData(Uri.parse("package:" + pkgName)),
					Intent.FLAG_ACTIVITY_NEW_TASK);
			app.icon = scaleIcon(c, info.activityInfo.loadIcon(pm));
			mCache.add(app);
		}
		return this;
	}

	private Drawable scaleIcon(Context c, Drawable icon) {
		Rect mOldBounds = new Rect();
		int iconSize = (int)
			c.getResources().getDimension(android.R.dimen.app_icon_size);
		int iconWidth = icon.getIntrinsicWidth();
		int iconHeight = icon.getIntrinsicHeight();
		if (icon instanceof PaintDrawable) {
			PaintDrawable painter = (PaintDrawable) icon;
			painter.setIntrinsicWidth(iconSize);
			painter.setIntrinsicHeight(iconSize);
		}

		if (iconSize > 0 && (iconSize < iconWidth || iconSize < iconHeight)) {
			float ratio = (float) iconWidth / iconHeight;

			if (iconWidth > iconHeight) {
				iconHeight = (int) (iconSize / ratio);
			} else if (iconHeight > iconWidth) {
				iconWidth = (int) (iconSize * ratio);
			}

			Bitmap.Config bc = icon.getOpacity() != PixelFormat.OPAQUE ?
				Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
			Bitmap bmp = Bitmap.createBitmap(iconWidth, iconHeight, bc);
			Canvas canvas = new Canvas(bmp);
			canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));

			mOldBounds.set(icon.getBounds());
			icon.setBounds(0, 0, iconWidth, iconHeight);
			icon.draw(canvas);
			icon.setBounds(mOldBounds);
			icon = new BitmapDrawable(bmp);
		}
		return icon;
	}

	public List<AppInfo> apps() {
		return mCache;
	}
}


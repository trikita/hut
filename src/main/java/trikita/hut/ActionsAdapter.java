package trikita.hut;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import android.os.Handler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ActionsAdapter extends SimpleCursorAdapter {
    private static String[] COLUMNS = new String[]{
            ActionsProvider.COLUMN_ICON, ActionsProvider.COLUMN_TITLE, ActionsProvider.COLUMN_ID
    };
    private static int[] VIEWS = new int[]{R.id.icon, R.id.label, R.id.check};

    private static Map<String, Drawable> mCache = new HashMap<>();
    private static Drawable mMissingIcon = null;

    public ActionsAdapter(Context c, final ActionsProvider.Category category, final ViewBinder viewBinder) {
        super(c, R.layout.item, App.actions().query(category, ""), COLUMNS, VIEWS);
        if (mMissingIcon == null) {
            mMissingIcon = c.getPackageManager().getDefaultActivityIcon();
        }
        setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return App.actions().query(category, constraint.toString());
            }
        });
        setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
                if (view instanceof ImageView) {
                    final ImageView imageView = (ImageView) view;
                    final String uri = cursor.getString(columnIndex);
                    Drawable d = mCache.get(uri);
                    if (d != null) {
                        imageView.setImageDrawable(d);
                    } else {
                        imageView.setImageDrawable(null);
                        imageView.post(new Runnable() {
                            public void run() {
                                try {
                                    Drawable drawable = null;
                                    if (uri != null) {
                                        if (uri.startsWith("data:image/png;base64,")) {
                                            String b64 = uri.substring(uri.indexOf(",") + 1);
                                            byte[] raw = Base64.decode(b64, 0);
                                            drawable = new BitmapDrawable(BitmapFactory.decodeByteArray(raw, 0, raw.length));
                                        } else {
                                            InputStream inputStream = view.getContext().getContentResolver().openInputStream(Uri.parse(uri));
                                            drawable = Drawable.createFromStream(inputStream, uri);
                                        }
                                    }
                                    if (drawable != null) {
                                        mCache.put(uri, drawable);
                                    } else {
                                        mCache.put(uri, mMissingIcon);
                                    }
                                    notifyDataSetChanged();
                                } catch (FileNotFoundException e) {
                                    mCache.put(uri, mMissingIcon);
                                }
                            }
                        });
                    }
                    return true;
                }
                return viewBinder != null && viewBinder.setViewValue(view, cursor, columnIndex);
            }
        });
    }
}

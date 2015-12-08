package trikita.hut;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import android.os.Handler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ActionsAdapter {
    private static String[] COLUMNS = new String[] {
            ActionsProvider.COLUMN_ICON, ActionsProvider.COLUMN_TITLE, ActionsProvider.COLUMN_ID
    };
    private static int[] VIEWS = new int[]{R.id.icon, R.id.label, R.id.check};

    private static Map<String, Drawable> mCache = new HashMap<>();

    public static SimpleCursorAdapter create(Context c, final ActionsProvider.Category category,
                                      final SimpleCursorAdapter.ViewBinder viewBinder) {
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(c, R.layout.item,
                App.actions().query(category, ""), COLUMNS, VIEWS);
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return App.actions().query(category, constraint.toString());
            }
        });
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
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
                                    InputStream inputStream = view.getContext().getContentResolver().openInputStream(Uri.parse(uri));
                                    Drawable drawable = Drawable.createFromStream(inputStream, uri);
                                    mCache.put(uri, drawable);
                                    adapter.notifyDataSetChanged();
                                } catch (FileNotFoundException e) {
                                }
                            }
                        });
                    }
                    return true;
                }
                if (viewBinder != null) {
                    return viewBinder.setViewValue(view, cursor, columnIndex);
                } else {
                    return false;
                }
            }
        });
        return adapter;
    }
}

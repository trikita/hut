package trikita.hut;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

public class ActionsAdapter {
    private static String[] COLUMNS = new String[] {
            ActionsProvider.COLUMN_ICON, ActionsProvider.COLUMN_TITLE, ActionsProvider.COLUMN_ID
    };
    private static int[] VIEWS = new int[]{R.id.icon, R.id.label, R.id.check};

    public static SimpleCursorAdapter create(Context c, final ActionsProvider.Category category,
                                      SimpleCursorAdapter.ViewBinder viewBinder) {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(c, R.layout.item,
                App.actions().query(category, ""), COLUMNS, VIEWS);
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return App.actions().query(category, constraint.toString());
            }
        });
        if (viewBinder != null) {
            adapter.setViewBinder(viewBinder);
        }
        return adapter;
    }
}

package trikita.hut;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class PickerActivity extends Activity {

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.drawer);

		GridView actionsView = ((GridView) findViewById(R.id.list));

		String action = getIntent().getAction();
		final SimpleCursorAdapter adapter;
		switch (action) {
			case "trikita.hut.intent.action.BLACKLIST":
				adapter = new ActionsAdapter(this, ActionsProvider.Category.ALL, new SimpleCursorAdapter.ViewBinder() {
					public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
						if (columnIndex == cursor.getColumnIndex(ActionsProvider.COLUMN_ID)) {
							CheckBox checkBox = (CheckBox) view;
							checkBox.setVisibility(View.VISIBLE);
							checkBox.setChecked(!App.actions()
									.isBlacklisted(cursor.getLong(columnIndex)));
							return true;
						}
						return false;
					}
				});
				actionsView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
						System.out.println("onItemClick: " + adapter.getItemId(pos));
						// TODO:

						App.actions().blacklist(adapter.getItemId(pos),
								!App.actions().isBlacklisted(adapter.getItemId(pos)));
						adapter.notifyDataSetChanged();
					}
				});
				break;
			case "trikita.hut.intent.action.PICK":
				adapter = new ActionsAdapter(this, ActionsProvider.Category.SHORTCUTS, null);
				actionsView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
						Cursor cursor = (Cursor) av.getAdapter().getItem(pos);
						App.actions().setShortcut(getIntent().getStringExtra("trikita.hut.intent.extra.SHORTCUT"),
								cursor.getString(cursor.getColumnIndex(ActionsProvider.COLUMN_ACTION)));
						finish();
					}
				});
				break;
			default:
				finish();
				return;
		}
		actionsView.setAdapter(adapter);

		EditText editText = ((EditText) findViewById(R.id.filter));
		editText.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
			public void afterTextChanged(Editable e) {}
			public void onTextChanged(CharSequence s, int start, int before, int n) {
				adapter.getFilter().filter(s);
			}
		});
	}
}


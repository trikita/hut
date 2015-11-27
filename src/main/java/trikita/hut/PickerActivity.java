package trikita.hut;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;

import java.util.HashSet;
import java.util.Set;

public class PickerActivity extends Activity {

	private ActionsProvider mActionsProvider = new ActionsProvider(this);
	private ActionsAdapter mActionsAdapter;

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.drawer);

		AbsListView listView = ((AbsListView) findViewById(R.id.list));

		String action = getIntent().getAction();
		switch (action) {
			case "trikita.hut.intent.action.BLACKLIST":
				mActionsAdapter = new ActionsAdapter(App.actions().getAll(), true);
				listView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
						ActionsAdapter.ViewHolder h = (ActionsAdapter.ViewHolder) v.getTag();
						ActionsProvider.ActionInfo info = mActionsAdapter.getItem(pos);
						h.checkBox.toggle();
						App.actions().blacklist(info, !h.checkBox.isChecked());
					}
				});
				break;
			case "trikita.hut.intent.action.PICK":
				mActionsAdapter = new ActionsAdapter(App.actions().getShortcutActions(), false);
				listView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
						ActionsProvider.ActionInfo info = mActionsAdapter.getItem(pos);
						System.out.println("Selected " + info.actionUri);
						App.actions().setShortcut(getIntent().getStringExtra("trikita.hut.intent.extra.SHORTCUT"),
								info.actionUri);
						finish();
					}
				});
				break;
			default:
				finish();
				return;
		}

		listView.setAdapter(mActionsAdapter);

		EditText editText = ((EditText) findViewById(R.id.filter));
		editText.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
			public void afterTextChanged(Editable e) {}
			public void onTextChanged(CharSequence s, int start, int before, int n) {
				mActionsAdapter.getFilter().filter(s);
			}
		});
	}
}


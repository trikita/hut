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
	private AppsAdapter mAppsAdapter;
	private AppsProvider mAppsProvider;
	private Set<String> mBlacklisted = new HashSet<>();

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.drawer);

		String action = getIntent().getAction();

		// If action is trikita.hut.intent.action.PICK - then add builtin actions
		mAppsProvider = new AppsProvider().update(this);
		mAppsAdapter = new AppsAdapter(mAppsProvider.apps(),
				action.equals("trikita.hut.intent.action.BLACKLIST"));
		AbsListView listView = ((AbsListView) findViewById(R.id.list));
		listView.setAdapter(mAppsAdapter);
		SharedPreferences prefs =
			PreferenceManager.getDefaultSharedPreferences(this);

		mBlacklisted.addAll(prefs.getStringSet("blacklist", new HashSet<String>()));

		listView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				System.out.println("Clicked on " + pos);
				AppsAdapter.ViewHolder h = (AppsAdapter.ViewHolder) v.getTag();
				AppsProvider.ActionInfo info = mAppsAdapter.getItem(pos);
				h.checkBox.toggle();
				if (h.checkBox.isChecked()) {
					mBlacklisted.remove(info.id);
				} else {
					mBlacklisted.add(info.id);
				}
				PreferenceManager.getDefaultSharedPreferences(PickerActivity.this)
					.edit()
					.putStringSet("blacklist", mBlacklisted)
					.apply();
			}
		});

		EditText editText = ((EditText) findViewById(R.id.filter));
		editText.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
			public void afterTextChanged(Editable e) {}
			public void onTextChanged(CharSequence s, int start, int before, int n) {
				mAppsAdapter.getFilter().filter(s);
			}
		});
	}
}


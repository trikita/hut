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

		String action = getIntent().getAction();

		// TODO: If action is trikita.hut.intent.action.PICK - then add builtin actions
		mActionsAdapter = new ActionsAdapter(App.actions().getAll(),
				action.equals("trikita.hut.intent.action.BLACKLIST"));
		AbsListView listView = ((AbsListView) findViewById(R.id.list));
		listView.setAdapter(mActionsAdapter);
		listView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				System.out.println("Clicked on " + pos);
				ActionsAdapter.ViewHolder h = (ActionsAdapter.ViewHolder) v.getTag();
				ActionsProvider.ActionInfo info = mActionsAdapter.getItem(pos);
				h.checkBox.toggle();
				App.actions().blacklist(info, !h.checkBox.isChecked());
			}
		});

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


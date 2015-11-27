package trikita.hut;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

public class PickerActivity extends Activity {
	private ActionsAdapter mActionsAdapter;

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.drawer);

		GridView actionsView = ((GridView) findViewById(R.id.list));

		String action = getIntent().getAction();
		switch (action) {
			case "trikita.hut.intent.action.BLACKLIST":
				mActionsAdapter = new ActionsAdapter(App.actions().getAll(), true);
				actionsView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
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
				actionsView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
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

		actionsView.setAdapter(mActionsAdapter);

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


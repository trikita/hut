package trikita.hut;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import butterknife.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LauncherActivity extends Activity {

	@Bind(R.id.background) View mBackgroundView;
	@Bind(R.id.drawer) View mDrawerView;
	@Bind(R.id.btn_apps) View mDrawerButton;
	@Bind(R.id.list) GridView mAppsListView;
	@Bind(R.id.filter) EditText mAppsFilter;

	private boolean mDrawerShown = false;
	private AppsProvider mAppsProvider;
	private AppsAdapter mAppsAdapter;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.main);
		ButterKnife.bind(this);

		mAppsProvider = new AppsProvider().update(this);
		mAppsAdapter = new AppsAdapter(mAppsProvider.apps(), false);
		mAppsListView.setAdapter(mAppsAdapter);
		mDrawerView.setVisibility(View.GONE);
	}

	@OnClick(R.id.btn_apps)
	public void openDrawer() {
		mAppsFilter.setText("");
		mAppsAdapter = new AppsAdapter(whitelist(mAppsProvider.apps()), false);
		mAppsListView.setAdapter(mAppsAdapter);
		mDrawerView.setVisibility(View.VISIBLE);
		mDrawerButton.setVisibility(View.GONE);
		mAppsFilter.setVisibility(View.GONE);
		mDrawerShown = true;
	}

	private List<AppsProvider.ActionInfo> whitelist(List<AppsProvider.ActionInfo> list) {
		List<AppsProvider.ActionInfo> filtered = new ArrayList<>();
		Set<String> set = PreferenceManager.getDefaultSharedPreferences(this)
			.getStringSet("blacklist", new HashSet<String>());
		for (AppsProvider.ActionInfo app : list) {
			if (set.contains(app.id) == false) {
				filtered.add(app);
			}
		}
		return filtered;
	}

	@OnLongClick(R.id.btn_apps)
	public boolean openDrawerWithFilter() {
		mAppsAdapter = new AppsAdapter(mAppsProvider.apps(), false);
		mAppsListView.setAdapter(mAppsAdapter);
		mDrawerView.setVisibility(View.VISIBLE);
		mDrawerButton.setVisibility(View.GONE);
		mAppsFilter.setText("");
		mAppsFilter.setVisibility(View.VISIBLE);
		mAppsFilter.requestFocus();
		mDrawerShown = true;
		return true;
	}

	@OnLongClick(R.id.background)
	public boolean openSettings() {
		startActivity(new Intent(this, SettingsActivity.class));
		return true;
	}

	@OnItemClick(R.id.list)
	public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		try {
			mAppsAdapter.getItem(pos).primaryAction.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}

	@OnItemLongClick(R.id.list)
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		try {
			mAppsAdapter.getItem(pos).settingsAction.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
		return true;
	}

	@OnTextChanged(R.id.filter)
	public void onFilterChanged(CharSequence s, int start, int before, int count) {
		mAppsAdapter.getFilter().filter(s.toString());
	}

	@Override
	public void onBackPressed() {
		if (mDrawerShown) {
			mDrawerShown = false;
			mDrawerView.setVisibility(View.GONE);
			mDrawerButton.setVisibility(View.VISIBLE);
		} else {
			super.onBackPressed();
		}
	}
}

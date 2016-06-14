package trikita.hut;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;

import java.net.URISyntaxException;
import java.util.Observable;
import java.util.Observer;

import butterknife.*;

public class LauncherActivity extends Activity implements Observer {

	@BindView(R.id.background) View mBackground;
	@BindView(R.id.drawer) View mDrawerView;
	@BindView(R.id.btn_apps) View mDrawerButton;
	@BindView(R.id.list) GridView mAppsListView;
	@BindView(R.id.filter) EditText mAppsFilter;

	private boolean mDrawerShown = false;
	private ActionsProvider.Category mOpenedCategory = null;
	private GestureDetector mGestureDetector;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
		setContentView(R.layout.main);
		ButterKnife.bind(this);

		mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

			private static final int MAX_VELOCITY_RATIO = 3;
			private static final int MIN_SWIPE_DISTANCE = 100;

			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				int dx = (int) (e2.getX() - e1.getX());
				int dy = (int) (e2.getY() - e1.getY());
				if (Math.abs(dx) > MIN_SWIPE_DISTANCE && Math.abs(velocityX) > MAX_VELOCITY_RATIO*Math.abs(velocityY)) {
					if (velocityX > 0) {
						run(App.actions().getShortcutUri(ActionsProvider.SHORTCUT_SWIPE_RIGHT));
					} else {
						run(App.actions().getShortcutUri(ActionsProvider.SHORTCUT_SWIPE_LEFT));
					}
					return true;
				} else if (Math.abs(dy) > MIN_SWIPE_DISTANCE && Math.abs(velocityY) > MAX_VELOCITY_RATIO*Math.abs(velocityX)) {
					if (velocityY > 0) {
						run(App.actions().getShortcutUri(ActionsProvider.SHORTCUT_SWIPE_DOWN));
					} else {
						run(App.actions().getShortcutUri(ActionsProvider.SHORTCUT_SWIPE_UP));
					}
					return true;
				}
				return false;
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				run(App.actions().getShortcutUri(ActionsProvider.SHORTCUT_TAP));
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				Log.d("LauncherActivity", "onLongPress");
				startActivity(new Intent(LauncherActivity.this, SettingsActivity.class));
			}
		});

		mDrawerView.setVisibility(View.INVISIBLE);

		App.actions().addObserver(this);
	}

	@Override
	protected void onDestroy() {
		App.actions().deleteObserver(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		revealDrawer(false, true);
	}

	@OnClick(R.id.btn_apps)
	public void openDrawer() {
		mAppsFilter.setText("");
		mOpenedCategory = ActionsProvider.Category.FAVOURITES;
		mAppsListView.setAdapter(new ActionsAdapter(this, mOpenedCategory, null));
		mAppsFilter.setVisibility(View.GONE);
		revealDrawer(true, true);
	}

	@OnLongClick(R.id.btn_apps)
	public boolean openDrawerWithFilter() {
		mAppsFilter.setText("");
		mOpenedCategory = ActionsProvider.Category.ALL;
		mAppsListView.setAdapter(new ActionsAdapter(this, mOpenedCategory, null));
		mAppsFilter.setVisibility(View.VISIBLE);
		mAppsFilter.requestFocus();
		revealDrawer(true, true);
		showKeyboard();
		return true;
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.showSoftInput(mAppsFilter, InputMethodManager.SHOW_IMPLICIT);
	}

	private void hideKeyboard() {
		View v = getCurrentFocus();
		if (v != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}

	private void revealDrawer(boolean show, boolean animate) {
		int cx = (mDrawerButton.getLeft() + mDrawerButton.getRight()) / 2;
		int cy = (mDrawerButton.getTop() + mDrawerButton.getBottom()) / 2;
		int r = Math.max(mBackground.getWidth(), mBackground.getHeight());
		if (show && !mDrawerShown) {
			mDrawerView.setVisibility(View.VISIBLE);
			if (animate && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				Animator anim =
						ViewAnimationUtils.createCircularReveal(mDrawerView, cx, cy, 0, r);
				anim.start();
			}
		} else if (mDrawerShown) {
			if (animate && mDrawerView.isAttachedToWindow() &&
					android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				Animator anim =
						ViewAnimationUtils.createCircularReveal(mDrawerView, cx, cy, r, 0);
				anim.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator animation) {
						mDrawerView.setVisibility(View.GONE);
						super.onAnimationEnd(animation);
					}
				});
				anim.start();
			} else {
				mDrawerView.setVisibility(View.GONE);
			}
			hideKeyboard();
		} else {
			return;
		}
        mDrawerButton.setVisibility(show ? View.GONE : View.VISIBLE);
        mDrawerShown = show;
	}

	@OnTouch(R.id.background)
	public boolean onGesture(MotionEvent e) {
		return mGestureDetector.onTouchEvent(e);
	}

	@OnItemClick(R.id.list)
	public void onItemClick(final AdapterView<?> av, View v, final int pos, long id) {
		Cursor cursor = (Cursor) av.getAdapter().getItem(pos);
		LauncherActivity.this.run(cursor.getString(cursor.getColumnIndex(ActionsProvider.COLUMN_ACTION)));
	}

	@OnItemLongClick(R.id.list)
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		Cursor cursor = (Cursor) av.getAdapter().getItem(pos);
		run(cursor.getString(cursor.getColumnIndex(ActionsProvider.COLUMN_SETTINGS)));
		return true;
	}

	private void run(String intentUri) {
		if (intentUri != null) {
			try {
				startActivity(Intent.parseUri(intentUri, 0)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
				overridePendingTransition(0, 0);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	@OnTextChanged(R.id.filter)
	public void onFilterChanged(CharSequence s, int start, int before, int count) {
		Filterable filterable = ((Filterable) mAppsListView.getAdapter());
		if (filterable != null) {
			filterable.getFilter().filter(s.toString());
		}
	}

	@Override
	public void onBackPressed() {
		if (mDrawerShown) {
			revealDrawer(false, true);
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if (mOpenedCategory != null) {
			((SimpleCursorAdapter) mAppsListView.getAdapter()).changeCursor(App.actions().query(mOpenedCategory, ""));
		}
	}
}

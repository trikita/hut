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

import butterknife.*;

public class LauncherActivity extends Activity {

	@Bind(R.id.drawer) View mDrawerView;
	@Bind(R.id.btn_apps) View mDrawerButton;
	@Bind(R.id.list) GridView mAppsListView;
	@Bind(R.id.filter) EditText mAppsFilter;

	private boolean mDrawerShown = false;
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		revealDrawer(false, true);
	}

	@OnClick(R.id.btn_apps)
	public void openDrawer() {
		mAppsFilter.setText("");
		mAppsListView.setAdapter(ActionsAdapter.create(this, ActionsProvider.Category.FAVOURITES, null));
		mAppsFilter.setVisibility(View.GONE);
		revealDrawer(true, true);
	}

	@OnLongClick(R.id.btn_apps)
	public boolean openDrawerWithFilter() {
		mAppsFilter.setText("");
		mAppsListView.setAdapter(ActionsAdapter.create(this, ActionsProvider.Category.ALL, null));
		mAppsFilter.setVisibility(View.VISIBLE);
		mAppsFilter.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.showSoftInput(mAppsFilter, InputMethodManager.SHOW_IMPLICIT);
		revealDrawer(true, true);
		return true;
	}

	private void revealDrawer(boolean show, boolean animate) {
		int cx = (mDrawerButton.getLeft() + mDrawerButton.getRight()) / 2;
		int cy = (mDrawerButton.getTop() + mDrawerButton.getBottom()) / 2;
		int r = Math.max(mDrawerView.getWidth(), mDrawerView.getHeight());
		if (show) {
			mDrawerView.setVisibility(View.VISIBLE);
			if (animate && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				Animator anim =
						ViewAnimationUtils.createCircularReveal(mDrawerView, cx, cy, 0, r);
				anim.start();
			}
		} else {
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
		}
        mDrawerButton.setVisibility(show ? View.GONE : View.VISIBLE);
        mDrawerShown = show;
	}

	@OnTouch(R.id.background)
	public boolean onGesture(MotionEvent e) {
		return mGestureDetector.onTouchEvent(e);
	}

	@OnItemClick(R.id.list)
	public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		Cursor cursor = (Cursor) av.getAdapter().getItem(pos);
		run(cursor.getString(cursor.getColumnIndex(ActionsProvider.COLUMN_ACTION)));
		revealDrawer(false, false);
	}

	@OnItemLongClick(R.id.list)
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		Cursor cursor = (Cursor) av.getAdapter().getItem(pos);
		run(cursor.getString(cursor.getColumnIndex(ActionsProvider.COLUMN_SETTINGS)));
		revealDrawer(false, false);
		return true;
	}

	private void run(String intentUri) {
		if (intentUri != null) {
			try {
				startActivity(Intent.parseUri(intentUri, 0)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
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
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.d("LauncherActivity", "dispatchKeyEvent: " + event);
		if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
			if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled() && mDrawerShown) {
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onBackPressed() {
		if (mDrawerShown) {
			revealDrawer(false, true);
		}
	}
}

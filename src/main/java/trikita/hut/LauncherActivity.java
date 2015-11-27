package trikita.hut;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

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
						App.actions().getShortcut(ActionsProvider.SHORTCUT_SWIPE_RIGHT).run(LauncherActivity.this);
					} else {
						App.actions().getShortcut(ActionsProvider.SHORTCUT_SWIPE_LEFT).run(LauncherActivity.this);
					}
					return true;
				} else if (Math.abs(dy) > MIN_SWIPE_DISTANCE && Math.abs(velocityY) > MAX_VELOCITY_RATIO*Math.abs(velocityX)) {
					if (velocityY > 0) {
						App.actions().getShortcut(ActionsProvider.SHORTCUT_SWIPE_DOWN).run(LauncherActivity.this);
					} else {
						App.actions().getShortcut(ActionsProvider.SHORTCUT_SWIPE_UP).run(LauncherActivity.this);
					}
					return true;
				}
				return false;
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				App.actions().getShortcut(ActionsProvider.SHORTCUT_TAP).run(LauncherActivity.this);
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				Log.d("LauncherActivity", "onLongPress");
				startActivity(new Intent(LauncherActivity.this, SettingsActivity.class));
			}
		});

		mAppsListView.setAdapter(new ActionsAdapter(App.actions().getAll(), false));
		mDrawerView.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onResume() {
		revealDrawer(false);
		super.onResume();
	}

	@OnClick(R.id.btn_apps)
	public void openDrawer() {
		mAppsFilter.setText("");
		mAppsListView.setAdapter(new ActionsAdapter(App.actions().getWhitelisted(), false));
		mAppsFilter.setVisibility(View.GONE);
		revealDrawer(true);
	}

	@OnLongClick(R.id.btn_apps)
	public boolean openDrawerWithFilter() {
		mAppsListView.setAdapter(new ActionsAdapter(App.actions().getAll(), false));
		mAppsFilter.setText("");
		mAppsFilter.setVisibility(View.VISIBLE);
		mAppsFilter.requestFocus();
		revealDrawer(true);
		return true;
	}

	private void revealDrawer(boolean show) {
		int cx = (mDrawerButton.getLeft() + mDrawerButton.getRight()) / 2;
		int cy = (mDrawerButton.getTop() + mDrawerButton.getBottom()) / 2;
		int r = Math.max(mDrawerView.getWidth(), mDrawerView.getHeight());
		if (show) {
			mDrawerView.setVisibility(View.VISIBLE);
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				Animator anim =
						ViewAnimationUtils.createCircularReveal(mDrawerView, cx, cy, 0, r);
				anim.start();
			}
		} else {
			if (mDrawerView.isAttachedToWindow() &&
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
		((ActionsAdapter) av.getAdapter()).getItem(pos).run(this);
	}

	@OnItemLongClick(R.id.list)
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		((ActionsAdapter) av.getAdapter()).getItem(pos).settings(this);
		return true;
	}

	@OnTextChanged(R.id.filter)
	public void onFilterChanged(CharSequence s, int start, int before, int count) {
		((ActionsAdapter) mAppsListView.getAdapter()).getFilter().filter(s.toString());
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
			revealDrawer(false);
		}
	}
}

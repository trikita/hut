package trikita.hut;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import butterknife.*;

import java.util.ArrayList;
import java.util.List;

public class LauncherActivity extends Activity {

	@Bind(R.id.drawer) View mDrawerView;
	@Bind(R.id.btn_apps) View mDrawerButton;
	@Bind(R.id.list) GridView mAppsListView;
	@Bind(R.id.filter) EditText mAppsFilter;

	private boolean mDrawerShown = false;

	private ActionsAdapter mActionsAdapter;

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
				Log.d("LauncherActivity", "onDown");
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				int dx = (int) (e2.getX() - e1.getX());
				int dy = (int) (e2.getY() - e1.getY());
				if (Math.abs(dx) > MIN_SWIPE_DISTANCE && Math.abs(velocityX) > MAX_VELOCITY_RATIO*Math.abs(velocityY)) {
					if (velocityX > 0) {
						Log.d("LauncherActivity", "on swipe right");
					} else {
						Log.d("LauncherActivity", "on swipe left");
					}
					return true;
				} else if (Math.abs(dy) > MIN_SWIPE_DISTANCE && Math.abs(velocityY) > MAX_VELOCITY_RATIO*Math.abs(velocityX)) {
					if (velocityY > 0) {
						Log.d("LauncherActivity", "on swipe down");
					} else {
						Log.d("LauncherActivity", "on swipe up");
					}
					return true;
				}
				return false;
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				Log.d("LauncherActivity", "onSingleTapUp");
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				Log.d("LauncherActivity", "onLongPress");
				startActivity(new Intent(LauncherActivity.this, SettingsActivity.class));
			}
		});

		mActionsAdapter = new ActionsAdapter(App.actions().getAll(), false);
		mAppsListView.setAdapter(mActionsAdapter);
		mDrawerView.setVisibility(View.GONE);
	}

	@OnClick(R.id.btn_apps)
	public void openDrawer() {
		mAppsFilter.setText("");
		mActionsAdapter = new ActionsAdapter(App.actions().getWhitelisted(), false);
		mAppsListView.setAdapter(mActionsAdapter);
		mAppsFilter.setVisibility(View.GONE);
		revealDrawer(true);
	}

	@OnLongClick(R.id.btn_apps)
	public boolean openDrawerWithFilter() {
		mActionsAdapter = new ActionsAdapter(App.actions().getAll(), false);
		mAppsListView.setAdapter(mActionsAdapter);
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
			mDrawerView.setVisibility(show ? View.VISIBLE : View.GONE);
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				Animator anim =
						ViewAnimationUtils.createCircularReveal(mDrawerView, cx, cy, 0, r);
				anim.start();
			}
		} else {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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

	private GestureDetector mGestureDetector;

	@OnTouch(R.id.background)
	public boolean onGesture(MotionEvent e) {
		return mGestureDetector.onTouchEvent(e);
	}

	@OnItemClick(R.id.list)
	public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		try {
			mActionsAdapter.getItem(pos).primaryAction.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}

	@OnItemLongClick(R.id.list)
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		try {
			ActionsProvider.ActionInfo action = mActionsAdapter.getItem(pos);
			if (action.settingsAction != null) {
				action.settingsAction.send();
			}
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
		return true;
	}

	@OnTextChanged(R.id.filter)
	public void onFilterChanged(CharSequence s, int start, int before, int count) {
		mActionsAdapter.getFilter().filter(s.toString());
	}

	@Override
	public void onBackPressed() {
		if (mDrawerShown) {
			revealDrawer(false);
		} else {
			super.onBackPressed();
		}
	}
}

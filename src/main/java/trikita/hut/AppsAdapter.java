package trikita.hut;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AppsAdapter extends BaseAdapter implements Filterable {
	private List<AppsProvider.ActionInfo> mAllApps;
	private List<AppsProvider.ActionInfo> mFilteredAppList;

	private boolean mMultiCheck = false;

	private Filter mFilter = new Filter() {
		protected FilterResults performFiltering(CharSequence s) {
			FilterResults results = new FilterResults();
			List<AppsProvider.ActionInfo> filtered = new ArrayList<>();
			if (s != null) {
				s = s.toString().toLowerCase();
			}
			for (AppsProvider.ActionInfo app : mAllApps) {
				String title = app.title.toLowerCase();
				if (s == null || s.length() == 0 || title.indexOf(s.toString()) >= 0) {
					filtered.add(app);
				}
			}
			results.values = filtered;
			results.count = filtered.size();
			return results;
		}
		protected void publishResults(CharSequence s, FilterResults results) {
			mFilteredAppList = (List<AppsProvider.ActionInfo>) results.values;
			notifyDataSetInvalidated();
		}
	};

	public AppsAdapter(List<AppsProvider.ActionInfo> apps, boolean multi) {
		super();
		mMultiCheck = multi;
		mAllApps = apps;
		mFilteredAppList = apps;
	}

	public int getCount() {
		return mFilteredAppList.size();
	}

	public AppsProvider.ActionInfo getItem(int pos) {
		return mFilteredAppList.get(pos);
	}

	public long getItemId(int pos) {
		return pos;
	}

	public Filter getFilter() {
		return mFilter;
	}

	public View getView(int pos, View v, ViewGroup vg) {
		AppsProvider.ActionInfo info = this.getItem(pos);

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater)
				vg.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.item, vg, false);
			v.setTag(new ViewHolder((ImageView) v.findViewById(R.id.icon),
						(TextView) v.findViewById(R.id.label),
						(CheckBox) v.findViewById(R.id.check)));
		}

		ViewHolder h = (ViewHolder) v.getTag();
		h.imageView.setImageDrawable(info.icon);
		h.textView.setText(info.title);

		if (mMultiCheck == false) {
			h.checkBox.setVisibility(View.GONE);
		} else {
			if (PreferenceManager
					.getDefaultSharedPreferences(vg.getContext())
					.getStringSet("blacklist", new HashSet<String>())
					.contains(info.id)) {
				h.checkBox.setChecked(false);
			} else {
				h.checkBox.setChecked(true);
			}
		}
		return v;
	}

	public static class ViewHolder {
		public final ImageView imageView;
		public final TextView textView;
		public final CheckBox checkBox;
		public ViewHolder(ImageView img, TextView txt, CheckBox chk) {
			imageView = img;
			textView = txt;
			checkBox = chk;
		}
	}
}


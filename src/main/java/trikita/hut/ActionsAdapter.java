package trikita.hut;

import android.content.Context;
import android.net.Uri;
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
import java.util.List;

public class ActionsAdapter extends BaseAdapter implements Filterable {

	private final List<ActionsProvider.ActionInfo> mOriginalList;
	private List<ActionsProvider.ActionInfo> mFilteredList;

	private boolean mShowCheckboxes = false;

	private final Filter mFilter = new Filter() {
		protected FilterResults performFiltering(CharSequence s) {
			FilterResults results = new FilterResults();
			List<ActionsProvider.ActionInfo> filtered = new ArrayList<>();
			if (s != null) {
				s = s.toString().toLowerCase();
			}
			for (ActionsProvider.ActionInfo app : mOriginalList) {
				String title = app.title.toLowerCase();
				if (s == null || s.length() == 0 || title.contains(s.toString())) {
					filtered.add(app);
				}
			}
			results.values = filtered;
			results.count = filtered.size();
			return results;
		}
		protected void publishResults(CharSequence s, FilterResults results) {
			mFilteredList = (List<ActionsProvider.ActionInfo>) results.values;
			notifyDataSetInvalidated();
		}
	};

	public ActionsAdapter(List<ActionsProvider.ActionInfo> actions, boolean showCheckboxes) {
		super();
		mShowCheckboxes = showCheckboxes;
		mOriginalList = actions;
		mFilteredList = mOriginalList;
	}

	public int getCount() {
		return mFilteredList.size();
	}

	public ActionsProvider.ActionInfo getItem(int pos) {
		return mFilteredList.get(pos);
	}

	public long getItemId(int pos) {
		return pos;
	}

	public Filter getFilter() {
		return mFilter;
	}

	public View getView(int pos, View v, ViewGroup vg) {
		ActionsProvider.ActionInfo info = getItem(pos);

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater)
				vg.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.item, vg, false);
			v.setTag(new ViewHolder((ImageView) v.findViewById(R.id.icon),
					(TextView) v.findViewById(R.id.label),
					(CheckBox) v.findViewById(R.id.check)));
		}

		ViewHolder h = (ViewHolder) v.getTag();
		try {
			h.imageView.setImageURI(Uri.parse(info.iconUri));
		} catch (Exception e) {}
		h.textView.setText(info.title);

		if (mShowCheckboxes) {
			h.checkBox.setChecked(!App.actions().isBlacklisted(info.id));
		} else {
			h.checkBox.setVisibility(View.GONE);
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


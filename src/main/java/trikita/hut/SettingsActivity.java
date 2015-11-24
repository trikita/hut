package trikita.hut;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	public void onCreate(Bundle b) {
		super.onCreate(b);
		addPreferencesFromResource(R.xml.settings);

		// Start wallpaper picker
		findPreference("wallpaper")
			.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
					startActivity(Intent.createChooser(intent, "Select wallpaper"));
					return true;
				}
			});
	}
}


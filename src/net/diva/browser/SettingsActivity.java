package net.diva.browser;

import net.diva.browser.common.UpdateSaturaionPoints;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		final ListPreference sortOrder = (ListPreference)findPreference("initial_sort_order");
		sortOrder.setSummary(sortOrder.getEntry());
		sortOrder.setOnPreferenceChangeListener(
				new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference self = (ListPreference)preference;
				int index = self.findIndexOfValue(newValue.toString());
				self.setSummary(index < 0 ? "" : self.getEntries()[index]);
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings_options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_update_saturations:
			new UpdateSaturaionPoints(this).execute();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

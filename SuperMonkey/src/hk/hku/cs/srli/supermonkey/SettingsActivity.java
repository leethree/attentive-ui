package hk.hku.cs.srli.supermonkey;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
    	
    	private PreferenceScreen ps;
    	
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            
            ps = getPreferenceScreen();
            
            initSummaries(ps);
        }

        @Override
        public void onResume() {
        	super.onResume();
        	
        	// Set up a listener whenever a key changes
            ps.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
        
        @Override
        public void onPause() {
        	super.onPause();
        	
        	// Unregister the listener whenever a key changes
            ps.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
        
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			updateSummary(findPreference(key));
		}
		
		private void initSummaries(PreferenceGroup pg) {
			for(int i = 0; i < pg.getPreferenceCount(); i++) {
				Preference p = pg.getPreference(i);
				if (p instanceof PreferenceGroup)
					initSummaries((PreferenceGroup) p);
				else
					updateSummary(p);
            }
        }

        private void updateSummary(Preference p) {
        	if (p instanceof ListPreference) {
                ListPreference listPref = (ListPreference) p; 
                p.setSummary(listPref.getEntry()); 
            }
            if (p instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p; 
                p.setSummary(editTextPref.getText()); 
            }
        }
    }
    
}

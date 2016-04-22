package fi.oulu.tol.esde009.ohapclient009;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

/**
 * Created by bel on 12.04.16.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "Debug_SettingsFragment";

    public final static String CENTRAL_UNIT_UTL = "central_unit_url";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferencesChanged()");
        if (key.equals(CENTRAL_UNIT_UTL)) {
            Preference preference = findPreference(key);
            // Set summary to be the user-description for the selected value
            preference.setSummary(sharedPreferences.getString(key, ""));
            Log.d(TAG, "Preference summary :" + preference.getSummary() + "");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}

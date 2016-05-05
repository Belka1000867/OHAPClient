package fi.oulu.tol.esde009.ohapclient009.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceScreen;

import fi.oulu.tol.esde009.ohapclient009.R;

/**
 * Preferences with annotations
 */
@PreferenceScreen(R.xml.preferences)
@EFragment
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "Debug_SettingsFragment";

    public final static String SERVER_ADDRESS = "key.server.address";
    public final static String AUTO_CONNECTION = "key.autoconnection:";

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferencesChanged()");
        if (key.equals(SERVER_ADDRESS)) {
            Preference preference = findPreference(key);
            // Set summary to be the user-description for the selected value
            preference.setSummary(sharedPreferences.getString(key, ""));
            Log.d(TAG, "Preference summary :" + preference.getSummary() + "");
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_fragment, new ContainerFragment_(), "ContainerList")
                    .addToBackStack(null)
                    .commit();
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

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
    }
}

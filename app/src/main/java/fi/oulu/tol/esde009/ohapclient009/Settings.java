package fi.oulu.tol.esde009.ohapclient009;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by bel on 12.04.16.
 */
public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}

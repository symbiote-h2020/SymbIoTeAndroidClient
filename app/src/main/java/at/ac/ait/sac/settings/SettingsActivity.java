package at.ac.ait.sac.settings;


import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * A thin wrapper around the actual settings fragment
 * A {@link PreferenceActivity} that presents a set of application settings.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}

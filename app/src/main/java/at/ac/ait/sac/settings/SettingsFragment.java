package at.ac.ait.sac.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import at.ac.ait.sac.R;

/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by EdeggerK on 13.06.2018.   ¯\_(ツ)_/¯
 */
public class SettingsFragment extends PreferenceFragment {

    private static final Logger LOG = LoggerFactory.getLogger(SettingsFragment.class);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.debug("onCreate: SettingsFragment");
        addPreferencesFromResource(R.xml.prferences);
        bindPreferenceSummaryToValue(findPreference(SettingsConstants.P_CORE_AAM));
        bindPreferenceSummaryToValue(findPreference(SettingsConstants.P_PLATFORM_ID));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            LOG.info("onPreferenceChange: {} -> {}",preference,value);
            String stringValue = value.toString();
            boolean result = false;
            switch (preference.getKey()){
                case SettingsConstants.P_CORE_AAM:{
                    String oldValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), SettingsConstants.P_CORE_AAM);
                    preference.setSummary(preference.getContext().getString(R.string.p_core_aam_summary,stringValue));
                    result = !stringValue.equalsIgnoreCase(oldValue);
                    break;
                }
                case SettingsConstants.P_PLATFORM_ID:{
                    String oldValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), SettingsConstants.P_PLATFORM_ID);
                    preference.setSummary(preference.getContext().getString(R.string.p_platform_id_summary,stringValue));
                    result = !stringValue.equalsIgnoreCase(oldValue);
                    break;
                }
                default:
                    LOG.warn("Unhandled Config change of: {}",preference.getKey());
            }
            return result;
        }


    };



    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        try{
            //first try to handle the pref as string
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }catch (ClassCastException e){
            //maybe it's a boolean
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false));
        }
    }

}


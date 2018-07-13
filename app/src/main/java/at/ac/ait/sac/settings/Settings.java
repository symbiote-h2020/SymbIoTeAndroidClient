package at.ac.ait.sac.settings;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.View;

import at.ac.ait.sac.SymbIoTeConstants;

/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by EdeggerK on 13.07.2018.   ¯\_(ツ)_/¯
 */
public class Settings {

    public static String getCoreAAm(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(SettingsConstants.P_CORE_AAM, SymbIoTeConstants.CORE_AAM_SERVER_URL_DEFAULT);
    }

    public static String getPlatformId(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(SettingsConstants.P_PLATFORM_ID, "ait_kiola");
    }
}

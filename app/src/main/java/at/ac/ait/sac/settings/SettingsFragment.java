package at.ac.ait.sac.settings;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import at.ac.ait.smilaconfig.ble.ConfigBeaconAdvertisementDialog;
import at.ac.ait.smilaconfig.ble.ExpireNowBeaconAdvertismentDialog;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

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
        addPreferencesFromResource(R.xml.smila_prferences);
        bindPreferenceSummaryToValue(findPreference(PreferenceConstants.P_GLOBAL_RSSI_THRESHOLD));
        bindPreferenceSummaryToValue(findPreference(PreferenceConstants.P_BEACON_EXPIRE));
        bindPreferenceSummaryToValue(findPreference(PreferenceConstants.P_NO_ACTION_SLEEP_MINUTES));
        bindPreferenceSummaryToValue(findPreference(PreferenceConstants.P_WEIGHT_INTERVAL_DAYS));
        bindPreferenceSummaryToValue(findPreference(PreferenceConstants.P_WELLBEING_INTERVAL_DAYS));
        bindPreferenceSummaryToValue(findPreference(PreferenceConstants.P_WAHOO_SCALE_MAC));
        bindPreferenceSummaryToValue(findPreference(PreferenceConstants.P_CORE_AAM));
        bindPreferenceSummaryToValue(findPreference(PreferenceConstants.P_DEBUG));
        findPreference(PreferenceConstants.P_FINISH_PREF).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LOG.debug("Showing QR code dialog with new pref value");
                showQRDialog(preference.getContext(), PreferenceConstants.QR_END);
                return true;
            }
        });
        findPreference(PreferenceConstants.P_ADVERTISE_CONFIG).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LOG.debug("Showing config beacon advertisement dialog");
                // Create and show the dialog.
                DialogFragment dlg = new ConfigBeaconAdvertisementDialog();
                dlg.show(getFragmentManager(), ConfigBeaconAdvertisementDialog.class.getSimpleName());
                return true;
            }
        });
        findPreference(PreferenceConstants.P_ADVERTISE_EXPIRE_NOW).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LOG.debug("Showing expire ble beacons now advertisement dialog");
                // Create and show the dialog.
                DialogFragment dlg = new ExpireNowBeaconAdvertismentDialog();
                dlg.show(getFragmentManager(), ExpireNowBeaconAdvertismentDialog.class.getSimpleName());
                return true;
            }
        });

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
                case PreferenceConstants.P_GLOBAL_RSSI_THRESHOLD:{
                    int oldValue = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "-200"));
                    int newValue = Integer.valueOf((String)value);
                    if ((-100 <= newValue) && (newValue <= 0)){
                        result = true;
                        preference.setSummary(stringValue);
                        if (oldValue != newValue){
                            LOG.debug("Showing QR code dialog with new pref value");
                            String msg = String.format(Locale.US, PreferenceConstants.QR_PREF,
                                    preference.getKey(), newValue);
                            showQRDialog(preference.getContext(), msg);
                        }
                    }else{
                        LOG.warn("ignoring invalid rssi {} (must be [-100,0]) ",newValue);
                        result = false;
                    }
                    break;
                }
                case PreferenceConstants.P_BEACON_EXPIRE:{
                    long oldValue = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "-200"));
                    long newValue = Long.valueOf((String)value);
                    if (newValue > 0){
                        result = true;
                        preference.setSummary(preference.getContext().getString(R.string.pref_beacon_expire_summary,stringValue));
                        if (oldValue != newValue){
                            LOG.debug("Showing QR code dialog with new pref value");
                            //the value must be in ms
                            String msg = String.format(Locale.US, PreferenceConstants.QR_PREF,
                                    preference.getKey(), TimeUnit.MINUTES.toMillis(newValue));
                            showQRDialog(preference.getContext(), msg);
                        }
                    }else{
                        LOG.warn("ignoring invalid beacon expire delay {} (must be > 0) ",newValue);
                        result = false;
                    }
                    break;
                }
                case PreferenceConstants.P_WEIGHT_INTERVAL_DAYS:{
                    int oldValue = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "-200"));
                    int newValue = Integer.valueOf((String)value);
                    if (newValue > 0){
                        result = true;
                        preference.setSummary(preference.getContext().getString(R.string.pref_weight_interval_days_summary,stringValue));
                        if (oldValue != newValue){
                            LOG.debug("Showing QR code dialog with new pref value");
                            String msg = String.format(Locale.US, PreferenceConstants.QR_PREF,
                                    preference.getKey(), newValue);
                            showQRDialog(preference.getContext(), msg);
                        }
                    }else{
                        LOG.warn("ignoring invalid days between weight measurements {} (must be > 0) ",newValue);
                        result = false;
                    }
                    break;
                }
                case PreferenceConstants.P_WELLBEING_INTERVAL_DAYS:{
                    int oldValue = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "-200"));
                    int newValue = Integer.valueOf((String)value);
                    if (newValue > 0){
                        result = true;
                        preference.setSummary(preference.getContext().getString(R.string.pref_wellbeing_interval_days_summary,stringValue));
                        if (oldValue != newValue){
                            LOG.debug("Showing QR code dialog with new pref value");
                            String msg = String.format(Locale.US, PreferenceConstants.QR_PREF,
                                    preference.getKey(), newValue);
                            showQRDialog(preference.getContext(), msg);
                        }
                    }else{
                        LOG.warn("ignoring invalid days between wellbeing measurements {} (must be > 0) ",newValue);
                        result = false;
                    }
                    break;
                }
                case PreferenceConstants.P_NO_ACTION_SLEEP_MINUTES:{
                    int oldValue = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "-200"));
                    int newValue = Integer.valueOf((String)value);
                    if (newValue > 0){
                        result = true;
                        preference.setSummary(preference.getContext().getString(R.string.pref_no_action_sleep_minutes_summary,stringValue));
                        if (oldValue != newValue){
                            LOG.debug("Showing QR code dialog with new pref value");
                            String msg = String.format(Locale.US, PreferenceConstants.QR_PREF,
                                    preference.getKey(), newValue);
                            showQRDialog(preference.getContext(), msg);
                        }
                    }else{
                        LOG.warn("ignoring invalid days between weight measurements {} (must be > 0) ",newValue);
                        result = false;
                    }
                    break;
                }
                case PreferenceConstants.P_WAHOO_SCALE_MAC:{
                    String oldValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), PreferenceConstants.P_WAHOO_SCALE_MAC_DEFAULT);
                    String newValue = (String)value;
                    if (BluetoothAdapter.checkBluetoothAddress(newValue)){
                        result = true;
                        preference.setSummary(preference.getContext().getString(R.string.pref_wahoo_scale_mac_summary,stringValue));
                        if (!newValue.equalsIgnoreCase(oldValue)){
                            LOG.debug("Showing QR code dialog with new pref value");
                            String msg = String.format(Locale.US, PreferenceConstants.QR_PREF,
                                    preference.getKey(), newValue);
                            showQRDialog(preference.getContext(), msg);
                        }
                    }else{
                        LOG.warn("ignoring invalid MAC {} (must be in format AA:BB:CC:DD:EE:FF)",newValue);
                        result = false;
                    }
                    break;
                }
                case PreferenceConstants.P_CORE_AAM:{
                    String oldValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), PreferenceConstants.P_CORE_AAM);
                    preference.setSummary(preference.getContext().getString(R.string.pref_core_aam_summary,stringValue));
                    if (!stringValue.equalsIgnoreCase(oldValue)){
                        result = true;
                        LOG.debug("Showing QR code dialog with new pref value");
                        String msg = String.format(Locale.US, PreferenceConstants.QR_PREF,
                                preference.getKey(), stringValue);
                        showQRDialog(preference.getContext(), msg);
                    }
                    break;
                }
                case PreferenceConstants.P_DEBUG:{
                    Boolean oldValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false);
                    preference.setSummary(((boolean)value)?preference.getContext().getString(R.string.pref_debug_summary_true):preference.getContext().getString(R.string.pref_debug_summary_false));
                    if (!oldValue.equals(value)){
                        result = true;
                        LOG.debug("Showing QR code dialog with new pref value");
                        String msg = String.format(Locale.US, PreferenceConstants.QR_PREF,
                                preference.getKey(), ((boolean)value)?Boolean.TRUE.toString():Boolean.FALSE.toString());
                        showQRDialog(preference.getContext(), msg);
                    }
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

    static private void showQRDialog(Context ctx, String msg) {
        AlertDialog dlg = new AlertDialog.Builder(ctx).setCancelable(false)
                .setView(R.layout.pref_qr_dlg)
                //.setMessage(msg)
                .setNegativeButton("close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LOG.debug("Closing QR code dlg");
                        dialogInterface.dismiss();
                    }
                }).create();
        LOG.debug("QR dialog created");
        //we need to show it before looking for views
        dlg.show();
        ImageView qr = dlg.findViewById(R.id.pref_qr_dlg_qr);
        TextView txt = dlg.findViewById(R.id.pref_qr_dlg_txt);
        txt.setText(msg);
        try {
            qr.setImageBitmap(encodeAsBitmap(msg));
        } catch (WriterException e) {
            LOG.error("Invalid QR code: {}",e);
        }
        LOG.debug("QR code dlg shown");
    }

    static private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, PreferenceConstants.QR_WIDTH, PreferenceConstants.QR_HEIGHT, null);
        } catch (IllegalArgumentException iae) {
            LOG.error("Unsupported format while creating QR code: {}",iae);
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


}


package at.ac.ait.sac;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import at.ac.ait.sac.settings.Settings;
import at.ac.ait.sac.settings.SettingsActivity;

/**
 * This is the main class to interact w/ SymbIoTe. It features to trigger the SymbIoTe core
 * registry search and querying a specific sensor retrieved previously.
 *
 * Which core you are asking? Please specify the desired symbIoTe core URL in the preferences
 * accessible in the action bar of the app. By default we are using
 * 'https://symbiote-open.man.poznan.pl/coreInterface'
 *
 * Some platform ids and their names on symbiote-open:
 *
 * smart-stadium-dev		Smart Stadium
 * xplatform				X_Platform
 * AITopenUwedat			AITopenUwedat
 * smeur					SMEUR
 * NXW-symphony-1			NXW-symphony-1
 * mobaas3					MoBaaS3
 * fer1					    OpenIoT
 * ait_kiola				ait_kiola
 * PLATFORM_1528795401120	Sensinov
 * NavigoDigitale			NavigoDigitale
 * educampus				EduCampus IOSB
 * oh-fer					OpenHAB-FER
 */
public class SymbIoTeClientActivity extends AppCompatActivity {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SymbIoTeClientActivity.class);

    private final SymbIoTeCoreSensorQueryTask.QueryTaskCallback mSymbIoTeQueryCallback = new SymbIoTeCoreSensorQueryTask.QueryTaskCallback() {
        @Override
        public void onSearchComplete(Collection<Sensor> sensors) {
            LOG.debug("onSearchComplete: found {} sensors",sensors.size());
            mAdapter.addAll(sensors);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Exception e) {
            LOG.error("onError: ",e);
        }
    };

    /**
     * This is the one reacting on the task when the actual sensor is being read via SymbIoTe core
     * and platform specific requests.
     * To exemplify, we pop up alert boxes to show the result.
     */
    private final SymbIoTeSensorReadingTask.SensorReaderCallback mSymbIoTeReaderCallback = new SymbIoTeSensorReadingTask.SensorReaderCallback() {

        @Override
        public void onSuccess(String responseBody) {
            LOG.debug("onSuccess: Response from sensor: {} ",responseBody);
            new AlertDialog.Builder(SymbIoTeClientActivity.this).setMessage(responseBody).show();
            enableSensorListView(true);
        }

        @Override
        public void onError(Exception e) {
            LOG.error("onError: ",e);
            new AlertDialog.Builder(SymbIoTeClientActivity.this).setMessage(e.getMessage()).setTitle("Error").setIcon(android.R.drawable.ic_dialog_alert).show();
            enableSensorListView(true);
        }
    };


    private SymbIoTeCoreSensorQueryTask mCoreQueryTask;
    private final List<Sensor> mSensors = new ArrayList<>();
    private ArrayAdapter<Sensor> mAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symb_io_te_client);
        Toolbar toolbar = findViewById(R.id.toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mListView = findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mSensors);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Sensor clicked = (Sensor) adapterView.getAdapter().getItem(i);
                LOG.debug("onItemClick: {}",clicked);
                Snackbar.make(view, getString(R.string.snack_reader_started), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                new SymbIoTeSensorReadingTask(SymbIoTeClientActivity.this, mSymbIoTeReaderCallback).execute(clicked.id);
                //don't allow multiple clicks on the list ...
                enableSensorListView(false);
            }
        });

        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getString(R.string.snack_query_started), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mCoreQueryTask = new SymbIoTeCoreSensorQueryTask(SymbIoTeClientActivity.this, mSymbIoTeQueryCallback);
                mCoreQueryTask.execute(Settings.getPlatformId(SymbIoTeClientActivity.this));
            }
        });
    }

    /**
     * enable/disable the list of available sensors to give a visual cue
     * @param enable false to disable the list
     */
    private void enableSensorListView(boolean enable) {
        mListView.setEnabled(enable);
        if (enable){
            mListView.setAlpha(1f);
            mListView.setBackgroundColor(Color.WHITE);
        }else{
            mListView.setAlpha(0.75f);
            mListView.setBackgroundColor(Color.GRAY);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_symb_io_te_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent startSettings = new Intent(this, SettingsActivity.class);
            startActivity(startSettings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

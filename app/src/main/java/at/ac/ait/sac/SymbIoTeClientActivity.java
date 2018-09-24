package at.ac.ait.sac;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
 *
 */
public class SymbIoTeClientActivity extends AppCompatActivity {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SymbIoTeClientActivity.class);

    private SymbIoTeCoreSensorQueryTask.QueryTaskCallback mSymbIoTeQueryCallback = new SymbIoTeCoreSensorQueryTask.QueryTaskCallback() {
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

    private SymbIoTeSensorReadingTask.SensorReaderCallback mSymbIoTeReaderCallback = new SymbIoTeSensorReadingTask.SensorReaderCallback() {

        @Override
        public void onSuccess(String responseBody) {
            LOG.debug("onSuccess: Response from sensor: {} ",responseBody);
            new AlertDialog.Builder(SymbIoTeClientActivity.this).setMessage(responseBody).show();
        }

        @Override
        public void onError(Exception e) {
            LOG.error("onError: ",e);
            new AlertDialog.Builder(SymbIoTeClientActivity.this).setMessage(e.getMessage()).setTitle("Error").setIcon(android.R.drawable.ic_dialog_alert).show();
        }
    };


    private SymbIoTeCoreSensorQueryTask mCoreQueryTask;
    private List<Sensor> mSensors = new ArrayList<>();
    private ArrayAdapter<Sensor> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symb_io_te_client);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ListView listView = findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mSensors);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Sensor clicked = (Sensor) adapterView.getAdapter().getItem(i);
                LOG.debug("onItemClick: {}",clicked);
                Snackbar.make(view, getString(R.string.snack_reader_started), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                new SymbIoTeSensorReadingTask(SymbIoTeClientActivity.this, mSymbIoTeReaderCallback).execute(clicked.id);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_symb_io_te_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

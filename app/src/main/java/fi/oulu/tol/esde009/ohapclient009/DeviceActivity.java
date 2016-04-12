package fi.oulu.tol.esde009.ohapclient009;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Device;

import java.net.MalformedURLException;
import java.net.URL;

public class DeviceActivity extends AppCompatActivity {

    public static final String EXTRA_CENTRAL_UNIT_URL = "fi.oulu.tol.009.CENTRAL_UNIT_URL";
    public static final String EXTRA_DEVICE_ID = "fi.oulu.tol.009.DEVICE_ID";

    CentralUnitConnection centralUnitConnection;
    Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /*
        * Get URL connection string EXTRA from new intention of the class
        * */
        String extraUrlConnection = getIntent().getStringExtra(EXTRA_CENTRAL_UNIT_URL);
        /*
        * For the first time EXTRA will be null, as there is no Intent and URL should be taken from preferences
        * */
        final String urlConnection = extraUrlConnection != null ? extraUrlConnection : "http://ohap.opimobi.com:8080/";

        /*
        * Get ID string EXTRA from new intention
        * ALWAYS taken from intent
        * */
        String deviceId = getIntent().getStringExtra(EXTRA_DEVICE_ID);

        /*
        * Initialize connection with central unit
        * */
        try {
            centralUnitConnection = new CentralUnitConnection(new URL(urlConnection));
            /*
            * Get registered device that user click from central unit
            * */
            device = (Device) centralUnitConnection.getItemById(Integer.parseInt(deviceId));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /*
        * Take visuals from device activity layout
        * */
        TextView textView_path = (TextView) findViewById(R.id.textView_path);
        TextView textView_name = (TextView) findViewById(R.id.textView_name);
        TextView textView_description = (TextView) findViewById(R.id.textView_description);
        Switch switch_value = (Switch) findViewById(R.id.switch_value);
        SeekBar seekBar_value = (SeekBar) findViewById(R.id.seekBar_value);
        EditText editText_decimal = (EditText) findViewById(R.id.editText_decimal);

        /*
        * Initialize device information in the ACTIVITY
        * set Title of an activity as device name
        * */
        setTitle(device.getName());
        
        textView_path.setText(device.getParent().toString());
        textView_name.setText(device.getName());
        textView_description.setText(device.getDescription());
        switch_value.setChecked(device.getBinaryValue());
        /*
        * Hide and deactivate Seek Bar
        * */
        seekBar_value.setVisibility(View.GONE);

        editText_decimal.setText("" + device.getDecimalValue());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

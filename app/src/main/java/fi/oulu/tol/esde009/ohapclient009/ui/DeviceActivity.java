package fi.oulu.tol.esde009.ohapclient009.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.opimobi.ohap.Device;

import java.net.MalformedURLException;
import java.net.URL;

import fi.oulu.tol.esde009.ohapclient009.networking.CentralUnitConnection;
import fi.oulu.tol.esde009.ohapclient009.R;
import fi.oulu.tol.esde009.ohapclient009.ui.fragments.SettingsFragment;

public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = "Debug_DeviceActivity";
    public static final String EXTRA_CENTRAL_UNIT_URL = "fi.oulu.tol.009.SERVER_ADDRESS";
    public static final String EXTRA_DEVICE_ID = "fi.oulu.tol.009.DEVICE_ID";

    private RelativeLayout uiRelativeLayout;
    private LayoutInflater mLayoutInflater;

    private Device mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        * Layout for UI inflater
        * */
        uiRelativeLayout = (RelativeLayout) findViewById(R.id.rellayout_device_control);
        mLayoutInflater = getLayoutInflater();


        /*
        * Get url of the central unit from the settings
        * */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String centralUnitUrl = sharedPreferences.getString("central_unit_url", "");

        if(centralUnitUrl.isEmpty()){
            Intent intent = new Intent(this, SettingsFragment.class);
            startActivity(intent);
        }

        /*
        * Get URL connection string EXTRA from new intention of the class
        * */
        String extraUrlConnection = getIntent().getStringExtra(EXTRA_CENTRAL_UNIT_URL);
        /*
        * For the first time EXTRA will be null, as there is no Intent and URL should be taken from preferences
        * should be something like this "http://ohap.opimobi.com:8080/"
        * */
        final String urlConnection = extraUrlConnection != null ? extraUrlConnection : centralUnitUrl;

        /*
        * Get ID string EXTRA from new intention
        * ALWAYS taken from intent
        * */
        String deviceId = getIntent().getStringExtra(EXTRA_DEVICE_ID);



        /*
         * Get registered mDevice that user click from central unit
         * */
        mDevice = (Device) CentralUnitConnection.getInstance().getItemById(Integer.parseInt(deviceId));


        /*
        * Take visuals from mDevice activity layout
        * */
//        TextView textView_path = (TextView) findViewById(R.id.textView_path);
//        TextView textView_name = (TextView) findViewById(R.id.textView_name);
//        TextView textView_description = (TextView) findViewById(R.id.textView_description);
        ImageView imageView_Picture = (ImageView) findViewById(R.id.device_picture);

        /*
        * Initialize mDevice information in the ACTIVITY
        * set Title of an activity as mDevice name
        * */
        setTitle(mDevice.getName());
        
//        textView_path.setText(mDevice.getParent().getName());
//        textView_name.setText(mDevice.getName());
//        textView_description.setText(mDevice.getDescription());

        switch (mDevice.getCategory()){
            case Device.LIGHT :
                imageView_Picture.setImageResource(R.mipmap.oc_light);
                createUiLight();
                break;
            case Device.HEATING :
                imageView_Picture.setImageResource(R.mipmap.ic_heating);
                createUiHeating();
                break;
            case Device.JEALOUSE :
                imageView_Picture.setImageResource(R.mipmap.oc_jealouse);
                createUiJalousie();
                break;
        }

        //switch_value.setChecked(mDevice.getBinaryValue());
        /*
        * Hide and deactivate Seek Bar
        * */
        //seekBar_value.setVisibility(View.GONE);

        //editText_decimal.setText("" + mDevice.getDecimalValue());
    }



    private void createUiLight(){
        Light lightUiClass = new Light(this, mDevice, mLayoutInflater, uiRelativeLayout);
        lightUiClass.realizeUi();
    }

    private void createUiHeating() {
        mLayoutInflater.inflate(R.layout.ui_heating, uiRelativeLayout, true);

        Button bLightOn = (Button) findViewById(R.id.button_on);
        Button bLightOff = (Button) findViewById(R.id.button_off);
        final SeekBar sbLightChange = (SeekBar) findViewById(R.id.sb_value_change);
        final TextView tvLightValue = (TextView) findViewById(R.id.tv_value);

        tvLightValue.setText(mDevice.getDecimalValue() + "\u2103");
        sbLightChange.setProgress((int) mDevice.getDecimalValue());

        bLightOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDevice.setDecimalValue(mDevice.getMaxValue());
                sbLightChange.setProgress(sbLightChange.getMax());
                tvLightValue.setText(""+ mDevice.getDecimalValue() + R.string.celsius);
            }
        });

        bLightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDevice.setDecimalValue(mDevice.getMinValue());
                sbLightChange.setProgress(0);
                tvLightValue.setText(""+ mDevice.getDecimalValue() + R.string.celsius);
            }
        });

        sbLightChange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int lightValueInt;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lightValueInt = progress;
                tvLightValue.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch()" + lightValueInt);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch" + lightValueInt);
                mDevice.setDecimalValue(lightValueInt);
            }
        });
    }
    
    private void createUiJalousie() {
        mLayoutInflater.inflate(R.layout.ui_jalousie, uiRelativeLayout, true);
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

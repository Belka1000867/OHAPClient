package fi.oulu.tol.esde009.ohapclient009;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Container;
import com.opimobi.ohap.Device;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by bel on 01.04.16.
 */
public class ContainerActivity extends AppCompatActivity {

    public static final String EXTRA_CENTRAL_UNIT_URL = "fi.oulu.tol.009.CENTRAL_UNIT_URL";
    public static final String EXTRA_CONTAINER_ID = "fi.oulu.tol.009.CONTAINER_ID";

    private CentralUnitConnection centralUnitConnection;
    private Container container;

    private static String DEBUG_TAG = "Debug_ContainerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        /*
        Get EXTRA central unit url from new intent of the class
        */
        String extraCentralUnitUrl = getIntent().getStringExtra(EXTRA_CENTRAL_UNIT_URL);

        /*
        For the first instantiation of the class Intent will not be used and extraCentralUnitUrl will be null
        Than need to get URL from preferences
        */
        final String centralUnitUrl = extraCentralUnitUrl != null ? extraCentralUnitUrl : getCentralUnitUrl();
        Log.d(DEBUG_TAG, "centralUnitUrl  " + centralUnitUrl);

        try {
            centralUnitConnection = new CentralUnitConnection(new URL(centralUnitUrl));
            Log.d(DEBUG_TAG, "centralUnitConnection  " + centralUnitConnection.getName());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /*
        get EXTRA container ID from new intent of the class
        */
        String extraContainerId = getIntent().getStringExtra(EXTRA_CONTAINER_ID);
        /*
        for the first instantiation of the class Intent will not be used and ID will be null
        if id is null than it is parent container
        */
        final String containerId;
        try {
            containerId = extraContainerId != null ? extraContainerId : centralUnitConnection.getId() + "";
            Log.d(DEBUG_TAG, "containerId " + containerId);
            container = (Container) centralUnitConnection.getItemById(Long.parseLong(containerId));
        } catch (Exception e) {
            e.printStackTrace();
        }



        ListView listView = (ListView) findViewById(R.id.listView_test);
        final ContainerListAdapter containerListAdapter = new ContainerListAdapter(container);

        listView.setAdapter(containerListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                /*
                * If selected item is not a container launch Device Activity
                * */
                if (containerListAdapter.getItem(position) instanceof Container){
                    intent = new Intent(ContainerActivity.this, ContainerActivity.class);
                    intent.putExtra(EXTRA_CENTRAL_UNIT_URL, centralUnitUrl);
                    intent.putExtra(EXTRA_CONTAINER_ID, containerListAdapter.getItemId(position)+"");
                }
                else
                {
                    intent = new Intent(ContainerActivity.this, DeviceActivity.class);
                    intent.putExtra(DeviceActivity.EXTRA_CENTRAL_UNIT_URL, centralUnitUrl);
                    intent.putExtra(DeviceActivity.EXTRA_DEVICE_ID, containerListAdapter.getItemId(position)+"");
                }

                startActivity(intent);
            }
        });
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


        switch (item.getItemId()){
            case R.id.action_settings:
               Intent settingActivity = new Intent(getBaseContext(), Settings.class);
                startActivity(settingActivity);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //get Central Unit URl from user preferences
    private String getCentralUnitUrl(){
        return "http://ohap.opimobi.com:8080/";
    }
}

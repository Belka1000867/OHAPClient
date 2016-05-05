package fi.oulu.tol.esde009.ohapclient009.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.opimobi.ohap.Container;
import com.opimobi.ohap.Device;
import com.opimobi.ohap.Item;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.net.MalformedURLException;
import java.net.URL;

import fi.oulu.tol.esde009.ohapclient009.R;
import fi.oulu.tol.esde009.ohapclient009.networking.CentralUnitConnection;
import fi.oulu.tol.esde009.ohapclient009.ui.fragments.DeviceFragment_;
import fi.oulu.tol.esde009.ohapclient009.ui.fragments.SettingsFragment;
import fi.oulu.tol.esde009.ohapclient009.ui.fragments.SettingsFragment_;
import fi.oulu.tol.esde009.ohapclient009.ui.fragments.ContainerFragment_;
import fi.oulu.tol.esde009.ohapclient009.utils.CentralUnitObserver;

/**
 * Activity to manipulate fragment(s) with UI and logical behaviour
 *
 */
@EActivity(R.layout.activity_container)
@OptionsMenu(R.menu.menu_device)
public class ContainerActivity extends AppCompatActivity implements ContainerFragment_.OnItemSelectedListener, CentralUnitObserver{

    public SharedPreferences sharedPreferences;
    private FragmentManager mFragmentManager;

    private static String DEBUG_TAG = "Debug_ContainerActivity";

    // When Item 'Settings' was selected in the overflow menu (AndroidAnnotations)
    // Settings are also use SupportFragmentManager as they were changed
    @OptionsItem (R.id.action_settings)
    void settings(){
        openSettings();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "onCreate()");

        if(!checkNetwork()) {
            return;
        }

        mFragmentManager = getSupportFragmentManager();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        /*
        * Checking if activity have saved state in order to prevent overlapping of fragments
        */
        if (savedInstanceState != null) {
            Log.d(DEBUG_TAG, "savedInstanceState");
            return;
        }

        String url = sharedPreferences.getString(SettingsFragment.SERVER_ADDRESS, "");
        if(url == null || url.length() == 0){
            openSettings();
            handleErrorMessage(R.string.error_message_server_address);
        }


        if (url != null && url.length() > 0) {
            try {
                CentralUnitConnection.getInstance().initialize(new URL(url), this);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else{
            openSettings();
        }

        /*
        * Adding fragment dynamically into the FrameLayout with tag ContainerList and adding to the back stack
        */
        mFragmentManager
                .beginTransaction()
                .add(R.id.container_fragment, new ContainerFragment_(), "ContainerList")
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "onStart()");

        if(checkNetwork()) {
            boolean isAutoConnection = sharedPreferences.getBoolean(SettingsFragment.AUTO_CONNECTION, true);
            if (isAutoConnection) {
                CentralUnitConnection.getInstance().start(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume");

    }

    //Method from the ContainerFragment.OnItemSelectedListener in order to get the message from the fragment and replace it
    @Override
    public void onItemSelected(Item item, Bundle args) {
        Log.d(DEBUG_TAG, "onItemSelected()");

        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();

        if (item instanceof Container) {
            //Creating new ContainerFragment_ ( "_" added because of AndroidAnnotations )
            ContainerFragment_ containerFragment = new ContainerFragment_();
            containerFragment.setArguments(args);

            mFragmentTransaction
                    .replace(R.id.container_fragment, containerFragment, "ContainerList");

        } else if(item instanceof Device) {
            DeviceFragment_ deviceFragment = new DeviceFragment_();
            deviceFragment.setArguments(args);

            mFragmentTransaction
                    .replace(R.id.container_fragment, deviceFragment, "Device");
        }

        //Adding to back stack and committing replacement
        mFragmentTransaction
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void handlePingResponse(String response) {

    }

    @Override
    public void handleErrorMessage(int errorId) {
        Toast.makeText(this, getResources().getString(errorId), Toast.LENGTH_LONG).show();
    }

    public void openSettings(){
        mFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, new SettingsFragment_(), "Settings")
                .addToBackStack(null)
                .commit();
    }

    private boolean checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        Log.d(DEBUG_TAG, "No network, cannot initiate connection!");
        handleErrorMessage(R.string.error_message_no_connection);
        return false;
    }
}

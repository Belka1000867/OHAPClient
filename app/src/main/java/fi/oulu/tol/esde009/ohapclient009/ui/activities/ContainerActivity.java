package fi.oulu.tol.esde009.ohapclient009.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
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
import fi.oulu.tol.esde009.ohapclient009.network.CentralUnitConnection;
import fi.oulu.tol.esde009.ohapclient009.ui.fragments.ContainerFragment_;
import fi.oulu.tol.esde009.ohapclient009.ui.fragments.DeviceFragment_;
import fi.oulu.tol.esde009.ohapclient009.ui.fragments.SettingsFragment_;
import fi.oulu.tol.esde009.ohapclient009.utils.AppConstants;
import fi.oulu.tol.esde009.ohapclient009.utils.ConnectionObserver;
import fi.oulu.tol.esde009.ohapclient009.utils.OhapErrorListener;

/**
 * Activity to manipulate fragment(s) with UI and logical behaviour
 *
 */
@EActivity(R.layout.activity_container)
@OptionsMenu(R.menu.menu_device)
public class ContainerActivity extends AppCompatActivity implements ContainerFragment_.OnItemSelectedListener, ConnectionObserver, OhapErrorListener{

    private FragmentManager mFragmentManager;
    private CentralUnitConnection mCentralUnitConnection;
    private SharedPreferences mSharedPreferences;

    public static int fragment = 0;

    private static final String DEBUG_TAG = "Debug_ContainerActivity";

    // When Item 'Settings' was selected in the overflow menu (AndroidAnnotations)
    // Settings are also use SupportFragmentManager as they were changed from the default
    @OptionsItem (R.id.action_settings)
    void settings(){
        openSettingsFragment();
    }

    // When Item 'Ping' was selected in the overflow menu (AndroidAnnotations)
    @OptionsItem (R.id.action_ping)
    void doPing(){
        mCentralUnitConnection.doPing();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "onCreate()");

//        If there is no internet connection tell it to user and do not continue further
        if(!checkNetwork()) {
            handleError(R.string.error_message_no_connection);
            return;
        }

        mFragmentManager = getSupportFragmentManager();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCentralUnitConnection = CentralUnitConnection.getInstance();

        /*
        * Checking if activity have saved state in order to prevent overlapping of fragments
        */
        if (savedInstanceState != null) {
            Log.d(DEBUG_TAG, "savedInstanceState");
            return;
        }

//       If the server address in the settings is Empty then go the Settings screen
//       if not then launch the CentralUnitConnection
        String url = mSharedPreferences.getString(SettingsFragment_.SERVER_ADDRESS, "");
        if(url.isEmpty() || url.length() == 0){
            handleError(R.string.error_message_server_address);
        }
        else{
            try {
                mCentralUnitConnection.initialize(new URL(url), this, this);
            } catch (MalformedURLException e) {
                Log.d(DEBUG_TAG, "!!! Error !!! initialize() : " + e.getMessage());
            }
        }

        /*
        * Adding fragment dynamically into the FrameLayout with tag ContainerList and adding to the back stack
        */
        addContainerFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "onStart()");
//        if(!checkNetwork()) {
//            boolean isAutoConnection = mSharedPreferences.getBoolean(SettingsFragment_.AUTO_CONNECTION, true);
//            if (isAutoConnection) {
//                mCentralUnitConnection.start();
//            }
//        }
//        else{
//            handleError(R.string.error_message_no_connection);
//        }
    }

    @Override
    protected void onStop() {
        Log.d(DEBUG_TAG, "onStop()");
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy()");
        super.onDestroy();
        CentralUnitConnection.getInstance().stopListening();
//        mCentralUnitConnection.stop();
    }

    //Method from the ContainerFragment.OnItemSelectedListener in order to get the message from the fragment and replace with another fragment
    @Override
    public void onItemSelected(Item item, Bundle args) {
        Log.d(DEBUG_TAG, "onItemSelected()");

        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();

        if (item instanceof Container) {

            // Creating new ContainerFragment_
            // char '_' added because of AndroidAnnotations
            ContainerFragment_ containerFragment = new ContainerFragment_();
            containerFragment.setArguments(args);

            mFragmentTransaction
                    .replace(R.id.container_fragment, containerFragment, AppConstants.TAG_CONTAINER_FRAGMENT);

        } else if(item instanceof Device) {
            // Creating new DeviceFragment_
            // char '_' added because of AndroidAnnotations
            DeviceFragment_ deviceFragment = new DeviceFragment_();
            deviceFragment.setArguments(args);

            mFragmentTransaction
                    .replace(R.id.container_fragment, deviceFragment, AppConstants.TAG_DEVICE_FRAGMENT);
        }

        //Adding to back stack and committing replacement
        mFragmentTransaction
                .addToBackStack(null)
                .commit();
    }

/*
* Method to handle possible error and ask the user to handle them by himself
* */
    @Override
    public void handleError(int id) {
        Log.d(DEBUG_TAG, "handleError()");

        int messageId;

        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);
        mAlertBuilder.setTitle(id);
        mAlertBuilder.setCancelable(false);

        switch (id){
            case R.string.error_message_no_connection:
                messageId = R.string.dialog_message_no_connection;

                mAlertBuilder
                        .setPositiveButton(R.string.dialog_button_ok, (dialog, which) -> {
                            finish();
                        });
                break;
            case R.string.error_message_socket_connection:

                messageId = R.string.dialog_message_again;

                mAlertBuilder
//                         Button YES will try to reconnect
                        .setPositiveButton(R.string.dialog_button_positive, (dialog, which) -> {
                            addContainerFragment();
                            dialog.dismiss();
                        })
//                         Button NO will close an application
                        .setNegativeButton(R.string.dialog_button_negative, (dialog, which) -> {
                            finish();
                        });
                break;
            case R.string.error_message_socket_connection_lost:

                messageId = R.string.dialog_message_reconnect;

                mAlertBuilder
//                         Button YES will try to reestablish connection
                        .setPositiveButton(R.string.dialog_button_positive, (dialog, which) -> {
                            replaceContainerFragemnt();
                            dialog.dismiss();
                        })
//                         Button NO will close an application
                        .setNegativeButton(R.string.dialog_button_negative, (dialog, which) -> {
                            finish();
                        });
                break;
            case R.string.error_message_server_address:

                messageId = R.string.dialog_message_enter_server_address;

                mAlertBuilder
//                         Button YES will open settings
                        .setPositiveButton(R.string.dialog_button_positive, (dialog, which) -> {
                            //
                            openSettingsFragment();
                        })
//                         Button NO will close an application
                        .setNegativeButton(R.string.dialog_button_negative, (dialog1, which1) -> {
                            finish();
                        });
                break;
            case R.string.error_message_wrong_server_address:

                messageId = R.string.dialog_message_enter_server_address;

                mAlertBuilder
//                         Button YES will open settings
                        .setPositiveButton(R.string.dialog_button_positive, (dialog, which) -> {
                            //
                            openSettingsFragment();
                        })
//                         Button NO will close an application
                        .setNegativeButton(R.string.dialog_button_negative, (dialog1, which1) -> {
                            finish();
                        });
                break;
            default:
                messageId = 404;
                break;
        }

        String message = getResources().getString(messageId);

        // Set message, create dialog and show it to the user
        mAlertBuilder.setMessage(message)
                .create()
                .show();
    }

    @Override
    public void handlePong() {
        Log.d(DEBUG_TAG, "Running on the thread with id: " + Thread.currentThread().getId());
        Log.d(DEBUG_TAG, "Looper : " + (Looper.myLooper() == Looper.getMainLooper()));
        showMessage(R.string.incomingmessage_pong);
    }

    @Override
    public void logout() {
        Log.d(DEBUG_TAG, "Running on the thread with id: " + Thread.currentThread().getId());
        Log.d(DEBUG_TAG, "Looper : " + (Looper.myLooper() == Looper.getMainLooper() ));
        showMessage(R.string.incomingmessage_logout);
    }

    public void openSettingsFragment(){
        mFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, new SettingsFragment_(), AppConstants.TAG_SETTINGS_FRAGMENT)
                .addToBackStack(null)
                .commit();
    }

    public void showMessage(int stringId){
        Toast.makeText(this, getResources().getString(stringId), Toast.LENGTH_LONG).show();
    }

    private void addContainerFragment(){
        Log.d(DEBUG_TAG, "addContainerFragment()");
        mFragmentManager
                .beginTransaction()
                .add(R.id.container_fragment, new ContainerFragment_(), AppConstants.TAG_CONTAINER_FRAGMENT)
                .addToBackStack(null)
                .commit();
        fragment++;
    }

    private void replaceContainerFragemnt(){
        Log.d(DEBUG_TAG, "addContainerFragment()");
        mFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, new ContainerFragment_(), AppConstants.TAG_CONTAINER_FRAGMENT)
                .addToBackStack(null)
                .commit();
        fragment++;
    }

    private boolean checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        Log.d(DEBUG_TAG, "No network, cannot initiate connection!");
        showMessage(R.string.error_message_no_connection);
        return false;
    }

}

package fi.oulu.tol.esde009.ohapclient009.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ListView;

import com.opimobi.ohap.Container;
import com.opimobi.ohap.Item;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import fi.oulu.tol.esde009.ohapclient009.R;
import fi.oulu.tol.esde009.ohapclient009.network.CentralUnitConnection;
import fi.oulu.tol.esde009.ohapclient009.ui.activities.ContainerActivity_;
import fi.oulu.tol.esde009.ohapclient009.utils.AppConstants;
import fi.oulu.tol.esde009.ohapclient009.utils.ContainerListAdapter;
import fi.oulu.tol.esde009.ohapclient009.utils.OhapErrorListener;

/**
 * Fragment with UI and logical behaviour to show the list of containers and/or devices
 * of the specified container.
 *
 * First instance will show the list from the Central unit container
 */

//Using Android Annotations initializing layout
@EFragment(R.layout.fragment_container)
public class ContainerFragment extends Fragment {

    private OnItemSelectedListener mCallback;
    private OhapErrorListener ohapErrorListener;

    private Container mContainer;

    private String prefCentralUnitUrl;
    private String finalCentralUnitUrl;

    private static String TAG = "Debug_ContainerFragment";

    //Interface to communicate with Activity
    public interface OnItemSelectedListener{
        void onItemSelected(Item item, Bundle args);
    }

/*
    Android Annotations = findViewById
*/
    @ViewById(R.id.listView_container)
    ListView listViewContainer;

/*
    Method is called after the views binding has happened
    Used because view was found by ViewById or ListView will be NULL
*/
    @AfterViews
    void initListContainer(){
        Log.d(TAG, "initListContainer()");

        try {
            Log.d(TAG, "mContainer = " + mContainer.getName() );
            mContainer.startListening();
            ContainerListAdapter containerListAdapter = new ContainerListAdapter(mContainer);
            listViewContainer.setAdapter(containerListAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            getActivity().setTitle(mContainer.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().setTitle("Container Fragment" + ContainerActivity_.fragment);
    }

/*
    Android annotations = list ItemClick
*/
    @ItemClick(R.id.listView_container)
    void launchNext(Item item){
        Log.d(TAG, "launchNext()");

        //Create bundle as arguments for new fragment
        Bundle args = new Bundle();
        args.putString(AppConstants.EXTRA_SERVER_ADDRESS_URL, finalCentralUnitUrl);
        args.putLong(AppConstants.EXTRA_ITEM_ID, item.getId());

        // Request Activity to change the fragment
        mCallback.onItemSelected(item, args);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach()");

//      Check that activity that start fragment implements listener interface
        try {
            mCallback = (OnItemSelectedListener) context;
            ohapErrorListener = (OhapErrorListener) context;
        } catch (ClassCastException e) {
            Log.d(TAG, "ClassCastException" + e.getMessage());
        }
/*
*       Setting application preferences
*/
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        /*
        * Get url of the central unit from the settings
        * */
        prefCentralUnitUrl = sharedPreferences.getString(SettingsFragment.SERVER_ADDRESS, "");
        Log.d(TAG, "prefCentralUnitUrl  " + prefCentralUnitUrl);

//      If server address is not defined by the user than open settings for him
        if(prefCentralUnitUrl.isEmpty()){
            Log.d(TAG, "URL from preferences is empty");
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_fragment, new SettingsFragment_(), "Settings")
                    .addToBackStack(null)
                    .commit();

            ohapErrorListener.handleError(R.string.error_message_server_address);
            return;
        }

        establishConnection();
    }

    private void establishConnection(){

        String argServerAddressUrl = null;
        Long argContainerId = null;
        Bundle arguments = getArguments();
        /*
        Get central unit url and ID from bundle arguments of the fragment
        If there is some data send to the fragment
        */
        if(arguments != null){
            argServerAddressUrl = arguments.getString(AppConstants.EXTRA_SERVER_ADDRESS_URL);
            argContainerId = arguments.getLong(AppConstants.EXTRA_ITEM_ID);
        }

        /*
        For the first instantiation of the class arguments will not be used and argCentralUnitUrl will be null
        Than need to get URL from preferences
        */
        finalCentralUnitUrl = argServerAddressUrl != null ? argServerAddressUrl : prefCentralUnitUrl;
        Log.d(TAG, "finalCentralUnitUrl  " + finalCentralUnitUrl);


        CentralUnitConnection mCentralUnitConnection = CentralUnitConnection.getInstance();
        Log.d(TAG, "centralUnitConnection  " + mCentralUnitConnection.getName());

        /*
        for the first instantiation of the class Intent will not be used and ID will be null
        if id is null than it is parent container
        */
        final long containerId = argContainerId != null ? argContainerId : mCentralUnitConnection.getId();
        Log.d(TAG, "centralUnitConnection  " + mCentralUnitConnection.getId());
        mContainer = (Container) mCentralUnitConnection.getItemById(containerId);
    }


}

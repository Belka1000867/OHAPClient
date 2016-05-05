package fi.oulu.tol.esde009.ohapclient009.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.opimobi.ohap.Container;
import com.opimobi.ohap.Item;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import fi.oulu.tol.esde009.ohapclient009.R;
import fi.oulu.tol.esde009.ohapclient009.networking.CentralUnitConnection;

import fi.oulu.tol.esde009.ohapclient009.utils.AppConstants;
import fi.oulu.tol.esde009.ohapclient009.utils.ContainerListAdapter;

/**
 * Fragment with UI and logical behaviour to show the list of containers and/or devices
 * of the specified container.
 *
 * First instance will show the list from the Central unit container
 */

//Using Android Annotations initializing layout
@EFragment(R.layout.fragment_container)
public class ContainerFragment extends Fragment {

    OnItemSelectedListener mCallback;

    private CentralUnitConnection centralUnitConnection;
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
        getActivity().setTitle(mContainer.getName());

        ContainerListAdapter containerListAdapter = new ContainerListAdapter(mContainer);
        listViewContainer.setAdapter(containerListAdapter);
    }

/*
    Android annotations = list ItemClick
*/
    @ItemClick(R.id.listView_container)
    void launchNext(Item item){
        Log.d(TAG, "launchNext()");
        Bundle args = new Bundle();
        args.putString(AppConstants.EXTRA_CENTRAL_UNIT_URL, finalCentralUnitUrl);
        args.putLong(AppConstants.EXTRA_ITEM_ID, item.getId());

        // Request Activity to change the fragment
        mCallback.onItemSelected(item, args);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach()");

        try {
            mCallback = (OnItemSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnItemSelectedListener");
        }
/*
*       Setting the preferences
*/
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        /*
        * Get url of the central unit from the settings
        * */
        prefCentralUnitUrl = sharedPreferences.getString(SettingsFragment.SERVER_ADDRESS, "");

        if(prefCentralUnitUrl.isEmpty()){
            Log.d(TAG, "URL from preferences is empty");
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_fragment, new SettingsFragment_(), "Settings")
                    .addToBackStack(null)
                    .commit();
            Toast.makeText(getActivity(), R.string.error_message_server_address, Toast.LENGTH_LONG).show();
            return;
        }

        establishConnection();
    }

    private void establishConnection(){
        String argCentralUnitUrl = null;
        Long argContainerId = null;
        try {
        /*
        Get central unit url and ID from bundle arguments of the fragment
        */
            argCentralUnitUrl = getArguments().getString(AppConstants.EXTRA_CENTRAL_UNIT_URL);
            argContainerId = getArguments().getLong(AppConstants.EXTRA_ITEM_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        For the first instantiation of the class arguments will not be used and argCentralUnitUrl will be null
        Than need to get URL from preferences
        */
        finalCentralUnitUrl = argCentralUnitUrl != null ? argCentralUnitUrl : prefCentralUnitUrl;
        Log.d(TAG, "finalCentralUnitUrl  " + finalCentralUnitUrl);


        centralUnitConnection = CentralUnitConnection.getInstance();
        Log.d(TAG, "centralUnitConnection  " + centralUnitConnection.getName());

        /*
        for the first instantiation of the class Intent will not be used and ID will be null
        if id is null than it is parent container
        */
        final Long containerId = argContainerId != null ? argContainerId : centralUnitConnection.getId();
        mContainer = (Container) centralUnitConnection.getItemById(containerId);

    }


}

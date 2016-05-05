package fi.oulu.tol.esde009.ohapclient009.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Device;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.net.MalformedURLException;
import java.net.URL;

import fi.oulu.tol.esde009.ohapclient009.R;
import fi.oulu.tol.esde009.ohapclient009.networking.CentralUnitConnection;
import fi.oulu.tol.esde009.ohapclient009.utils.AppConstants;

/**
 * Fragment to control UI and behaviour of Device
 */
@EFragment(R.layout.fragment_device)
public class DeviceFragment extends Fragment {

    private Device mDevice;

    private final static String TAG = "Debug_DeviceFragment";

    @ViewById
    TextView textView_path,
             textView_name,
             textView_description;

    @AfterViews
    void initViews(){
        String parentName = mDevice.getParent().getName();
        String deviceName = mDevice.getName();

        getActivity().setTitle( parentName + " > " + deviceName);

        textView_path.setText(parentName);
        textView_name.setText(deviceName);
        textView_description.setText(mDevice.getDescription());
    }


    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach()");
        super.onAttach(context);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefCentralUnitConnection = sharedPreferences.getString(SettingsFragment_.CENTRAL_UNIT_URL, "");

        String argCentralUnitConnection = getArguments().getString(AppConstants.EXTRA_CENTRAL_UNIT_URL);
        final String finalCentralUnitConnection = argCentralUnitConnection != null ? argCentralUnitConnection : prefCentralUnitConnection;

        Long deviceId = getArguments().getLong(AppConstants.EXTRA_ITEM_ID);

//        CentralUnitConnection centralUnitConnection = CentralUnitConnection.getInstance();
        /*
         * Get registered mDevice that user click from central unit
         * */
        mDevice = (Device) CentralUnitConnection.getInstance().getItemById(deviceId);

    }
}

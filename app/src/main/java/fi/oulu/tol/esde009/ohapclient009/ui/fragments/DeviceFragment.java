package fi.oulu.tol.esde009.ohapclient009.ui.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.opimobi.ohap.Device;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;

import java.text.DecimalFormat;

import fi.oulu.tol.esde009.ohapclient009.R;
import fi.oulu.tol.esde009.ohapclient009.network.CentralUnitConnection;
import fi.oulu.tol.esde009.ohapclient009.utils.AppConstants;
import fi.oulu.tol.esde009.ohapclient009.utils.Metrics;

/**
 * Fragment to control UI and behaviour of Device
 */
@EFragment(R.layout.fragment_device)
public class DeviceFragment extends Fragment {

    private CentralUnitConnection mCentralUnitConnection;

    private Device mDevice;
    private Context mContext;
    private View mInflaterView;
    private Device.Type mDeviceType;
    private Device.ValueType mDeviceValueType;

    private final static String TAG = "Debug_DeviceFragment";

    @ViewById(R.id.textView_path)
    TextView textViewPath;

    @ViewById(R.id.textView_description)
    TextView textViewDescription;

    @ViewById(R.id.textView_name)
    TextView textViewName;

    @ViewById(R.id.relativelayout_device)
    RelativeLayout relativeLayoutDevice;

    @AfterViews
    void initViews(){
        Log.d(TAG, "initView()");
        String parentName = mDevice.getParent().getName();
        String deviceName = mDevice.getName();

        try {
            getActivity().setTitle( parentName + " > " + deviceName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        textViewPath.setText(parentName);
        textViewName.setText(deviceName);
        textViewDescription.setText(mDevice.getDescription());

        /*
        * Choose resource of the xml file to create a view and inflate for the specified type of the device
        * */
        createInflaterView();
        relativeLayoutDevice.addView(mInflaterView);
    }


    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach()");
        super.onAttach(context);
        mContext = context;

        /*
         * Get ID from arguments
         * Get registered mDevice that user click from central unit
         * */
        Long deviceId = getArguments().getLong(AppConstants.EXTRA_ITEM_ID);
        mCentralUnitConnection = CentralUnitConnection.getInstance();
        mDevice = (Device) mCentralUnitConnection.getItemById(deviceId);
        mDeviceType = mDevice.getType();
        mDeviceValueType = mDevice.getValueType();
    }

    private void createInflaterView(){
        Resources mResources = getResources();
        int layoutId;
        /*
        * Choose the specified xml layout depending on the Device type and ValueType
        * */
        if(mDeviceType == Device.Type.ACTUATOR){
            layoutId = (mDeviceValueType == Device.ValueType.DECIMAL) ? R.layout.inflater_device_actuator_decimal
                    : R.layout.inflater_device_actuator_binary;
        }
        else{
            layoutId = (mDeviceValueType == Device.ValueType.DECIMAL) ? R.layout.inflater_device_sensor_decimal :
                    R.layout.inflater_device_sensor_binary;
        }

        XmlPullParser resource = mResources.getLayout(layoutId);

        mInflaterView = LayoutInflater.from(mContext).inflate(resource, relativeLayoutDevice, false);

        fillFragmentContent();
    }

    private void fillFragmentContent(){

        if(mDeviceType == Device.Type.ACTUATOR){
            if(mDeviceValueType == Device.ValueType.DECIMAL){
                inflateDeviceDecimalActuator();
            }
            else{
                inflateDeviceBinaryActuator();
            }
        }
        else{
            if(mDeviceValueType == Device.ValueType.DECIMAL){
                inflateDeviceDecimalSensor();
            }
            else{
                inflateDeviceBinarySensor();
            }
        }
    }

    /*
    *  Actuators
    * */

    private void inflateDeviceBinaryActuator(){

        Switch switchBinaryActuator = (Switch) mInflaterView.findViewById(R.id.switchBinaryActuator);

        Log.d(TAG, "Device " + mDevice.getName() +  " state : " + mDevice.getBinaryValue());

        switchBinaryActuator.setChecked(mDevice.getBinaryValue());
        switchBinaryActuator.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDevice.changeBinaryValue(isChecked);
            Log.d(TAG, "Device " + mDevice.getName() +  " state is changed on " + isChecked);
        });
    }

    private void inflateDeviceDecimalActuator(){

        final int TICK = 10;
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // Layout views
        SeekBar seekBar = (SeekBar) mInflaterView.findViewById(R.id.seekBarDecimalActuator);
        TextView tvMinValue = (TextView) mInflaterView.findViewById(R.id.tvDecimalActuatorMinValue);
        TextView tvMaxValue = (TextView) mInflaterView.findViewById(R.id.tvDecimalActuatorMaxValue);
        TextView tvCurrentValue = (TextView) mInflaterView.findViewById(R.id.tvDecimalActuatorCurrentValue);
        TextView tvUnit = (TextView) mInflaterView.findViewById(R.id.tvDecimalActuatorUnit);

        Button bIncreaseOne = (Button) mInflaterView.findViewById(R.id.butIncreaseOne);
        Button bDecreaseOne = (Button) mInflaterView.findViewById(R.id.butDecreaseOne);
        Button bIncreaseHalf = (Button) mInflaterView.findViewById(R.id.butIncreaseHalf);
        Button bDecreaseHalf = (Button) mInflaterView.findViewById(R.id.butDecreaseHalf);

        // Device values
        // Count interval between minimal and maximum values
        final double minValDouble = mDevice.getMinValue();
        final double maxValDouble = mDevice.getMaxValue();

        // Counting interval from minimum to maximum
        final double intervalDouble = (Math.abs(minValDouble) + Math.abs(maxValDouble));
        final int interval = (int) intervalDouble * TICK;

        // Initialize device current value and set it to the textView
        double curValDouble = mDevice.getDecimalValue();

        // Find what is the position of 'zero' on the seekBar
        final int maxValue = (int) maxValDouble * TICK;
        final int positionZero = interval - maxValue;
        final double positionZeroDouble = intervalDouble - maxValDouble;
        int curValue = (int) curValDouble * TICK;
        int curPosition = positionZero + curValue;

//      Set interval for the seekBar * TICK for the tick of 1/TICK
        seekBar.setMax(interval);
//      Show device min and max values on the seekBar
        tvMinValue.setText(String.valueOf(minValDouble));
        tvMaxValue.setText(String.valueOf(maxValDouble));
//      Set device UNIT to TextView
        String unit = chooseUnit();
        tvUnit.setText(unit);
        String textButIncreaseOne = getString(R.string.text_button_increase_one) + unit;
        String textButDecreaseOne = getString(R.string.text_button_decrease_one) + unit;
        String textButIncreaseHalf = getString(R.string.text_button_increase_half) + unit;
        String textButDecreaseHalf = getString(R.string.text_button_decrease_half) + unit;
//      Set text for buttons
        bIncreaseOne.setText(textButIncreaseOne);
        bDecreaseOne.setText(textButDecreaseOne);
        bIncreaseHalf.setText(textButIncreaseHalf);
        bDecreaseHalf.setText(textButDecreaseHalf);

//      Show device current value
        tvCurrentValue.setText(decimalFormat.format(curValDouble));
//      Set current position on the seekBar
        seekBar.setProgress(curPosition);
        Log.d(TAG, "Position : " + curPosition);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            double value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                double progressDouble = (double) progress / TICK;
                // value depending from the 'zero' of seekBar if the interval start from negative number
                // set current value as 'zero' of the seekBar + current position in decimal format

                /*
                *
                *  WARNING ! Solution is only suitable for intervals :
                *  1) start from 0 ; example [0;28]
                *  2) with equal volumes to both sides from 'zero' [-50; 50]
                *
                * */
                value = positionZeroDouble == intervalDouble / 2 ? progressDouble - maxValDouble : progressDouble;
                // get decimal value of progress in format #.#
                String valueDecFormat = decimalFormat.format(value);

                tvCurrentValue.setText(valueDecFormat);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double decimalValueFloor = Math.floor(value * 10) / 10;
                mDevice.changeDecimalValue(decimalValueFloor);
                Log.d(TAG, " SEND Progress :" + value);
            }
        });

        /*
        *  Functionality for buttons
        * */

        /*
        *
        *  WARNING ! Solution is only suitable for intervals :
        *  1) start from 0 ; example [0;28]
        *  2) with equal volumes to both sides from 'zero' [-50; 50]
        *
        * */

        bIncreaseOne.setOnClickListener(v -> {
            int newPosition = seekBar.getProgress() + TICK;
            double decimalPosition = (double) newPosition / TICK;

            double decimalValue = positionZeroDouble == intervalDouble / 2 ? decimalPosition - maxValDouble : decimalPosition;

            Log.d(TAG, "Position : " + newPosition);
            Log.d(TAG, "decimalPosition : " + decimalPosition);
            Log.d(TAG, "decimalPosition - maxValDouble : " + (decimalPosition - maxValDouble));
            Log.d(TAG, "decimalValue : " + decimalValue);

//           New value should not be more than max position when increasing
            if(decimalValue <= maxValDouble) {
                seekBar.setProgress(newPosition);
                tvCurrentValue.setText(decimalFormat.format(decimalValue));
//              Format the number '#.#########' to the view of '#.#'
                double decimalValueFloor = Math.floor(decimalValue * 10) / 10;
                mDevice.changeDecimalValue(decimalValueFloor);
            }
        });

        bDecreaseOne.setOnClickListener(v -> {
            int newPosition = seekBar.getProgress() - TICK;
            double decimalPosition = (double) newPosition / TICK;

            double decimalValue = positionZeroDouble == intervalDouble / 2 ? decimalPosition - maxValDouble : decimalPosition;

            if(decimalValue >= minValDouble) {
                seekBar.setProgress(newPosition);
                tvCurrentValue.setText(decimalFormat.format(decimalValue));
//              Format the number '#.#########' to the view of '#.#'
                double decimalValueFloor = Math.floor(decimalValue * 10) / 10;
                mDevice.changeDecimalValue(decimalValueFloor);
            }
        });

        bIncreaseHalf.setOnClickListener(v -> {
            int newPosition = seekBar.getProgress() + TICK/2;
            double decimalPosition = (double) newPosition / TICK;
            double decimalValue = positionZeroDouble == intervalDouble / 2 ? decimalPosition - maxValDouble : decimalPosition;

            if(decimalValue <= maxValDouble) {
                seekBar.setProgress(newPosition);
                tvCurrentValue.setText(decimalFormat.format(decimalValue));
//              Format the number '#.#########' to the view of '#.#'
                double decimalValueFloor = Math.floor(decimalValue * 10) / 10;
                mDevice.changeDecimalValue(decimalValueFloor);
            }
        });

        bDecreaseHalf.setOnClickListener(v -> {
            int newPosition = seekBar.getProgress() - TICK/2;
            double decimalPosition = (double) newPosition / TICK;
            double decimalValue = positionZeroDouble == intervalDouble / 2 ? decimalPosition - maxValDouble : decimalPosition;

            if(decimalValue >= minValDouble) {
                seekBar.setProgress(newPosition);
                tvCurrentValue.setText(decimalFormat.format(decimalValue));
//              Format the number '#.#########' to the view of '#.#'
                double decimalValueFloor = Math.floor(decimalValue * 10) / 10;
                mDevice.changeDecimalValue(decimalValueFloor);
            }
        });

    }

    /*
    *  Sensors
    * */

    //The value of the item cannot be be changed by the client
    private void inflateDeviceBinarySensor(){
        TextView tvBinarySensorValue = (TextView) mInflaterView.findViewById(R.id.tvBinarySensorValue);

        final int condition = mDevice.getBinaryValue() ? R.string.text_device_condition_on : R.string.text_device_condition_off;
        String conditionString = getString(condition);
        tvBinarySensorValue.setText(conditionString);
    }

    // The value of the item cannot be be changed by the client
    private void inflateDeviceDecimalSensor(){
        TextView tvCurrentValue = (TextView) mInflaterView.findViewById(R.id.tvDecimalSensorValue);
        TextView tvUnit = (TextView) mInflaterView.findViewById(R.id.tvDecimalSensorUnit);

        tvCurrentValue.setText(mDevice.getDecimalValue() + "");

        String unit = chooseUnit();
        tvUnit.setText(unit);
    }

    private String chooseUnit(){
        String unit;
        switch (mDevice.getUnit()){
            case Metrics.DEVICE_UNIT_CELSIUS:
                unit = Metrics.UNIT_CELSIUS;
                break;
            case Metrics.DEVICE_UNIT_FAHRENHEIT:
                unit = Metrics.UNIT_FAHRENHEIT;
                break;
            default:
                unit = Metrics.DEFAULT;
                break;
        }
        return unit;
    }
}

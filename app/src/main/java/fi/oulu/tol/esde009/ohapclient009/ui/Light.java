package fi.oulu.tol.esde009.ohapclient009.ui;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.opimobi.ohap.Device;

import fi.oulu.tol.esde009.ohapclient009.R;

/**
 * Created by bel on 14.04.16.
 */
public class Light {

    private static final String TAG = "Debug_Light";

    private Activity activity;
    private Device device;
    private LayoutInflater layoutInflater;
    private RelativeLayout uiRelativeLayout;

    public Light(Activity activity,
                 Device device,
                 LayoutInflater layoutInflater,
                 RelativeLayout relativeLayout){
        this.activity = activity;
        this.device = device;
        this.layoutInflater = layoutInflater;
        this.uiRelativeLayout = relativeLayout;
    }

    public void realizeUi(){
        layoutInflater.inflate(R.layout.ui_light, uiRelativeLayout, true);

        Button bLightOn = (Button) activity.findViewById(R.id.button_on);
        Button bLightOff = (Button) activity.findViewById(R.id.button_off);
        final SeekBar sbLightChange = (SeekBar) activity.findViewById(R.id.sb_value_change);
        final TextView tvLightValue = (TextView) activity.findViewById(R.id.tv_value);

        tvLightValue.setText(device.getDecimalValue()+"%");
        sbLightChange.setProgress((int)device.getDecimalValue());

        bLightOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.setDecimalValue(device.getMaxValue());
                sbLightChange.setProgress(sbLightChange.getMax());
                tvLightValue.setText(device.getDecimalValue() + "%");
            }
        });

        bLightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.setDecimalValue(device.getMinValue());
                sbLightChange.setProgress(0);
                tvLightValue.setText(device.getDecimalValue() + "%");
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
                device.setDecimalValue(lightValueInt);
            }
        });
    }

}

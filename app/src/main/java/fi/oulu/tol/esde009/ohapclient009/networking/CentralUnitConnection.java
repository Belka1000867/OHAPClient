package fi.oulu.tol.esde009.ohapclient009.networking;

import android.util.Log;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Container;
import com.opimobi.ohap.Device;

import java.net.URL;

/**
 * Central Unit management
 * Connection to Central Unit
 *
 *
 */
public class CentralUnitConnection extends CentralUnit {

    //count listeners
    private int nListeners;

    private static String DEBUG_TAG = "Debug_CentralUnitConnection";

    public CentralUnitConnection(URL url){
        super(url);
        Log.d(DEBUG_TAG, "CentralUnitConnection()");

        setName("Central Unit 009");
        setDescription("Main Central Unit in the system");

        Log.d(DEBUG_TAG, "Container hall");
        Container hall = new Container(this, 1);
        hall.setName("First floor");
        hall.setDescription("Main room in the house");
        this.itemAddedEventSource.fireEvent(hall);

        Log.d(DEBUG_TAG, "Device lampDevice");
        Device lampDevice = new Device(hall, 2, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        lampDevice.setName("Ceiling Lamp");
        lampDevice.setDescription("Device to manipulate light");
        lampDevice.setCategory(Device.LIGHT);
        lampDevice.setMinMaxValues(0, 100);
        lampDevice.setDecimalValue(50);
        //hall.itemAddedEventSource.fireEvent(lampDevice);

        Log.d(DEBUG_TAG, "Device windowDevice");
        Device windowDevice = new Device(hall, 3, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        windowDevice.setName("Windows Jalousie");
        windowDevice.setDescription("Device to manipulate jalousie openness");
        windowDevice.setCategory(Device.JEALOUSE);
        windowDevice.setMinMaxValues(0, 100);
        windowDevice.setDecimalValue(windowDevice.getMaxValue() / 2);
        //hall.itemAddedEventSource.fireEvent(windowDevice);

        Log.d(DEBUG_TAG, "Device heatingDevice");
        Device heatingDevice = new Device(hall, 4, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        heatingDevice.setName("Heating system");
        heatingDevice.setDescription("Device to manipulate temperature");
        heatingDevice.setCategory(Device.HEATING);
        heatingDevice.setMinMaxValues(0, 100);
        heatingDevice.setDecimalValue(50);
        //hall.itemAddedEventSource.fireEvent(heatingDevice);

        Device oxygenSensorDevice = new Device(hall, 5, Device.Type.SENSOR, Device.ValueType.DECIMAL);
        oxygenSensorDevice.setName("Oxygen balance");
        oxygenSensorDevice.setDescription("Oxygen monitoring");
        oxygenSensorDevice.setMinMaxValues(0, 100);
        oxygenSensorDevice.setDecimalValue(40);
        //hall.itemAddedEventSource.fireEvent(oxygenSensorDevice);

        Log.d(DEBUG_TAG, "2nd floor");
        Container floor2 = new Container(this, 6);
        floor2.setName("2nd floor");
        floor2.setDescription("2nd floor");
        this.itemAddedEventSource.fireEvent(floor2);

        Log.d(DEBUG_TAG, "Device windowDevice 2");
        Device windowDevice2 = new Device(floor2, 7, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        windowDevice2.setName("Windows Jalousie");
        windowDevice2.setDescription("Device to manipulate jalousie openness");
        windowDevice2.setCategory(Device.JEALOUSE);
        windowDevice2.setMinMaxValues(0, 100);
        windowDevice2.setDecimalValue(windowDevice.getMaxValue() / 2);
        //floor2.itemAddedEventSource.fireEvent(windowDevice2);

        Log.d(DEBUG_TAG, "Device heatingDevice 2");
        Device heatingDevice2 = new Device(floor2, 8, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        heatingDevice2.setName("Heating system");
        heatingDevice2.setDescription("Device to manipulate temperature");
        heatingDevice2.setCategory(Device.HEATING);
        heatingDevice2.setMinMaxValues(0, 100);
        heatingDevice2.setDecimalValue(50);
        //floor2.itemAddedEventSource.fireEvent(heatingDevice2);

        Log.d(DEBUG_TAG, "room1");
        Container room1 = new Container(floor2, 9);
        room1.setName("room 1");
        room1.setDescription("room 1");
        //floor2.itemAddedEventSource.fireEvent(room1);

        Log.d(DEBUG_TAG, "Device lampDevice 3");
        Device lampDevice3 = new Device(room1, 10, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        lampDevice3.setName("Ceiling Lamp");
        lampDevice3.setDescription("Device to manipulate light");
        lampDevice3.setCategory(Device.LIGHT);
        lampDevice3.setMinMaxValues(0, 100);
        lampDevice3.setDecimalValue(50);
        //room1.itemAddedEventSource.fireEvent(lampDevice3);

        Log.d(DEBUG_TAG, "Device windowDevice ");
        Device heatingDevice3 = new Device(room1, 11, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        heatingDevice3.setName("Heating system");
        heatingDevice3.setDescription("Device to manipulate temperature");
        heatingDevice3.setCategory(Device.HEATING);
        heatingDevice3.setMinMaxValues(0, 100);
        heatingDevice3.setDecimalValue(50);
        //room1.itemAddedEventSource.fireEvent(heatingDevice3);

        Log.d(DEBUG_TAG, "room 2");
        Container room2 = new Container(floor2, 12);
        room2.setName("room 2");
        room2.setDescription("room 2");
        //floor2.itemAddedEventSource.fireEvent(room2);

        Log.d(DEBUG_TAG, "Device lampDevice 4");
        Device lampDevice4 = new Device(room2, 13, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        lampDevice4.setName("Ceiling Lamp");
        lampDevice4.setDescription("Device to manipulate light");
        lampDevice4.setCategory(Device.LIGHT);
        lampDevice4.setMinMaxValues(0, 100);
        lampDevice4.setDecimalValue(50);
        //room2.itemAddedEventSource.fireEvent(lampDevice4);

    }

    @Override
    protected void changeBinaryValue(Device device, boolean value) {

    }

    @Override
    protected void changeDecimalValue(Device device, double value) {

    }

    @Override
    protected void listeningStateChanged(Container container, boolean listening) {
        Log.d(DEBUG_TAG, "listening" + listening);

        if(listening) {
            nListeners++;
            sendListeningStart();

            if(nListeners==1)
                startNetworking();
        }
        else {
            sendListeningStop(container);
            nListeners--;

            if(nListeners==0)
                stopNetworking();
        }

    }

    private void startNetworking(){

    }

    private void stopNetworking(){

    }

    private void sendListeningStart(){

    }

    private void sendListeningStop(Container container){

    }

    private long getUniqueId(){
        return this.getItemCount()+1;
    }
}

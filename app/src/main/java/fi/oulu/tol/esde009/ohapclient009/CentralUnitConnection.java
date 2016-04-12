package fi.oulu.tol.esde009.ohapclient009;

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
        setDescription("Main Central Unit in the system.");

        Log.d(DEBUG_TAG, "Container hall");
        Container hall = new Container(this, getUniqueId());
        hall.setName("Hall");
        hall.setDescription("Main room in the house");
        this.itemAddedEventSource.fireEvent(hall);

        Log.d(DEBUG_TAG, "Device lampDevice");
        Device lampDevice = new Device(hall, this.getItemCount() + hall.getItemCount()+1, Device.Type.ACTUATOR, Device.ValueType.BINARY);
        lampDevice.setName("Ceiling Lamp");
        lampDevice.setDescription("Device to manipulate light.");
        lampDevice.setBinaryValue(true);
        //itemRegisteredEventSource.fireEvent(lampDevice);
        hall.itemAddedEventSource.fireEvent(lampDevice);

        Log.d(DEBUG_TAG, "Device windowDevice");
        Device windowDevice = new Device(hall, this.getItemCount() + hall.getItemCount()+1, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        windowDevice.setName("Windows Jalousie");
        windowDevice.setDescription("Device to manipulate jalousie openness.");
        windowDevice.setMinMaxValues(0, 100);
        windowDevice.setDecimalValue(50);
        //itemRegisteredEventSource.fireEvent(windowDevice);
        hall.itemAddedEventSource.fireEvent(windowDevice);

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
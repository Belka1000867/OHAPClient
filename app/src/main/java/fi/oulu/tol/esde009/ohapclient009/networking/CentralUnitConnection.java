package fi.oulu.tol.esde009.ohapclient009.networking;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Container;
import com.opimobi.ohap.Device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import fi.oulu.tol.esde009.ohapclient009.R;
import fi.oulu.tol.esde009.ohapclient009.utils.CentralUnitObserver;

/**
 * Central Unit management
 * Connection to Central Unit
 *
 *
 */
public class CentralUnitConnection extends CentralUnit {

    // Networking code running or not
    private boolean running = false;

    // The thread that takes care of sending messages to the server.
    private HandlerThread handlerThread = null;

    /*
    * A class derived from Thread you will implement
    * This one initializes all the networking objects and then waits for incoming data in a while loop while running is true.
    * When the networking is closed, the loop ends and all networking objects are closed and null'ed.
    * */
    private IncomingThread incomingThread = null;

    /*
    * Using which we connect to the server and send and receive data in the two threads mentioned
    * Connecting to the server may block so it has to be done in a separate thread from the main app thread
    * */
    private Socket socket = null;

    //does the actual sending of data to the server within the HandlerThread.
    private Handler outgoingMessageHandler = null;

    // Used to write data to the socket.
    private OutputStream outputStream = null;

    // Used to read data from the socket
    private InputStream inputStream = null;

    // Url of the server
    private URL url = null;

    // Who is notified about events related to the communication with the server, mainly about data arriving from the echo server
    private CentralUnitObserver centralUnitObserver = null;

    //count listeners
    private int nListeners;

    private static final String TAG = "Debug_CentrUnConnection";

    private static CentralUnitConnection instance = null;

    public static synchronized CentralUnitConnection getInstance() {
        if (null == instance) {
            try {
                instance = new CentralUnitConnection();
            } catch (MalformedURLException e) {
                Log.d(TAG, "URL was not in shape!");
                e.printStackTrace();
            }
        }
        return instance;
    }

    private CentralUnitConnection() throws MalformedURLException {
        super(new URL("http://ohap.opimobi.com:8080/"));
        Log.d(TAG, "CentralUnitConnection()");
        hardCodingCentralContainer();
    }

    public void initialize(URL url, CentralUnitObserver centralUnitObserver)  {
        this.url = url;
        this.centralUnitObserver = centralUnitObserver;
    }


    @Override
    protected void changeBinaryValue(Device device, boolean value) {

    }

    @Override
    protected void changeDecimalValue(Device device, double value) {

    }

    @Override
    protected void listeningStateChanged(Container container, boolean listening) {
        Log.d(TAG, "listening" + listening);

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

    private void sendListeningStart(){

    }

    private void sendListeningStop(Container container){

    }

    /*
    *    Private methods
    * */

    private void startNetworking(){
        Log.d(TAG, "startNetworking()");
        running = true;

        handlerThread = new HandlerThread("NetworkingThread");
        handlerThread.start();

        incomingThread = new IncomingThread();
        incomingThread.start();
    }

    private void stopNetworking(){
        Log.d(TAG, "stopNetworking()");
        running = false;

        handlerThread.quit();
        handlerThread = null;

        incomingThread.interrupt();
        try {
            incomingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        incomingThread = null;

        outgoingMessageHandler = null;
    }

    private class IncomingMessageAction implements Runnable {

        private String incomingMessage;

        public IncomingMessageAction(String incomingMessage) {
            Log.d(TAG, "IncomingMessageAction (String " + incomingMessage + ")");
            this.incomingMessage = incomingMessage;
        }

        @Override
        public void run() {
            Log.d(TAG, "IncomingMessageAction run() ");
            // Handle ping reply message.
            if (null != centralUnitObserver) {
                centralUnitObserver.handlePingResponse(incomingMessage);
            }
        }
    }

    private class OutgoingMessageAction implements Runnable{

        private byte[] outgoingMessage;

        public OutgoingMessageAction(String outgoingMessage){
            this.outgoingMessage = outgoingMessage.getBytes();
        }

        @Override
        public void run() {

            if(outgoingMessage.length == 0) {
                Log.d(TAG, "Outgoing Message is empty ! ");
                return;
            }

            if(socket == null) {
                Log.d(TAG, "Socket is null ! ");
                return;
            }

            if(outputStream == null) {
                Log.d(TAG, "Output stream is null ! ");
                return;
            }

            try {
                outputStream.write(outgoingMessage);
                Log.d(TAG, "Message is written to the output stream.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class IncomingThread extends Thread {

        Handler incomingHandler;

        @Override
        public void run() {
            Log.d(TAG, "IncomingThread run() ");

            Log.d(TAG, "Setting incomingHandler");
            // incoming messages are handled in the main thread's event handling queue after being received by the IncomingThread.
            incomingHandler = new Handler(Looper.getMainLooper());

            Log.d(TAG, "Create socket ");
            socket = new Socket();
            try {
                Log.d(TAG, "Setting timeout");
                // Set timeout for reading input stream
                socket.setSoTimeout(5000);

                Log.d(TAG, "Connecting socket");
                Log.d(TAG, " Host: " + url.getHost() + " port: " + url.getPort());
                socket.connect(new InetSocketAddress(url.getHost(), url.getPort()), 5000);
                Log.d(TAG, "Socket is connected");

                outputStream = socket.getOutputStream();
                Log.d(TAG, "Output stream ready");

                inputStream = socket.getInputStream();
                Log.d(TAG, "Input stream ready");

                Log.d(TAG, "Setting outgoingMessageHandler");
                // outgoing messages are handled (sent to the server) in the HandlerThread's thread.
                outgoingMessageHandler = new Handler(handlerThread.getLooper());

                byte[] incomingData = new byte[2048];

                while(running){
                    Log.d(TAG, "Running : " + running);
                    if(socket != null){
                        Log.d(TAG, "Socket :" + socket.toString());
                        try {

                            Log.d(TAG, "Reading from input stream.");
                            //socket.setSoTimeout(5000);
                            int result = inputStream.read(incomingData);

                            if(result == -1){
                                // If connection with server lost
                                running = false;
                                showError(R.string.error_message_socket_connection_lost);
                            }

                            Log.d(TAG, "Available in input stream : " + inputStream.available() );
                            Log.d(TAG, "Result of input stream : " + result);

                            if(incomingData.length > 0){
                                Log.d(TAG, "Incoming data received.");
                                String message = new String(incomingData);
                                Log.d(TAG, "Message from the server : " + message);

                                IncomingMessageAction incomingMessageAction = new IncomingMessageAction(message);
                                incomingHandler.post(incomingMessageAction);
                            }
                        } catch (IOException e) {
                            //No handling error getting message from the server as NOT getting messages from the server is the most possible result
                            Log.d(TAG, "Error while reading from input stream : " + e.getMessage());
                        }
                    }
                }

                //closing socket and setting all variables to null
                closeSocket();
            } catch (IOException e) {
                Log.d(TAG, "Problem in connecting to the server " + e.getMessage());

                running = false;
                showError(R.string.error_message_socket_connection);
            }


        }

        private void closeSocket(){
            if(socket != null){
                running = false;
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Socket is not closed : " + e.getMessage());
                }
                socket = null;
                outputStream = null;
                inputStream = null;
                outgoingMessageHandler = null;
            }
        }

        private void showError(final int errorId){
            incomingHandler.post(new Runnable() {
                @Override
                public void run() {
                    centralUnitObserver.handleErrorMessage(errorId);
                }
            });
        }
    }


    private void hardCodingCentralContainer(){
        setName("Central Unit 009");
        setDescription("Main Central Unit in the system");

        Log.d(TAG, "Container hall");
        Container hall = new Container(this, 1);
        hall.setName("First floor");
        hall.setDescription("Main room in the house");
        this.itemAddedEventSource.fireEvent(hall);

        Log.d(TAG, "Device lampDevice");
        Device lampDevice = new Device(hall, 2, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        lampDevice.setName("Ceiling Lamp");
        lampDevice.setDescription("Device to manipulate light");
        lampDevice.setCategory(Device.LIGHT);
        lampDevice.setMinMaxValues(0, 100);
        lampDevice.setDecimalValue(50);

        Log.d(TAG, "Device windowDevice");
        Device windowDevice = new Device(hall, 3, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        windowDevice.setName("Windows Jalousie");
        windowDevice.setDescription("Device to manipulate jalousie openness");
        windowDevice.setCategory(Device.JEALOUSE);
        windowDevice.setMinMaxValues(0, 100);
        windowDevice.setDecimalValue(windowDevice.getMaxValue() / 2);

        Log.d(TAG, "Device heatingDevice");
        Device heatingDevice = new Device(hall, 4, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        heatingDevice.setName("Heating system");
        heatingDevice.setDescription("Device to manipulate temperature");
        heatingDevice.setCategory(Device.HEATING);
        heatingDevice.setMinMaxValues(0, 100);
        heatingDevice.setDecimalValue(50);

        Device oxygenSensorDevice = new Device(hall, 5, Device.Type.SENSOR, Device.ValueType.DECIMAL);
        oxygenSensorDevice.setName("Oxygen balance");
        oxygenSensorDevice.setDescription("Oxygen monitoring");
        oxygenSensorDevice.setMinMaxValues(0, 100);
        oxygenSensorDevice.setDecimalValue(40);

        Log.d(TAG, "2nd floor");
        Container floor2 = new Container(this, 6);
        floor2.setName("2nd floor");
        floor2.setDescription("2nd floor");
        this.itemAddedEventSource.fireEvent(floor2);

        Log.d(TAG, "Device windowDevice 2");
        Device windowDevice2 = new Device(floor2, 7, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        windowDevice2.setName("Windows Jalousie");
        windowDevice2.setDescription("Device to manipulate jalousie openness");
        windowDevice2.setCategory(Device.JEALOUSE);
        windowDevice2.setMinMaxValues(0, 100);
        windowDevice2.setDecimalValue(windowDevice.getMaxValue() / 2);

        Log.d(TAG, "Device heatingDevice 2");
        Device heatingDevice2 = new Device(floor2, 8, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        heatingDevice2.setName("Heating system");
        heatingDevice2.setDescription("Device to manipulate temperature");
        heatingDevice2.setCategory(Device.HEATING);
        heatingDevice2.setMinMaxValues(0, 100);
        heatingDevice2.setDecimalValue(50);

        Log.d(TAG, "room1");
        Container room1 = new Container(floor2, 9);
        room1.setName("room 1");
        room1.setDescription("room 1");

        Log.d(TAG, "Device lampDevice 3");
        Device lampDevice3 = new Device(room1, 10, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        lampDevice3.setName("Ceiling Lamp");
        lampDevice3.setDescription("Device to manipulate light");
        lampDevice3.setCategory(Device.LIGHT);
        lampDevice3.setMinMaxValues(0, 100);
        lampDevice3.setDecimalValue(50);

        Log.d(TAG, "Device windowDevice ");
        Device heatingDevice3 = new Device(room1, 11, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        heatingDevice3.setName("Heating system");
        heatingDevice3.setDescription("Device to manipulate temperature");
        heatingDevice3.setCategory(Device.HEATING);
        heatingDevice3.setMinMaxValues(0, 100);
        heatingDevice3.setDecimalValue(50);

        Log.d(TAG, "room 2");
        Container room2 = new Container(floor2, 12);
        room2.setName("room 2");
        room2.setDescription("room 2");

        Log.d(TAG, "Device lampDevice 4");
        Device lampDevice4 = new Device(room2, 13, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        lampDevice4.setName("Ceiling Lamp");
        lampDevice4.setDescription("Device to manipulate light");
        lampDevice4.setCategory(Device.LIGHT);
        lampDevice4.setMinMaxValues(0, 100);
        lampDevice4.setDecimalValue(50);
    }

    /*
    * Getters and Setters
    * */

    public URL getUrl(){
        return url;
    }

}

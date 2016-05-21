package fi.oulu.tol.esde009.ohapclient009.network;

import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Container;
import com.opimobi.ohap.Device;
import com.opimobi.ohap.EventSource;
import com.opimobi.ohap.Item;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import fi.oulu.tol.esde009.ohapclient009.R;
import fi.oulu.tol.esde009.ohapclient009.utils.ConnectionObserver;
import fi.oulu.tol.esde009.ohapclient009.utils.MessageType;
import fi.oulu.tol.esde009.ohapclient009.utils.OhapErrorListener;
import fi.oulu.tol.esde009.ohapclient009.utils.ProtocolData;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Central Unit management
 * Connection to Central Unit
 */
public class CentralUnitConnection extends CentralUnit{

    // Networking code running or not
    private boolean running = false;

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

    private ConnectionObserver connectionObserver;
    private OhapErrorListener ohapErrorListener;

    // Used to write data to the socket.
    private OutputStream outputStream = null;

    // Used to read data from the socket
    private InputStream inputStream = null;

    private EventSource.Listener<Container, Item> mEventSourceListener = null;

    //count listeners
    // static ???
    private int mListeners;

    private static final String TAG = "Debug_CentrUnConnection";

    private static CentralUnitConnection instance = null;

    // Url of the server
//    private static final String url = "http://10.0.3.2:18001/";
    private static final String url = "http://ohap.opimobi.com/";

/*
* Constructor with hardcoded URL
* */
    private CentralUnitConnection() throws MalformedURLException {
        super(new URL(url));
        Log.d(TAG, "CentralUnitConnection()");
    }

    public static CentralUnitConnection getInstance() {
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

    public void initialize(URL url, ConnectionObserver connectionObserver, OhapErrorListener ohapErrorListener)  {
        setURL(url);
        this.connectionObserver = connectionObserver;
        this.ohapErrorListener = ohapErrorListener;
    }

    //sends the ping message to the echo server, if there is a connection to it.
    public void doPing(){
        Log.d(TAG, "doPing()");

            long pingIdentifier = SystemClock.uptimeMillis();

            //Log.d(TAG, "Send ping to server: " + pingIdentifier);

            OutgoingMessage outgoingMessage = new OutgoingMessage();
            outgoingMessage.integer8(MessageType.PING)
                    .integer32(pingIdentifier);

            sendOutgoingMessage(outgoingMessage);
    }

    public void logout(){
        if (null != socket) {
            Log.d(TAG, "message-logout");
            OutgoingMessage outgoingMessage = new OutgoingMessage();
            outgoingMessage.integer8(MessageType.LOGOUT)
                    .text("So Long, and Thanks for All the Fish");

            Observable.just(outgoingMessage)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(message -> new OutgoingMessageAction(message, true));
        }
    }

    public void initializeEventListener(EventSource.Listener<Container, Item> eventSourceListener){
        Log.d(TAG, "initializeEventListener()");
        this.mEventSourceListener = eventSourceListener;
    }


    @Override
    protected void changeBinaryValue(Device device, boolean value) {
        Log.d(TAG, "message-binary-changed");

        OutgoingMessage outgoingMessage = new OutgoingMessage();
        outgoingMessage.integer8(MessageType.BINARY_CHANGED)
                .integer32(device.getId())
                .binary8(value);
        sendOutgoingMessage(outgoingMessage);
    }

    @Override
    protected void changeDecimalValue(Device device, double value) {
        Log.d(TAG, "message-decimal-changed");

        OutgoingMessage outgoingMessage = new OutgoingMessage();
        outgoingMessage.integer8(MessageType.DECIMAL_CHANGED)
                .integer32(device.getId())
                .decimal64(value);
        sendOutgoingMessage(outgoingMessage);
    }

    @Override
    protected void listeningStateChanged(Container container, boolean listening) {
        Log.d(TAG, "listeningStateChanged() ");
        Log.d(TAG, "mListeners " + mListeners);

        if(listening) {
            mListeners++;
            if(mListeners ==1)
                startNetworking();

            sendListeningStart(container);
        }
        else {
            sendListeningStop(container);
            mListeners--;

            if(mListeners ==0)
                stopNetworking();
        }

    }

    /*
    *    Private methods
    * */

    //Send message-listening with the message-type-listening-start type and the mIdentifier of the given container.
    private void sendListeningStart(Container container){
        Log.d(TAG, "message-listening-start");

        OutgoingMessage outgoingMessage = new OutgoingMessage();
        outgoingMessage.integer8(MessageType.LISTENING_START)
                .integer32(container.getId());
        sendOutgoingMessage(outgoingMessage);

    }

    //Send message-listening with the message-type-listening-stop type and the mIdentifier of the given container.
    private void sendListeningStop(Container container){
        Log.d(TAG, "message-listening-stop");
        OutgoingMessage outgoingMessage = new OutgoingMessage();
        outgoingMessage.integer8(MessageType.LISTENING_STOP)
                .integer32(container.getId());
        sendOutgoingMessage(outgoingMessage);
    }

    private void startNetworking(){
        Log.d(TAG, "startNetworking()");
        running = true;

        incomingThread = new IncomingThread();
        incomingThread.start();
    }

    private void stopNetworking(){
        Log.d(TAG, "stopNetworking()");

        running = false;

        if(incomingThread != null) {
            incomingThread.interrupt();
            try {
                incomingThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "!!! InterruptedException found : " + e.getMessage());
                incomingThread.interrupt();
            }
            incomingThread = null;
        }
    }

    private void sendOutgoingMessage(OutgoingMessage outgoingMessage) {
        // RxJava
        // Send outgoing message on the io thread that is separate from the UI thread

        Observable
                .just(outgoingMessage)
                .subscribeOn(Schedulers.io())
//      used Retrolambda's  to simplify code
                .subscribe(OutgoingMessageAction::new);
    }

    private void sendError(int errorId){
        Observable.just(errorId)
                .observeOn(AndroidSchedulers.mainThread())
                        //simplified version with Lambda in Java 1.8
                .subscribe(ohapErrorListener::handleError);
    }

    private class OutgoingMessageAction{
// Just class, not Runnable, because RxJava will not handle the Runnable class
        private OutgoingMessage outgoingMessage;
        private boolean close = false;

        public OutgoingMessageAction(OutgoingMessage outgoingMessage){
            Log.d(TAG, "************* OutgoingMessage *************");
            Log.d(TAG, "Is running on the thread with id: " + Thread.currentThread().getId());
            this.outgoingMessage = outgoingMessage;
            run();
        }

        public OutgoingMessageAction(OutgoingMessage outgoingMessage, boolean close){
            Log.d(TAG, "************* OutgoingMessage close connection");
            this.outgoingMessage = outgoingMessage;
            this.close = close;
            run();
        }

        public void run() {
//              If connection with the server is already established

            if(socket != null && outputStream != null) {
                try {
                    outgoingMessage.writeTo(outputStream);
                    if (close) {
                        stopNetworking();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class IncomingThread extends Thread {

        @Override
        public void run() {
            //Log.d(TAG, "IncomingThread is running() in the thread with id: " + Thread.currentThread().getId());
            socket = new Socket();
//            Log.d(TAG, "Socket created");
            try {
                // Set timeout for reading input stream
                socket.setSoTimeout(5000);

//                Log.d(TAG, "Connecting socket");
                Log.d(TAG, " Host: " + getURL().getHost() + " port: " + getURL().getPort());
                socket.connect(new InetSocketAddress(getURL().getHost(), getURL().getPort()), 5000);
                Log.d(TAG, "Socket is connected");

                outputStream = socket.getOutputStream();
                Log.d(TAG, "Output stream ready");

                inputStream = socket.getInputStream();
                Log.d(TAG, "Input stream ready");

                OutgoingMessage outgoingMessage = new OutgoingMessage();
//              current version of the server does not check login names nor passwords.
                outgoingMessage.integer8(MessageType.LOGIN)
                        .integer8(MessageType.PROTOCOL_VERSION)
                        .text("Aleksandr")
                        .text("password");

                // Sending a login message to the OHAP server through a method
                // that create an Observable object of RxJava in a new thread
                Log.d(TAG, "message-login");
                sendOutgoingMessage(outgoingMessage);

                while(running){
                    Log.d(TAG, "* * * Running * * *");
                    Log.d(TAG, "IncomingThread is running on the thread with id: " + Thread.currentThread().getId());
                    if(socket != null){
                        IncomingMessage incomingMessage = new IncomingMessage();
                        try {
                            incomingMessage.readFrom(inputStream);

                            Log.d(TAG, "*_*_*_* Data from input stream received *_*_*_*");
                            // RxJava is used hear to decrease the amount of code and make code much more readable
                            // Transfer to Observable incoming message and
                            // execute it in the Main Thread without using Runnable object
                            // Here we start new thread for every incoming message
                            Observable.just(incomingMessage)
                                    .observeOn(AndroidSchedulers.mainThread())
                                        //simplified version with method reference with Lambda in Java 1.8
                                    .subscribe(IncomingMessageAction::new,
                                            // Error handling
                                            onError -> Log.d(TAG, "!!! onSubscribe Error : " + onError.getMessage()
                                            ));

                        } catch (IOException e) {
                            //No handling error getting message from the server as NOT getting messages from the server is the most possible result
                            Log.d(TAG, "!!! Error while reading from input stream : " + e.getMessage());

                            // If Throwable is "End of message input" than the connection lost
                            // send notification to the User
                            if(e instanceof EOFException){
                                Log.d(TAG, "!!! EOFException");
                                running = false;
                                if(ohapErrorListener != null) {
                                    //getInstance().stopListening();
                                    sendError(R.string.error_message_socket_connection_lost);
                                }
                                Log.d(TAG, "Amount of mListeners: " + mListeners);
                            }
                        } // end of try/catch loop
                    }
                } //end of while loop

                //logout before closing connection that server knows we have finished the communication
                //logout();
                // If not running anymore than close connection with the socket
                closeConnection();

            } catch (IOException e) {
                Log.d(TAG, "!!! Failed to connect to server : " + e.getMessage());
                // Stop listening to the Central Unit Container as there is no connection with the server
                getInstance().stopListening();
                if(ohapErrorListener != null) {
                    sendError(R.string.error_message_socket_connection);
                }
            }
            catch (IllegalArgumentException iae){
                Log.d(TAG, "!!! Wrong server address : " + iae.getMessage());
                getInstance().stopListening();
                if(ohapErrorListener != null) {
                    sendError(R.string.error_message_wrong_server_address);
                }
            }
        }

        private void closeConnection(){
            Log.d(TAG, "closeConnection()");
            //closing socket and setting all variables to null
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
            }
            if(mEventSourceListener != null){
                Log.d(TAG, "removeListener");
                itemAddedEventSource.removeListener(mEventSourceListener);
                itemRemovedEventSource.removeListener(mEventSourceListener);
            }
        }
    }

    private class IncomingMessageAction {

        private IncomingMessage incomingMessage;

        private Device device;
        private long mIdentifier;

        private Bundle mDataBundle;

        private static final String TAG = "Debug_IncomMessAction";

        public IncomingMessageAction(IncomingMessage incomingMessage){
            Log.d(TAG, "IncomingMessageAction()");
//            Log.d(TAG, "Running on the thread with id: " + Thread.currentThread().getId());
//            Log.d(TAG, "Looper of the main thread ? = " + (Looper.myLooper() == Looper.getMainLooper() ));
            if(incomingMessage != null) {
                this.incomingMessage = incomingMessage;

                mDataBundle = new Bundle();

                handleIncomingMessage();
            }
        }

        public void handleIncomingMessage() {
            //Log.d(TAG, "run()");
            int messageType = incomingMessage.integer8();

            if(messageType != MessageType.LOGOUT) {
                mIdentifier = incomingMessage.integer32();
            }

            switch (messageType) {
                case MessageType.LOGOUT:
                    Log.d(TAG, "----- message-type-logout -----");

                    logout();

                    break;
                case MessageType.PING:
                    Log.d(TAG, "----- message-type-ping -----");

                    handlePing();

                    break;
                case MessageType.PONG:
                    Log.d(TAG, "----- message-type-pong -----");

                    handlePong();

                    break;
                case MessageType.DECIMAL_SENSOR:
                    // The value of the item cannot be be changed by the client
                    Log.d(TAG, "----- message-type-decimal-sensor -----");

                    createDevice(Device.Type.SENSOR, Device.ValueType.DECIMAL);

                    break;
                case MessageType.DECIMAL_ACTUATOR:
                    // The value of the item can be changed by the client
                    Log.d(TAG, "----- message-type-decimal-actuator ----- ");

                    createDevice(Device.Type.ACTUATOR, Device.ValueType.DECIMAL);

                    break;
                case MessageType.BINARY_SENSOR:
                    // The value of the item cannot be be changed by the client
                    Log.d(TAG, "----- message-type-binary-sensor -----");

                    createDevice(Device.Type.SENSOR, Device.ValueType.BINARY);

                    break;
                case MessageType.BINARY_ACTUATOR:
                    // The value of the item can be changed by the client
                    Log.d(TAG, "----- message-type-binary-actuator ----- ");

                    createDevice(Device.Type.ACTUATOR, Device.ValueType.BINARY);

                    break;
                case MessageType.CONTAINER:
                    Log.d(TAG, "----- message-type-container -----");

                    createContainer();

                    break;
                case MessageType.DECIMAL_CHANGED:
                    Log.d(TAG, "----- message-type-decimal-changed ----");

                    // Change decimal-value
                    ((Device)getItemById(mIdentifier))
                            .setDecimalValue(incomingMessage.decimal64());

                    break;
                case MessageType.BINARY_CHANGED:
                    Log.d(TAG, "message-type-binary-changed");

                    // Change decimal-value
                    ((Device)getItemById(mIdentifier))
                            .setBinaryValue(incomingMessage.binary8());

                    break;
                case MessageType.ITEM_REMOVED:
                    Log.d(TAG, "message-type-item-removed");

                    getItemById(mIdentifier).destroy();

                    break;
                default:
                    Log.d(TAG, "unknown-message-type");
                    break;
            }
        }

        private void logout(){
            Log.d(TAG, "logout-error-text");

            String logoutErrorText = incomingMessage.text();

            Log.d(TAG, "LogOut text : " +  logoutErrorText);

            if(connectionObserver != null){
                connectionObserver.logout();
            }
        }

        private void handlePing(){
            // Create outgoing message
            OutgoingMessage outgoingMessage = new OutgoingMessage();

            outgoingMessage.integer8(MessageType.PONG)
                    .integer32(mIdentifier);

            sendOutgoingMessage(outgoingMessage);
        }

        private void handlePong() {
            Log.d(TAG, "pong-message-arrived: " + mIdentifier);

            if(connectionObserver != null){
                connectionObserver.handlePong();
            }
        }

        private void receiveItemData(){
            //item-data
            //item-data-parent-mIdentifier
            mDataBundle.putLong(ProtocolData.ITEM_DATA_PARENT_IDENTIFIER, incomingMessage.integer32());
            Log.d(TAG, "ITEM_DATA_PARENT_IDENTIFIER :" + mDataBundle.getLong(ProtocolData.ITEM_DATA_PARENT_IDENTIFIER));

            //item-data-name
            mDataBundle.putString(ProtocolData.ITEM_DATA_NAME, incomingMessage.text());
            Log.d(TAG, "ITEM_DATA_NAME :" + mDataBundle.getString(ProtocolData.ITEM_DATA_NAME));

            //item-data-description
            mDataBundle.putString(ProtocolData.ITEM_DATA_DESCRIPTION, incomingMessage.text());
            Log.d(TAG, "ITEM_DATA_DESCRIPTION :" + mDataBundle.getString(ProtocolData.ITEM_DATA_DESCRIPTION));

            //item-data-internal
            mDataBundle.putBoolean(ProtocolData.ITEM_DATA_INTERNAL, incomingMessage.binary8());
            Log.d(TAG, "ITEM_DATA_INTERNAL :" + mDataBundle.getBoolean(ProtocolData.ITEM_DATA_INTERNAL));

        }

        private void createDevice(Device.Type deviceType, Device.ValueType deviceValueType){

            if(Device.ValueType.DECIMAL == deviceValueType){

                //decimal-value
//                mDataMap.put(ProtocolData.DECIMAL_VALUE, incomingMessage.decimal64());
                mDataBundle.putDouble(ProtocolData.DECIMAL_VALUE, incomingMessage.decimal64());
                Log.d(TAG, "DECIMAL_VALUE :" + mDataBundle.getDouble(ProtocolData.DECIMAL_VALUE));

                //item-data
                receiveItemData();

                //decimal-min
                mDataBundle.putDouble(ProtocolData.DECIMAL_MIN, incomingMessage.decimal64());
                Log.d(TAG, "DECIMAL_MIN :" + mDataBundle.getDouble(ProtocolData.DECIMAL_MIN));

                //decimal-max
                mDataBundle.putDouble(ProtocolData.DECIMAL_MAX, incomingMessage.decimal64());
                Log.d(TAG, "DECIMAL_MAX :" + mDataBundle.getDouble(ProtocolData.DECIMAL_MAX));

                //decimal-unit
                mDataBundle.putString(ProtocolData.DECIMAL_UNIT, incomingMessage.text());
                Log.d(TAG, "DECIMAL_UNIT :" + mDataBundle.getString(ProtocolData.DECIMAL_UNIT));

                //decimal-abbreviation
                mDataBundle.putString(ProtocolData.DECIMAL_ABBREVIATION, incomingMessage.text());
                Log.d(TAG, "DECIMAL_ABBREVIATION :" + mDataBundle.getString(ProtocolData.DECIMAL_ABBREVIATION));
            }
            else{
                //binary-value
                mDataBundle.putBoolean(ProtocolData.BINARY_VALUE, incomingMessage.binary8());

                //item-data
                receiveItemData();
            }

            long parentId = mDataBundle.getLong(ProtocolData.ITEM_DATA_PARENT_IDENTIFIER);

            Container parentContainer = (Container) getItemById(parentId);
            Device newDevice = new Device(parentContainer, mIdentifier, deviceType, deviceValueType);

            newDevice.setName(mDataBundle.getString(ProtocolData.ITEM_DATA_NAME));
            newDevice.setDescription(mDataBundle.getString(ProtocolData.ITEM_DATA_DESCRIPTION));
            newDevice.setInternal(mDataBundle.getBoolean(ProtocolData.ITEM_DATA_INTERNAL));

            if(Device.ValueType.DECIMAL == deviceValueType){
                newDevice.setMinMaxValues(mDataBundle.getDouble(ProtocolData.DECIMAL_MIN), mDataBundle.getDouble(ProtocolData.DECIMAL_MAX));
                newDevice.setDecimalValue(mDataBundle.getDouble(ProtocolData.DECIMAL_VALUE));
                newDevice.setUnit(mDataBundle.getString(ProtocolData.DECIMAL_UNIT), mDataBundle.getString(ProtocolData.DECIMAL_ABBREVIATION));
            }
            else{
                newDevice.setBinaryValue(mDataBundle.getBoolean(ProtocolData.BINARY_VALUE));
            }

//            Log.d(TAG, "item-mIdentifier: " + mIdentifier);
//            Log.d(TAG, "item-data-parent-mIdentifier: " + parentId);
//            Log.d(TAG, "item-data-name: " + newDevice.getName());
//            Log.d(TAG, "item-data-description: " + newDevice.getDescription());
//            Log.d(TAG, "item-data-internal: " + newDevice.isInternal());

        }

        private void createContainer(){

            //item-data
            receiveItemData();

            long parentId = mDataBundle.getLong(ProtocolData.ITEM_DATA_PARENT_IDENTIFIER);
            String name = mDataBundle.getString(ProtocolData.ITEM_DATA_NAME);
            String description = mDataBundle.getString(ProtocolData.ITEM_DATA_DESCRIPTION);
            boolean internal = mDataBundle.getBoolean(ProtocolData.ITEM_DATA_INTERNAL);

            if (mIdentifier == MessageType.ROOT_CONTAINER) {
                Log.d(TAG, "root-item-mIdentifier: " + mIdentifier);
                setName(name);
                setDescription(description);
                setInternal(internal);
            }
            else {
                Container newContainer = new Container((Container) getItemById(parentId), mIdentifier);
                newContainer.setName(name);
                newContainer.setDescription(description);
                newContainer.setInternal(internal);
            }

//            Log.d(TAG, "item-mIdentifier: " + mIdentifier);
//            Log.d(TAG, "item-data-parent-mIdentifier: " + parentId);
//            Log.d(TAG, "item-data-name: " + name);
//            Log.d(TAG, "item-data-description: " + description);
//            Log.d(TAG, "item-data-internal: " + internal);
        }
    }

}

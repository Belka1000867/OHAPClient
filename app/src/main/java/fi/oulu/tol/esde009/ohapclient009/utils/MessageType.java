package fi.oulu.tol.esde009.ohapclient009.utils;

/**
 * Specification of Message types from client and from server
 *
 * http://ohap.opimobi.com/ohap_specification_20160311.html
 */
public class MessageType {




    /*
    * messages-from-client-to-server
    * */

    public static final int LOGIN = 0x00;
    public static final int PROTOCOL_VERSION = 0x01;
    public static final int LISTENING_START = 0x0c;
    public static final int LISTENING_STOP = 0x0d;


    /*
    * messages-from-server-to-client
    * */

    public static final int ROOT_CONTAINER = 0x00;
    public static final int LOGOUT = 0x01;
    public static final int PING = 0x02;
    public static final int PONG = 0x03;
    public static final int DECIMAL_SENSOR = 0x04;
    public static final int DECIMAL_ACTUATOR = 0x05;
    public static final int BINARY_SENSOR = 0x06;
    public static final int BINARY_ACTUATOR = 0x07;

/*
    Information about a container.
    The server informs about the previously unseen or changed container,
    which belongs to the container that is listened by the client.
*/
    public static final int CONTAINER = 0x08;
    public static final int DECIMAL_CHANGED = 0x09;
    public static final int BINARY_CHANGED = 0x0a;
    public static final int ITEM_REMOVED = 0x0b;

}

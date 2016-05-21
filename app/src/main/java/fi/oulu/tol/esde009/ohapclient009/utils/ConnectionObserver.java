package fi.oulu.tol.esde009.ohapclient009.utils;

/**
 * Created by bel on 15.05.16.
 */
public interface ConnectionObserver {

    void handlePong();

    void logout();
}

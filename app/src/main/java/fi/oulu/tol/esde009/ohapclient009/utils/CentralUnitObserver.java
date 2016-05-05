package fi.oulu.tol.esde009.ohapclient009.utils;

/**
 * Created by bel on 05.05.16.
 */
public interface CentralUnitObserver {
    void handlePingResponse(String response);

    void handleErrorMessage(int errorId);
}


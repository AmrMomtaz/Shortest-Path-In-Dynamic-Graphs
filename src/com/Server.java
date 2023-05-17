package com;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Represents the server.
 */
public interface Server extends Remote {

    /**
     * Remote method called by the client to execute a batch of operations and
     * returns list of the results of the queries.
     */
    List<Integer> executeBatch(Operation[] batch) throws RemoteException;
}

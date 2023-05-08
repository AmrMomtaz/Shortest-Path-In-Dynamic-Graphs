package com;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents an operation in batch sent by the client.
 */
public interface Operation extends Remote {

    int getA() throws RemoteException;
    int getB() throws RemoteException;
    OperationType getOperationType() throws RemoteException;

    enum OperationType implements Serializable { QUERY, ADD, DELETE }
}

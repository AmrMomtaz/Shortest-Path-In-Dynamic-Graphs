package com.client;

import com.Operation;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementation of the Operation interface.
 */
public final class OperationImpl extends UnicastRemoteObject implements Operation {
    private final int A;
    private final int B;
    private final OperationType operationType;

    public OperationImpl(int a, int b, OperationType operationType) throws RemoteException {
        super();
        this.A = a;
        this.B = b;
        this.operationType = operationType;
    }

    public int getA() throws RemoteException {
        return this.A;
    }

    public int getB() throws RemoteException {
        return this.B;
    }

    public OperationType getOperationType() throws RemoteException {
        return this.operationType;
    }

    @Override
    public String toString() {
        return "{" + A + ", " + B + ", " + operationType +'}';
    }
}

package com;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Server extends Remote {
    List<Integer> executeBatch(Operation[] batch) throws RemoteException;
}

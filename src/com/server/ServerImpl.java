package com.server;

import com.Server;
import com.Operation;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ServerImpl extends UnicastRemoteObject implements Server {

    public ServerImpl() throws RemoteException {}

    @Override
    public synchronized List<Integer> executeBatch(Operation[] batch) throws RemoteException {
        List<Integer> newList = new ArrayList<>();
        newList.add(17);
        return newList;
    }
}

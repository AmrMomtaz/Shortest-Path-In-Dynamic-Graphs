package com.client;


import com.Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class Client {

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
            Server server = (Server) registry.lookup("Update");
            List<Integer> batchResult = server.executeBatch(null);
            System.out.println(batchResult);
        } catch (Exception exception) {
            System.out.println("Exception in client side" + exception);
        }

    }
}

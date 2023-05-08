package com.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.Server;
import com.Operation;


/**
 * Represents clients main program.
 */
public class Client {

    private static final Random random = new Random();
    private static final Logger logger = LogManager.getLogger(Client.class);
    private static final int MAX_COOLDOWN = 10000; // Maximum cooldown of generating batches in ms
    private static final int REGISTRY_PORT_NUMBER = 1099;

    public static void main(String[] args) throws InterruptedException, RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", REGISTRY_PORT_NUMBER);
        Server server = (Server) registry.lookup("Update");
        while(true) {
            long startGenerationTime = System.currentTimeMillis();
            Operation[] batch = BatchGenerator.generateBatch();
            logger.info("New batch generated in " +
                    (System.currentTimeMillis() - startGenerationTime) + " ms");
            logger.info("Generated batch -> " + Arrays.toString(batch));

            List<Integer> result = server.executeBatch(batch);
            logger.info("Received response -> " + result);

            int cooldown = random.nextInt(MAX_COOLDOWN - 1000) + 1000;
            logger.info("Sleeping for " + cooldown + " ms");
            Thread.sleep(cooldown);
        }
    }
}

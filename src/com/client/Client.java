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

    /**
     * Arguments are as follows:
     * 0 -> Client ID
     * 1 -> Registry IP address
     * 2 -> Registry port number
     * 3 -> Server registry key
     * 4 -> Maximum cooldown
     * 5 -> Minimum cooldown
     * 6 -> Number of operations per batch
     * 7 -> Nodes range
     * 8 -> Query frequency
     * 9 -> Add frequency
     */
    public static void main(String[] args) throws InterruptedException, RemoteException, NotBoundException {
        if (args.length != 10) {
            logger.error("Invalid number of arguments entered");
            System.exit(-1);
        }
        final String clientID = "[client:" + args[0]+"]";
        Registry registry = LocateRegistry.getRegistry(args[1], Integer.parseInt(args[2]));
        Server server = (Server) registry.lookup(args[3]);
        final int maximumCooldown = Integer.parseInt(args[4]);
        final int minimumCooldown = Integer.parseInt(args[5]);
        final BatchGenerator batchGenerator = new BatchGenerator
                (Integer.parseInt(args[6]), Integer.parseInt(args[7]),
                 Double.parseDouble(args[8]), Double.parseDouble(args[9]));
        while(true) {
            long startGenerationTime = System.currentTimeMillis();
            Operation[] batch = batchGenerator.generateBatch();
            logger.info(clientID + " New batch generated in " +
                    (System.currentTimeMillis() - startGenerationTime) + " ms");
            logger.info(clientID + " Generated batch -> " + Arrays.toString(batch));

            List<Integer> result = server.executeBatch(batch);
            logger.info(clientID + " Received response -> " + result);

            int cooldown = random.nextInt
                    (maximumCooldown - minimumCooldown) + minimumCooldown;
            logger.info(clientID + " Sleeping for " + cooldown + " ms");
            Thread.sleep(cooldown);
        }
    }
}

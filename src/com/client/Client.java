package com.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.Server;
import com.Operation;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;


/**
 * Represents the client which generates a random batch and submit it to the server.
 */
public class Client {

    private static final Random random = new Random();
    private static Logger logger;

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
        initializeLoggerConfigs(args[0]);
        if (args.length != 10) {
            logger.error("Invalid number of arguments");
            System.exit(-1);
        }
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
            logger.info(" New batch generated in " +
                    (System.currentTimeMillis() - startGenerationTime) + " ms -> "
                     + Arrays.toString(batch));

            long startSubmitTime = System.currentTimeMillis();
            List<Integer> result = server.executeBatch(batch);
            logger.info((System.currentTimeMillis() - startSubmitTime));
            logger.info(" Received response after " +
                        (System.currentTimeMillis() - startSubmitTime)
                        + "ms -> " + result);

            int cooldown = random.nextInt
                    (maximumCooldown - minimumCooldown) + minimumCooldown;
            logger.info(" Sleeping for " + cooldown + " ms");
            Thread.sleep(cooldown);
        }
    }

    /**
     * Sets up the logger configurations
     */
    private static void initializeLoggerConfigs(String clientID) {
        ConfigurationBuilder<BuiltConfiguration> builder
                = ConfigurationBuilderFactory.newConfigurationBuilder();

        // Configuring the layout style
        LayoutComponentBuilder layoutBuilder = builder
                .newLayout("PatternLayout")
                .addAttribute("pattern", "%d{yyyy-MM-dd HH:mm:ss} [%-5p] %c{1}:%L - %m%n");

        // Adding File and Console appenders
        AppenderComponentBuilder consoleAppenderBuilder = builder
                .newAppender("Console", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
                .add(layoutBuilder);
        AppenderComponentBuilder fileAppenderBuilder = builder
                .newAppender("File", "File")
                .addAttribute("fileName", "client" + clientID + ".log")
                .addAttribute("append", false)
                .add(layoutBuilder);
        builder.add(consoleAppenderBuilder)
                .add(fileAppenderBuilder)
                .add(builder.newRootLogger(Level.INFO)
                .add(builder.newAppenderRef("File"))
                .add(builder.newAppenderRef("Console")));

        Configurator.initialize(builder.build());
        logger = LogManager.getLogger("Client");
    }
}

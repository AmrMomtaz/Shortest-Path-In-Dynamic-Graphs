import com.Server;
import com.server.ServerImpl;
import com.server.algorithm.MemoizedShortestPath;
import com.server.algorithm.ShortestPathAlgorithm;
import com.server.algorithm.StatefulShortestPath;
import com.server.algorithm.StatelessShortestPath;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

/**
 * Configures and runs RMI registry, the server and clients in separate processes.
 */
public class Main {

    private static final String PROPERTIES_FILE_NAME = "system.properties";
    private static final String ARTIFACT_PATH = System.getProperty("user.dir") +
            "\\out\\artifacts\\client_jar";
    private static Logger logger;

    public static void main(String[] args) throws IOException {
        // Reading and setting system properties
        HashMap<String, String> systemProperties = readSystemProperties();

        // Registry properties
        final String REGISTRY_IP_ADDRESS = systemProperties.get("registry.ip");
        final int REGISTRY_PORT_NUMBER = Integer.parseInt(systemProperties.get("registry.portNumber"));

        // Server properties
        final String INITIAL_GRAPH_FILENAME = systemProperties.get("server.initialGraphFileName");
        final String SHORTEST_PATH_ALGORITHM = systemProperties.get("server.shortestPathAlgorithm").toLowerCase();
        final int MINIMUM_NUMBER_OF_QUERIES_TO_EXECUTE_PARALLEL
                = Integer.parseInt(systemProperties.get("server.minNumberOfQueriesToExecuteParallel"));
        final String SERVER_REGISTRY_KEY = systemProperties.get("server.registryKey");
        final int SERVER_NUM_OF_THREADS = Integer.parseInt(systemProperties.get("server.numOfThreads"));

        // Client properties
        final int CLIENT_COUNT = Integer.parseInt(systemProperties.get("client.count"));
        final String CLIENT_MAX_COOLDOWN = systemProperties.get("client.maximumCooldown");
        final String CLIENT_MIN_COOLDOWN = systemProperties.get("client.minimumCooldown");
        final String CLIENT_NUM_OPERATION = systemProperties.get("client.numberOfOperationPerBatch");
        final String CLIENT_NODES_RANGE = systemProperties.get("client.nodesRange");
        final String CLIENT_QUERY_FREQUENCY = systemProperties.get("client.queryFrequency");
        final String CLIENT_ADD_FREQUENCY = systemProperties.get("client.addFrequency");

        initializeLoggerConfigs();

        // Starting RMI registry on localhost
        logger.info("Starting the RMI registry on localhost and port number [" + REGISTRY_PORT_NUMBER + "]");
        Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT_NUMBER);

        // Creating and binding the remote server object
        logger.info("Binding the remote server object and reading initial graph");
        ShortestPathAlgorithm shortestPathAlgorithm;
        String initialGraphFileName = Objects.requireNonNull(Main.class.getClassLoader().getResource
                (INITIAL_GRAPH_FILENAME)).getPath();
        switch (SHORTEST_PATH_ALGORITHM) {
            case "stateless" ->  shortestPathAlgorithm
                    = new StatelessShortestPath(initialGraphFileName);
            case "stateful" -> shortestPathAlgorithm
                    = new StatefulShortestPath(initialGraphFileName);
            case "memoized" -> shortestPathAlgorithm
                    = new MemoizedShortestPath(initialGraphFileName);
            default -> {
                shortestPathAlgorithm = null;
                logger.error("Unknown server algorithm configured");
                System.exit(-1);
            }
        }
        Server server = new ServerImpl
                (shortestPathAlgorithm, MINIMUM_NUMBER_OF_QUERIES_TO_EXECUTE_PARALLEL,
                 SERVER_NUM_OF_THREADS);
        try {
            registry.bind(SERVER_REGISTRY_KEY, server);
        } catch (AlreadyBoundException e) {
            registry.rebind(SERVER_REGISTRY_KEY, server);
        }
        logger.info("Server started successfully");

        for (int clientID = 1 ; clientID <= CLIENT_COUNT ; clientID++) {
            // Creating and running the clients processes
            ProcessBuilder processBuilder = new ProcessBuilder
                    ( "cmd", "/c", "start", "Client " + clientID, "java", "-jar", "client.jar", clientID + "",
                            REGISTRY_IP_ADDRESS, REGISTRY_PORT_NUMBER + "", SERVER_REGISTRY_KEY,
                            CLIENT_MAX_COOLDOWN, CLIENT_MIN_COOLDOWN, CLIENT_NUM_OPERATION,
                            CLIENT_NODES_RANGE, CLIENT_QUERY_FREQUENCY, CLIENT_ADD_FREQUENCY)
                    .directory(new File(ARTIFACT_PATH));
            processBuilder.start();
        }
    }

    //
    // Private Methods
    //

    /**
     * Returns a map which contains all the system properties.
     */
    private static HashMap<String, String> readSystemProperties() throws FileNotFoundException {
        HashMap<String, String> systemProperties = new HashMap<>();
        Scanner scanner = new Scanner(new File(Objects.requireNonNull(Main.class.getClassLoader()
                .getResource(PROPERTIES_FILE_NAME)).getPath()));
        while(scanner.hasNextLine()) {
            try {
                String[] lineTokens = scanner.nextLine().split(" ");
                systemProperties.put(lineTokens[0], lineTokens[2]);
            } catch (Exception e) {
                // do nothing (ignore broken lines).
            }
        }
        scanner.close();
        return systemProperties;
    }

    /**
     * Sets up the logger configurations
     */
    private static void initializeLoggerConfigs() {
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
                .addAttribute("fileName", "server.log")
                .addAttribute("append", false)
                .add(layoutBuilder);
        builder.add(consoleAppenderBuilder)
                .add(fileAppenderBuilder)
                .add(builder.newRootLogger(Level.INFO)
                .add(builder.newAppenderRef("File"))
                .add(builder.newAppenderRef("Console")));

        Configurator.initialize(builder.build());
        logger = LogManager.getLogger(Server.class);
    }
}

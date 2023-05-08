import com.Server;
import com.server.ServerImpl;
import com.server.algorithm.MemoizedShortestPath;
import com.server.algorithm.StatefulShortestPath;
import com.server.algorithm.StatelessShortestPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Configures and runs the server and clients.
 */
public class Main {

    private static final String PROPERTIES_FILE_NAME = "system.properties";
    private static final Logger logger = LogManager.getLogger(Server.class);

    public static void main(String[] args) throws RemoteException, FileNotFoundException {
        // Reading and setting system properties
        HashMap<String, String> systemProperties = readSystemProperties();
        final int REGISTRY_PORT_NUMBER = Integer.parseInt(systemProperties.get("registry.portNumber"));
        final String INITIAL_GRAPH_FILENAME = systemProperties.get("initialGraphFileName");
        final String SHORTEST_PATH_ALGORITHM = systemProperties.get("server.shortestPathAlgorithm");

        // Starting RMI registry at localhost
        logger.info("Starting the RMI registry on localhost and port number: " + REGISTRY_PORT_NUMBER);
        Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT_NUMBER);

        // Creating and binding the remote object
        logger.info("Binding the remote server object and reading initial graph");
        Server server = new ServerImpl();
        String initialGraphFileName = Main.class.getClassLoader().getResource
                (INITIAL_GRAPH_FILENAME).getPath();
        switch (SHORTEST_PATH_ALGORITHM) {
            case "stateless" -> ServerImpl.setShortestPathAlgorithm
                    (new StatelessShortestPath(initialGraphFileName));
            case "stateful" -> ServerImpl.setShortestPathAlgorithm
                    (new StatefulShortestPath(initialGraphFileName));
            case "memoized" -> ServerImpl.setShortestPathAlgorithm
                    (new MemoizedShortestPath(initialGraphFileName));
            default -> {
                logger.error("Unknown server algorithm configured");
                System.exit(-1);
            }
        }
        try {
            registry.bind("Update", server);
        } catch (AlreadyBoundException e) {
            registry.rebind("Update", server);
        }
        logger.info("Server started successfully");
    }

    //
    // Private Methods
    //

    private static HashMap<String, String> readSystemProperties() throws FileNotFoundException {
        HashMap<String, String> systemProperties = new HashMap<>();
        Scanner scanner = new Scanner(new File(Main.class.getClassLoader()
                .getResource(PROPERTIES_FILE_NAME).getPath()));
        while(scanner.hasNextLine()) {
            String[] lineTokens = scanner.nextLine().split(" ");
            systemProperties.put(lineTokens[0], lineTokens[2]);
        }
        scanner.close();
        return systemProperties;
    }
}

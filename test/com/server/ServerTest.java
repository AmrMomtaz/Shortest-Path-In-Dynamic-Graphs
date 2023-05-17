package com.server;

import com.Operation;
import com.client.BatchGenerator;
import com.server.algorithm.MemoizedShortestPath;
import com.server.algorithm.StatefulShortestPath;
import com.server.algorithm.StatelessShortestPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ServerTest {

    private static final int TRIALS = 20;

    // Server properties
    private static String INITIAL_GRAPH_FILENAME;
    private static int MINIMUM_NUMBER_OF_QUERIES_TO_EXECUTE_PARALLEL;
    private static int SERVER_NUM_OF_THREADS;

    // Client properties
    private static int CLIENT_NUM_OPERATION ;
    private static int CLIENT_NODES_RANGE;
    private static double CLIENT_QUERY_FREQUENCY;
    private static double CLIENT_ADD_FREQUENCY;

    /**
     * Parses the system configs.
     */
    @BeforeAll
    static void readSystemConfigs() throws FileNotFoundException {
        HashMap<String, String> systemProperties = readSystemProperties();

        // Server properties
        INITIAL_GRAPH_FILENAME = systemProperties.get("server.initialGraphFileName");
        MINIMUM_NUMBER_OF_QUERIES_TO_EXECUTE_PARALLEL
                = Integer.parseInt(systemProperties.get("server.minNumberOfQueriesToExecuteParallel"));
        SERVER_NUM_OF_THREADS = Integer.parseInt(systemProperties.get("server.numOfThreads"));

        // Client properties
        CLIENT_NUM_OPERATION = Integer.parseInt(systemProperties.get("client.numberOfOperationPerBatch"));
        CLIENT_NODES_RANGE = Integer.parseInt(systemProperties.get("client.nodesRange"));
        CLIENT_QUERY_FREQUENCY = Double.parseDouble(systemProperties.get("client.queryFrequency"));
        CLIENT_ADD_FREQUENCY = Double.parseDouble(systemProperties.get("client.addFrequency"));
    }

    /**
     * Tests that correctness of the shortest path algorithms.
     */
    @Test
    public void testServer() throws FileNotFoundException, RemoteException {
        ServerImpl statelessServer
                = new ServerImpl(new StatelessShortestPath(INITIAL_GRAPH_FILENAME),
                MINIMUM_NUMBER_OF_QUERIES_TO_EXECUTE_PARALLEL, SERVER_NUM_OF_THREADS);
        List<Long> statelessRunTimes = new ArrayList<>();

        ServerImpl memoizedServer
                = new ServerImpl(new MemoizedShortestPath(INITIAL_GRAPH_FILENAME),
                MINIMUM_NUMBER_OF_QUERIES_TO_EXECUTE_PARALLEL, SERVER_NUM_OF_THREADS);
        List<Long> memoizedRunTimes = new ArrayList<>();

        ServerImpl statefulServer
                = new ServerImpl(new StatefulShortestPath(INITIAL_GRAPH_FILENAME),
                MINIMUM_NUMBER_OF_QUERIES_TO_EXECUTE_PARALLEL, SERVER_NUM_OF_THREADS);
        List<Long> statefulRunTimes = new ArrayList<>();

        BatchGenerator batchGenerator = new BatchGenerator(
                CLIENT_NUM_OPERATION, CLIENT_NODES_RANGE,
                CLIENT_QUERY_FREQUENCY, CLIENT_ADD_FREQUENCY);

        for (int i = 0 ; i < TRIALS ; i++) {
            Operation[] randomBatch = batchGenerator.generateBatch();

            long startTime = System.currentTimeMillis();
            List<Integer> statelessResult = statelessServer.executeBatch(randomBatch);
            statelessRunTimes.add(System.currentTimeMillis() - startTime);

            startTime = System.currentTimeMillis();
            List<Integer> memoizedResult = memoizedServer.executeBatch(randomBatch);
            memoizedRunTimes.add(System.currentTimeMillis() - startTime);

            startTime = System.currentTimeMillis();
            List<Integer> statefulResult = statefulServer.executeBatch(randomBatch);
            statefulRunTimes.add(System.currentTimeMillis() - startTime);

            assertArrayEquals(statefulResult.toArray(), memoizedResult.toArray());
            assertArrayEquals(memoizedResult.toArray(), statelessResult.toArray());
        }

        printStatistics(statelessRunTimes, "Stateless:");
        printStatistics(memoizedRunTimes, "Memoized:");
        printStatistics(statefulRunTimes, "Stateful:");
    }

    //
    // Private Methods
    //

    private static void printStatistics(List<Long> runtimes, String label) {
        if (runtimes.size() != TRIALS) {
            throw new IllegalArgumentException("List must contain exactly 20 values");
        }
        System.out.println(label);
        double overallSum = 0;

        for (int i = 0; i < 20; i += 5) {
            List<Long> sublist = runtimes.subList(i, i + 5);
            long sum = 0;
            for (long value : sublist) {
                sum += value;
                overallSum += value;
            }
            double average = (double) sum / sublist.size();
            System.out.printf("Average of entries %d-%d: %.2f\n", i+1, i+5, average);
        }

        double overallAverage = overallSum / runtimes.size();
        System.out.printf("Overall average: %.2f\n", overallAverage);
        System.out.println("--------------------------");
    }

    private static HashMap<String, String> readSystemProperties() throws FileNotFoundException {
        HashMap<String, String> systemProperties = new HashMap<>();
        Scanner scanner = new Scanner(new File(Objects.requireNonNull
                (ServerTest.class.getClassLoader().getResource
                ("system.properties")).getPath()));
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
}
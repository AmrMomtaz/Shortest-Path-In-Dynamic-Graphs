package com.client;

import com.Operation;
import com.Operation.OperationType;

import java.util.Random;

/**
 * Generates random batches of three types operation (add,remove,query) as following:
 *    i) Add (A) -> Adds new edge in the graph
 *    ii) Delete (D) -> deletes an edge in the graph
 *    iii) Query (Q) -> queries the shortest path between two nodes in the graph
 */
public class BatchGenerator {

    // Generator configuration
    private static final int NUMBER_OF_OPERATIONS_PER_BATCH = 1000;
    private static final int NODES_RANGE = 100; // Number of nodes in the graph
    private static final double QUERY_FREQUENCY = 0.99; // frequency of query operations in the batch (range:[0,1])
    private static final double ADD_FREQUENCY = 1; // frequency of add operations in all update queries (range:[0,1])
    private static final Random random = new Random();

    /**
     * Generates a random batch of operations.
     * First, identify the number of query operations, add and delete operations.
     * Then creating an array of these operations and finally shuffle that array.
     */
    public static Operation[] generateBatch() {
        Operation[] randomBatch = new Operation[NUMBER_OF_OPERATIONS_PER_BATCH];
        int queryOperations = (int) (QUERY_FREQUENCY * NUMBER_OF_OPERATIONS_PER_BATCH);
        int addOperations = (int) ((NUMBER_OF_OPERATIONS_PER_BATCH - queryOperations) * ADD_FREQUENCY);

        for (int i = 0 ; i < queryOperations ; i++)
            randomBatch[i] = generateRandomOperation(OperationType.QUERY);

        for (int i = queryOperations ; i < queryOperations + addOperations ; i++)
            randomBatch[i] = generateRandomOperation(OperationType.ADD);

        for (int i = queryOperations + addOperations ; i < NUMBER_OF_OPERATIONS_PER_BATCH ; i++)
            randomBatch[i] = generateRandomOperation(OperationType.DELETE);

        shuffleArray(randomBatch);
        return randomBatch;
    }

    //
    // Private Methods
    //

    /**
     * Generates a single operation of the given type.
     */
    private static Operation generateRandomOperation(OperationType type) {
        int a = random.nextInt(NODES_RANGE);
        int b = random.nextInt(NODES_RANGE);
        return new Operation(a, b, type);
    }

    /**
     * Shuffles the operation array using Fisher-Yates algorithm.
     */
    private static void shuffleArray(Operation[] array) {
        Random random = new Random();

        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Operation temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}

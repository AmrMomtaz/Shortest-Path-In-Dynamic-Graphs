package com.client;

import com.Operation;
import com.Operation.OperationType;

import java.rmi.RemoteException;
import java.util.Random;

/**
 * Generates random batches of three types operation (ADD, REMOVE, QUERY) as following:
 *    i) Add -> Adds new edge in the graph
 *    ii) Delete -> deletes an edge in the graph
 *    iii) Query -> queries the shortest path between two nodes in the graph
 */
public class BatchGenerator {

    // Generator configuration
    private final Random random;
    private final int numberOfOperationsPerBatch;
    private final int nodesRange; // Number of nodes in the graph
    private final double queryFrequency; // Frequency of query operations in the batch (range:[0,1])
    private final double addFrequency; // Frequency of add operations in all update queries (range:[0,1])

    public BatchGenerator(int numberOfOperationsPerBatch, int nodesRange,
                          double queryFrequency, double addFrequency) {

        this.random = new Random();
        this.numberOfOperationsPerBatch = numberOfOperationsPerBatch;
        this.nodesRange = nodesRange;
        this.queryFrequency = queryFrequency;
        this.addFrequency = addFrequency;
    }

    /**
     * Generates a random batch of operations.
     * First, identify the number of query operations, add and delete operations.
     * Then creating an array of these operations and finally shuffle that array.
     */
    public Operation[] generateBatch() throws RemoteException {
        Operation[] randomBatch = new Operation[numberOfOperationsPerBatch];
        int queryOperations = (int) (queryFrequency * numberOfOperationsPerBatch);
        int addOperations = (int) ((numberOfOperationsPerBatch - queryOperations) * addFrequency);

        for (int i = 0 ; i < queryOperations ; i++)
            randomBatch[i] = generateRandomOperation(OperationType.QUERY);

        for (int i = queryOperations ; i < queryOperations + addOperations ; i++)
            randomBatch[i] = generateRandomOperation(OperationType.ADD);

        for (int i = queryOperations + addOperations; i < numberOfOperationsPerBatch; i++)
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
    private Operation generateRandomOperation(OperationType type) throws RemoteException {
        int a = random.nextInt(nodesRange);
        int b = random.nextInt(nodesRange);
        return new OperationImpl(a, b, type);
    }

    /**
     * Shuffles the operation array using Fisher-Yates algorithm.
     */
    private void shuffleArray(Operation[] array) {
        Random random = new Random();

        for (int i = array.length - 1; i > 0 ; i--) {
            int j = random.nextInt(i + 1);
            Operation temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}

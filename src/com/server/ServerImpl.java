package com.server;

import com.Server;
import com.Operation;
import com.Operation.OperationType;
import com.server.algorithm.ShortestPathAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the Server interface.
 */
public class ServerImpl extends UnicastRemoteObject implements Server {

    private final Logger logger;
    private final ShortestPathAlgorithm shortestPathAlgorithm;
    private final int minimumNumberOfQueriesToExecuteInParallel;
    private final int numOfThreads;

    public ServerImpl(ShortestPathAlgorithm shortestPathAlgorithm,
                      int minimumNumberOfQueriesToExecuteInParallel,
                      int numOfThreads) throws RemoteException {
        super();
        this.shortestPathAlgorithm = shortestPathAlgorithm;
        this.minimumNumberOfQueriesToExecuteInParallel
                = minimumNumberOfQueriesToExecuteInParallel;
        this.numOfThreads = numOfThreads;
        this.logger = LogManager.getLogger(Server.class);
    }

    @Override
    public synchronized List<Integer> executeBatch(Operation[] batch) throws RemoteException {
        if (shortestPathAlgorithm == null)
            logger.error("ShortestPathAlgorithm isn't set.");
        logger.info("Received a batch -> " + getBatchContent(batch));
        long processingStartTime = System.currentTimeMillis();
        List<List<Operation>> splitBatch = splitBatch(batch);
        List<Integer> result = performTransactions(splitBatch);
        logger.info("Batch processed in " + (System.currentTimeMillis() -
                processingStartTime) + " ms -> " + result);
        return result;
    }

    //
    // Private Methods
    //

    /**
     * Performs update transactions serially and query transactions parallel
     */
    private List<Integer> performTransactions(List<List<Operation>> splitBatch) throws RemoteException {
        List<Integer> result = new ArrayList<>();
        for (List<Operation> transaction : splitBatch) {
            if (transaction.get(0).getOperationType() == OperationType.QUERY)
                result.addAll(performQueryTransaction(transaction));
            else performUpdateTransaction(transaction);
        }
        return result;
    }

    /**
     *  Performs a single query transaction parallel if the batch queries >=
     *  MINIMUM_NUMBER_OF_QUERIES_TO_EXECUTE_PARALLEL. Otherwise, performs it
     *  serially to overcome the overhead of the parallel execution.
     *  It appends the query results in the given list.
     */
    private List<Integer> performQueryTransaction(List<Operation> queryTransaction) throws RemoteException {
        List<Integer> transactionResult = new ArrayList<>(queryTransaction.size());
        if (queryTransaction.size() < minimumNumberOfQueriesToExecuteInParallel) {
            for (Operation operation : queryTransaction)
                transactionResult.add(performQueryOperation(operation));
        }
        else {
            List<List<Operation>> dividedList = divideList(queryTransaction, numOfThreads);
            ConcurrentHashMap<Integer, List<Integer>> partialResults = new ConcurrentHashMap<>(numOfThreads);
            ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
            for (int i = 0 ; i < numOfThreads ; i++) {
                List<Operation> queryOperations = dividedList.get(i);
                final int finalI = i;
                executor.submit(() -> {
                    List<Integer> result = new ArrayList<>(queryOperations.size());
                    for (Operation queryOperation : queryOperations) {
                        try {
                            result.add(performQueryOperation(queryOperation));
                        } catch (RemoteException e) {
                            logger.error(e.getMessage());
                            System.exit(-1);
                        }
                    }
                    partialResults.put(finalI, result);
                });
            }
            executor.shutdown();
            try {
                boolean finishedExecution = executor.awaitTermination(2, TimeUnit.MINUTES);
                if (! finishedExecution) {
                    logger.error("Executor didn't wait for all tasks to finish execution");
                    System.exit(-1);
                }
            } catch (InterruptedException exception) {
                logger.error(exception.getMessage());
                System.exit(-1);
            }
            for (int i = 0 ; i < numOfThreads ; i++)
                transactionResult.addAll(partialResults.get(i));
        }
        return transactionResult;
    }

    /**
     * Performs a single query operation and appends the result to the list.
     */
    private int performQueryOperation(Operation queryOperation) throws RemoteException {
        if (queryOperation.getOperationType() != OperationType.QUERY) {
            logger.error("Received UPDATE operation in query transaction");
            System.exit(-1);
        }
        return shortestPathAlgorithm.queryShortestPath
                (queryOperation.getA(), queryOperation.getB());
    }

    /**
     * Divides the queryOperations to number of parts.
     */
    private List<List<Operation>> divideList(List<Operation> list, int numParts) {
        int listSize = list.size();
        int partitionSize = (int) Math.floor((double) listSize / numParts);
        List<List<Operation>> partitions = new ArrayList<>();
        for (int i = 0 ; i < listSize ; i += partitionSize)
            partitions.add(list.subList(i, Math.min(i + partitionSize, listSize)));
        while (partitions.size() < numParts)
            partitions.add(new ArrayList<>());
        return partitions;
    }

    /**
     * Performs a single update transaction serially and updates the
     * ShortestPathAlgorithm state after processing the transaction.
     */
    private void performUpdateTransaction(List<Operation> updateTransaction) throws RemoteException {
        for (Operation operation : updateTransaction) {
            switch (operation.getOperationType()) {
                case ADD ->
                        shortestPathAlgorithm.addNewEdge(operation.getA(), operation.getB());
                case DELETE ->
                        shortestPathAlgorithm.deleteEdge(operation.getA(), operation.getB());
                default -> {
                    logger.error("Received QUERY operation in update transaction");
                    System.exit(-1);
                }
            }
        }
        shortestPathAlgorithm.updateStateIfAny();
    }

    /**
     * Accumulates consecutive updates in a single list and consecutive queries
     * in a single list in order to discriminate between these two types of operations.
     */
    private List<List<Operation>> splitBatch(Operation[] batch) throws RemoteException {
        List<List<Operation>> splitList = new ArrayList<>();
        List<Operation> currentSplit = new ArrayList<>();
        for (Operation currentOperation : batch) {
            OperationType currentOperationType = currentOperation.getOperationType();
            OperationType currentSplitType = currentSplit.isEmpty()
                    ? null
                    : currentSplit.get(0).getOperationType();
            if (currentSplit.isEmpty()
                    || currentOperationType == currentSplitType
                    || currentOperationType == OperationType.ADD && currentSplitType == OperationType.DELETE
                    || currentOperationType == OperationType.DELETE && currentSplitType == OperationType.ADD)
                currentSplit.add(currentOperation);
            else {
                splitList.add(currentSplit);
                currentSplit = new ArrayList<>();
                currentSplit.add(currentOperation);
            }
        }
        if (! currentSplit.isEmpty()) splitList.add(currentSplit);
        return splitList;
    }

    private String getBatchContent(Operation[] batch) throws RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < batch.length; i++) {
            Operation operation = batch[i];
            sb.append("{").append(operation.getA()).append(", ")
                    .append(operation.getB()).append(", ")
                    .append(operation.getOperationType()).append('}');
            if (i < batch.length - 1)  sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}

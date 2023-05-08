package com.server.algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Abstract class for algorithms to find the shortest path between nodes in a
 * dynamic directed unweighted graph.
 */
public abstract class ShortestPathAlgorithm {

    protected final HashMap<Integer, HashSet<Integer>> graph;

    public ShortestPathAlgorithm(String initialGraphFilePath) throws FileNotFoundException {
        this.graph = new HashMap<>();
        initializeGraph(initialGraphFilePath);
    }

    //
    // Abstract methods
    //

    /**
     * Adds a new edge in the graph.
     */
    public abstract void addNewEdge(int a, int b);

    /**
     * Deletes an edge.
     */
    public abstract void deleteEdge(int a, int b);

    /**
     * Returns the shortest path between two nodes.
     */
    public abstract int queryShortestPath(int a, int b);

    /**
     * Updates the state, and it is invoked after finishing a set of updates
     * on the graph.
     */
    public abstract void updateStateIfAny();

    //
    // Private methods
    //

    /**
     * Initializes the graph with the given file path.
     */
    private void initializeGraph(String initialGraphFilePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(initialGraphFilePath));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("S")) break;
            else {
                String[] splitLine = line.split(" ");
                int leftOperand = Integer.parseInt(splitLine[0]);
                int rightOperand = Integer.parseInt(splitLine[1]);
                graph.putIfAbsent(leftOperand, new HashSet<>());
                graph.get(leftOperand).add(rightOperand);
            }
        }
        scanner.close();
    }
}
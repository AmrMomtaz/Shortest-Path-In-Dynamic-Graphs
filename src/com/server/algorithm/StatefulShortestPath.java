package com.server.algorithm;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This implementation always pre-calculates all the answers in advance and when a
 * query comes it just fetches the answer from the state.
 *
 * Steps:
 * 1) When a query is sent we just fetch the answer from the state.
 * 2) If the state doesn't contain the answer we would know that the nodes aren't
 *    connected, and we would return -1 in that case.
 * 3) After updating the graph, updates the state to be consistent.
 * 4) Doesn't update the state if the updates done involves adding an edge which
 *    already exists or removing an edge which didn't exist before.
 *
 * Notes:
 * 1) The fastest implementation when there are large number of queries and low
 *    number of updates.
 * 2) Uses the largest memory space to save the whole state each time.
 */
public class StatefulShortestPath extends ShortestPathAlgorithm {

    private final HashMap<Integer, HashMap<Integer, Integer>> state;
    private boolean shouldUpdateState;

    public StatefulShortestPath(String initialGraphFilePath) throws FileNotFoundException {
        super(initialGraphFilePath);
        this.state = new HashMap<>();
        this.shouldUpdateState = true;
        this.updateStateIfAny();
    }

    @Override
    public void addNewEdge(int a, int b) {
        if (graph.containsKey(a) && graph.containsKey(b)) {
            HashSet<Integer> neighbours = graph.get(a);
            if (! neighbours.contains(b)) { // The edge didn't exist before
                neighbours.add(b);
                shouldUpdateState = true;
            }
        }
        else {
            graph.putIfAbsent(a, new HashSet<>());
            graph.putIfAbsent(b, new HashSet<>());
            graph.get(a).add(b);
            shouldUpdateState = true;
        }
    }

    @Override
    public void deleteEdge(int a, int b) {
        if (graph.containsKey(a) && graph.get(a).contains(b)) {
            graph.get(a).remove(b);
            shouldUpdateState = true;
        }
    }

    @Override
    public int queryShortestPath(int a, int b) {
        // Either A or B doesn't exist in the graph
        if (! graph.containsKey(a) || ! graph.containsKey(b)) return -1;

        // If the answer is in the state return it. Otherwise, return -1
        return state.get(a).getOrDefault(b, -1);
    }

    /**
     * Clears the old state re-calculates the new one if necessary.
     */
    @Override
    public void updateStateIfAny() {
        if (shouldUpdateState) {
            // Clears the state
            this.state.clear();
            this.shouldUpdateState = false;

            // Performs BFS for all nodes
            for (int node : graph.keySet()) {
                HashMap<Integer, Integer> stateForNode = new HashMap<>();
                Queue<Integer> queue = new LinkedList<>(graph.get(node));
                HashSet<Integer> visitedNodes = new HashSet<>();
                visitedNodes.add(node);
                this.state.put(node, stateForNode);
                int path = 1, size = queue.size();
                while (! queue.isEmpty()) {
                    while(size-- > 0) {
                        int neighbour = queue.poll();
                        if (! visitedNodes.contains(neighbour)) {
                            visitedNodes.add(neighbour);
                            stateForNode.put(neighbour, path);
                            queue.addAll(graph.get(neighbour));
                        }
                    }
                    size = queue.size();
                    path++;
                }
            }
        }
    }
}

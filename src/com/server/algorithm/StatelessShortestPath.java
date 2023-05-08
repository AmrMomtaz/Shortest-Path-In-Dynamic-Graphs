package com.server.algorithm;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This implementation doesn't keep any state.
 *
 * Steps:
 * 1) When a query comes we calculate the result each time.
 * 2) There is no state or anything needs to be updated after updating the graph.
 *    We just perform BFS each time a query comes.
 *
 * Notes:
 * 1) It works the best when there are a lot of updates and low number of queries.
 * 2) Finding the shortest path between two unconnected nodes consumes a lot of time.
 * 3) It is the simplest implementation among the three.
 */
public class StatelessShortestPath extends ShortestPathAlgorithm {

    public StatelessShortestPath(String initialGraphFilePath) throws FileNotFoundException {
        super(initialGraphFilePath);
    }

    @Override
    public void addNewEdge(int a, int b) {
        graph.putIfAbsent(a, new HashSet<>());
        graph.putIfAbsent(b, new HashSet<>());
        graph.get(a).add(b);
    }

    @Override
    public void deleteEdge(int a, int b) {
        if (graph.containsKey(a))
            graph.get(a).remove(b);
    }

    @Override
    public int queryShortestPath(int a, int b) {
        // Either A or B doesn't exist in the graph
        if (! graph.containsKey(a) || ! graph.containsKey(b)) return -1;

        // Performing BFS
        Queue<Integer> queue = new LinkedList<>(graph.get(a));
        HashSet<Integer> visitedNodes = new HashSet<>();
        visitedNodes.add(a);
        int path = 1, size = queue.size();
        while (! queue.isEmpty()) {
            while(size-- > 0) {
                int neighbour = queue.poll();
                if (! visitedNodes.contains(neighbour)) {
                    visitedNodes.add(neighbour);
                    if (b == neighbour) return path;
                    queue.addAll(graph.get(neighbour));
                }
            }
            size = queue.size();
            path++;
        }

        // Nodes aren't connected
        return -1;
    }

    @Override
    public void updateStateIfAny() {
        // Do nothing
    }
}

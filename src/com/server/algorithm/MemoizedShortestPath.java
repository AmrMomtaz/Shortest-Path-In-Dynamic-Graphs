package com.server.algorithm;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This implementation is compromise between the two extremes StatefulShortestPath &
 * StatelessShortestPath. It caches results in the state while serving queries such
 * that if a query is already served before, no need to re-calculate it.
 *
 * Steps:
 * 1) Stores pre-calculated results (it is initially empty).
 * 2) When a new query comes, checks whether the answer is cached in the state.
 * 3) If not, performs BFS to find the shortest path between node A and B.
 * 4) While performing the BFS, once reaching the answer, breaks execution (doesn't
 *    proceed finding the shortest path between A and other nodes).
 *    OPTIONAL: We can save state where to carry on execution next time but adds more overhead.
 * 5) If the execution is completed and wasn't broken (because B isn't connected to A).
 *    Marks that the execution for node A is completed (handled in completedExecution HashSet).
 *    Such that if the state didn't contain an entry for the query while having the
 *    execution for A finished, we would know that A & B aren't connected.
 * 6) If the graph is modified (either by adding or removing edges), the state is
 *    cleared along with the completedExecution set except in the following cases:
 *        i) Edges which existed before are added.
 *        ii) Edges which involve creating a new node are added.
 *        iii) Edges which didn't exist before are deleted.
 *
 * Notes:
 * 1) Has the best performance in normal circumstances.
 * 2) Has the most code complexity.
 */
public class MemoizedShortestPath extends ShortestPathAlgorithm {

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> state;
    private final ConcurrentHashMap<Integer, Boolean> completedExecution;
    private boolean shouldUpdateState;

    public MemoizedShortestPath(String initialGraphFilePath) throws FileNotFoundException {
        super(initialGraphFilePath);
        this.state = new ConcurrentHashMap<>();
        this.completedExecution = new ConcurrentHashMap<>();
        this.shouldUpdateState = false;
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

        // Checking the state
        state.putIfAbsent(a, new ConcurrentHashMap<>());
        ConcurrentHashMap<Integer, Integer> stateA = state.get(a);
        if (stateA.containsKey(b)) return stateA.get(b);

        // Checking if they are not connected
        if (completedExecution.contains(a)) return -1;

        // Performing BFS while updating the state
        Queue<Integer> queue = new LinkedList<>(graph.get(a));
        HashSet<Integer> visitedNodes = new HashSet<>();
        visitedNodes.add(a);
        int path = 1, size = queue.size();
        while (! queue.isEmpty()) {
            while(size-- > 0) {
                int neighbour = queue.poll();
                if (! visitedNodes.contains(neighbour)) {
                    visitedNodes.add(neighbour);
                    stateA.put(neighbour, path);
                    if (b == neighbour) return path;
                    queue.addAll(graph.get(neighbour));
                }
            }
            size = queue.size();
            path++;
        }

        // They are not connected
        completedExecution.put(a, true);
        return -1;
    }

    /**
     * Clears the state if necessary.
     */
    @Override
    public void updateStateIfAny() {
        if (shouldUpdateState) {
            this.state.clear();
            this.completedExecution.clear();
            this.shouldUpdateState = false;
        }
    }
}

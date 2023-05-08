package com.server.algorithm;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This implementation is compromise between the two extremes server.algorithm.StatefulShortestPath &
 * server.algorithm.StatelessShortestPath. It keeps building a state while answering queries such
 * that if a query is already answered before we won't need to calculate it again.
 *
 * Steps:
 * 1) The algorithm performs BFS to find the shortest path between nodes.
 * 2) If the given nodes aren't connected, it returns -1.
 * 3) The state stores pre-calculated results (it is initially empty).
 * 4) When a new query comes, We first check whether the answer is already
 *    calculated in the state (so we return it immediately) or not.
 * 5) If not, we perform BFS to find the shortest path between node A and B.
 * 6) While performing the BFS, once we got the answer, we break execution (we don't
 *    proceed finding the shortest path between A and other nodes).
 *    OPTIONAL: We can save state where to carry on execution next time.
 * 7) If the execution is competed and wasn't broken (because B isn't connected to A).
 *    We mark that we finished execution for node A (handled in completedExecution HashSet).
 *    Such that if the state didn't contain the answer of the query while we've already
 *    finished execution for A before, we would know that A & B aren't connected.
 * 8) If we've modified the graph (either by adding or removing edges), the state is
 *    cleared along with the completedExecution hashset except in the following cases:
 *        i) We've added an edge which existed before.
 *        ii) We've added an edge which involves creating a new node.
 *        iii) We've removed an edge which didn't exist.
 *
 * Notes:
 * 1) This implementation has the best performance in normal circumstances.
 * 2) This implementation has the most code complexity.
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

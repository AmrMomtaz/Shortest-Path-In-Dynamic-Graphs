# Incremental Calculation Of Shortest Path In Dynamic Graphs

This project is adapted from the **ACM Sigmod Programming contest 2016** and was part of **CS432: Distributed Systems** course which aims at understanding the basics of RMI/RPC implementation and applying it.

## Problem Description

In graph theory, the shortest path problem is the problem of finding a path between two 
vertices (or nodes) in a graph such that the sum of the weights of its constituent edges is 
minimized. This is a fundamental and well-studied combinatorial optimization problem with 
many practical uses: from GPS navigation to routing schemes in computer networks; search 
engines apply solutions to this problem on website interconnectivity graphs and social 
networks apply them to graphs of peoples' relationships.

In this project, the task is to answer shortest path queries on a changing graph, as quickly as 
possible. An initial graph is provided which is processes and indexed. 
Once this is done, A workload consisting of a series of 
sequential operation batches will be issued. Each operation is either a graph modification (insertion or 
removal) or a query about the shortest path between two nodes in the graph. The program is 
expected to correctly answer all queries as if all operations had been executed in the order they 
were given. 

The graphs are directed and unweighted. Input is provided via standard 
input and files, and the output must appear on the standard output and files.

## Specification

There are three operations that are applied to the original graph which can be produced by any sequence. 
The three operation types are as follows: 
* 'Q'/query: this operation needs to be answered with the distance of the shortest 
(directed) path from the first node to the second node in the current graph. The answer 
should appear as output in the form of a single line containing the decimal ASCII 
representation of the integer distance between the two nodes, i.e., the number of edges 
on a shortest directed path between them. If there is no path between the nodes or if 
either of the nodes does not exist in the graph, the answer should be -1. The distance 
between any node and itself is always 0. 
* 'A'/add: This operation requires you to modify your current graph by adding another 
edge from the first node in the operation to the second. As was the case during the 
input of the original graph input, if the edge already exists, the graph remains 
unchanged. If one (or both) of the specified endpoints of the new edge does not exist in 
the graph, it should be added. This operation should not produce any output. 
* 'D'/delete: This operation requires you to modify your current graph by removing the 
edge from the first node in the operation to the second. If the specified edge does not 
exist in the graph, the graph should remain unchanged. This operation should not 
produce any output.

## Project Structure

This section contains the overall structure of the project:
* **Project Resources:**
    * **InitialGraph.txt:** contains the initial graph with the
    format described in the problem statement.
    * **system.properties:** contains all the configuration to
    be set for the RMI registry, server and client.
* **Project Interfaces** (remote interfaces):
    * **Operation:** represents an operation to be executed
    sent from the client to the server. Could be query, add
    or delete edges.
    * **Server:** represents the server main functionalities
    which are called from the client side to be executed
    by the remote object.
* **Project Modules:**
    * **Main:** Driver code of the system. Reads and parses
    the _system.properties_ file, runs the RMI registry in a
    new process locally, initializes and binds the server
    object and creates a process for each client.
    * **Client Side:**
        * **OperationImpl:** implementation of the _Operation_ interface.
        * **BatchGenerator:** generates a new random batch
        of operations using the configured system
        properties.
        * **Client:** represents the client which generates
        new random batches using _BatchGenerator_ and
        sends them to the server.
    * **Server Side:**
        * **ShortestPathAlgorithm:** Abstract class contains
        common and main functions for any shortest
        path. Parent class for _MemoizedShortestPath_,
        _StatelessShortestPath_ and _StatefulShortestPath_.
        They are described in the next subsection.
        * **ServerImpl:** Implementation of the _Server_
        interface which uses an object of
        _ShortestPathAlgorithm_ to serve the incoming
        batch from the clients.

## Shoretest Path Algorithm

This section contains descriptions for the different shortest
path algorithm implementations used.
The main algorithm to find the shortest path between two
nodes in an unweighted directed graph is to use
**breadth-first-search (BFS)** which is used in the three different
implementations. The key difference between the three
implementations is whether we will use caching and
pre-calculated results or not.
The three implementations are as follows:
* **StatelessShortestPath:** doesn’t keep any state or cache
and calculates each incoming query.
   * **Steps:**
      1) When a query comes, calculates the result each
      time.
      2) There is no state or anything that needs to be
      updated after updating the graph.
      3) We just perform BFS each time a query comes.
   * **Notes:**
      * works the best when there are a lot of updates
      and low number of queries.
      * Finding the shortest path between two
      unconnected nodes consumes a lot of time.
      * The simplest implementation among the three.
* **StatefulShortestPath:** Always pre-calculates all the
answers in advance and when a query comes it just
fetches the answer from the state.
   * **Steps:**
      1) When a query is sent we just fetch the answer
      from the state.
      2) If the state doesn't contain the answer we would
      know that the nodes aren't connected, and we
      would return -1 in that case.
      3) After updating the graph, updates the state to be
      consistent.
      4) Doesn't update the state if the updates done
      involve adding an edge which already exists or
      removing an edge which didn't exist before.
   * **Notes:**
      * The fastest implementation when there are a
      large number of queries and low number of
      updates.
      * Uses the largest memory space to save the
      whole state each time.
* **MemoizedShortestPath:** Compromise between the two
extremes _StatefulShortestPath_ & _StatelessShortestPath_.
Caches result in the state while serving queries such that
if a query is already served before, no need to re-calculate
it.
   * **Steps:**
      1) Stores pre-calculated results (it is initially
      empty).
      2) When a new query comes, checks whether the
      answer is cached in the state.
      3) If not, performs BFS to find the shortest path
      between node A and B.
      4) While performing the BFS, once reaching the
      answer, breaks execution (doesn't proceed
      finding the shortest path between A and other
      nodes).
      **OPTIONAL**: We can save state where to carry on
      execution next time this adds more space
      overhead.
      5) If the execution is completed and wasn't broken
      (because B isn't connected to A). Marks that the
      execution for node A is completed (handled in
      completedExecution HashSet). Such that if the
      state didn't contain an entry for the query while
      having the execution for A finished, we would
      know that A & B aren't connected.
      6) If the graph is modified (either by adding or
      removing edges), the state is cleared along with
      the completedExecution set except in the
      following cases:
         * Edges which existed before are added.
         * Edges which involve creating a new node
         are added.
         * Edges which didn't exist before are deleted.
   * **Notes:**
      * Has the best performance in normal
      circumstances.
      * Has the most code complexity.

## Parallelization & Batch Processing

This section contains how the server handles the incoming
batch and how it uses parallelization.
First of all, the client sends a batch of operations of different
types with any order. The server only handles one client at a
time.
The server receives this batch of operation (using RMI). It
aggregates the consecutive query operations in a list and the
consecutive update operations (add or delete) in a list and it
groups all these lists in one single list (list of lists) while
preserving the order of the initial batch.
It uses this list and for each group of update operations it
performs the update transaction and the same for the query
transaction (transaction means a group of operations of similar
type).
For the update transaction, It performs the operations on the
graph serially. It cannot be parallelized for the correctness of
the system.
For the query transaction, It checks whether the number of
operations in this transaction is less than a configured number
in the system properties. If so, it performs the transaction
serially. Otherwise, it performs it in parallel using the
pre-defined number of threads.

## Logging

**Log4j2** as the system logger. The logs are displayed
in the system console and also written in log files.

**For the server**, it logs when the RMI registry is initialized
successfully and the remote object is created and binded and
the server is running. If any error occurred it’s shown also.
When the server receives a batch from the client. It logs the
incoming batch with its content and it logs the processing time
along with the result of the batch having the following format
as an example:

![image](https://github.com/AmrMomtaz/Shortest-Path-In-Dynamic-Graphs/assets/61145262/dc714afb-5fc3-4cff-a9c4-a7ecc6ab4bac)


**For the client**, it logs the generation of a new batch along with
the time taken for its generation and its content. Then it logs
the response received from the server. Here is an example of
the client's logs:

![image](https://github.com/AmrMomtaz/Shortest-Path-In-Dynamic-Graphs/assets/61145262/f6f7bdb6-068e-4da5-a277-48787ead5d85)

The server log file is created in the content root path while the
client's log files are created in the same location of the jar file
which runs the client. The server log file name is “server.log”
and the client’s is “client{ID}.log” where {ID} is the client’s
identifier.

## How to run

I’ve run the project on **IntelliJ IDEA** on **Windows 10** using **Java JDK
18**. 
The project depends on _apache.logging.log4j.core 2.20_ and
_JUnit5.8.1_.
If you want to run on another OS you will have to
change the process builder command in _Main.java_ according to
the OS.

To run the client, the client’s jar file must be created. I’ve
configured the IDE to create the client.jar which you might need
to set it. 
The jar is by default created in the artifact path. The
process builder uses the default artifact path to run the
command from. Just change it if the jar isn’t in its default
location according to the location of the jar.

The RMI registry runs on localhost on a separate process
created by Main.java. If it’s already running on a different
machine. You will have to change the _Main.java_ to locate it
using its IP and port number. You will also have to configure the
_SSH_ between the machine running the RMI registry process.

**Steps:**
   1) The initial graph file must have the same format and
   MUST exist in the resources directory.
   2) Configure _the system.properties_ which MUST exist in the
   resources and must be as following:
      1) All the properties must be set in the file with the
      same format.
      2) _server.shortestPathAlgorithm_ property must be set to
      be {“_stateless_”, “_stateful_”, “_memoized_”}.
      3) The number of threads must be less than or equal to
      the minimum number of query operations to
      parallelize. This is to avoid runtime errors.
   3) Build the client jar (it must be located in the default
   artifact path or change the path as mentioned above).
   4) Run Main.java

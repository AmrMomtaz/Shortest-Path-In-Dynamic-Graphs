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
      1) When a query is sent, The answer is fetched
      from the state.
      2) If the state doesn't contain the answer we would
      know that the nodes aren't connected, (-1) is returned
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
      **OPTIONAL**: The state can be saved where to carry on
      execution next time but this adds more overhead.
      5) If the execution is completed and wasn't broken
      (because B isn't connected to A). Marks that the
      execution for node A is completed (handled in
      completedExecution HashSet). Such that if the
      state didn't contain an entry for the query while
      having the execution for A finished, this implies
      that A & B aren't connected.
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

## Evaluation

This section contains the comparison between different
variations of the system.
The first part contains measures of
response time between the different implementations of the
shortest path algorithm.
The second part contains the
evaluation of the parallelism.
The third part contains the
evaluations using different numbers of clients varying
from 5 to 15 clients and their average response time.

In all the tables the average response time is measured in (ms).
The same initial graph is used for all the evaluations which is as follows:

![image](https://github.com/AmrMomtaz/Shortest-Path-In-Dynamic-Graphs/assets/61145262/a9fbecbf-a363-4755-b7e8-034d674c2e66)

### **1) Evaluating the different shortest path algorithm implementation**
In this subsection, a comparison of the different implementations will
be held while varying the number of queries in the batch and
the number of updates.
The following values are fixed:
   * Number of operations is 1,000 per batch.
   * Number of nodes in the graph is 50.
   * No parallelism.
   * Number of generated batches is 20 batches.
   The query frequency and the add frequency will be changed in each configuration.
   * Just one client.

#### **First Configuration (balanced):**
_Query frequency = 0.5 & Add frequency = 0.5_

| **Batches** | **Stateless** | **Memoized** | **Stateful** |
| ------- | --------- | -------- | -------- |
| 5       | 76        | 93       | 2,845     |
| 10      | 66        | 75       | 3,092     |
| 15      | 66        | 77       | 3,088     |
| 20      | 65        | 78       | 3,034     |
| **Average** | **69**      | **81**       | **3,015**     |

#### **Second Configuration (high query load):**
_Query frequency = 0.8 & Add frequency = 0.7_

| **Batch** | **Stateless** | **Memoized** | **Stateful** |
| ----- | --------- | -------- | -------- |
| **5**     | 115       | 113      | 1,422     |
| **10**    | 96        | 119      | 1,547     |
| **15**    | 108       | 113      | 1,544     |
| **20**    | 107       | 113      | 1,522     |
| **Average** | **107**  | **115**      | **1,501**     |

#### **Third Configuration (high update load):**
_Query frequency = 0.15 & Add frequency = 0.7_

| **Batch** | **Stateless** | **Memoized** | **Stateful** |
| ----- | --------- | -------- | -------- |
| **5**     | 33        | 34       | 2,179     |
| **10**    | 23        | 31       | 2,240     |
| **15**    | 24        | 30       | 2,256     |
| **20**    | 24        | 27       | 2,288     |
| **Average** | **26**   | **31**       | **2,241**     |

#### **Forth Configuration (high update load with no delete):**
_Query frequency = 0.15 & Add frequency = 1_

| **Batch** | **Stateless** | **Memoized** | **Stateful** |
| ----- | --------- | -------- | -------- |
| **5**     | 32        | 28       | 351      |
| **10**    | 27        | 3        | 1        |
| **15**    | 26        | 3        | 1        |
| **20**    | 27        | 3        | 1        |
| **Average without the first 5 batches** | **27** | **3** | **1** |

### **2) Evaluating the parallelism**

The main goal of this subsection is to evaluate the effect of
parallelism. The following values are fixed:
   * Number of operations is 100,000 per batch.
   * Number of nodes in the graph is 200.
   * Query frequency is 0.9.
   * Add frequency is 1.
   * Number of generated batches is 5 batches.
   * Shortest path algorithm is stateless.
   * Minimum number of queries to parallelize is equal to
   the number of threads.
   * Just one client.

| **Batch** | **1 thread** | **2 threads** | **4 threads** | **6 threads** | **12 threads** |
| ----- | -------- | --------- | --------- | --------- | ---------- |
| **1**     | 5,646     | 7,564      | 5,923      | 6,200      | 7,743       |
| **2**     | 13,628    | 11,887     | 9,438      | 9,117      | 11,421      |
| **3**     | 18,125    | 14,709     | 11,482     | 10,913     | 13,461      |
| **4**     | 21,219    | 15,980     | 12,446     | 11,962     | 14,312      |
| **5**     | 21,419    | 16,098     | 12,666     | 12,452     | 14,873      |
| **Average** | **16,007** | **13,247**    | **10,391**    | **10,128**    | **12,362**     |

### **3) Evaluating different number of clients**

In this subsection, The system is evaluated using many clients
varying from 5 to 15. Two comparisons are made, one involves 
the different implementations of the shortest path algorithm. And
the other one concerns the parallelization.

#### **First Configuration (non-parallelized):**

The following values are fixed:
   * Number of operations is 1,000 per batch.
   * Number of nodes in the graph is 50.
   * No parallelism.
   * Number of generated batches will be 20.
   * Query frequency is 0.6 and add frequency is 0.5 (balanced).
   * Number of batches is 5.

| **Number Of Clients** | **Stateless** | **Memoized** | **Stateful** |
| ----- | --------- | -------- | -------- |
| **5**     | 1,880     | 2,441    | 3,104    |
| **6**     | 2,250     | 2,831    | 3,936    |
| **7**     | 2,736     | 2,777    | 4,602    |
| **8**     | 3,040     | 3,128    | 5,539    |
| **9**     | 3,683     | 3,405    | 6,108    |
| **10**    | 3,764     | 3,894    | 7,065    |
| **11**    | 4,376     | 4,439    | 7,892    |
| **12**    | 4,589     | 4,514    | 9,095    |
| **13**    | 5,103     | 4,936    | 9,292    |
| **14**    | 5,137     | 5,378    | 9,794    |
| **15**    | 5,414     | 5,776    | 10,750   |

![image](https://github.com/AmrMomtaz/Shortest-Path-In-Dynamic-Graphs/assets/61145262/b38be342-b591-46fb-8210-1cef06ec0b69)

#### **Second Configuration (parallelized):**

The following values are fixed:
   * Number of operations is 2,000 per batch.
   * Number of nodes in the graph is 50.
   * Query frequency is 0.95.
   * Add frequency is 1.
   * Number of generated batches is 5.
   * Shortest path algorithm is stateless.
   * Number of threads and the minimum number to parallelize is 2.

| **Batch** | **Non-Parallelized** | **Parallelized** |
| ----- | --------------- | ------------ |
| **5**     | 3,241           | 3,026        |
| **6**     | 3,966           | 3,550        |
| **7**     | 4,558           | 4,157        |
| **8**     | 5,226           | 4,700        |
| **9**     | 6,097           | 5,535        |
| **10**    | 6,630           | 5,944        |
| **11**    | 7,376           | 6,513        |
| **12**    | 8,167           | 7,188        |
| **13**    | 8,471           | 7,865        |
| **14**    | 9,191           | 8,245        |
| **15**    | 10,602          | 8,964        |

![image](https://github.com/AmrMomtaz/Shortest-Path-In-Dynamic-Graphs/assets/61145262/430d1338-fe18-49a4-a608-29a92fec7afd)

## Conclusions

From the previous results we can conclude the following:
   * The memoized implementation is the best one among the
   three. It has a very good performance whether we have a
   balanced, high update load or high query load.
   * The stateless has the best performance when we have a
   high update load on the graph.
   * The stateful has the best performance when we have a
   high query load. Especially, the graph iis fixed without any updates and it
   is only queried.
   * The parallelization has improved the runtime. The
   speedup will be improved more when having a larger
   number of consucitive queries and denser graphs.
   * Increasing the number of threads doesn’t necessarily
   improve the speedup. Since it adds more overhead for the
   creation of the threads and dividing the tasks and combining the results.

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

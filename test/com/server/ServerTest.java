//package com.server;
//
//import com.Operation;
//import com.Server;
//import com.client.BatchGenerator;
//import com.server.algorithm.MemoizedShortestPath;
//import com.server.algorithm.ShortestPathAlgorithm;
//import com.server.algorithm.StatefulShortestPath;
//import com.server.algorithm.StatelessShortestPath;
//import org.junit.jupiter.api.Test;
//
//import java.io.FileNotFoundException;
//import java.rmi.RemoteException;
//import java.util.List;
//
//class ServerTest {
//
//    @Test
//    public void test() throws FileNotFoundException, RemoteException {
//        ServerImpl server = new ServerImpl();
////        ShortestPathAlgorithm statefulAlgorithm =
////                new StatefulShortestPath("InitialGraph.txt");
////        ShortestPathAlgorithm memoizedAlgorithm =
////                new MemoizedShortestPath("InitialGraph.txt");
//        ShortestPathAlgorithm statelessAlgorithm =
//                new StatelessShortestPath("InitialGraph.txt");
//
//        for (int i = 0 ; i < 20 ; i++) {
//            Operation[] randomBatch = BatchGenerator.generateBatch();
//
////            ServerImpl.setShortestPathAlgorithm(statefulAlgorithm);
////            List<Integer> statefulResult = server.executeBatch(randomBatch);
////
////            ServerImpl.setShortestPathAlgorithm(memoizedAlgorithm);
////            List<Integer> memoizedResult = server.executeBatch(randomBatch);
//
//            server.setShortestPathAlgorithm(statelessAlgorithm);
//            List<Integer> statelessResult = server.executeBatch(randomBatch);
//
////            assertArrayEquals(statefulResult.toArray(), memoizedResult.toArray());
////            assertArrayEquals(memoizedResult.toArray(), statelessResult.toArray());
//            System.out.println("---------------------------");
//        }
//    }
//}
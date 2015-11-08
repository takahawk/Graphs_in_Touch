package takahawk.graphsintouch.core;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import takahawk.takalibrary.DisjointSetForest;

/**
 * Static class with algorithm methods
 * @authors takahawk
 */
public class Algorithms {

    public static class VertexPair {

        private int _parent;
        private int _child;

        public int parent() {
            return _parent;
        }

        public int child() {
            return _child;
        }

        public VertexPair(int parent, int child) {
            this._parent = parent;
            this._child = child;
        }


    }
    /**
     * Performs depth first search from a giver initial vertex and returns resulting tree
     * @param graph graph to which algorithm will be apllied
     * @param initVertex initial vertex
     * @return map represents branches of dfs-tree (key - child, value - parent)
     */
    public static List<VertexPair> depthFirstSearch(Graph graph, int initVertex) {
        List<VertexPair> result = new ArrayList<>();
        Deque<VertexPair> stack = new ArrayDeque<>();
        Set<Integer> discovered = new HashSet<>();
        stack.push(new VertexPair(initVertex, initVertex));
        while (!stack.isEmpty()) {
           VertexPair pair = stack.pop();
            if (!discovered.contains(pair._child)) {
                discovered.add(pair._child);
                result.add(pair);

                int vertex = pair._child;
                for (Graph.Edge edge : graph.edges(vertex)) {
                    int adjacentVertex = (vertex != edge.getIn()) ? edge.getIn() : edge.getOut();
                    if (!discovered.contains(adjacentVertex)) {
                        stack.push(new VertexPair(vertex, adjacentVertex));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns minimum spanning tree. Method uses Kruskal's algorithm for this purpose. Working only for undirected graph
     * @return map represents branches of minimum-spanning-tree (key - child, value - parent)
     */
    public static Map<Integer, Integer> minTreeKruskal(Graph graph) {
        if (graph.isDirected())
            throw new UnsupportedOperationException("Kruscal's algorithm works only for undirected graphs");
        Map<Integer, Integer> tree = new HashMap<>();
        // get all edges via iterator
        List<Graph.Edge> edges = new ArrayList<>();
        for (Graph.Edge edge : graph.edges())
            edges.add(edge);
        // sort edges by weight
        Collections.sort(edges, new Comparator<Graph.Edge>() {
                    @Override
                    public int compare(Graph.Edge o1, Graph.Edge o2) {
                        return o1.getWeight() - o2.getWeight();
                    }
                }
        );

        // for algorithm we use disjoint set forest data structure (a.k.a. union-find, merge-find etc. see wikipedia)
        DisjointSetForest<Integer> set = new DisjointSetForest<>();
        for (Integer number : graph.vertexes())
            set.makeSet(number);
        // adding edges to tree, while it can't be possible to add edge that don't creates a cycle
        for (Graph.Edge edge : edges) {
            if (set.find(edge.getIn()) != set.find(edge.getOut())) {
                if (!tree.containsKey(edge.getIn()))
                    tree.put(edge.getIn(), edge.getOut());
                else
                    tree.put(edge.getOut(), edge.getIn());
                set.union(edge.getIn(), edge.getOut());
            }
        }
        return tree;
    }
}
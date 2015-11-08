package takahawk.graphsintouch.core;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}

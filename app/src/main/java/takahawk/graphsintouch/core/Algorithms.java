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

    /**
     * Performs depth first search from a giver initial vertex and returns resulting tree
     * @param graph graph to which algorithm will be apllied
     * @param initVertex initial vertex
     * @return map represents branches of dfs-tree (key - child, value - parent)
     */
    public static List<Graph.Edge> depthFirstSearch(Graph graph, int initVertex) {
        List<Graph.Edge> result = new ArrayList<Graph.Edge>();
        // Map<Integer, Integer> result = new HashMap<>();
        Deque<Integer> stack = new ArrayDeque<>();
        Set<Integer> discovered = new HashSet<>();
        stack.push(initVertex);
        while (!stack.isEmpty()) {
            int vertex = stack.pop();
            if (!discovered.contains(vertex)) {
                discovered.add(vertex);

                for (Graph.Edge edge : graph.edges(vertex)) {
                    result.add(edge);
                    if (edge.getIn() != vertex)
                        stack.push(edge.getIn());
                    else
                        stack.push(edge.getOut());
                }
            }
        }
        return result;
    }
}

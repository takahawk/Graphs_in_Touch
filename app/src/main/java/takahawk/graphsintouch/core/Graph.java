package takahawk.graphsintouch.core;

import java.util.*;

import takahawk.takalibrary.DisjointSetForest;
/**
 * Graph is a representation of a set of objects where some pairs of objects are connected by links.
 * @author takahawk
 */
public class Graph {
    /*
        For graph representing used custom construction named by me 'Double-sided adjacency list'
        It looks like a usual adjacency list with exception: add another vertex-list that stores also INBOUND edges
        Such a construction uses 2-time more memory, but allows to perform remove vertex and change vertex number
        operations much faster
        Also it insignificantly faster at work with undirected graph
     */
    private Map<Integer, List<Edge> > adjOutList = new HashMap<>();
    private Map<Integer, List<Edge> > adjInList = new HashMap<>();
    private boolean directed;

    public Graph() {
        directed = false;
    }

    public Graph(boolean directed) {
        this.directed = directed;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) { this.directed = directed; }

    public class Edge {
        private int in;
        private int out;
        private int weight;

        public int getIn() { return in; }
        public int getOut() { return out; }
        public int getWeight() { return weight; }

        private Edge(int out, int in, int weight) {
            this.in = in;
            this.out = out;
            this.weight = weight;
        }

    }

    public int vertexCount() {
        return adjInList.size();
    }

    public int edgeCount() {
        int count = 0;
        for (Integer key : adjInList.keySet())
            count += adjInList.get(key).size();
        return count;
    }

    /**
     * Iterates through edges
     * @return iterable object that iterates through all graph edges
     */
    public Iterable<Graph.Edge> edges() {
        return new Iterable<Edge>() {
            @Override
            public Iterator<Edge> iterator() {
                return new Iterator<Edge>() {
                    Iterator<Integer> mapIterator = adjOutList.keySet().iterator();
                    List<Edge> list = new ArrayList<>();
                    int listIndex = -1;
                    @Override
                    public boolean hasNext() {
                        if (++listIndex >= list.size()) {
                            if (mapIterator.hasNext()) {
                                list = adjOutList.get(mapIterator.next());
                                listIndex = -1;
                                return hasNext();
                            } else {
                                return false;
                            }
                        }
                        return true;
                    }

                    @Override
                    public Edge next() {
                        return list.get(listIndex);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };

    }

    /**
     * Returns edge reference for a given vertex numbers
     * @param out outbound vertex number
     * @param in inbound vertex number
     * @return reference to edge or null if such an edge doesn't exist's
     */
    private Edge EdgeByNumber(int out, int in) {
        if (!adjOutList.containsKey(out) || !adjInList.containsKey(in))
            return null;
        for (Edge r : adjOutList.get(out))
            if (r.in == in)
                return r;

        // if graph is undirected edges are double-sided
        if (!directed)
            for (Edge r : adjInList.get(out))
                if (r.out == in)
                    return r;
        return null;
    }
    /**
     * Add to graph vertex with a given number
     * @param number number of new vertex
     * @return true - if vertex added, false - if vertex with specified number is exist.
     */
    public boolean addVertex(int number) {
        if (!adjOutList.containsKey(number)) {
            adjOutList.put(number, new ArrayList<Edge>());
            adjInList.put(number, new ArrayList<Edge>());
            return true;
        }
        return false;
    }

    /**
     * Return true if graph has a vertex with given number
     * @param number number of vertex
     * @return true - if vertex exists, false - otherwise
     */
    public boolean hasVertex(int number) {
        return adjInList.containsKey(number);
    }


    /**
     * Returns max number of vertex
     * @return max number of vertex
     */
    public int maxNumber() {
        if (adjInList.size() != 0)
            return Collections.max(adjInList.keySet());
        else
            return 0;
    }

    /**
     * Remove vertex with a given number
     * @param number number of vertex to be removed
     */
    public void removeVertex(int number) {
        if (adjOutList.containsKey(number)) {
            // remove all edges of vertex from other vertexes adjacent lists
            for (Edge edge : adjOutList.get(number))
                adjInList.get(edge.in).remove(edge);
            for (Edge edge : adjInList.get(number))
                adjOutList.get(edge.out).remove(edge);
            adjOutList.remove(number);
            adjInList.remove(number);
        }
    }

    /**
     * Change vertex number to new value
     * @param number old number
     * @param newNumber new number
     */
    public void changeVertexNumber(int number, int newNumber) {
        if (adjOutList.containsKey(number) && !adjOutList.containsKey(newNumber)) {
            // since Edge object reference is the same for both vertex list's, we can simple change number only in one list
            adjOutList.put(newNumber, new ArrayList<Edge>());
            for (Edge edge : adjOutList.get(number)) {
                adjOutList.get(newNumber).add(edge);
                edge.out = newNumber;
            }
            adjInList.put(newNumber, new ArrayList<Edge>());
            for (Edge edge : adjInList.get(number)) {
                adjInList.get(newNumber).add(edge);
                edge.in = newNumber;
            }
            adjOutList.remove(number);
            adjInList.remove(number);
        }
    }

    /**
     * Add new Edge with weight 1
     * @param out outbound vertex number
     * @param in inbound vertex number
     */
    public boolean addEdge(int out, int in) {
        return addEdge(out, in, 1);
    }
    /**
     * Add new Edge to graph
     * @param out outbound vertex number
     * @param in inbound vertex number
     * @param weight Edge weight
     */
    public boolean addEdge(int out, int in, int weight) {
        if (adjOutList.containsKey(out)) {
            Edge edge = EdgeByNumber(out, in);
            // test if edge already exists, counter edge are allowed only in directed
            if (edge != null && (directed && edge.out == out))
                return false;
            edge = new Edge(out, in, weight);
            // add edge at once in two lists - as outbound edge and as inbound edge
            adjOutList.get(out).add(edge);
            adjInList.get(in).add(edge);
            return true;
        }
        return false;
    }

    /**
     * Remove Edge from graph
     * @param out outbound vertex number
     * @param in inbound vertex number
     */
    public void removeEdge(int out, int in) {
        if (adjOutList.containsKey(out)) {
            Edge edge = EdgeByNumber(out, in);
            adjOutList.get(out).remove(edge);
            adjInList.get(in).remove(edge);
        }
    }

    /**
     * Return true if out vertex is adjacent to in vertex
     * @param in inbound vertex number
     * @param out outbound vertex number
     */
    public boolean adjacent(int out, int in) {
        if (EdgeByNumber(out, in) != null)
            return true;
        return false;
    }

    /**
     * Performs depth first search from a giver initial vertex and returns resulting tree
     * @param initVertex initial vertex
     * @return map represents branches of dfs-tree (key - child, value - parent)
     */
    public Map<Integer, Integer> depthFirstSearch(int initVertex) {
        Map<Integer, Integer> result = new HashMap<>();
        Deque<Integer> stack = new ArrayDeque<>();
        Set<Integer> discovered = new HashSet<>();
        stack.push(initVertex);
        while (!stack.isEmpty()) {
            int vertex = stack.pop();
            if (!discovered.contains(vertex)) {
                discovered.add(vertex);
                for (Edge edge : adjOutList.get(vertex)) {
                    result.put(edge.in, vertex);
                    stack.push(edge.in);
                }
                if (!directed) {
                    for (Edge edge : adjInList.get(vertex)) {
                        if (!result.containsKey(edge.out)) {
                            result.put(edge.out, vertex);
                            stack.push(edge.out);
                        }
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
    public Map<Integer, Integer> minTreeKruskal() {
        if (directed)
            throw new UnsupportedOperationException("Kruscal's algorithm works only for undirected graphs");
        Map<Integer, Integer> tree = new HashMap<>();
        // get all edges via iterator
        List<Edge> edges = new ArrayList<>();
        for (Edge edge : edges())
            edges.add(edge);
        // sort edges by weight
        Collections.sort(edges, new Comparator<Edge>() {
                    @Override
                    public int compare(Edge o1, Edge o2) {
                        return o1.weight - o2.weight;
                    }
                }
        );

        // for algorithm we use disjoint set forest data structure (a.k.a. union-find, merge-find etc. see wikipedia)
        DisjointSetForest<Integer> set = new DisjointSetForest<>();
        for (Integer number : adjOutList.keySet())
            set.makeSet(number);
        // adding edges to tree, while it can't be possible to add edge that don't creates a cycle
        for (Edge edge : edges) {
            if (set.find(edge.in) != set.find(edge.out)) {
                if (!tree.containsKey(edge.in))
                    tree.put(edge.in, edge.out);
                else
                    tree.put(edge.out, edge.in);
                set.union(edge.in, edge.out);
            }
        }
        return tree;
    }

    /**
     * Returns maximum spanning tree. Method uses Prim's algorithm for this purpose. Working only for undirected graph
     * @return map represents branches of maximum-spanning-tree (key - child, value - parent)
     */
    public Map<Integer, Integer> maxTreePrim() {
        if (directed)
            throw new UnsupportedOperationException("Prim's algorithm works only for undirected graphs");
        Map<Integer, Integer> tree = new HashMap<>();
        List<Edge> edges = new ArrayList<>();
        Set<Integer> labeled = new HashSet<>();
        Iterator<Integer> vertexIt = adjInList.keySet().iterator();
        while (vertexIt.hasNext()) {
            int vertex = vertexIt.next();
            if (labeled.contains(vertex))
                continue;
            labeled.add(vertex);
            edges.addAll(adjOutList.get(vertex));
            edges.addAll(adjInList.get(vertex));

            while (!edges.isEmpty()) {
                Collections.sort(edges, new Comparator<Edge>() {
                            @Override
                            public int compare(Edge o1, Edge o2) {
                                return o2.weight - o1.weight;
                            }
                        }
                );
                Iterator<Edge> it = edges.iterator();
                while (it.hasNext()) {
                    Edge edge = it.next();
                    if (!labeled.contains(edge.in)) {
                        labeled.add(edge.in);
                        edges.addAll(adjOutList.get(edge.in));
                        edges.addAll(adjInList.get(edge.in));
                        tree.put(edge.in, edge.out);
                        break;
                    }
                    if (!labeled.contains(edge.out)) {
                        labeled.add(edge.out);
                        edges.addAll(adjOutList.get(edge.out));
                        edges.addAll(adjInList.get(edge.out));
                        tree.put(edge.out, edge.in);
                        break;
                    }
                    it.remove();
                }
            }
        }
        return tree;
    }

    /**
     * Returns list of node numbers that represents the shortest path from source to destination. Used Dijkstra algorithm
     * @param source source vertex number
     * @param destination destination vertex number
     * @return list representing shortest path from source to destination
     */
    public List<Integer> shortestPathDijkstra(int source, int destination) {
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        Set<Integer> unvisited = new TreeSet(adjInList.keySet());

        dist.put(source, 0);

        while (!unvisited.isEmpty()) {
            int min = -1;
            for (int i : unvisited) {
                if (dist.containsKey(i) && (min == - 1 || dist.get(i) > dist.get(min)))
                    min = i;
            }
            if (min == -1)
                break;
            int current_node = min;
            unvisited.remove(current_node);

            for (Edge edge : adjOutList.get(current_node)) {
                int alt = dist.get(current_node) + edge.weight;
                if (!dist.containsKey(edge.in) || alt < dist.get(edge.in)) {
                    dist.put(edge.in, alt);
                    prev.put(edge.in, current_node);
                }

            }

            for (Edge edge : adjInList.get(current_node)) {
                int alt = dist.get(current_node) + edge.weight;
                if (!dist.containsKey(edge.out) || alt < dist.get(edge.out)) {
                    dist.put(edge.out, alt);
                    prev.put(edge.out, current_node);
                }

            }
        }

        List<Integer> result = new ArrayList<>();
        if (!prev.containsKey(destination))
            return null;

        int node = destination;
        while (node != source) {
            result.add(node);
            node = prev.get(node);
        }
        result.add(source);
        Collections.reverse(result);
        return result;
    }
}

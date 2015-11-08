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

    /**
     * Return count of vertexes
     * @return count of vertexes
     */
    public int vertexCount() {
        return adjInList.size();
    }

    /**
     * Returns count of edges
     * @return count of edges
     */
    public int edgeCount() {
        int count = 0;
        for (Integer key : adjInList.keySet())
            count += adjInList.get(key).size();
        return count;
    }

    /**
     * Returns set of all numbers of vertexes
     * @return set of all numbers of vertexes
     */
    public Set<Integer> vertexes() {
        return adjInList.keySet();
    }

    /**
     * Return all edges adjacent to vertex
     * NOTE: result depends on directed graph or undirected
     * @param vertex vertex
     * @return list with all adjacent edges
     */
    public List<Edge> getAdjacentEdges(int vertex) {
        List<Edge> result = new ArrayList<>();
        result.addAll(adjOutList.get(vertex));
        if (!directed)
            result.addAll(adjInList.get(vertex));
        return result;
    }
    /**
     * Returns list of all edges
     * @return list of all edges
     */
    public List<Edge> getAllEdges() {
        List<Edge> result = new ArrayList<>();
        for (List<Edge> edges : adjOutList.values()) {
            result.addAll(edges);
        }
        if (!directed)
            for (List<Edge> edges : adjInList.values()) {
                result.addAll(edges);
            }
        return result;
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
     * Iterates through edges adjacent to one vertex
     * NOTE: results depends on directed graph or undirected
     * @param outbound outbound vertex
     * @return iterable object for iterating through all edges adjacent to specified vertex
     */
    public Iterable<Graph.Edge> edges(final int outbound) {
        return new Iterable<Edge>() {
            @Override
            public Iterator<Edge> iterator() {
                return new Iterator<Edge>() {
                    boolean iterateInbound = !directed;
                    Iterator<Graph.Edge> iterator = adjOutList.get(outbound).iterator();
                    List<Edge> list = new ArrayList<>();
                    int listIndex = -1;
                    @Override
                    public boolean hasNext() {
                        if (!iterator.hasNext()) {
                            if (iterateInbound) {
                                iterator = adjInList.get(outbound).iterator();
                                iterateInbound = false;
                                return hasNext();
                            } else {
                                return false;
                            }
                        }
                        return true;
                    }

                    @Override
                    public Edge next() {
                        return iterator.next();
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




}

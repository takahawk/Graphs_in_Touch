package takahawk.graphsintouch.controller;

import android.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import takahawk.graphsintouch.core.Algorithms;
import takahawk.graphsintouch.core.Graph;
import takahawk.graphsintouch.view.Edge;
import takahawk.graphsintouch.view.GraphView;
import takahawk.graphsintouch.view.Node;

/**
 * Controller class for manipulating platform-independent view and model.
 * @author takahawk
 */
public class GraphController {

    private static final java.util.Random rand = new java.util.Random();
    private GraphView.Control control;
    private Graph graph = new Graph(true);
    private Node selectedNode;
    private Deque<Operation> undoDeque = new ArrayDeque<Operation>();
    private Deque<Operation> redoDeque = new ArrayDeque<Operation>();

    public GraphController(GraphView.Control control) {
        this.control = control;
    }

    public void restore(DataBundle bundle) {
        for (int i = 0; i < bundle.x.length; i++) {
            control.addNode(new Node(bundle.x[i], bundle.y[i], control.radius, bundle.num[i]));
        }
        for (int i = 0; i < bundle.edge_in.length; i++) {

            control.addEdge(new Edge(nodeByNumber(bundle.edge_out[i]),
                                nodeByNumber(bundle.edge_in[i]), bundle.edge_weight[i]));
        }
    }

    private Node nodeByNumber(int number) {
        for (Node node : control.nodes()) {
            if (node.number() == number)
                return node;
        }
        return null;
    }
    /**
     * Undo the last operation
     * @return true - if operation undone, false - if operation stack is empty
     */
    public boolean undo() {
        if (undoDeque.isEmpty())
            return false;
        Operation operation = undoDeque.pop();
        operation.undo();
        redoDeque.push(operation);
        return true;
    }

    /**
     * Redo the last undone operation
     * @return true - if operation redone, false - if operation stack is empty
     */
    public boolean redo() {
        if (redoDeque.isEmpty())
            return false;
        Operation operation = redoDeque.pop();
        operation.apply();
        undoDeque.push(operation);
        return true;
    }
    /**
     * Select the node and give it focus
     * @param node node to be selected
     */
    public void selectNode(Node node) {
        if (selectedNode != null)
            selectedNode.focused = false;
            selectedNode = node;
        if (node != null)
            node.focused = true;
    }

    /**
     * Select node by coordinates
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return true - if node is selected, false - if no node is selected
     */
    public boolean selectNode(float x, float y) {
        if (selectedNode != null) {
            selectedNode.focused = false;
            selectedNode = null;
        }
        selectedNode = getNode(x, y);
        if (selectedNode != null) {
            selectedNode.focused = true;
            return true;
        }
        return false;

    }

    public boolean selectNodeExtendedRadius(float x, float y) {
        if (selectedNode != null) {
            selectedNode.focused = false;
            selectedNode = null;
        }
        selectedNode = getExtendedRadiusNode(x, y);
        if (selectedNode != null) {
            selectedNode.focused = true;
            return true;
        }
        return false;
    }

    private Node getNode(float x, float y) {
        Node result = null;
        for (Node node : control.nodes()) {
            float dX = Math.abs(node.x() - x);
            float dY = Math.abs(node.y() - y);
            if ((dX < control.radius) && (dY < control.radius)) {
                result = node;
                break;
            }
        }
        return result;
    }

    public boolean checkForNode(float x, float y) {
        return getNode(x, y) != null;
    }

    private Edge getEdge(float x, float y) {
        Edge result = null;
        for (Edge edge : control.edges()) {
            // (x-x1)/(x2-x1) = (y-y1)/(y2-y1)
            // x - x1 = (y - y1) * (x2 - x1) / (y2 - y1)
            // x = (y - y1) * (x2 - x1) / (y2 - y1) + x1
            // y = (x - x1) * (y2 - y1) / (x2 - x1) + y1
            float pX = (y - edge.y1()) * (edge.x2() - edge.x1()) / (edge.y2() - edge.y1()) + edge.x1();
            float pY = (x - edge.x1()) * (edge.y2() - edge.y1()) / (edge.x2() - edge.x1()) + edge.y1();
            // (x - x1) * (y2 - y1) = (y - y1) * (x2 - x1)
            if (Math.abs(pX - x) < GraphView.BASE_EDGE_SELECTION_WIDTH &&
                    Math.abs(pY - y) < GraphView.BASE_EDGE_SELECTION_WIDTH)
                    result = edge;
        }
        return result;
    }

    /**
     * Get node if coordinates points in 2-times radius field of it
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return node reference or null if nothing placed on specified coordinates
     */
    private Node getExtendedRadiusNode(float x, float y) {
        Node result = null;
        for (Node node : control.nodes()) {
            float dX = Math.abs(node.x() - x);
            float dY = Math.abs(node.y() - y);
            if ((dX < control.radius * 2) && (dY < control.radius * 2)) {
                result = node;
                break;
            }
        }
        return result;
    }

    private Node getNodeByNumber(int number) {
        for (Node node : control.nodes()) {
            if (node.number() == number)
                return node;
        }
        return null;
    }

    /**
     * Return true if node selected
     * @return true - if node selected, false - otherwise
     */
    public boolean nodeSelected() {
        return selectedNode != null;
    }

    /**
     * Add vertex to graph and corresponding node view if specified number doesn't exist
     * @param number number of new node
     * @param x x-coordinate of new node
     * @param y y-coordinate of new node
     * @return true - if new node added, false - if node with specified number exists
     */
    public boolean addNode(int number, float x, float y) {
        if (!graph.hasVertex(number)) {
            AddNode op = new AddNode(x, y, number);
            op.apply();
            undoDeque.push(op);
            return true;
        }
        return false;
    }

    /**
     * Move selected node by given distance
     * @param dX horizontal movement distance
     * @param dY vertical movement distance
     */
    public void moveNode(float dX, float dY) {
        if (undoDeque.peek() instanceof MoveNode && ((MoveNode) undoDeque.peek()).node == selectedNode) {
            ((MoveNode) undoDeque.peek()).add(dX, dY);
            selectedNode.move(dX, dY);
        }
        else {
            Operation op = new MoveNode(dX, dY);
            op.apply();
            undoDeque.push(op);
        }
    }

    /**
     * Add vertex with a number greater by 1 than maximum in graph
     * @param x x-coordinate of new node
     * @param y y-coordinate of new node
     * @return number of added node
     */
    public int addNode(float x, float y) {
        int number = graph.maxNumber() + 1;
        Operation op = new AddNode(x, y, number);
        op.apply();
        undoDeque.push(op);
        return number;
    }

    /**
     * Remove selected node
     */
    public void removeNode() {
        if (selectedNode != null) {
            Operation op = new RemoveNode(selectedNode);
            op.apply();
            undoDeque.push(op);
            selectedNode = null;
        }
    }

    /**
     * Remove element at specified position or do nothing if there are nothing
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public void remove(float x, float y) {
        Node node = getNode(x, y);
        if (node != null) {
            Operation op = new RemoveNode(node);
            op.apply();
            undoDeque.push(op);
            return;
        }
        Edge edge = getEdge(x, y);
        if (edge != null) {
            Operation op = new RemoveEdge(edge);
            op.apply();
            undoDeque.push(op);
        }
    }


    /**
     * Starts adding edge if specified coordinates tips on node
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return true - if adding started, false - if not
     */
    public boolean startAddingEdge(float x, float y) {
        selectNode(getExtendedRadiusNode(x, y));
        if (selectedNode == null)
            return false;
        control.createQuasiEdge(selectedNode);
        return true;
    }

    /**
     * Continue edge adding with coordinates that specify moving of cursor
     * @param dX horizontal cursor position offset
     * @param dY vertical cursor position offset
     */
    public void continueAddingEdge(float dX, float dY) {
        control.moveQuasiEdge(dX, dY);
    }

    /**
     * Add new edge with outbound selected node and inbound node coordinates specified with weight 1
     * @param in_x inbound node horizontal coordinate
     * @param in_y inbound node vertical coorditate
     * @return pair of two numbers: of outbound and inbound vertex respectively. Or null if edge is not added
     */
    public Pair<Integer, Integer> addEdge(float in_x, float in_y) {
        control.killQuasiEdge();
        Node in = getExtendedRadiusNode(in_x, in_y);
        if (in != null) {
            // TODO:
            Integer weight = rand.nextInt(20);
            if (graph.addEdge(selectedNode.number(), in.number(), weight)) {
                Operation op = new AddEdge(selectedNode, in, weight);
                op.apply();
                undoDeque.push(op);
                return new Pair<Integer, Integer>(selectedNode.number(), in.number());
            }
        }
        return null;
    }

    public int performDijkstra(float x, float y) {
        Node second = getNode(x, y);
        if (selectedNode == second)
            return 0;
        if (second != null) {
            PerformDijkstra op = new PerformDijkstra(selectedNode, second);
            op.apply();
            undoDeque.push(op);
            if (op.result == null)
                return -1;
            int sum = 0;
            for (Edge edge : op.result)
                sum += edge.label();
            return sum;
        }
        return -1;
    }

    public void performDFS() {
        if (selectedNode == null)
            return;
        Operation op = new PerformDFS(selectedNode);
        op.apply();
        undoDeque.push(op);
    }

    public int performPrim() {
        if (graph.isDirected())
            return -1;
        Operation op = new PerformPrim();
        op.apply();
        undoDeque.push(op);
        int sum = 0;
        for (Edge edge : ((PerformPrim) op).result)
            sum += edge.label();
        return sum;
    }

    public int performKruskal() {
        if (graph.isDirected())
            return -1;
        Operation op = new PerformKruskal();
        op.apply();
        undoDeque.push(op);
        int sum = 0;
        for (Edge edge : ((PerformKruskal) op).result)
            sum += edge.label();
        return sum;
    }

    public void switchDirectedUndirected() {
        Operation op = new SwitchDirectedUndirected();
        op.apply();
        undoDeque.push(op);
    }

    public void clearAlgorithms() {
        Operation op = new ClearAlgorithms();
        op.apply();
        undoDeque.push(op);
    }



    public interface Operation {
        void apply();
        void undo();
    }

    class AddNode
        implements Operation {
        Node node;
        List<Operation> undoedEdges;

        public AddNode(float x, float y, int nodeNumber) {
            node = new Node(x, y, control.radius, nodeNumber);
        }

        @Override
        public void apply() {

            graph.addVertex(node.number());
            control.addNode(node);
            selectNode(node);

            if (undoedEdges != null)
                for (Operation op : undoedEdges)
                    op.undo();
        }

        @Override
        public void undo() {
            undoedEdges = new ArrayList<Operation>();
            for (Edge edge : control.edges()) {
                if (edge.in == node || edge.out == node)
                    undoedEdges.add(new RemoveEdge(edge));

            }
            for (Edge edge : control.markers())
                if (edge.in == node || edge.out == node)
                    undoedEdges.add(new RemoveEdge(edge));
            for (Operation op : undoedEdges)
                op.apply();

            selectedNode = null;
            node.focused = false;
            graph.removeVertex(node.number());
            control.removeNode(node);
        }
    }

    class RemoveNode
        implements Operation {
        Node node;
        List<Operation> removedEdges;

        public RemoveNode(Node node) {
            this.node = node;
        }

        @Override
        public void apply() {
            removedEdges = new ArrayList<Operation>();
            for (Edge edge : control.edges()) {
                if (edge.in == node || edge.out == node)
                    removedEdges.add(new RemoveEdge(edge));
            }
            for (Edge edge : control.markers())
                if (edge.in == node || edge.out == node)
                    removedEdges.add(new RemoveEdge(edge));
            for (Operation op : removedEdges)
                op.apply();

            selectedNode = null;
            node.focused = false;
            graph.removeVertex(node.number());
            control.removeNode(node);
        }

        @Override
        public void undo() {
            graph.addVertex(node.number());
            control.addNode(node);
            selectNode(node);

            if (removedEdges != null)
                for (Operation op : removedEdges)
                    op.undo();
        }
    }

    public DataBundle getAllData() {
        DataBundle d = new DataBundle();
        d.x = new float[graph.vertexCount()];
        d.y = new float[graph.vertexCount()];
        d.num = new int[graph.vertexCount()];

        int edges = graph.edgeCount();
        d.edge_in = new int[edges];
        d.edge_out = new int[edges];
        int i = 0;
        for (Node node : control.nodes()) {
            d.x[i] = node.x();
            d.y[i] = node.y();
            d.num[i] = node.number();
            i++;
        }
        i = 0;
        for (Edge edge: control.edges()) {
            d.edge_in[i] = edge.in.number();
            d.edge_out[i] = edge.out.number();
            d.edge_weight[i] = edge.label();
            i++;
        }

        return d;
    }
    public static class DataBundle {
        public float[] x;
        public float[] y;
        public int[] num;

        public int[] edge_in;
        public int[] edge_out;
        public int[] edge_weight;

        public DataBundle() {

        }
    }

    class MoveNode
        implements Operation {
        Node node;
        float dX;
        float dY;

        public MoveNode(float dX, float dY) {
            node = selectedNode;
            this.dX = dX;
            this.dY = dY;
        }

        public void add(float dX, float dY) {

            // TODO: node moving
            this.dX += dX;
            this.dY += dY;

        }
        @Override
        public void apply() {
            node.move(dX, dY);
        }

        @Override
        public void undo() {
            node.move(-dX, -dY);
        }
    }

    class AddEdge
        implements Operation {
        Edge edge;

        public AddEdge(Node out, Node in, Integer weight) {
            edge = new Edge(out, in, weight);
        }

        @Override
        public void apply() {
            graph.addEdge(edge.out.number(), edge.in.number());
            control.addEdge(edge);
        }

        @Override
        public void undo() {
            graph.removeEdge(edge.out.number(), edge.in.number());
            control.removeEdge(edge);
        }
    }

    class RemoveEdge
        implements Operation {
        Edge edge;

        public RemoveEdge(Edge edge) {
            this.edge = edge;
        }

        @Override
        public void apply() {
            graph.removeEdge(edge.out.number(), edge.in.number());
            control.removeEdge(edge);
        }

        @Override
        public void undo() {
            graph.addEdge(edge.out.number(), edge.in.number());
            control.addEdge(edge);
        }
    }

    class PerformDijkstra
        implements Operation {
        Node out, in;
        List<Edge> result;

        public PerformDijkstra(Node out, Node in) {
            this.out = out;
            this.in = in;
        }

        @Override
        public void apply() {
            List<Integer> res = Algorithms.shortestPathDijkstra(graph, out.number(), in.number());
            if (res == null)
                return;
            if (result == null) {
                result = new ArrayList<Edge>();
                for (int i = 0; i < res.size() - 1; i++) {
                    for (Edge edge : control.edges()) {
                        if ((edge.out.number() == res.get(i) && edge.in.number() == res.get(i + 1)))
                            result.add(edge);
                        if (!graph.isDirected() &&
                                (edge.in.number() == res.get(i) && edge.out.number() == res.get(i + 1)))
                            result.add(edge);
                    }
                }
            }
            control.setMarkers(result);
        }

        @Override
        public void undo() {
            control.setMarkers(null);
        }
    }

    class PerformDFS
            implements Operation {
        Node out;
        List<Edge> result;

        public PerformDFS(Node out) {
            this.out = out;
        }

        @Override
        public void apply() {
            if (result == null) {
                result = new ArrayList<Edge>();
                List<Algorithms.VertexPair> res = Algorithms.depthFirstSearch(graph, out.number());
                for (Algorithms.VertexPair pair : res) {
                    result.add(new Edge(getNodeByNumber(pair.parent()),
                                        getNodeByNumber(pair.child()), 0));
                }
            }
            control.setMarkers(result);
        }

        @Override
        public void undo() {
            control.setMarkers(null);
        }
    }

    class PerformPrim
        implements Operation {

        List<Edge> result;

        public PerformPrim() {
        }

        @Override
        public void apply() {
            if (result == null) {
                result = new ArrayList<Edge>();
                Map<Integer, Integer> res = Algorithms.maxTreePrim(graph);
                for (Map.Entry<Integer, Integer> entry : res.entrySet()) {
                    for (Edge edge : control.edges()) {
                        if (entry.getKey() == edge.out.number() && entry.getValue() == edge.in.number())
                            result.add(edge);
                        if ((entry.getKey() == edge.in.number() && entry.getValue() == edge.out.number()))
                            result.add(edge);
                    }
                }
            }
            control.setMarkers(result);
        }

        @Override
        public void undo() {
            control.clearMarkers();
        }
    }

    class PerformKruskal
        implements Operation {

        List<Edge> result;

        public PerformKruskal() {
        }

        @Override
        public void apply() {
            if (result == null) {
                result = new ArrayList<Edge>();
                Map<Integer, Integer> res = Algorithms.minTreeKruskal(graph);
                for (Map.Entry<Integer, Integer> entry : res.entrySet()) {
                    for (Edge edge : control.edges()) {
                        if (entry.getKey() == edge.in.number() && entry.getValue() == edge.out.number())
                            result.add(edge);
                        if (!graph.isDirected() &&
                                (entry.getKey() == edge.out.number() && entry.getValue() == edge.in.number()))
                            result.add(edge);
                    }
                }
            }
            control.setMarkers(result);
        }

        @Override
        public void undo() {
            control.clearMarkers();
        }
    }

    class ClearAlgorithms
        implements Operation {
        List<Edge> markers;

        public void ClearAlgorithms() {

        }

        @Override
        public void apply() {
            markers = control.clearMarkers();
        }

        @Override
        public void undo() {
            control.setMarkers(markers);
        }
    }

    class SwitchDirectedUndirected
        implements Operation {

        boolean initialDirected;

        public SwitchDirectedUndirected() {
            this.initialDirected = graph.isDirected();
        }

        @Override
        public void apply() {
            graph.setDirected(!initialDirected);
            control.setDirected(graph.isDirected());
        }

        @Override
        public void undo() {
            graph.setDirected(initialDirected);
            control.setDirected(graph.isDirected());
        }
    }
}

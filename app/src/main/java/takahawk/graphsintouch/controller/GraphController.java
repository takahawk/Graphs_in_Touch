package takahawk.graphsintouch.controller;

import java.util.ArrayDeque;
import java.util.Deque;

import takahawk.graphsintouch.core.Graph;
import takahawk.graphsintouch.view.GraphView;
import takahawk.graphsintouch.view.Node;

/**
 * Controller class for manipulating platform-independent view and model.
 * @author takahawk
 */
public class GraphController {

    private GraphView.Control control;
    private Graph graph = new Graph();
    private Node selectedNode;
    private Deque<Operation> undoDeque = new ArrayDeque<Operation>();
    private Deque<Operation> redoDeque = new ArrayDeque<Operation>();

    public GraphController(GraphView.Control control) {
        this.control = control;
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
        if (selectedNode != null) {
            selectedNode.focused = false;
            selectedNode = node;
            node.focused = true;
        }
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

    private Node getNode(float x, float y) {
        for (Node node : control.nodes()) {
            if (    Math.abs(node.x() - x) < control.radius &&
                    Math.abs(node.y() - y) < control.radius)
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
            undoDeque.push(new AddNode(x, y, number));
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
        else
            undoDeque.push(new MoveNode(dX, dY));
    }

    /**
     * Add vertex with a number greater by 1 than maximum in graph
     * @param x x-coordinate of new node
     * @param y y-coordinate of new node
     */
    public void addNode(float x, float y) {
        int number = graph.maxNumber() + 1;
        undoDeque.push(new AddNode(x, y, number));
    }

    /**
     * Remove selected node
     */
    public void removeNode() {
        if (selectedNode != null) {
            graph.removeVertex(selectedNode.number());
            control.removeNode(selectedNode);
            selectedNode = null;
        }
    }

    /**
     * Add new edge with outbound selected node and inbound node coordinates specified
     * @param in_x inbound node horizontal coordinate
     * @param in_y inbound node vertical coorditante
     */
    public void addEdge(float in_x, float in_y) {
        Node in = getNode(in_x, in_y);
        if (in != null)
            if (graph.addEdge(selectedNode.number(), in.number())) {
                control.addEdge(selectedNode, in);
            }
    }




    public interface Operation {
        void apply();
        void undo();
    }

    class AddNode
        implements Operation {
        Node node;

        public AddNode(float x, float y, int nodeNumber) {
            node = new Node(x, y, control.radius, nodeNumber);
            apply();
        }

        @Override
        public void apply() {
            graph.addVertex(node.number());
            control.addNode(node);
            selectNode(node);
        }

        @Override
        public void undo() {
            selectedNode = null;
            node.focused = false;
            graph.removeVertex(node.number());
            control.removeNode(node);
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
            apply();
        }

        public void add(float dX, float dY) {
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
}

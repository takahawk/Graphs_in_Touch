package takahawk.graphsintouch.controller;

import takahawk.graphsintouch.core.Graph;
import takahawk.graphsintouch.view.GraphView;

/**
 * Controller class for manipulating platform-independent view and model.
 * @author takahawk
 */
public class GraphController {

    private GraphView.Control control;
    private Graph graph = new Graph();
    private GraphView.Node selectedNode;

    public GraphController(GraphView.Control control) {
        this.control = control;
    }

    /**
     * Select the node and give it focus
     * @param node node to be selected
     */
    public void selectNode(GraphView.Node node) {
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

    private GraphView.Node getNode(float x, float y) {
        for (GraphView.Node node : control.nodes()) {
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
        if (graph.addVertex(number)) {
            selectNode(control.addNode(number, x, y));
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
        selectedNode.move(dX, dY);
    }

    /**
     * Add vertex with a number greater by 1 than maximum in graph
     * @param x x-coordinate of new node
     * @param y y-coordinate of new node
     */
    public void addNode(float x, float y) {
        int number = graph.maxNumber() + 1;
        graph.addVertex(number);
        selectNode(control.addNode(number, x, y));
    }

    public void removeNode() {
        control.removeNode(selectedNode);
        selectedNode = null;
    }

    /**
     * Add new edge with outbound selected node and inbound node coordinates specified
     * @param in_x inbound node horizontal coordinate
     * @param in_y inbound node vertical coorditante
     */
    public void addEdge(float in_x, float in_y) {
        GraphView.Node in = getNode(in_x, in_y);
        if (in != null)
            if (graph.addEdge(selectedNode.number(), in.number())) {
                control.addEdge(selectedNode, in);
            }
    }


}

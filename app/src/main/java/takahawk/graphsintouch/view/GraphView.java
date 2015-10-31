package takahawk.graphsintouch.view;

import takahawk.graphsintouch.controller.GraphController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Platform independent graph view
 * @author takahawk
 */
public class GraphView {

    private float nodeRadius;
    private boolean directedEdges;

    protected List<Node> nodes = new ArrayList<Node>();
    protected List<Edge> edges = new ArrayList<Edge>();

    public GraphView(float nodeRadius, boolean directedEdges) {
        this.nodeRadius = nodeRadius;
        this.directedEdges = directedEdges;
    }
    public GraphController getController() {
        return new GraphController(new Control());
    }







    public class Control {

        public final float radius = nodeRadius;

        private Control() {}

        public void setDirected(boolean directed) {
            directedEdges = directed;
        }

        public void addNode(Node node) {
            nodes.add(node);
        }

        public void removeNode(Node node) {
            nodes.remove(node);
        }

        public void addEdge(Node out, Node in) {
            edges.add(new Edge(out, in));
        }

        public void removeEdge(Edge edge) {
            edges.remove(edge);
        }

        public Iterable<Node> nodes() {
            return getNodes();
        }

        public Iterable<Edge> edges() {
            return getEdges();
        }
    }

    public boolean isDirectedEdges() {
        return directedEdges;
    }

    public Iterable<Node> getNodes() {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return nodes.iterator();
            }
        };
    }

    public Iterable<Edge> getEdges() {
        return new Iterable<Edge>() {
            @Override
            public Iterator<Edge> iterator() {
                return edges.iterator();
            }
        };
    }
}

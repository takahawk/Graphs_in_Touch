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

    private interface NodeListener {
        void nodeMoved();
    }

    public class Node {
        private List<NodeListener> listeners = new ArrayList<NodeListener>();
        private float _x;
        private float _y;
        private int _number;
        public boolean focused;

        public void move(float dX, float dY) {
            _x += dX;
            _y += dY;
            for (NodeListener nl : listeners) {
                nl.nodeMoved();
            }
        }

        public float x() {
            return _x;
        }
        public float y() {
            return _y;
        }
        public int number() {
            return _number;
        }

        public Node(float x, float y, int number) {
            _x = x;
            _y = y;
            _number = number;
            focused = false;
        }


    }

    public class Edge
        implements NodeListener {
        public Node out;
        public Node in;

        private float _x1, _y1;
        private float _x2, _y2;

        public float x1() {
            return _x1;
        }
        public float x2() {
            return _x2;
        }
        public float y1() {
            return _y1;
        }
        public float y2() {
            return _y2;
        }


        public Edge(Node out, Node in) {
            this.out = out;
            this.in = in;
            out.listeners.add(this);
            in.listeners.add(this);

            double cos = (in.x() - out.x()) / (Math.sqrt(Math.pow(in.x() - out.x(), 2) + Math.pow(in.y() - out.y(), 2)));
            double sin = Math.sin(Math.acos(cos));
            _x1 = (float) (out.x() + nodeRadius * cos);
            _y1 = (float) (out.y() + nodeRadius * sin);
            _x2 = (float) (in.x() - nodeRadius * cos);
            _y2 = (float) (in.y() - nodeRadius * sin);
        }

        public void nodeMoved() {
            double cos = (in.x() - out.x()) / (Math.sqrt(Math.pow(in.x() - out.x(), 2) + Math.pow(in.y() - out.y(), 2)));
            double sin = Math.sin(Math.acos(cos));
            _x1 = (float) (out.x() + nodeRadius * cos);
            _y1 = (float) (out.y() + nodeRadius * sin);
            _x2 = (float) (in.x() - nodeRadius * cos);
            _y2 = (float) (in.y() - nodeRadius * sin);
        }
    }

    public class Control {

        public final float radius = nodeRadius;

        private Control() {}

        public void setDirected(boolean directed) {
            directedEdges = directed;
        }

        public Node addNode(int number, float x, float y) {
            Node newNode = new Node(x, y, number);
            nodes.add(newNode);
            return newNode;
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

package takahawk.graphsintouch.view;

import takahawk.graphsintouch.controller.GraphController;
import takahawk.graphsintouch.core.Graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Platform independent graph view
 * @author takahawk
 */
public class GraphView
    implements Serializable {

    public static final float BASE_EDGE_SELECTION_WIDTH = 50;
    private float nodeRadius;
    private boolean directedEdges;
    private GraphController controller;

    protected List<Node> nodes = new ArrayList<Node>();
    protected List<Edge> edges = new ArrayList<Edge>();
    protected List<Edge> markers = new ArrayList<Edge>();
    protected Edge quasiEdge;

    public GraphView(float nodeRadius, boolean directedEdges) {
        this.nodeRadius = nodeRadius;
        this.directedEdges = directedEdges;
    }

    public GraphController getController() {
        if (controller == null)
            controller = new GraphController(new Control());
        controller.validate();
        return controller;
    }

    public class Control
        implements Serializable {

        public final float radius = nodeRadius;

        private Control() {}

        public void setDirected(boolean directed) {
            directedEdges = directed;
        }

        public void createQuasiEdge(Node origin) {
            quasiEdge = new Edge(origin, new Node(origin.x(), origin.y(), 20, 0), 0);
        }

        public void moveQuasiEdge(float dX, float dY) {
            quasiEdge.in.move(dX, dY);
        }

        public void killQuasiEdge() {
            quasiEdge = null;
        }

        public void addNode(Node node) {
            nodes.add(node);
        }

        public void removeNode(Node node) {
            nodes.remove(node);
        }

        public void addEdge(Edge edge) {
            edges.add(edge);
        }

        public void removeEdge(Edge edge) {
            edges.remove(edge);
        }

        public void setMarkers(List<Edge> markers) { GraphView.this.markers = markers; }
        public List<Edge> clearMarkers() {
            List<Edge> mark = markers;
            GraphView.this.markers = null;
            return mark;
        }

        public Iterable<Node> nodes() {
            return getNodes();
        }

        public Iterable<Edge> edges() {
            return getEdges();
        }

        public Iterable<Edge> markers() { return getMarkers(); }
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

    public Iterable<Edge> getMarkers() {
        return new Iterable<Edge>() {
            @Override
            public Iterator<Edge> iterator() {
                if (markers != null)
                    return markers.iterator();
                return new Iterator<Edge>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public Edge next() {
                        return null;
                    }

                    @Override
                    public void remove() {

                    }
                };
            }
        };
    }

    public Edge getQuasiEdge() {
        return quasiEdge;
    }

    public float getMaxX() {
        float max = 0;
        for (Node node : nodes) {
            float temp = node.x() + node.radius();
            if (temp > max) {
                max = temp;
            }
        }
        return max;
    }

    public float getMinX() {
        float min = 0;
        for (Node node : nodes) {
            float temp = node.x() - node.radius();
            if (temp < min) {
                min = temp;
            }
        }
        return min;
    }

    public float getMaxY() {
        float max = 0;
        for (Node node : nodes) {
            float temp = node.y() + node.radius();
            if (temp > max) {
                max = temp;
            }
        }
        return max;
    }

    public float getMinY() {
        float min = 0;
        for (Node node : nodes) {
            float temp = node.y() - node.radius();
            if (temp < min) {
                min = temp;
            }
        }
        return min;
    }
}

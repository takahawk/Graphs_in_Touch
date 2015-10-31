package takahawk.graphsintouch.view;

/**
 * Created by takahawk on 31.10.2015.
 */
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
        out.addListener(this);
        in.addListener(this);

        double cos = (in.x() - out.x()) / (Math.sqrt(Math.pow(in.x() - out.x(), 2) + Math.pow(in.y() - out.y(), 2)));
        double sin = Math.sin(Math.acos(cos));
        _x1 = (float) (out.x() + out.radius() * cos);
        _y1 = (float) (out.y() + out.radius() * sin);
        _x2 = (float) (in.x() - in.radius() * cos);
        _y2 = (float) (in.y() - in.radius() * sin);
    }

    @Override
    public void nodeChanged() {
        double cos = (in.x() - out.x()) / (Math.sqrt(Math.pow(in.x() - out.x(), 2) + Math.pow(in.y() - out.y(), 2)));
        double sin = Math.sin(Math.acos(cos));
        _x1 = (float) (out.x() + out.radius() * cos);
        _y1 = (float) (out.y() + out.radius() * sin);
        _x2 = (float) (in.x() - in.radius() * cos);
        _y2 = (float) (in.y() - in.radius() * sin);
    }
}
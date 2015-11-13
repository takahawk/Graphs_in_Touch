package takahawk.graphsintouch.view;

import android.util.Pair;

/**
 * Created by takahawk on 31.10.2015.
 */
public class Edge
        implements NodeListener, Focusable {
    public Node out;
    public Node in;

    private float _x1, _y1;
    private float _x2, _y2;
    private double _angle;
    private int _label;
    private boolean focused;

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
    public double angle() { return _angle; }
    public int label() { return _label; }


    public Edge(Node out, Node in, int label) {
        this.out = out;
        this.in = in;
        _label = label;
        out.addListener(this);
        in.addListener(this);

        double cos = (in.x() - out.x()) / (Math.sqrt(Math.pow(in.x() - out.x(), 2) + Math.pow(in.y() - out.y(), 2)));
        _angle = Math.acos(cos);
        double sin = Math.sin(_angle);
        _x1 = (float) (out.x() + out.radius() * cos);

        _x2 = (float) (in.x() - in.radius() * cos);
        if (out.y() < in.y()) {
            _y1 = (float) (out.y() + out.radius() * sin);
            _y2 = (float) (in.y() - in.radius() * sin);
        } else {
            _y1 = (float) (out.y() - out.radius() * sin);
            _y2 = (float) (in.y() + in.radius() * sin);
        }
    }

    @Override
    public void nodeChanged() {
        double cos = (in.x() - out.x()) / (Math.sqrt(Math.pow(in.x() - out.x(), 2) + Math.pow(in.y() - out.y(), 2)));
        _angle = Math.acos(cos);
        double sin = Math.sin(_angle);
        _x1 = (float) (out.x() + out.radius() * cos);
        _x2 = (float) (in.x() - in.radius() * cos);

        if (out.y() < in.y()) {
            _y1 = (float) (out.y() + out.radius() * sin);
            _y2 = (float) (in.y() - in.radius() * sin);
        } else {
            _y1 = (float) (out.y() - out.radius() * sin);
            _y2 = (float) (in.y() + in.radius() * sin);
        }
    }

    public Pair<Float, Float> getLabelPos() {
        return new Pair<Float, Float>((_x2 + _x1) / 2 + ((float)Math.cos(_angle + Math.PI / 2) * 50), (_y2 + _y1) / 2 + ((float)Math.sin(_angle + Math.PI / 2) * 50));
    }

    @Override
    public void focus() {
        focused = true;
    }

    @Override
    public void unfocus() {
        focused = false;
    }
}
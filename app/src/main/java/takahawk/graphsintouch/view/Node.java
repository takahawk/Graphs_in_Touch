package takahawk.graphsintouch.view;

import java.util.ArrayList;
import java.util.List;

/**
 * @author takahawk
 */
public class Node
    implements Focusable {
    private List<NodeListener> listeners = new ArrayList<NodeListener>();
    private float _x;
    private float _y;
    private float _radius;
    private int _number;
    private boolean focused;

    public void addListener(NodeListener nl) { listeners.add(nl); }
    public void removeListener(NodeListener nl) { listeners.remove(nl); }

    public void move(float dX, float dY) {
        _x += dX;
        _y += dY;
        for (NodeListener nl : listeners) {
            nl.nodeChanged();
        }
    }

    public void setRadius(float radius) {
        _radius = radius;
        for (NodeListener nl : listeners) {
            nl.nodeChanged();
        }
    }

    public float x() {
        return _x;
    }
    public float y() {
        return _y;
    }
    public float radius() {return _radius; }
    public int number() {
        return _number;
    }

    public void setNumber(int number) {
        _number = number;
    }

    public Node(float x, float y, float radius, int number) {
        _x = x;
        _y = y;
        _radius = radius;
        _number = number;
        focused = false;
    }

    @Override
    public void focus() {
        focused = true;
    }

    @Override
    public void unfocus() {
        focused = false;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }
}

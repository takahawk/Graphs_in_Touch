package takahawk.graphsintouch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import takahawk.graphsintouch.view.Edge;
import takahawk.graphsintouch.view.GraphView;
import takahawk.graphsintouch.view.Node;

/**
 * Android View for drawing the graph
 */
public class GraphCanvas
    extends View {

    public static final float BASE_NODE_RADIUS = 25;
    public static final int NODE_COLOR = Color.YELLOW;
    public static final int SELECTION_COLOR = Color.BLUE;
    public static final float TEXT_SIZE = 20;

    GraphView graphView;
    RectF rect;

    Paint nodePaint;
    Paint textPaint;
    Paint edgePaint;
    Paint selectionPaint;

    public GraphCanvas(Context context, GraphView graphView) {
        super(context);
        init();
        this.graphView = graphView;
    }

    private void init() {
        nodePaint = new Paint();
        nodePaint.setColor(NODE_COLOR);

        textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);

        edgePaint = new Paint();
        selectionPaint = new Paint();
        selectionPaint.setColor(SELECTION_COLOR);
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(5);

        rect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (Node node : graphView.getNodes()) {

            rect.set(node.x() - BASE_NODE_RADIUS,
                    node.y() - BASE_NODE_RADIUS,
                    node.x() + BASE_NODE_RADIUS,
                    node.y() + BASE_NODE_RADIUS);
            canvas.drawOval(rect,
                    nodePaint);
            drawRectText(Integer.toString(node.number()), canvas, rect);
            if (node.focused) {
                canvas.drawCircle(node.x(), node.y(), BASE_NODE_RADIUS, selectionPaint);
            }
        }

        for (Edge edge : graphView.getEdges()) {
            canvas.drawLine(edge.x1(), edge.y1(), edge.x2(), edge.y2(), edgePaint);
        }
    }


    private void drawRectText(String text, Canvas canvas, RectF r) {

        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float width = r.width();
        int numOfChars = textPaint.breakText(text,true,width,null);
        int start = (text.length()-numOfChars)/2;
        canvas.drawText(text,start,start+numOfChars,r.centerX(),r.centerY(),textPaint);
    }
}

package takahawk.graphsintouch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.IntegerRes;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import takahawk.graphsintouch.view.Edge;
import takahawk.graphsintouch.view.GraphView;
import takahawk.graphsintouch.view.Node;

/**
 * Android View for drawing the graph
 */
public class GraphCanvas
    extends View {

    public static final float BASE_NODE_RADIUS = 35;
    public static final int NODE_COLOR = Color.YELLOW;
    public static final int BORDER_COLOR = Color.BLACK;
    public static final int SELECTION_COLOR = Color.BLUE;
    public static final float TEXT_SIZE = 25;
    public static final float MAX_SCALE = 5f;
    public static final float MIN_SCALE = 0.1f;
    public static final float EDGE_TIP_LENGTH = 35;
    public static final double EDGE_TIP_ANGLE = Math.PI / 6;
    public static final float SCROLLBAR_WIDTH = 20;

    private float initX = 0;
    private float initY = 0;

    private float scaleX = 1;
    private float scaleY = 1;

    GraphView graphView;
    RectF rect;

    Paint nodePaint;
    Paint textPaint;
    Paint edgePaint;
    Paint borderPaint;
    Paint selectionPaint;
    Paint markerPaint;
    Paint scrollbarPaint;

    Path edgeTip;

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
        edgePaint.setStrokeWidth(3);

        selectionPaint = new Paint();
        selectionPaint.setColor(SELECTION_COLOR);
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(5);

        borderPaint = new Paint();
        borderPaint.setColor(BORDER_COLOR);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);

        markerPaint = new Paint();
        markerPaint.setColor(Color.CYAN);
        markerPaint.setStrokeWidth(10);

        scrollbarPaint = new Paint();
        scrollbarPaint.setColor(Color.GRAY);

        edgeTip = new Path();

        rect = new RectF();
    }

    public void moveInitPoint(float x, float y) {
        initX += x;
        initY += y;
    }

    public float getInitX() {
        return initX;
    }

    public float getInitY() {
        return initY;
    }

    public float getScaleX() {
        return scaleX;
    }
    public float getScaleY() {
        return scaleY;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = Math.max(MIN_SCALE, Math.min(scaleX, MAX_SCALE));
        this.scaleY = Math.max(MIN_SCALE, Math.min(scaleY, MAX_SCALE));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (Node node : graphView.getNodes()) {

            rect.set((initX + node.x() - BASE_NODE_RADIUS) * scaleX,
                    (initY + node.y() - BASE_NODE_RADIUS) * scaleY,
                    (initX + node.x() + BASE_NODE_RADIUS) * scaleX,
                    (initY + node.y() + BASE_NODE_RADIUS) * scaleY);
            canvas.drawOval(rect, nodePaint);
            drawRectText(Integer.toString(node.number()), canvas, rect);
            if (node.isFocused()) {
                canvas.drawOval(rect, selectionPaint);
            } else {
                canvas.drawOval(rect, borderPaint);
            }
        }

        for (Edge edge : graphView.getEdges()) {
            Paint paint = edge.isFocused() ? selectionPaint : edgePaint;
            float x1 = (initX + edge.x1()) * scaleX;
            float y1 = (initY + edge.y1()) * scaleY;
            float x2 = (initX + edge.x2()) * scaleX;
            float y2 = (initY + edge.y2()) * scaleY;
            canvas.drawLine(x1,
                    y1,
                    x2,
                    y2, paint);

            Pair<Float, Float> labelPos = edge.getLabelPos();
            canvas.drawText(Integer.toString(edge.label()), (initX + labelPos.first) * scaleX, (initY + labelPos.second) * scaleY, paint);
            if (graphView.isDirectedEdges()) {
                edgeTip.reset();
                edgeTip.moveTo(x2, y2);
                double angle1, angle2;
                if (y2 < y1) {
                    angle1 = EDGE_TIP_ANGLE + edge.angle();
                    angle2 = edge.angle() - EDGE_TIP_ANGLE;
                } else {
                    angle1 = - (EDGE_TIP_ANGLE + edge.angle());
                    angle2 = - (edge.angle() - EDGE_TIP_ANGLE);
                }
                float tip1x = (float) (x2 - EDGE_TIP_LENGTH * Math.cos(angle1));
                float tip1y = (float) (y2 + EDGE_TIP_LENGTH * Math.sin(angle1));

                float tip2x = (float) (x2 - EDGE_TIP_LENGTH * Math.cos(angle2));
                float tip2y = (float) (y2 + EDGE_TIP_LENGTH * Math.sin(angle2));

                edgeTip.lineTo( tip1x, tip1y);
                edgeTip.lineTo(tip2x,tip2y);
                edgeTip.moveTo(tip1x, tip1y);
                edgeTip.lineTo(tip2x, tip2y);
                canvas.drawPath(edgeTip, paint);
            }
        }

        Edge edge = graphView.getQuasiEdge();
        if (edge != null) {
            float x1 = (initX + edge.x1()) * scaleX;
            float y1 = (initY + edge.y1()) * scaleY;
            float x2 = (initX + edge.x2()) * scaleX;
            float y2 = (initY + edge.y2()) * scaleY;
            canvas.drawLine(x1,
                    y1,
                    x2,
                    y2, edgePaint);

            if (graphView.isDirectedEdges()) {
                edgeTip.reset();
                edgeTip.moveTo(x2, y2);
                double angle1, angle2;
                if (y2 < y1) {
                    angle1 = EDGE_TIP_ANGLE + edge.angle();
                    angle2 = edge.angle() - EDGE_TIP_ANGLE;
                } else {
                    angle1 = -(EDGE_TIP_ANGLE + edge.angle());
                    angle2 = -(edge.angle() - EDGE_TIP_ANGLE);
                }
                float tip1x = (float) (x2 - EDGE_TIP_LENGTH * Math.cos(angle1));
                float tip1y = (float) (y2 + EDGE_TIP_LENGTH * Math.sin(angle1));

                float tip2x = (float) (x2 - EDGE_TIP_LENGTH * Math.cos(angle2));
                float tip2y = (float) (y2 + EDGE_TIP_LENGTH * Math.sin(angle2));

                edgeTip.lineTo(tip1x, tip1y);
                edgeTip.lineTo(tip2x, tip2y);
                edgeTip.moveTo(tip1x, tip1y);
                edgeTip.lineTo(tip2x, tip2y);
                canvas.drawPath(edgeTip, edgePaint);
            }
        }
        for (Edge marker : graphView.getMarkers()) {
            float x1 = (initX + marker.x1()) * scaleX;
            float y1 = (initY + marker.y1()) * scaleY;
            float x2 = (initX + marker.x2()) * scaleX;
            float y2 = (initY + marker.y2()) * scaleY;
            canvas.drawLine(x1,
                    y1,
                    x2,
                    y2, markerPaint);
        }

        drawScrollbars(canvas);
    }


    private void drawRectText(String text, Canvas canvas, RectF r) {

        textPaint.setTextSize(20 * scaleX);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float width = r.width();
        int numOfChars = textPaint.breakText(text,true,width,null);
        int start = (text.length()-numOfChars)/2;
        canvas.drawText(text,start,start+numOfChars,r.centerX(),r.centerY(),textPaint);
    }

    private void drawScrollbars(Canvas canvas) {
        double minX = graphView.getMinX(); minX = minX < initX ? minX : initX;
        double minY = graphView.getMinY(); minY = minY < initY ? minY : initY;
        double maxX = graphView.getMaxX(); maxX = maxX > initX ? maxX : initX;
        double maxY = graphView.getMaxY(); maxY = maxY > initX ? maxY : initY;
        Log.v("GRAPH", "InitX: " + initX);
        Log.v("GRAPH", "MinX: " + minX);
        Log.v("GRAPH", "MaxX: " + maxX);
        Log.v("GRAPH", "MinY: " + minY);
        Log.v("GRAPH", "MaxY: " + maxY);
        if (Math.abs((maxX - minX) / scaleX) > getWidth()) {
            float x2 = (float) ((initX - minX) / (maxX - minX) * getWidth());
            x2 = x2 > 0 ? x2 : 0;
            float x1 = (float) (((initX - minX) + getWidth()) / (maxX - minX) * getWidth());
            x1 = x1 < getWidth() ? x1 : getWidth();
            x2 = getWidth() - x2;
            x1 = getWidth() - x1;
            canvas.drawRect(x1,
                    getHeight() - SCROLLBAR_WIDTH,
                    x2,
                    getHeight(),
                    scrollbarPaint);
        }
    }

    public void setGraphView(GraphView view) {
        graphView = view;
    }
}

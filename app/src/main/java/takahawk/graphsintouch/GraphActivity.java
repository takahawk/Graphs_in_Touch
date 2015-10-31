package takahawk.graphsintouch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import takahawk.graphsintouch.controller.GraphController;
import takahawk.graphsintouch.view.GraphView;

public class GraphActivity
        extends AppCompatActivity
        implements View.OnTouchListener {

    private enum Mode { NORMAL, ADD_NODE};

    private Mode mode = Mode.NORMAL;
    private boolean autoNumbering = true;

    private DrawerLayout drawerLayout;
    private LinearLayout algorithmLayout;
    private FrameLayout graphLayout;
    private GraphController controller;
    private GraphCanvas canvas;

    // for drag events
    private boolean drag = false;
    private int dragX;
    private int dragY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        algorithmLayout = (LinearLayout) findViewById(R.id.algorithm_layout);
        graphLayout = (FrameLayout) findViewById(R.id.graph_layout);
        GraphView graphView = new GraphView(GraphCanvas.BASE_NODE_RADIUS, true);
        controller = graphView.getController();
        controller.addNode(1, 10, 20);
        controller.addNode(2, 40, 600);
        controller.selectNode(10, 20);
        controller.addEdge(40, 600);
        canvas = new GraphCanvas(this, graphView);
        canvas.setOnTouchListener(this);
        graphLayout.addView(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (mode) {
            case ADD_NODE:
                return addNodeModeTouch(event);
            default:
                return normalModeTouch(event);
        }
    }

    public boolean normalModeTouch(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                controller.selectNode(x, y);
                drag = true;
                dragX = x;
                dragY = y;
                canvas.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (drag && controller.nodeSelected()) {
                    controller.moveNode(x - dragX, y - dragY);
                    dragX = x;
                    dragY = y;
                } else {

                }
                canvas.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                drag = false;
                break;
        }

        return true;
    }
    public boolean addNodeModeTouch(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (autoNumbering) {
                    controller.addNode(x, y);
                } else {
                    // TODO: non-autonumbering node adding
                }
                drag = true;
                dragX = x;
                dragY = y;
                canvas.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (drag && controller.nodeSelected()) {
                    controller.moveNode(x - dragX, y - dragY);
                    dragX = x;
                    dragY = y;
                } else {

                }
                canvas.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                drag = false;
                break;
        }

        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveGraphToFile(View view) {
        view.setBackgroundColor(Color.BLUE);
    }

    public void showAlgorithms(View view) {
        if (algorithmLayout.getVisibility() == View.GONE) {
            algorithmLayout.setVisibility(View.VISIBLE);
            view.setBackgroundColor(Color.BLUE);
        } else {
            algorithmLayout.setVisibility(View.GONE);
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void switchAddNodeMode(View view){
        if (mode == Mode.ADD_NODE) {
            mode = Mode.NORMAL;
            view.setBackgroundColor(Color.TRANSPARENT);
        } else {
            mode = Mode.ADD_NODE;
            view.setBackgroundColor(Color.BLUE);
        }
    }


}

package takahawk.graphsintouch;

import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import takahawk.graphsintouch.controller.GraphController;
import takahawk.graphsintouch.view.GraphView;

public class GraphActivity
        extends AppCompatActivity
        implements View.OnTouchListener {

    private enum Mode { NORMAL, ADD_NODE, ADD_EDGE, REMOVE, DIJKSTRA, DFS };
    private final String TAG = "Graphs";

    private Mode mode = Mode.NORMAL;
    private boolean autoNumbering = true;
    private boolean autoWeighting = true;

    private DrawerLayout drawerLayout;
    private LinearLayout algorithmLayout;
    private LinearLayout mainLayout;
    private LinearLayout toolbarLayout;
    private FrameLayout graphLayout;
    private GraphController controller;
    private GraphCanvas canvas;

    // for drag events
    private boolean drag = false;
    private float dragX;
    private float dragY;

    // icons
    private ImageView addNodeIcon;
    private ImageView addEdgeIcon;
    private ImageView deleteIcon;
    private ImageView undoIcon;
    private ImageView redoIcon;

    private TextView dijkstraMenuItem;
    private TextView dfsMenuItem;

    // scale zoom in/zoom out
    private ScaleGestureDetector scaleDetector;

    private float canvasX, canvasY;

    Snackbar permanentSnackbar;

    private boolean algorithmPerformed = false;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        GraphController.DataBundle bundle = controller.getAllData();
        outState.putFloatArray("x", bundle.x);
        outState.putFloatArray("y", bundle.y);
        outState.putIntArray("num", bundle.num);
        outState.putIntArray("edge_in", bundle.edge_in);
        outState.putIntArray("edge_out", bundle.edge_out);
        outState.putIntArray("weight", bundle.edge_weight);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        GraphController.DataBundle bundle = new GraphController.DataBundle();
        bundle.x = savedInstanceState.getFloatArray("x");
        bundle.y = savedInstanceState.getFloatArray("y");
        bundle.num = savedInstanceState.getIntArray("num");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // icons
        addNodeIcon = (ImageView) findViewById(R.id.add_node_icon);
        addEdgeIcon = (ImageView) findViewById(R.id.add_edge_icon);
        deleteIcon = (ImageView) findViewById(R.id.delete_icon);
        undoIcon = (ImageView) findViewById(R.id.undo_icon);
        redoIcon = (ImageView) findViewById(R.id.redo_icon);

        dijkstraMenuItem = (TextView) findViewById(R.id.dijkstra_item);
        dfsMenuItem = (TextView) findViewById(R.id.dfs_item);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbarLayout = (LinearLayout)  findViewById(R.id.toolbarLayout);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        algorithmLayout = (LinearLayout) findViewById(R.id.algorithm_layout);
        graphLayout = (FrameLayout) findViewById(R.id.graph_layout);
        GraphView graphView = new GraphView(GraphCanvas.BASE_NODE_RADIUS, true);
        controller = graphView.getController();
        controller.addNode(1, 50, 300);
        controller.addNode(2, 40, 600);
        controller.selectNode(40, 300);
        controller.performDijkstra(40, 600);
        // controller.addEdge(40, 600);
        controller.remove(40, 450);
        canvas = new GraphCanvas(this, graphView);
        canvas.setOnTouchListener(this);
        addNodeIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionIndex() == 0) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            switchAddNodeMode();
                            break;
                    }
                }
                return true;
            }
        });
        addEdgeIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionIndex() == 0) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            switchAddEdgeMode();
                            break;
                    }
                }
                return true;
            }
        });
        deleteIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionIndex() == 0) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            if (controller.nodeSelected()) {
                                controller.removeNode();
                                blink(v);
                                canvas.invalidate();
                            }
                            switchRemoveMode();
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            switchRemoveMode();
                            break;
                    }
                }
                return true;
            }
        });
        dijkstraMenuItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionIndex() == 0) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            switchDijkstraMode();
                            break;
                    }
                }
                return true;
            }
        });
        dfsMenuItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionIndex() == 0) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            switchDFSMode();
                            break;
                    }
                }
                return true;
            }
        });
        graphLayout.addView(canvas);
        permanentSnackbar = Snackbar.make(graphLayout, "", Snackbar.LENGTH_INDEFINITE);

        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                canvas.setScale(canvas.getScaleX() * scaleFactor, canvas.getScaleY() * scaleFactor);
                canvas.invalidate();
                return true;
            }
        });
        int[] coord = new int[2];
        canvas.getLocationOnScreen(coord);
        canvasX = coord[0];
        canvasY = coord[1];
        mainLayout.getLocationOnScreen(coord);
        canvasX += coord[0];
        canvasY += coord[1];
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.v(TAG, mode.toString());
        switch (mode) {
            case ADD_NODE:
                return addNodeModeTouch(event);
            case ADD_EDGE:
                return addEdgeModeTouch(event);
            case REMOVE:
                return removeModeTouch(event);
            case DIJKSTRA:
                if (event.getPointerCount() == 1)
                    return dijkstraModeTouch(event);
                else
                    return scaleDetector.onTouchEvent(event);
            case DFS:
                if (event.getPointerCount() == 1)
                    return dfsModeTouch(event);
                else
                    return scaleDetector.onTouchEvent(event);

            default:
                if (event.getPointerCount() == 1) {
                    Log.v(TAG, "Normal mode handler");
                    return normalModeTouch(event);
                } else {
                    return scaleDetector.onTouchEvent(event);
                }

        }
    }

    public boolean normalModeTouch(MotionEvent event) {
        if (algorithmPerformed)
            clearAlgorithms();

        /* int x = (int) event.getX();
        int y = (int) event.getY();*/
        float x = event.getX() / canvas.getScaleX() - canvas.getInitX() ;
        float y = event.getY() / canvas.getScaleY() - canvas.getInitY() ;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                controller.selectNode(x, y);
                drag = true;
                dragX = event.getX();
                dragY = event.getY();
                canvas.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (drag) {
                    if (controller.nodeSelected()) {
                        controller.moveNode((event.getX() - dragX) / canvas.getScaleX(),
                                            (event.getY() - dragY) / canvas.getScaleY());
                        dragX = event.getX();
                        dragY = event.getY();
                    } else {
                            canvas.moveInitPoint((event.getX() - dragX) / canvas.getScaleX(),
                                    (event.getY() - dragY) / canvas.getScaleY());
                            dragX = event.getX();
                            dragY = event.getY();

                    }
                    canvas.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                drag = false;
                break;

        }

        return true;
    }
    public boolean addNodeModeTouch(MotionEvent event) {


        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                float x = event.getX(event.getActionIndex()) / canvas.getScaleX() - canvas.getInitX();
                float y = event.getY(event.getActionIndex()) / canvas.getScaleY() - canvas.getInitY();
                int number;
                if (autoNumbering) {
                    number = controller.addNode(x, y);
                } else {
                    number = 0;
                    // TODO: non-autonumbering node adding
                }
                canvas.invalidate();
                break;
        }

        return true;
    }
    public boolean addEdgeModeTouch(MotionEvent event) {
        float x = event.getX() / canvas.getScaleX() - canvas.getInitX();
        float y = event.getY() / canvas.getScaleY() - canvas.getInitY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (controller.startAddingEdge(x, y)) {
                    drag = true;
                    dragX = event.getX();
                    dragY = event.getY();
                    canvas.invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (controller.nodeSelected()) {
                    controller.continueAddingEdge((event.getX() - dragX) / canvas.getScaleX(),
                            (event.getY() - dragY) / canvas.getScaleY());
                    dragX = event.getX();
                    dragY = event.getY();
                }
                canvas.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (controller.nodeSelected()) {
                    Pair<Integer, Integer> edgeNum = controller.addEdge(x, y);
                    canvas.invalidate();
                }
                drag = false;
                break;
        }

        return true;
    }

    public boolean removeModeTouch(MotionEvent event) {


        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                float x = event.getX(event.getActionIndex()) / canvas.getScaleX() - canvas.getInitX();
                float y = event.getY(event.getActionIndex()) / canvas.getScaleY() - canvas.getInitY();
                controller.remove(x, y);
                canvas.invalidate();
                break;
        }

        return true;
    }

    public boolean dijkstraModeTouch(MotionEvent event) {


        /* int x = (int) event.getX();
        int y = (int) event.getY();*/
        final float x = event.getX() / canvas.getScaleX() - canvas.getInitX() ;
        final float y = event.getY() / canvas.getScaleY() - canvas.getInitY() ;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (controller.nodeSelected()) {
                    if (controller.checkForNode(x, y)) {
                        permanentSnackbar.setText(getResources().getString(R.string.dijkstra_applying));
                        new AsyncTask<Void, Integer, Integer>() {
                            @Override
                            public Integer doInBackground(Void... params) {

                                return controller.performDijkstra(x, y);
                            }

                            @Override
                            public void onPostExecute(Integer result) {
                                String resultStr;
                                if (result == -1)
                                    resultStr = getResources().getString(R.string.dijkstra_failure);
                                else
                                    resultStr = getResources().getString(R.string.dijkstra_done, result);
                                permanentSnackbar.setText(resultStr);
                                canvas.invalidate();
                            }

                        }.execute();

                    }
                } else {
                    controller.selectNode(x, y);
                    if (controller.nodeSelected()) {
                        permanentSnackbar.setText(R.string.dijkstra_mode_continue);
                        canvas.invalidate();
                    }


                }
                drag = true;
                dragX = event.getX();
                dragY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (drag) {
                        canvas.moveInitPoint((event.getX() - dragX) / canvas.getScaleX(),
                                (event.getY() - dragY) / canvas.getScaleY());
                        dragX = event.getX();
                        dragY = event.getY();

                    canvas.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                drag = false;
                break;

        }

        return true;
    }

    public boolean dfsModeTouch(MotionEvent event) {
        /* int x = (int) event.getX();
        int y = (int) event.getY();*/
        final float x = event.getX() / canvas.getScaleX() - canvas.getInitX() ;
        final float y = event.getY() / canvas.getScaleY() - canvas.getInitY() ;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (controller.checkForNode(x, y)) {
                    controller.selectNode(x, y);
                    // permanentSnackbar.setText(getResources().getString(R.string.dijkstra_applying, 0));
                    new DFS().execute();

                }
                drag = true;
                dragX = event.getX();
                dragY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (drag) {
                    canvas.moveInitPoint((event.getX() - dragX) / canvas.getScaleX(),
                            (event.getY() - dragY) / canvas.getScaleY());
                    dragX = event.getX();
                    dragY = event.getY();

                    canvas.invalidate();
                }
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
        if (permanentSnackbar != null)
            permanentSnackbar.dismiss();
        if (algorithmLayout.getVisibility() == View.GONE) {
            algorithmLayout.setVisibility(View.VISIBLE);
            blinkOn(view);
        } else {
            algorithmLayout.setVisibility(View.GONE);
            blinkOff(view);
        }
    }

    public void undoClick(View view) {
        controller.undo();
        blink(view);
        canvas.invalidate();
    }

    public void redoClick(View view) {
        controller.redo();
        blink(view);
        canvas.invalidate();
    }

    public void switchNormalMode() {
        clearAlgorithms();
        if (permanentSnackbar != null)
            permanentSnackbar.dismiss();
        mode = Mode.NORMAL;
    }
    public void switchAddNodeMode(){
        clearAlgorithms();
        if (mode == Mode.ADD_NODE) {
            if (permanentSnackbar != null)
                permanentSnackbar.dismiss();
            mode = Mode.NORMAL;
            blinkOff(addNodeIcon);
        } else {
            permanentSnackbar = Snackbar.make(graphLayout, R.string.add_node_mode_enter, Snackbar.LENGTH_INDEFINITE);
            permanentSnackbar.show();
            mode = Mode.ADD_NODE;
            blinkOn(addNodeIcon);
        }
    }

    public void switchAddEdgeMode() {
        clearAlgorithms();
        if (mode == Mode.ADD_EDGE) {
            if (permanentSnackbar != null)
                permanentSnackbar.dismiss();
            mode = Mode.NORMAL;
            blinkOff(addEdgeIcon);
        } else {
            permanentSnackbar = Snackbar.make(graphLayout, R.string.add_edge_mode_enter, Snackbar.LENGTH_INDEFINITE);
            permanentSnackbar.show();
            mode = Mode.ADD_EDGE;
            blinkOn(addEdgeIcon);
        }
    }
    public void switchRemoveMode() {
        clearAlgorithms();
        if (mode == Mode.REMOVE) {
            if (permanentSnackbar != null)
                permanentSnackbar.dismiss();
            mode = Mode.NORMAL;
            blinkOff(deleteIcon);
        } else {
            permanentSnackbar = Snackbar.make(graphLayout, R.string.remove_mode_enter, Snackbar.LENGTH_INDEFINITE);
            permanentSnackbar.show();
            mode = Mode.REMOVE;
            blinkOn(deleteIcon);
        }

    }

    public void switchDijkstraMode() {

        if (mode == Mode.DIJKSTRA) {
            if (permanentSnackbar != null)
                permanentSnackbar.dismiss();
            clearAlgorithms();
            mode = Mode.NORMAL;
        } else {
            clearAlgorithms();
            if (controller.nodeSelected()) {
                permanentSnackbar = Snackbar.make(graphLayout, R.string.dijkstra_mode_continue, Snackbar.LENGTH_INDEFINITE);
            } else
                permanentSnackbar = Snackbar.make(graphLayout, R.string.dijkstra_mode_enter, Snackbar.LENGTH_INDEFINITE);
            permanentSnackbar.show();
            mode = Mode.DIJKSTRA;
            blinkOn(dijkstraMenuItem);
            drawerLayout.closeDrawers();
        }
    }

    public void switchDFSMode() {
        if (mode == Mode.DFS) {
            if (permanentSnackbar != null)
                permanentSnackbar.dismiss();
            clearAlgorithms();
            mode = Mode.NORMAL;
        } else {
            clearAlgorithms();
            if (controller.nodeSelected()) {
                new DFS().execute();
            }

            // permanentSnackbar.show();
            mode = Mode.DFS;
            blinkOn(dfsMenuItem);
            drawerLayout.closeDrawers();
        }
    }

    private void blink(View view) {
        TransitionDrawable td = (TransitionDrawable) view.getBackground();
        td.startTransition(100);
        td.reverseTransition(100);
    }
    private void blinkOff(View view) {
        TransitionDrawable td = (TransitionDrawable) view.getBackground();
        td.reverseTransition(100);
    }

    private void blinkOn(View view) {
        TransitionDrawable td = (TransitionDrawable) view.getBackground();
        td.startTransition(100);
    }

    private class DFS
        extends AsyncTask<Void, Void, Void> {

        @Override
        public Void doInBackground(Void... params) {
            controller.performDFS();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            canvas.invalidate();
        }
    }

    public void performPrim(View view) {
        blink(view);
        drawerLayout.closeDrawers();
        new AsyncTask<Void, Void, Integer>() {

            @Override
            public Integer doInBackground(Void... params) {
                return controller.performPrim();
            }

            @Override
            protected void onPostExecute(Integer result) {
                canvas.invalidate();
                if (result == -1)
                    permanentSnackbar.setText(getResources().getString(R.string.prim_failure));
                else
                    permanentSnackbar.setText(getResources().getString(R.string.prim_done, result));
                permanentSnackbar.show();
            }
        }.execute();
    }

    public void performKruskal(View view) {
        blink(view);
        drawerLayout.closeDrawers();
        new AsyncTask<Void, Void, Integer>() {

            @Override
            public Integer doInBackground(Void... params) {
                return controller.performKruskal();
            }

            @Override
            protected void onPostExecute(Integer result) {
                canvas.invalidate();
                if (result == -1)
                    permanentSnackbar.setText(getResources().getString(R.string.kruskal_failure));
                else
                    permanentSnackbar.setText(getResources().getString(R.string.kruskal_done, result));
                permanentSnackbar.show();
            }
        }.execute();
    }

    public void switchDirectedUndirected(View view) {
        blink(view);
        controller.switchDirectedUndirected();
        canvas.invalidate();
    }

    public void clearAlgorithms() {
        if (mode == Mode.DIJKSTRA)
            blinkOff(dijkstraMenuItem);
        if (mode == Mode.DFS)
            blinkOff(dfsMenuItem);
        controller.clearAlgorithms();
        canvas.invalidate();
    }

}

package takahawk.graphsintouch;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import takahawk.graphsintouch.controller.GraphController;
import takahawk.graphsintouch.view.GraphView;

public class GraphActivity
        extends AppCompatActivity
        implements View.OnTouchListener {

    private RetainGraphFragment graphFragment;
    private enum Mode { NORMAL, ADD_NODE, ADD_EDGE, EDIT, REMOVE, DIJKSTRA, DFS };
    private final String TAG = "Graphs";

    private Mode mode = Mode.NORMAL;
    private boolean autoNumbering = true;
    private boolean autoWeighting = true;

    private DrawerLayout drawerLayout;
    private LinearLayout algorithmLayout;
    private LinearLayout mainLayout;
    private LinearLayout toolbarLayout;
    private LinearLayout editLayout;
    private FrameLayout graphLayout;
    private GraphController controller;
    private GraphCanvas canvas;

    private DrawerMainFragment drawerMainFragment;

    // for drag events
    private boolean drag = false;
    private float dragX;
    private float dragY;

    GraphView graphView;

    // icons
    private ImageView addNodeIcon;
    private ImageView addEdgeIcon;
    private ImageView deleteIcon;
    private ImageView editIcon;
    private ImageView undoIcon;
    private ImageView redoIcon;

    private TextView dijkstraMenuItem;
    private TextView dfsMenuItem;

    private EditText editValue;

    // scale zoom in/zoom out
    private ScaleGestureDetector scaleDetector;

    private float canvasX, canvasY;

    Snackbar permanentSnackbar;

    private boolean algorithmPerformed = false;

    private AsyncTask<Void, Void, Integer> asyncTask;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putFloat("initX", canvas.getInitX());
        outState.putFloat("initY", canvas.getInitY());
        outState.putFloat("scaleX", canvas.getScaleX());
        outState.putFloat("scaleY", canvas.getScaleY());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        canvas.moveInitPoint(
                savedInstanceState.getFloat("initX"),
                savedInstanceState.getFloat("initY"));
        canvas.setScale(
                savedInstanceState.getFloat("scaleX"),
                savedInstanceState.getFloat("scaleY")
        );
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        FragmentManager fm = getFragmentManager();
        graphFragment = (RetainGraphFragment) fm.findFragmentByTag("graph");

        if (graphFragment == null) {
            graphView = new GraphView(GraphCanvas.BASE_NODE_RADIUS, true);
            controller = graphView.getController();
            graphFragment = new RetainGraphFragment();
            fm.beginTransaction().add(graphFragment, "graph").commit();
            graphFragment.setController(controller);
            graphFragment.setGraphView(graphView);
        } else {
            graphView = graphFragment.getGraphView();
            controller = graphFragment.getController();
        }

        // icons
        initIcons();

        editLayout = (LinearLayout) findViewById(R.id.editLayout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbarLayout = (LinearLayout)  findViewById(R.id.toolbarLayout);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        algorithmLayout = (LinearLayout) findViewById(R.id.algorithm_layout);
        graphLayout = (FrameLayout) findViewById(R.id.graph_layout);

        canvas = new GraphCanvas(this, graphView);
        canvas.setOnTouchListener(this);

        editValue = (EditText) findViewById(R.id.edit_value);
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

        if (savedInstanceState != null)
            return;

        drawerMainFragment = new DrawerMainFragment();
        drawerMainFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.drawer_container, drawerMainFragment).commit();
    }

    private void initIcons() {
        addNodeIcon = (ImageView) findViewById(R.id.add_node_icon);
        addEdgeIcon = (ImageView) findViewById(R.id.add_edge_icon);
        editIcon = (ImageView) findViewById(R.id.edit_icon);
        deleteIcon = (ImageView) findViewById(R.id.delete_icon);
        undoIcon = (ImageView) findViewById(R.id.undo_icon);
        redoIcon = (ImageView) findViewById(R.id.redo_icon);

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
                            if (controller.elementSelected()) {
                                controller.remove();
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
        editIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionIndex() == 0) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            if (controller.elementSelected())
                                switchEditMode();
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (asyncTask != null && asyncTask.getStatus() != AsyncTask.Status.FINISHED)
            asyncTask.cancel(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.v(TAG, mode.toString());
        switch (mode) {
            case ADD_NODE:
                return addNodeModeTouch(event);
            case ADD_EDGE:
                return addEdgeModeTouch(event);
            case EDIT:
                return editModeTouch(event);
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
                controller.select(x, y);
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

    public boolean editModeTouch(MotionEvent event) {
        switchEditMode();
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
                        asyncTask = new AsyncTask<Void, Void, Integer>() {
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

                        };
                        asyncTask.execute();

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
    public void onBackPressed() {
        moveTaskToBack(true);
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


    public void showAlgorithms(View view) {
        if (permanentSnackbar != null)
            permanentSnackbar.dismiss();
        blink(view);
        DrawerAlgorithmsFragment algorithmsFragment = new DrawerAlgorithmsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.drawer_container, algorithmsFragment);
        transaction.addToBackStack(null);
        transaction.commit();

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
            controller.stopAddingEdge();
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

    public void switchEditMode() {
        clearAlgorithms();
        if (mode == Mode.EDIT) {
            if (permanentSnackbar != null)
                permanentSnackbar.dismiss();
            mode = Mode.NORMAL;
            editLayout.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editValue.getWindowToken(), 0);
            blinkOff(editIcon);
        } else {
            permanentSnackbar = Snackbar.make(graphLayout, R.string.edit_mode_enter, Snackbar.LENGTH_INDEFINITE);
            // permanentSnackbar.show();
            mode = Mode.EDIT;
            editLayout.setVisibility(View.VISIBLE);
            if (editValue.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            blinkOn(editIcon);
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
            dijkstraMenuItem = (TextView) findViewById(R.id.dijkstra_item);
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
                asyncTask = new DFS().execute();
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
        extends AsyncTask<Void, Void, Integer> {

        @Override
        public Integer doInBackground(Void... params) {
            controller.performDFS();
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            canvas.invalidate();
        }
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

    public void performPrim(View view) {
        blink(view);
        drawerLayout.closeDrawers();
        asyncTask = new AsyncTask<Void, Void, Integer>() {

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
        };
        asyncTask.execute();
    }

    public void performKruskal(View view) {
        blink(view);
        drawerLayout.closeDrawers();
        asyncTask = new AsyncTask<Void, Void, Integer>() {

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
        };
        asyncTask.execute();
    }

    public void dijkstraClick(View view) {
        switchDijkstraMode();
    }

    public void dfsClick(View view) {
        switchDFSMode();
    }

    public void backToMainMenu(View view) {
        blink(view);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.drawer_container, drawerMainFragment);
        transaction.commit();
    }

    public void changeValue(View view) {
        controller.changeElement(editValue.getText().toString());
        switchEditMode();
    }

    public void saveGraphToFile(View view) {
        blink(view);
        try {
            FileOutputStream fos = openFileOutput("graph", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(graphView);
            os.close();
            fos.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadGraphFromFile(View view) {
        blink(view);
        switchNormalMode();
        try {
            FileInputStream fis = openFileInput("graph");
            ObjectInputStream is = new ObjectInputStream(fis);
            graphView = (GraphView) is.readObject();
            controller = graphView.getController();
            canvas.setGraphView(graphView);
            canvas.invalidate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

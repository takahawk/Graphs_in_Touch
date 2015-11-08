package takahawk.graphsintouch;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import takahawk.graphsintouch.controller.GraphController;
import takahawk.graphsintouch.view.GraphView;

/**
 * Created by takahawk on 07.11.2015.
 */
public class RetainGraphFragment
    extends Fragment {

    private GraphController controller;
    private GraphView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public GraphController getController() {
        return controller;
    }

    public void setController(GraphController controller) {
        this.controller = controller;
    }

    public GraphView getGraphView() {
        return this.view;
    }

    public void setGraphView(GraphView view) {
        this.view = view;
    }
}

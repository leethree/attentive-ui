
package hk.hku.cs.srli.monkeydemo.demo;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hk.hku.cs.srli.monkeydemo.DemoListActivity;
import hk.hku.cs.srli.monkeydemo.SingleDemoActivity;

/**
 * A fragment representing a single demo screen. This fragment is either
 * contained in a {@link DemoListActivity} in two-pane mode (on tablets) or a
 * {@link SingleDemoActivity} on handsets.
 */
public abstract class DemoFragmentBase extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The demo content this fragment is presenting.
     */
    protected DemoContent.DemoItem demoItem;

    public DemoFragmentBase() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the demo content specified by the fragment
            // arguments.
            demoItem = DemoContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResource(), container, false);
    }
    
    protected abstract int getLayoutResource();
}

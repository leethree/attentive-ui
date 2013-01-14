
package hk.hku.cs.srli.monkeydemo.demo;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hk.hku.cs.srli.monkeydemo.R;
import hk.hku.cs.srli.monkeydemo.SingleDemoActivity;
import hk.hku.cs.srli.monkeydemo.DemoListActivity;

/**
 * A fragment representing a single demo screen. This fragment is either
 * contained in a {@link DemoListActivity} in two-pane mode (on tablets) or a
 * {@link SingleDemoActivity} on handsets.
 */
public class DemoFragmentBase extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The demo content this fragment is presenting.
     */
    private DemoContent.DemoItem mItem;

    public DemoFragmentBase() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the demo content specified by the fragment
            // arguments.
            mItem = DemoContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResource(), container, false);

        // Show the demo title as text in a TextView.
        if (mItem != null) {
            TextView title = (TextView) rootView.findViewById(R.id.demo_title);
            if (title != null) title.setText(mItem.title);
        }

        return rootView;
    }
    
    protected int getLayoutResource() {
        return R.layout.fragment_demo_base;
    }
}

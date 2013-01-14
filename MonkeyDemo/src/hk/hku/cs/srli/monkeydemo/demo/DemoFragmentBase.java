
package hk.hku.cs.srli.monkeydemo.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hk.hku.cs.srli.monkeydemo.R;
import hk.hku.cs.srli.monkeydemo.SingleDemoActivity;
import hk.hku.cs.srli.monkeydemo.DemoListActivity;

/**
 * A fragment representing a single Type detail screen. This fragment is either
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
     * The dummy content this fragment is presenting.
     */
    private DemoContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DemoFragmentBase() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DemoContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResource(), container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.demo_title)).setText(mItem.content);
        }

        return rootView;
    }
    
    protected int getLayoutResource() {
        return R.layout.fragment_demo_base;
    }
}

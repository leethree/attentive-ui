
package hk.hku.cs.srli.monkeydemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import hk.hku.cs.srli.monkeydemo.demo.DemoContent;
import hk.hku.cs.srli.monkeydemo.demo.DemoFragmentBase;

/**
 * An activity representing a list of demos. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link SingleDemoActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link DemoListFragment} and the item details (if present) is a
 * {@link DemoFragmentBase}.
 * <p>
 * This activity also implements the required {@link DemoListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class DemoListActivity extends Activity
        implements DemoListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_list);
        
        getActionBar().setSubtitle("Powered by Monkey");

        if (findViewById(R.id.single_demo_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((DemoListFragment) getFragmentManager()
                    .findFragmentById(R.id.demo_list))
                    .setActivateOnItemClick(true);
        }

    }

    /**
     * Callback method from {@link DemoListFragment.Callbacks} indicating that
     * the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(DemoFragmentBase.ARG_ITEM_ID, id);
            DemoFragmentBase fragment = DemoContent.ITEM_MAP.get(id).fragment;
            if (!fragment.isAdded()) {
                // do this only if the fragment is not added
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.single_demo_container, fragment)
                        .commit();
            }

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, SingleDemoActivity.class);
            detailIntent.putExtra(DemoFragmentBase.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}

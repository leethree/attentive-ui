
package hk.hku.cs.srli.factfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;

public class OrderActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        ListView listView = (ListView) findViewById(R.id.listView1);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
                android.R.layout.simple_list_item_1,
                android.R.id.text1, 
                DummyData.getInstance(getResources()).getOrder());
        listView.setAdapter(adapter);
        
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            
                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    adapter.remove(adapter.getItem(position));
                                }
                                adapter.notifyDataSetChanged();
                            }
                            
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }
                        });
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.order, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = getParentActivityIntent();
                // return to the exisiting parent activity instead of creating a new one.
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                navigateUpTo(upIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

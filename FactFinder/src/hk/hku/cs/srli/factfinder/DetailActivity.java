
package hk.hku.cs.srli.factfinder;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

import hk.hku.cs.srli.factfinder.DummyData.Category;
import hk.hku.cs.srli.factfinder.DummyData.FactItem;

public class DetailActivity extends Activity {
    
    private FactItem mFact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        
        // get intent data
        Bundle b = getIntent().getExtras();
 
        // Selected image id
        int id = b.getInt("id", -1);
        int section = b.getInt("section");
        Log.v("onCreate", "id " + id + ", section " + section);
        mFact = DummyData.getInstance(getResources()).getItem(Category.of(section), id);
        
        setTitle(mFact.title);
        Log.v("onCreate", "content " + mFact.content);
        TextView text = (TextView) findViewById(R.id.content);
        text.setText(mFact.content);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

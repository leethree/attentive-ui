
package hk.hku.cs.srli.factfinder;

import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.io.IOException;

import hk.hku.cs.srli.factfinder.DataSet.DataItem;
import hk.hku.cs.srli.factfinder.ui.FFDialog;

public class DetailActivity extends SherlockActivity 
        implements DialogInterface.OnClickListener, View.OnClickListener {
    
    private DataItem mFact;
    private FFDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = FFApp.getApp(this).getFFTheme();
        if (theme != 0) setTheme(theme);
        setContentView(R.layout.activity_detail);
        
        // get intent data
        Bundle b = getIntent().getExtras();
 
        // selected data item id and category
        int id = b.getInt("id");
        int section = b.getInt("section");
        
        mFact = FFApp.getData(this).getItem(section, id);
        mDialog = FFDialog.newInstance(section, id);
        mDialog.setListener(this);
        
        setTitle(FFApp.getData(this).getCategoryAt(section).getName());
        TextView title = (TextView) findViewById(R.id.textTitle);
        title.setText(mFact.title);
        
        TextView text = (TextView) findViewById(R.id.content);
        if (mFact.content != null && mFact.content.length() > 0) {
            // workaround to replace line breaks
            String content = mFact.content
                    .replace("\n\n", "<br><br>").replace("\n", "<br><br>");
            text.setText(Html.fromHtml(content));
        } else {
            // the content is empty
            text.setText("");
            text.setVisibility(View.GONE);
        }
        
        TextView price = (TextView) findViewById(R.id.textPrice);
        if (mFact.type != null && mFact.type.length() > 0)
            price.setText(mFact.type + ": " + DataSet.formatMoney(mFact.price));
        else
            price.setText("A la carte: " + DataSet.formatMoney(mFact.price));
        price.setOnClickListener(this);
        
        TextView status = (TextView) findViewById(R.id.textStatus);
        status.setText("" + mFact.nlikes + " people liked this.");
        
        final ImageButton image = (ImageButton) findViewById(R.id.image_view);

        try {
            if (mFact.thumb != null && mFact.thumb.length() > 0) {
                // Load image from assets
                image.setImageDrawable(
                        Drawable.createFromResourceStream(getResources(), null, 
                                getAssets().open(mFact.thumb), null));
            } else image.setImageResource(R.drawable.placeholder);
        } catch (IOException e) {
            // Image loading failed, use placeholder instead.
            image.setImageResource(R.drawable.placeholder);
        }
        image.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                ActionBar ab = getSupportActionBar();
                if (ab.isShowing()) {
                    // hide Action Bar
                    ab.hide();
                    image.setContentDescription(getString(R.string.hint_fullscreen_exit));
                } else {
                    ab.show();
                    image.setContentDescription(getString(R.string.hint_fullscreen));
                }
            }
        });

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_with_edit:
                // show "add multiple" dialog
                mDialog.show(getFragmentManager(), "dialog");
                return true;
            case R.id.action_add:
                addToOrder(mFact, 1);
                navigateBack();
                return true;
            case android.R.id.home:
                navigateBack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        addToOrder(mFact, mDialog.getNumber());
        navigateBack();
    }

    @Override
    public void onClick(View v) {
        addToOrder(mFact, 1);
        navigateBack();
    }
    
    private void addToOrder(DataItem item, int number) {
        while (number > 0) {
            FFApp.getOrder(DetailActivity.this).add(mFact);
            --number;
        }
    }

    private void navigateBack() {
        Intent upIntent = getParentActivityIntent();
        // return to the exisiting parent activity instead of creating a new one.
        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        navigateUpTo(upIntent);
    }
}

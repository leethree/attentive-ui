package hk.hku.cs.srli.factfinder;

import android.content.Context;
import android.database.DataSetObserver;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private final List<String> mItems;
    private final ArrayAdapter<String> mAdapter;
    
    public Order(Context context) {
        mItems = new ArrayList<String>();
        
        mAdapter = new ArrayAdapter<String>(context, 
                android.R.layout.simple_list_item_1,
                android.R.id.text1, 
                mItems);
    }
    
    public ArrayAdapter<String> getAdapter() {
        return mAdapter;
    }

    public void add(String title) {
        mAdapter.add(title);
    }
    
    public String getSumText() {
        return "Total: " + mItems.size();
    }
    
    public void submit() {
        mAdapter.clear();
    }
    
    public void registerObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }
}

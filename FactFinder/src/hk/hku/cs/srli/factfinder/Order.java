package hk.hku.cs.srli.factfinder;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private final List<String> mItems;
    private BaseAdapter mAdapter;
    
    public Order(Context context) {
        mItems = new ArrayList<String>();
        mAdapter = null;
    }
    
    public List<String> getItemList() {
        return mItems;
    }
    
    public void setAdapter(BaseAdapter adapter) {
        this.mAdapter = adapter;
    }
    
    public void add(String title) {
        mItems.add(title);
        mAdapter.notifyDataSetChanged();
    }
    
    public String getSumText() {
        return "Total: " + mItems.size();
    }
    
    public void submit() {
        mItems.clear();
        mAdapter.notifyDataSetChanged();
    }
}

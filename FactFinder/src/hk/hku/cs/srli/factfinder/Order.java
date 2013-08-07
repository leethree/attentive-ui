package hk.hku.cs.srli.factfinder;

import android.content.Context;
import android.widget.BaseAdapter;

import hk.hku.cs.srli.factfinder.DataSet.DataItem;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private final List<DataItem> mItems;
    private BaseAdapter mAdapter;
    
    public Order(Context context) {
        mItems = new ArrayList<DataItem>();
        mAdapter = null;
    }
    
    public List<DataItem> getItemList() {
        return mItems;
    }
    
    public void setAdapter(BaseAdapter adapter) {
        this.mAdapter = adapter;
    }
    
    public void add(DataItem item) {
        mItems.add(item);
        mAdapter.notifyDataSetChanged();
    }
    
    public int getSum() {
        int sum = 0;
        for (DataItem item : mItems) {
            sum += item.price;
        }
        return sum;
    }
    
    public void submit() {
        mItems.clear();
        mAdapter.notifyDataSetChanged();
    }
}

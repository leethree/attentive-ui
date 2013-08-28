package hk.hku.cs.srli.factfinder;

import android.content.Context;
import android.widget.BaseAdapter;

import hk.hku.cs.srli.factfinder.DataSet.DataItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private final List<DataItem> mItems;
    private WeakReference<BaseAdapter> mAdapter;
    
    public Order(Context context) {
        mItems = new ArrayList<DataItem>();
        mAdapter = null;
    }
    
    public List<DataItem> getItemList() {
        return mItems;
    }
    
    public void setAdapter(BaseAdapter adapter) {
        this.mAdapter = new WeakReference<BaseAdapter>(adapter);
    }
    
    public void add(DataItem item) {
        mItems.add(item);
        notifyAdapter();
        FFApp.log("Order", "Add order item: " + item);
    }
    
    public int getSum() {
        int sum = 0;
        for (DataItem item : mItems) {
            sum += item.price;
        }
        return sum;
    }
    
    public void reset() {
        clear();
        mAdapter = null;
    }
    
    public void clear() {
        mItems.clear();
        notifyAdapter();
    }
    
    public void submit() {
        FFApp.logImportant("Order", "Submit order: " + mItems);
        FFApp.log("Order", "Order total: " + DataSet.formatMoney(getSum()));
        clear();
    }
    
    private void notifyAdapter() {
        if (mAdapter != null && mAdapter.get() != null)
            mAdapter.get().notifyDataSetChanged();
    }
}

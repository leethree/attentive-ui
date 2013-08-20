
package hk.hku.cs.srli.factfinder;

import android.app.Fragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;

import java.util.List;

import hk.hku.cs.srli.factfinder.DataSet.DataItem;

public class OrderFragment extends Fragment {

    private boolean mCollapsed = true;
    private ListView mListView;
    private Button mInvisibleButton;
    private Order mOrder;
    private OrderAdapter mAdapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);
        mListView = (ListView) rootView.findViewById(R.id.orderListView);
        mInvisibleButton = (Button) rootView.findViewById(R.id.invisibleButton);
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mOrder = FFApp.getOrder(getActivity());
        mAdapter = new OrderAdapter(getActivity(), mOrder.getItemList()); 
        mOrder.setAdapter(mAdapter);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(getView().findViewById(R.id.textEmpty));
        
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        mListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            
                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mAdapter.remove(mAdapter.getItem(position));
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                            
                            @Override
                            public boolean canDismiss(int position) {
                                return !mCollapsed;
                            }
                        });
        mListView.setOnTouchListener(touchListener);
        mListView.setOnScrollListener(touchListener.makeScrollListener());
        
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            
            @Override
            public void onChanged() {
                refreshOrder();
            } 
        });
        
        Button submitButton = (Button) getView().findViewById(R.id.buttonOrder);
        submitButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mOrder.submit();
                Toast.makeText(getActivity(), "Order submitted.", Toast.LENGTH_LONG).show();
            }
        });
        
        refreshOrder();
    }
    
    public void setOnClickListener(View.OnClickListener listener) {
        mInvisibleButton.setOnClickListener(listener);
    }
    
    public void setCollapsed(boolean collapsed) {
        if (collapsed) {
            mCollapsed = true;
            mInvisibleButton.setVisibility(View.VISIBLE);
        } else {
            mCollapsed = false;
            mInvisibleButton.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshOrder() {
        TextView sum = (TextView) getView().findViewById(R.id.textOrderSum);
        if (mOrder.getSum() > 0) {
            sum.setText("Total: " + DataSet.formatMoney(mOrder.getSum()));
        } else {
            sum.setText("");
        }
        
        Button submitButton = (Button) getView().findViewById(R.id.buttonOrder);
        submitButton.setEnabled(mAdapter.getCount() > 0);
    }

    public static class OrderAdapter extends ArrayAdapter<DataItem> {

        public OrderAdapter(Context context, List<DataItem> objects) {
            super(context, R.layout.order_item, R.id.textViewLeft, objects);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            DataItem item = getItem(position);
            
            // name
            TextView text1 = (TextView) view.findViewById(R.id.textViewLeft);
            if (item.name != null && item.name.length() > 0)
                text1.setText(item.name);
            else
                text1.setText(item.title);
            
            // price
            TextView text2 = (TextView) view.findViewById(R.id.textViewRight);
            text2.setText(DataSet.formatMoney(item.price));
            
            // category name
            TextView text3 = (TextView) view.findViewById(R.id.textViewSmall);
            text3.setText(FFApp.getData(getContext()).getCategoryFromItem(item).getName());
            return view;
        }
    }
}

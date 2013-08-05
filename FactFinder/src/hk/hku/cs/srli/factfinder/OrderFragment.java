
package hk.hku.cs.srli.factfinder;

import android.app.Fragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;

import java.util.List;

import hk.hku.cs.srli.factfinder.ui.FFSlidingPaneLayout;

public class OrderFragment extends Fragment {

    private boolean mCollapsed = true;
    private FFSlidingPaneLayout mSlidingPane;
    private ListView mListView;
    private Button mInvisibleButton;
    private Order mOrder;
    private OrderAdapter mAdapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView = (ListView) getView().findViewById(R.id.orderListView);
        mOrder = FFApp.getOrder(getActivity());
        mAdapter = new OrderAdapter(getActivity(), mOrder.getItemList()); 
        mOrder.setAdapter(mAdapter);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(getView().findViewById(R.id.textEmpty));
        
        mInvisibleButton = (Button) getView().findViewById(R.id.invisibleButton);
        mInvisibleButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mSlidingPane.openPane();
            }
        });
        
        mSlidingPane = (FFSlidingPaneLayout) getActivity().findViewById(R.id.slidingPaneLayout);
        mSlidingPane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            
            @Override
            public void onPanelSlide(View arg0, float arg1) {
                mCollapsed = true;
                mInvisibleButton.setVisibility(View.VISIBLE);
                mSlidingPane.setTouchOnChildren(true);
            }
            
            @Override
            public void onPanelOpened(View arg0) {
                mCollapsed = false;
                mInvisibleButton.setVisibility(View.INVISIBLE);
                mSlidingPane.setTouchOnChildren(false);
            }
            
            @Override
            public void onPanelClosed(View arg0) {
                mCollapsed = true;
                mInvisibleButton.setVisibility(View.VISIBLE);
                mSlidingPane.setTouchOnChildren(true);
            }
        });
        
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

    private void refreshOrder() {
        TextView sum = (TextView) getView().findViewById(R.id.textOrderSum);
        sum.setText(mOrder.getSumText());
    }

    public static class OrderAdapter extends ArrayAdapter<String> {

        public OrderAdapter(Context context, List<String> objects) {
            super(context, 
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1, 
                    objects);
        }
    }
}


package hk.hku.cs.srli.factfinder;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;

public class OrderFragment extends Fragment {

    private boolean mCollapsed = true;
    SlidingPaneLayout mSlidingPane;
    ListView mListView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mListView = (ListView) getView().findViewById(R.id.listView1);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), 
                android.R.layout.simple_list_item_1,
                android.R.id.text1, 
                DummyData.getInstance(getResources()).getOrder());
        mListView.setAdapter(adapter);
        
        mSlidingPane = (SlidingPaneLayout) getActivity().findViewById(R.id.slidingPaneLayout);
        mSlidingPane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            
            @Override
            public void onPanelSlide(View arg0, float arg1) {
                mCollapsed = true;
                mListView.setEnabled(false);
            }
            
            @Override
            public void onPanelOpened(View arg0) {
                mCollapsed = false;
                mListView.setEnabled(true);
            }
            
            @Override
            public void onPanelClosed(View arg0) {
                mCollapsed = true;
                mListView.setEnabled(false);
            }
        });
        //mCollapsed = !mSlidingPane.isOpen();
        Log.v("mCollapsed", "" + mCollapsed);
        
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        mListView,
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
                                return !mCollapsed;
                            }
                        });
        mListView.setOnTouchListener(touchListener);
        mListView.setOnScrollListener(touchListener.makeScrollListener());
        
        RelativeLayout paneLayout = (RelativeLayout) getView();
        paneLayout.setClickable(true);
        paneLayout.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mSlidingPane.openPane();
            }
        });
    }

}

package hk.hku.cs.srli.monkeydemo.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.swipedismiss.SwipeDismissTouchListener;

import hk.hku.cs.srli.monkeydemo.R;

public class SwipeFragment extends DemoFragmentBase {
    
    private Button mButton;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        mButton = (Button) rootView.findViewById(R.id.button);
        
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),
                        "Don't click me!", Toast.LENGTH_SHORT).show();
            }
        });
        
        mButton.setOnTouchListener(new SwipeDismissTouchListener(
                mButton,
                null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        //Toast.makeText(getActivity(),
                        //        "Removed and reset", Toast.LENGTH_SHORT).show();
                    }
                }));
        return rootView;
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_swipe;
    }
    
}

package hk.hku.cs.srli.monkeydemo.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import hk.hku.cs.srli.monkeydemo.R;
import hk.hku.cs.srli.widget.Tooltip;

public class TextFragment extends DemoFragmentBase {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
     
        rootView.findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                showTooltip(v);
            }
        });
        
        return rootView;
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_text;
    }
    
    public void showTooltip(View view) {
        Tooltip tooltip = new Tooltip(getActivity());
        tooltip.setText(R.string.dummy_text);
         
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        wm.addView(tooltip, tooltip.getLayoutParams());
    }
}

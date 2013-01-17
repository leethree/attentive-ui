package hk.hku.cs.srli.monkeydemo.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import hk.hku.cs.srli.monkeydemo.R;
import hk.hku.cs.srli.widget.Tooltip;
import hk.hku.cs.srli.widget.TooltipFragment;

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
        String text = getResources().getString(R.string.dummy_text);
        TooltipFragment tooltip = new TooltipFragment(text);
        tooltip.show(getFragmentManager(), "tooltip");
    }
}

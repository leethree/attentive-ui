package hk.hku.cs.srli.monkeydemo.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import hk.hku.cs.srli.monkeydemo.R;

public class TextFragment extends DemoFragmentBase {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        
        TextView text = new TextView(getActivity());
        text.setText(R.string.dummy_text);
        
        WindowManager.LayoutParams wlp = new WindowManager.LayoutParams();
        wlp.gravity = Gravity.TOP|Gravity.LEFT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
         
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        wm.addView(text, wlp);
        
        return rootView;
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_text;
    }
    
}

package hk.hku.cs.srli.widget;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TooltipFragment extends DialogFragment {

    private String text;
    
    public TooltipFragment(String text) {
        this.text = text;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_Tooltip);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.tooltip, container, true);
        TextView textview = (TextView) rootview.findViewById(android.R.id.message);
        textview.setText(text);
        return rootview;
    }
    
}

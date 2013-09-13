package hk.hku.cs.srli.widget.abs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.actionbarsherlock.internal.widget.ScrollingTabContainerView.TabView;

import hk.hku.cs.srli.widget.util.HoverHandler;

public class HoverTabView extends TabView {

    private HoverHandler hover;
    
    public HoverTabView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        hover = new HoverHandler(this);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event);
    }
}

package hk.hku.cs.srli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

public class HoverGridView extends GridView {
    
    private HoverDelegate hover;
    
    public HoverGridView(Context context) {
        super(context);
        init();
    }

    public HoverGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HoverGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        hover = new HoverDelegate(this);
        
        hover.setOnHoverEventListener(new HoverDelegate.OnHoverEventListener() {
            
            @Override
            public void onHoverExit(View v) {
                // clear selection
                setSelection(INVALID_POSITION);
            }
            
            @Override
            public void onHoverEnter(View v) {
                requestFocusFromTouch();
                setSelection(2);
            }
        });
    }
    
    @Override
    public void onHoverChanged(boolean hovered) {
        hover.onHoverChanged(hovered);
        super.onHoverChanged(hovered);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event) || super.onHoverEvent(event);
    }
}

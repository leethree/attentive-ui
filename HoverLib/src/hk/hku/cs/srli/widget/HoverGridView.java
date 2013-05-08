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
        
        hover.setOnHoverMoveListener(new HoverDelegate.OnHoverMoveListener() {
            
            @Override
            public void onHoverMove(View v, int x, int y) {
                int position = pointToPosition(x, y);
                if (isInTouchMode()) {
                    // exit touch mode
                    requestFocusFromTouch();
                }
                setSelection(position);
            }
        });
    }
    
    @Override
    public void onHoverChanged(boolean hovered) {
        hover.onHoverChanged(hovered);
        if (!hovered) {
            // clear selection
            setSelection(INVALID_POSITION);
        }
        super.onHoverChanged(hovered);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event) || super.onHoverEvent(event);
    }
}

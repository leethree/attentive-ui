package hk.hku.cs.srli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

import hk.hku.cs.srli.widget.util.HoverHandler;
import hk.hku.cs.srli.widget.util.HoverHandler.OnHoverMoveListener;

public class HoverGridView extends GridView implements OnHoverMoveListener {
    
    private HoverHandler hover;
    
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
        hover = new HoverHandler(this);
        hover.setOnHoverMoveListener(this);
    }

    
    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        if (!hovered) {
            // clear selection
            setSelection(INVALID_POSITION);
        }
    }
    
    @Override
    public void onHoverMove(View v, int x, int y) {
        int position = pointToPosition(x, y);
        if (isInTouchMode()) {
            // exit touch mode
            // TODO: find a way to get back to touch mode.
            requestFocusFromTouch();
        }
        setSelection(position);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event);
    }
}

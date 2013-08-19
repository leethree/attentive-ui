package hk.hku.cs.srli.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import hk.hku.cs.srli.widget.util.EdgeEffectHelper;
import hk.hku.cs.srli.widget.util.HoverHandler;

public class HoverScrollView extends ScrollView {
    
    private HoverHandler hover;
    private EdgeEffectHelper edge;
    
    public static enum ScrollState {TOP, MIDDLE, BOTTOM, NOT_SCROLLABLE}
    // to change this, use changeState(newState)
    private ScrollState state;
    
    public HoverScrollView(Context context) {
        super(context);
        init(null, 0);
    }

    public HoverScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HoverScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    
    private void init(AttributeSet attrs, int defStyle) {
        hover = new HoverHandler(this);
        edge = new EdgeEffectHelper(this);
        
        hover.setOnHoverMoveListener(edge);
        state = ScrollState.NOT_SCROLLABLE;
        edge.setEdgeGlow(false, false, false, false);
        setWillNotDraw(false);
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        edge.onHoverChanged(hovered);
    }
    
    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        // get all hover events from here 
        hover.onHoverEvent(event);
        // don't interference with children
        return false;
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        // already handled above
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        final int restoreCount = canvas.save();
        canvas.translate(0, getScrollY());
        edge.draw(canvas);
        canvas.restoreToCount(restoreCount);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateScrollState();
    }
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        updateScrollState();
    }
    
    private void updateScrollState() {
        if (getChildCount() <= 0) {
            changeState(ScrollState.NOT_SCROLLABLE);
            return;
        }
        
        // find the last child (should be the only child)
        float childHeight = getChildAt(getChildCount() - 1).getMeasuredHeight();
        if (getScrollY() <= 0) {
            if(childHeight <=  getHeight())
                changeState(ScrollState.NOT_SCROLLABLE);
            else
                changeState(ScrollState.TOP);
        } else if(childHeight <=  getHeight() + getScrollY()){
            // the bottom has been reached
            changeState(ScrollState.BOTTOM);
        } else {
            changeState(ScrollState.MIDDLE);
        }
    }
    
    private void changeState(ScrollState newState) {
        if (newState == state) return;
        state = newState;
        switch(state) {
            case NOT_SCROLLABLE:
                edge.setVerticalScrollable(false, false);
                return;
            case TOP:
                edge.setVerticalScrollable(false, true);
                return;
            case BOTTOM:
                edge.setVerticalScrollable(true, false);
                return;
            case MIDDLE:
                edge.setVerticalScrollable(true, true);
                return;
        }
    }
}

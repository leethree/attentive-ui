package hk.hku.cs.srli.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import hk.hku.cs.srli.widget.HoverScrollView.ScrollState;
import hk.hku.cs.srli.widget.util.EdgeEffectHelper;
import hk.hku.cs.srli.widget.util.HoverHandler;

// almost identical to GridView
public class HoverListView extends ListView {
    
    private HoverHandler hover;
    private EdgeEffectHelper edge;
    
    // to change this, use changeState(newState)
    private ScrollState state;
    
    public HoverListView(Context context) {
        super(context);
        init(null, 0);
    }

    public HoverListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HoverListView(Context context, AttributeSet attrs, int defStyle) {
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
        edge.draw(canvas);
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
        
        float top = getChildAt(0).getTop();
        float bottom = getChildAt(getChildCount() - 1).getBottom();

        boolean reachedTop = getFirstVisiblePosition() == 0 && top >= 0;
        boolean reachedBottom = 
                getLastVisiblePosition() == getAdapter().getCount() - 1 &&
                        bottom <=  getHeight();
        
        if (reachedTop && reachedBottom)
            changeState(ScrollState.NOT_SCROLLABLE);
        else if (reachedTop && !reachedBottom)
            changeState(ScrollState.TOP);
        else if (!reachedTop && reachedBottom)
            changeState(ScrollState.BOTTOM);
        else
            changeState(ScrollState.MIDDLE);
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

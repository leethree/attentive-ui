package hk.hku.cs.srli.factfinder.ui;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class FFSlidingPaneLayout extends SlidingPaneLayout {
    
    private boolean mTouchOnChildren = false;

    public FFSlidingPaneLayout(Context context) {
        super(context);
        init();
    }
    
    public FFSlidingPaneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public FFSlidingPaneLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
        setParallaxDistance(0);
    }
    
    public void setTouchOnChildren(boolean touchOnChildren) {
        this.mTouchOnChildren = touchOnChildren;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (this.mTouchOnChildren)
            // don't intercept event if touch on children is allowed.
            return false;
        else
            return super.onInterceptTouchEvent(arg0);
    }

}

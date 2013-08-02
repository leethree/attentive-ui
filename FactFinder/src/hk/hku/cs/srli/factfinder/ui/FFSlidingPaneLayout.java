package hk.hku.cs.srli.factfinder.ui;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class FFSlidingPaneLayout extends SlidingPaneLayout {

    public FFSlidingPaneLayout(Context context) {
        super(context);
    }
    
    public FFSlidingPaneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public FFSlidingPaneLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (isOpen())
            return super.onInterceptTouchEvent(arg0);
        else
            // don't intercept event if it's collapsed.
            return false;
    }

}

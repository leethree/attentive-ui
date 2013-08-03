package hk.hku.cs.srli.factfinder.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class FFSlidingPaneLayout extends SlidingPaneLayout {

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
        setParallaxDistance(20);
        // XXX this is not working
        setCoveredFadeColor(Color.WHITE);
        setSliderFadeColor(Color.WHITE);
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

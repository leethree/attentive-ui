package hk.hku.cs.srli.factfinder.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import hk.hku.cs.srli.widget.HoverButton;

public class PriceButton extends HoverButton {

    private boolean touched = false;
    private int pendingVisibility = VISIBLE;
    
    public PriceButton(Context context) {
        super(context);
        init();
    }

    public PriceButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PriceButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // nothing to do here
    }
    
    @Override
    public void setVisibility(int visibility) {
        // ignore if it's touched or hovered.
        if (touched || isHovered()) {
            pendingVisibility = visibility;
        } else {
            super.setVisibility(visibility);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            touched = false;
            setVisibility(pendingVisibility);
        } else {
            touched = true;
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        super.onHoverEvent(event);
        // do not consume the event
        return false;
    }
}

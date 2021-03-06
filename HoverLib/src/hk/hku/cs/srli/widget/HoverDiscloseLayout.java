
package hk.hku.cs.srli.widget;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import hk.hku.cs.srli.widget.util.HoverHandler;

public class HoverDiscloseLayout extends RelativeLayout {

    private HoverHandler hover;
    private boolean dimmed;
    
    public HoverDiscloseLayout(Context context) {
        super(context);
        init();
    }

    public HoverDiscloseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HoverDiscloseLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        hover = new HoverHandler(this, HoverHandler.HOVER_TIMEOUT * 2);
        if (!HoverHandler.isHoverEnabled(getContext())) {
            // disable itself
            setEnabled(false);
        }
        setBackgroundResource(R.drawable.transition_dim);
        dimmed = false;
    }
    
    @Override
    protected void onFinishInflate() {
        if (isEnabled()) setAllChildrenVisibility(INVISIBLE);
        super.onFinishInflate();
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        TransitionDrawable transition = (TransitionDrawable) getBackground();
        // animate background transition
        if (hovered) {
            if (dimmed) {
                transition.reverseTransition(250);
                dimmed = false;
            }
        } else {
            transition.startTransition(500);
            dimmed = true;
        }
        super.onHoverChanged(hovered);
        if (isEnabled()) updateChildrenVisibility();
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        hover.onHoverEvent(event);
        // do not consume the event
        return false;
    }
    
    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        boolean ret = super.dispatchHoverEvent(event);
        if (isEnabled()) updateChildrenVisibility();
        return ret;
    }
    
    private void updateChildrenVisibility() {
        if (isHovered() || hasActiveChild()) {
            setAllChildrenVisibility(VISIBLE);
        } else {
            setAllChildrenVisibility(INVISIBLE);
        }
    }
    
    private boolean hasActiveChild() {
        for(int i = 0; i < getChildCount(); ++i) {
            boolean ret = false;
            View child = getChildAt(i);
            ret |= child.isHovered();
            ret |= child.isPressed();
            ret |= child.isSelected();
            ret |= child.isFocused();
            if (ret) return true;
        }
        return false;
    }

    private void setAllChildrenVisibility(int visibility) {
        for(int i = 0; i < getChildCount(); ++i) {
            // TODO: maintain original state if child is already hidden. 
            getChildAt(i).setVisibility(visibility);
        }
    }
}

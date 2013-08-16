
package hk.hku.cs.srli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import hk.hku.cs.srli.widget.util.HoverHandler;

public class HoverDiscloseLayout extends RelativeLayout {

    private HoverHandler hover;
    
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
        hover = new HoverHandler(this);
    }
    
    @Override
    protected void onFinishInflate() {
        setAllChildrenVisibility(INVISIBLE);
        super.onFinishInflate();
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        updateChildrenVisibility();
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
        updateChildrenVisibility();
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

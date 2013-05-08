
package hk.hku.cs.srli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class HoverDiscloseLayout extends FrameLayout {

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
        hover.onHoverChanged(hovered);
        if (hovered) {
            setAllChildrenVisibility(VISIBLE);
        } else {
            setAllChildrenVisibility(INVISIBLE);
        }
        super.onHoverChanged(hovered);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event) || super.onHoverEvent(event);
    }
    
    private void setAllChildrenVisibility(int visibility) {
        for(int i = 0; i < getChildCount(); ++i) {
            // TODO: maintain original state if child is already hidden. 
            getChildAt(i).setVisibility(visibility);
        }
    }
}

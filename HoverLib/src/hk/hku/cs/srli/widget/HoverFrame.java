
package hk.hku.cs.srli.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import hk.hku.cs.srli.widget.util.EdgeEffectHelper;
import hk.hku.cs.srli.widget.util.HoverHandler;

public class HoverFrame extends FrameLayout {

    private HoverHandler hover;
    private EdgeEffectHelper edge;
    
    public HoverFrame(Context context) {
        super(context);
        init(null, 0);
    }

    public HoverFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HoverFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        hover = new HoverHandler(this);
        edge = new EdgeEffectHelper(this);
        
        hover.setOnHoverMoveListener(edge);
        edge.applyStyledAttributes(attrs, defStyle);
        
        setWillNotDraw(false);
    }
    
    public void setEdgeGlow(boolean left, boolean top, boolean right, boolean bottom) {
        edge.setEdgeGlow(left, top, right, bottom);
    }
    
    public void setEdgeGlowColorRes(int left, int top, int right, int bottom) {
        edge.setEdgeGlowColorRes(left, top, right, bottom);
    }
    
    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        edge.onHoverChanged(hovered);
    }
    
    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        hover.onHoverEvent(event);
        return super.onInterceptHoverEvent(event);
    }
   
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (isEnabled())
            edge.draw(canvas);
    }

}


package hk.hku.cs.srli.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import hk.hku.cs.srli.widget.util.EdgeEffectHelper;
import hk.hku.cs.srli.widget.util.HoverHandler;

public class HoverEdgeGlowLayout extends FrameLayout {

    private HoverHandler hover;
    private EdgeEffectHelper edge;
    
    public HoverEdgeGlowLayout(Context context) {
        super(context);
        init(null, 0);
    }

    public HoverEdgeGlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HoverEdgeGlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        hover = new HoverHandler(this);
        edge = new EdgeEffectHelper(this);
        
        hover.setOnHoverMoveListener(edge);
        hover.setOnLongHoverListener(edge);
        edge.applyStyledAttributes(attrs, defStyle);
        
        setWillNotDraw(false);
    }
    
    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        edge.onHoverChanged(hovered);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        hover.onHoverEvent(event);
        // do not consume the event
        return super.onHoverEvent(event);
    }
   
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        edge.draw(canvas);
    }

}

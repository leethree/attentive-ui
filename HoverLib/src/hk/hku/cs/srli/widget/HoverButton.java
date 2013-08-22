package hk.hku.cs.srli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;

import hk.hku.cs.srli.widget.util.HoverHandler;
import hk.hku.cs.srli.widget.util.TooltipManager;
import hk.hku.cs.srli.widget.util.HoverHandler.OnLongHoverListener;

/**
 * Button with Hover support.
 */
public class HoverButton extends Button
        implements OnLongClickListener, OnLongHoverListener {
    
    private HoverHandler hover;
    
    public HoverButton(Context context) {
        super(context);
        init();
    }

    public HoverButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HoverButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnLongClickListener(this);
        hover = new HoverHandler(this);
        hover.setOnLongHoverListener(this);
    }
    
    public HoverHandler getHoverHandler() {
        return hover;
    }
    
    @Override
    public boolean onLongClick(View v) {
        if (getContentDescription() != null && getContentDescription().length() > 0) {
            Tooltip tp = TooltipManager.showAndHide(this, getContentDescription(),
                    getWidth() * 1/2 + 15, getHeight() * 1/2 + 15, TooltipManager.LONG_DELAY);
            hover.attachTooltip(tp);
        }
        // do not prevent default action
        return false;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        // prevent the tooltip from leaking
        TooltipManager.hide(this);
        super.onDetachedFromWindow();
    }
    
    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        if (!hovered) {
            hover.dettachTooltip();
            TooltipManager.hide(this, 1000);
        }
    }
    
    @Override
    public boolean onLongHover(View v, int x, int y) {
        if (getContentDescription() != null && getContentDescription().length() > 0) {
            Tooltip tp = TooltipManager.show(this, getContentDescription(), x + 15, y + 15);
            hover.attachTooltip(tp);
        }
        return true;
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event);
    }
}

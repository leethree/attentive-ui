package hk.hku.cs.srli.widget.abs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.actionbarsherlock.internal.view.menu.ActionMenuItemView;

import hk.hku.cs.srli.widget.HoverHandler;
import hk.hku.cs.srli.widget.Tooltip;
import hk.hku.cs.srli.widget.TooltipManager;
import hk.hku.cs.srli.widget.TooltipManager.TooltipView;

public class HoverActionMenuItemView extends ActionMenuItemView implements TooltipView, HoverHandler.OnLongHoverListener {
    
    private HoverHandler hover;
    
    public HoverActionMenuItemView(Context context) {
        super(context);
        init();
    }

    public HoverActionMenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HoverActionMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
        hover = new HoverHandler(this);
        hover.setOnLongHoverListener(this);
    }
    
    // Intercept hover events to prevent children from hovering.
    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return true;
    }
    
    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        if (!hovered) {
            // clear tooltip
            TooltipManager.hide(this);
        }
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event);
    }

    @Override
    public boolean onLongHover(View v, int x, int y) {
        TooltipManager.show(this, getContentDescription(), x, y);
        return true;
    }

    @Override
    public void setTooltip(Tooltip tooltip) {
        if (tooltip != null) {
            tooltip.setHoverHandler(hover);
            hover.setTooltipMode(true);
        } else {
            hover.setTooltipMode(false);
        }
    }
}

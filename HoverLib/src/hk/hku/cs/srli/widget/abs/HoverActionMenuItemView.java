package hk.hku.cs.srli.widget.abs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.actionbarsherlock.internal.view.menu.ActionMenuItemView;
import com.actionbarsherlock.internal.view.menu.MenuItemImpl;

import hk.hku.cs.srli.widget.util.HoverHandler;
import hk.hku.cs.srli.widget.util.TooltipManager;

public class HoverActionMenuItemView extends ActionMenuItemView
        implements HoverHandler.OnLongHoverListener {
    
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
    
    @Override
    public void initialize(MenuItemImpl itemData, int menuType) { 
        super.initialize(itemData, menuType);
        // XXX: workaround for getting long content description
        setContentDescription(itemData.getTitle());
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
            hover.dettachTooltip();
        }
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event);
    }

    @Override
    public boolean onLongHover(View v, int x, int y) {
        hover.attachTooltip(TooltipManager.show(this, getContentDescription(), x, y));
        return true;
    }
}

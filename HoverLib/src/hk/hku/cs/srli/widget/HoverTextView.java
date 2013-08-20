package hk.hku.cs.srli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import hk.hku.cs.srli.widget.util.HoverHandler;
import hk.hku.cs.srli.widget.util.TooltipManager;
import hk.hku.cs.srli.widget.util.HoverHandler.OnLongHoverListener;

public class HoverTextView extends TextView implements OnLongHoverListener {
    
    private HoverHandler hover;

    public HoverTextView(Context context) {
        super(context);
        init();
    }
    
    public HoverTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public HoverTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
        hover = new HoverHandler(this);
        hover.setOnLongHoverListener(this);
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
            TooltipManager.hide(this);
            hover.dettachTooltip();
        }
    }
    
    @Override
    public boolean onLongHover(View v, int x, int y) {
        hover.attachTooltip(TooltipManager.show(HoverTextView.this, getText()));
        return true;
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event);
    }
}

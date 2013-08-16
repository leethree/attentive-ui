package hk.hku.cs.srli.factfinder.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;

import hk.hku.cs.srli.widget.Tooltip;
import hk.hku.cs.srli.widget.util.HoverHandler;
import hk.hku.cs.srli.widget.util.TooltipManager;
import hk.hku.cs.srli.widget.util.HoverHandler.OnLongHoverListener;

public class PriceButton extends Button 
        implements OnLongClickListener, OnLongHoverListener {

    private HoverHandler hover;
    private boolean touched;
    
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
        setOnLongClickListener(this);
        hover = new HoverHandler(this);
        hover.setOnLongHoverListener(this);
    }
    
    @Override
    public void setVisibility(int visibility) {
        Log.v("setVisibility", "vis: " + visibility);
        Log.v("setVisibility", "touched: " + touched);
        // ignore if it's touched.
        if (touched) return;
        super.setVisibility(visibility);
    }
    
    @Override
    public boolean onLongClick(View v) {
        if (getContentDescription() != null && getContentDescription().length() > 0) {
            Tooltip tp = TooltipManager.showAndHide(PriceButton.this, getContentDescription(),
                    getWidth() / 2, getHeight() / 2, TooltipManager.LONG_DELAY);
            hover.attachTooltip(tp);
        }
        return true;
    }
    
    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        if (!hovered) {
            hover.dettachTooltip();
            TooltipManager.hide(this);
        }
    }
    @Override
    public boolean onLongHover(View v, int x, int y) {
        if (getContentDescription() != null && getContentDescription().length() > 0) {
            Tooltip tp = TooltipManager.show(PriceButton.this, getContentDescription(), x, y);
            hover.attachTooltip(tp);
        }
        return true;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            touched = false;
            super.setVisibility(INVISIBLE);
        } else {
            touched = true;
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        hover.onHoverEvent(event);
        // do not consume the event
        return false;
    }
}

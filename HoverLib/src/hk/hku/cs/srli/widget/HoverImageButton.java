package hk.hku.cs.srli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;

import hk.hku.cs.srli.widget.util.HoverHandler;
import hk.hku.cs.srli.widget.util.TooltipManager;
import hk.hku.cs.srli.widget.util.HoverHandler.OnLongHoverListener;

/**
 * ImageButton with Hover support.
 */
public class HoverImageButton extends ImageButton
        implements OnLongClickListener, OnLongHoverListener {
    
    private HoverHandler hover;
    
    public HoverImageButton(Context context) {
        super(context);
        init();
    }

    public HoverImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HoverImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnLongClickListener(this);
        hover = new HoverHandler(this);
        hover.setOnLongHoverListener(this);
    }
    @Override
    public boolean onLongClick(View v) {
        if (getContentDescription() != null && getContentDescription().length() > 0) {
            Tooltip tp = TooltipManager.showAndHide(HoverImageButton.this, getContentDescription(),
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
            Tooltip tp = TooltipManager.show(HoverImageButton.this, getContentDescription(), x, y);
            hover.attachTooltip(tp);
        }
        return true;
    }
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event);
    }
}

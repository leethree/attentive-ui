package hk.hku.cs.srli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

/**
 * ImageButton with Hover support.
 */
public class HoverImageButton extends ImageButton {
    
    private HoverDelegate hover;
    
    public HoverImageButton(Context context) {
        super(context);
        init(null, 0);
    }

    public HoverImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HoverImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setOnLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                TooltipManager.show(HoverImageButton.this, getContentDescription(),
                        getWidth() / 2, getHeight() / 2);
                return false;
            }
        });
        hover = new HoverDelegate(this);
        hover.setOnLongHoverListener(new HoverDelegate.OnLongHoverListener() {
            
            @Override
            public boolean onLongHover(View v, int x, int y) {
                TooltipManager.show(HoverImageButton.this, getContentDescription(),
                        x, y);
                return false;
            }
        });
    }
    
    @Override
    public void onHoverChanged(boolean hovered) {
        hover.onHoverChanged(hovered);
        super.onHoverChanged(hovered);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event) || super.onHoverEvent(event);
    }
}


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

    private float mHoverX;
    private float mHoverY;
    
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

    }
    
    private boolean hovered;
    
    @Override
    public void onHoverChanged(boolean hovered) {
        this.hovered = hovered;
        super.onHoverChanged(hovered);
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE) {
            mHoverX = event.getRawX();
            mHoverY = event.getRawY();
            if (hovered) {
                TooltipManager.show(HoverImageButton.this, getContentDescription());
            }
        }
        return super.onHoverEvent(event);
    }
}

package hk.hku.cs.srli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class HoverTextView extends TextView {
    
    private HoverDelegate hover;

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
        hover = new HoverDelegate(this);
        hover.setOnLongHoverListener(new HoverDelegate.OnLongHoverListener() {
            
            @Override
            public boolean onLongHover(View v, int x, int y) {
                TooltipManager.show(HoverTextView.this, getText());
                return true;
            }
        });
        
        hover.setOnHoverEventListener(new HoverDelegate.OnHoverEventListener() {
            
            @Override
            public void onHoverExit(View v) {
                TooltipManager.hide(HoverTextView.this);
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

package hk.hku.cs.srli.widget;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class HoverDelegate {

    private View view;
    private OnLongHoverListener onLongHoverListener;
    
    private boolean hovered = false;
    private float hoverX;
    private float hoverY;
    
    private boolean hasPerformedLongHover = false;
    private CheckForLongHover pendingCheckForLongHover;
    
    public HoverDelegate(View view) {
        this.view = view;
    }
    
    public void onHoverChanged(boolean hovered) {
        if (hovered && !this.hovered) {
            checkForLongHover();
        }
        this.hovered = hovered;
    }
    
    public boolean onHoverEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE) {
            if (hovered) {
                hoverX = event.getRawX();
                hoverY = event.getRawY();
            }
        }
        return false;
    }
    
    public void setOnLongHoverListener(OnLongHoverListener onLongHoverListener) {
        this.onLongHoverListener = onLongHoverListener;
    }
    
    public interface OnLongHoverListener {
        
        public boolean onLongHover(View v, int x, int y);
    }
    
    private void checkForLongHover() {
        if (onLongHoverListener != null) {
            hasPerformedLongHover = false;

            if (pendingCheckForLongHover == null) {
                pendingCheckForLongHover = new CheckForLongHover();
            }

            view.postDelayed(pendingCheckForLongHover,
                    ViewConfiguration.getLongPressTimeout());
        }
    }
    
    private class CheckForLongHover implements Runnable {

        public void run() {
            if (hovered && !hasPerformedLongHover
                    && onLongHoverListener != null) {
                final int[] screenPos = new int[2];
                view.getLocationOnScreen(screenPos);
                int x = (int) hoverX - screenPos[0];
                int y = (int) hoverY - screenPos[1];
                if (onLongHoverListener.onLongHover(view, x, y)) {
                    hasPerformedLongHover = true;
                }
            }
        }
    }
}

package hk.hku.cs.srli.widget;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class HoverDelegate {

    private View view;
    private OnLongHoverListener onLongHoverListener;
    private OnHoverEventListener onHoverEventListener;
    
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
            if (onHoverEventListener != null) onHoverEventListener.onHoverEnter(view);
            checkForLongHover();
        } else if (!hovered && this.hovered) {
            if (onHoverEventListener != null) onHoverEventListener.onHoverExit(view);
        }
        this.hovered = hovered;
    }
    
    public boolean onHoverEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                // workaround for non-hoverable views.
                view.setHovered(true);
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                view.setHovered(false);
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                if (hovered) {
                    hoverX = event.getRawX();
                    hoverY = event.getRawY();
                }
                break;
        }
        return false;
    }
    
    public void setOnLongHoverListener(OnLongHoverListener onLongHoverListener) {
        this.onLongHoverListener = onLongHoverListener;
    }
    
    public void setOnHoverEventListener(OnHoverEventListener onHoverEventListener) {
        this.onHoverEventListener = onHoverEventListener;
    }
    
    public interface OnLongHoverListener {
        
        public boolean onLongHover(View v, int x, int y);
    }
    
    public interface OnHoverEventListener {
        public void onHoverEnter(View v);
        public void onHoverExit(View v);
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

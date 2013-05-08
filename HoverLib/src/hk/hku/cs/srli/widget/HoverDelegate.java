package hk.hku.cs.srli.widget;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class HoverDelegate {

    private View view;
    private OnLongHoverListener onLongHoverListener;
    private OnHoverEventListener onHoverEventListener;
    private OnHoverMoveListener onHoverMoveListener;
    
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
                    if (onHoverMoveListener != null) {
                        final int[] screenPos = new int[2];
                        getLocalCoordinate(screenPos);
                        onHoverMoveListener.onHoverMove(view, screenPos[0], screenPos[1]);
                    }
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
    
    public void setOnHoverMoveListener(OnHoverMoveListener onHoverMoveListener) {
        this.onHoverMoveListener = onHoverMoveListener;
    }
    
    public interface OnLongHoverListener {
        
        public boolean onLongHover(View v, int x, int y);
    }
    
    // TODO: remove this and just use onHoverChanged. 
    public interface OnHoverEventListener {
        public void onHoverEnter(View v);
        public void onHoverExit(View v);
    }
    
    public interface OnHoverMoveListener {
        public void onHoverMove(View v, int x, int y); 
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
                getLocalCoordinate(screenPos);
                if (onLongHoverListener.onLongHover(view, screenPos[0], screenPos[1])) {
                    hasPerformedLongHover = true;
                }
            }
        }
    }
    
    private void getLocalCoordinate(int[] position) {
        view.getLocationOnScreen(position);
        position[0] = (int) hoverX - position[0];
        position[1] = (int) hoverY - position[1];
    }
}

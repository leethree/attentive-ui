package hk.hku.cs.srli.widget.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import hk.hku.cs.srli.widget.R;
import hk.hku.cs.srli.widget.Tooltip;

public class TooltipManager {

    public static final long LONG_DELAY = 3500; // 3.5 seconds
    public static final long SHORT_DELAY = 2000; // 2 seconds
    
    private static TooltipManager instance = null;
    
    private final Handler handler;
    
    private Tooltip tooltip;
    private Context context;
    
    // Singleton object.
    private TooltipManager(Context context) {
        this.context = context;
        this.handler = new Handler();
    }
    
    private static TooltipManager getInstance(Context context) {
        if (instance == null) {
            instance = new TooltipManager(context);
        } else {
            instance.context = context;
        }
        return instance;
    }
    
    public static Tooltip show(View view, CharSequence text) {
        return show(view, text, 0, 0);
    }
    
    public static Tooltip show(View view, CharSequence text, int xoffset, int yoffset) {
        return getInstance(view.getContext()).showTooltipNow(view, text, xoffset, yoffset);
    }
    
    public static Tooltip show(View view, CharSequence text, long delay) {
        return show(view, text, 0, 0, delay);
    }
    
    public static Tooltip show(View view, CharSequence text, int xoffset, int yoffset, long delay) {
        if (delay > 0) {
            return getInstance(view.getContext()).showTooltipLater(view, text, xoffset, yoffset, delay);
        } else {
            return show(view, text, xoffset, yoffset);
        }
    }
    
    public static Tooltip showAndHide(View view, CharSequence text, long duration) {
        return showAndHide(view, text, 0, 0, duration);
    }
    
    public static Tooltip showAndHide(View view, CharSequence text, int xoffset, int yoffset, long duration) {
        if (duration > 0) {
            Tooltip ret = getInstance(view.getContext()).showTooltipNow(view, text, xoffset, yoffset);
            instance.hideTooltipLater(duration);
            return ret;
        }
        return null;
    }
    
    public static void hide(View view) {
        getInstance(view.getContext()).hideTooltipNow();
    }
    
    public static void hide(View view, long delay) {
        if (delay > 0) {
            getInstance(view.getContext()).hideTooltipLater(delay);
        } else {
            hide(view);
        }
    }
     
    private Tooltip showTooltipNow(View view, CharSequence text, int xoffset, int yoffset) {
        final Tooltip tooltip = makeTooltip(text);
        doShow(tooltip, makeParams(view, xoffset, yoffset));
        return tooltip;
    }
    
    private Tooltip showTooltipLater(View view, CharSequence text, int xoffset, int yoffset, long delay) {
        final Tooltip tooltip = makeTooltip(text);
        final WindowManager.LayoutParams params = makeParams(view, xoffset, yoffset);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.doShow(tooltip, params);
            }
        }, delay);
        return tooltip;
    }
    
    private void hideTooltipNow() {
        doHide(tooltip);
        tooltip = null;
    }
    
    private void hideTooltipLater(long delay) {
        final Tooltip tooltip = this.tooltip;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.doHide(tooltip);
            }
        }, delay);
    }
    
    private Tooltip makeTooltip(CharSequence text) {
        Tooltip tooltip = (Tooltip) LayoutInflater.from(context).inflate(R.layout.tooltip, null);
        tooltip.setText(text);
        return tooltip;
    }
    
    private WindowManager.LayoutParams makeParams(View view, int xoffset, int yoffset) {
        final int[] screenPos = new int[2];
        final Rect displayFrame = new Rect();
        view.getLocationOnScreen(screenPos);
        view.getWindowVisibleDisplayFrame(displayFrame);
        
        WindowManager.LayoutParams params = Tooltip.getDefaultLayoutParams();
        params.gravity = Gravity.TOP|Gravity.LEFT;
        params.x = xoffset + screenPos[0] - displayFrame.left;
        params.y = yoffset + screenPos[1] - displayFrame.top;
        return params;
    }
    
    private void doShow(Tooltip tooltip, WindowManager.LayoutParams params) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(tooltip, params); // Show new tooltip first.
        doHide(this.tooltip); // Then hide existing tooltip if any.
        this.tooltip = tooltip;
    }
    
    private void doHide(Tooltip tooltip) {
        if (tooltip != null) {
            // TODO: remove this when HoverHandler correctly handles tooltip events.
            if (tooltip.isHovered()) { // The tooltip is currently in focus.
                final Tooltip tooltipToHide = tooltip;
                // Try to hide it later.
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        instance.doHide(tooltipToHide);
                    }
                }, 1000);
            } else if (tooltip.getParent() != null) { // Make sure it's attached before removing.
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.removeView(tooltip);
            }
        }
    }
}

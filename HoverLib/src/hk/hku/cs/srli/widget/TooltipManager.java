package hk.hku.cs.srli.widget;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class TooltipManager {

    private static TooltipManager instance = null;
    
    private Tooltip tooltip;
    private Context context;
    
    // Single object.
    private TooltipManager(Context context) {
        this.context = context;
    }
    
    public static TooltipManager getInstance(Context context) {
        if (instance == null) {
            instance = new TooltipManager(context);
        }
        return instance;
    }
    
    public static void show(View view, CharSequence text) {
        getInstance(view.getContext()).showTooltip(view, text);
    }
    
    public static void show(View view, CharSequence text, int xoffset, int yoffset) {
        getInstance(view.getContext()).showTooltip(view, text, xoffset, yoffset);
    }
    
    public void showTooltip(View view, CharSequence text) {
        showTooltip(view, text, 0, 0);
    }
    
    public void showTooltip(View view, CharSequence text, int xoffset, int yoffset) {
        final int[] screenPos = new int[2];
        final Rect displayFrame = new Rect();
        view.getLocationOnScreen(screenPos);
        view.getWindowVisibleDisplayFrame(displayFrame);
        xoffset = xoffset + screenPos[0] - displayFrame.left;
        yoffset = yoffset + screenPos[1] - displayFrame.top;
        
        hideTooltip(); // Hide existing tooltip.
        tooltip = new Tooltip(context);
        tooltip.setText(text);
        WindowManager.LayoutParams params = Tooltip.getDefaultLayoutParams();
        if (xoffset > 0 && yoffset > 0) {
            params.gravity = Gravity.TOP|Gravity.LEFT;
            params.x = xoffset;
            params.y = yoffset;
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(tooltip, params);
    }
    
    public static void hide(View view) {
        getInstance(view.getContext()).hideTooltip();
    }
    
    public void hideTooltip() {
        if (tooltip != null) {
            // Make sure it's attached before removing.
            if (tooltip.getParent() != null) {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.removeView(tooltip);
            }
            tooltip = null;
        }
    }
}

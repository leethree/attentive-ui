package hk.hku.cs.srli.widget;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class TooltipManager {

    private static TooltipManager instance = null;
    
    private Toast tooltip;
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
        
        if (tooltip != null) tooltip.cancel();
        tooltip = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        if (xoffset > 0 && yoffset > 0) {
            tooltip.setGravity(Gravity.TOP|Gravity.LEFT, xoffset, yoffset);
        }
        tooltip.show();
    }
}

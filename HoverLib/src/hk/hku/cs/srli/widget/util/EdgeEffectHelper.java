package hk.hku.cs.srli.widget.util;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import hk.hku.cs.srli.widget.R;
import hk.hku.cs.srli.widget.util.HoverHandler.OnHoverMoveListener;

public class EdgeEffectHelper implements OnHoverMoveListener {
    
    public static final int DEFAULT_COLOR = 0;
    public static final int SCROLL_COLOR = R.color.scrollable_edge;
    public static final int OVERSCROLL_COLOR = R.color.overscroll_edge;
    
    private View view;
    
    private EdgeEffect leftEdge;
    private EdgeEffect rightEdge;
    private EdgeEffect topEdge;
    private EdgeEffect bottomEdge;
    
    private boolean leftEdgeGlow = false;
    private boolean rightEdgeGlow = false;
    private boolean topEdgeGlow = false;
    private boolean bottomEdgeGlow = false;
    
    // use transparent color as default
    private int leftEdgeColor = DEFAULT_COLOR;
    private int rightEdgeColor = DEFAULT_COLOR;
    private int topEdgeColor = DEFAULT_COLOR;
    private int bottomEdgeColor = DEFAULT_COLOR;
    
    public EdgeEffectHelper(View view) {
        this.view = view; 
        
        leftEdge = new EdgeEffect(view.getContext());
        rightEdge = new EdgeEffect(view.getContext());
        topEdge = new EdgeEffect(view.getContext());
        bottomEdge = new EdgeEffect(view.getContext());
    }
    
    public void applyStyledAttributes(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = view.getContext().obtainStyledAttributes(
                attrs, R.styleable.EdgeGlow, defStyle, 0);
        
        try {
            leftEdgeGlow = a.getBoolean(R.styleable.EdgeGlow_leftEdgeGlow, false);
            rightEdgeGlow = a.getBoolean(R.styleable.EdgeGlow_rightEdgeGlow, false);
            topEdgeGlow = a.getBoolean(R.styleable.EdgeGlow_topEdgeGlow, false);
            bottomEdgeGlow = a.getBoolean(R.styleable.EdgeGlow_bottomEdgeGlow, false);
            leftEdgeColor = a.getColor(R.styleable.EdgeGlow_leftEdgeColor, 0);
            rightEdgeColor = a.getColor(R.styleable.EdgeGlow_rightEdgeColor, 0);
            topEdgeColor = a.getColor(R.styleable.EdgeGlow_topEdgeColor, 0);
            bottomEdgeColor = a.getColor(R.styleable.EdgeGlow_bottomEdgeColor, 0);
        } finally {
            a.recycle();
        }
        
        updateColors();
    }

    @Override
    public void onHoverMove(View v, int x, int y) {
        final float ratioX = (float) x / view.getWidth();
        final float ratioY = (float) y / view.getHeight();
        final float factor = 0.6f;
        
        if (leftEdgeGlow) leftEdge.onDrift(ratioX * factor);
        if (rightEdgeGlow) rightEdge.onDrift((1 - ratioX) * factor);
        if (topEdgeGlow) topEdge.onDrift(ratioY * factor);
        if (bottomEdgeGlow) bottomEdge.onDrift((1 - ratioY) * factor);

        if (!areEdgeEffectsFinished()) {
            // edge effects not finished, refresh UI
            view.postInvalidateOnAnimation();
        }
    }
    
    public void onHoverChanged(boolean hovered) {
        if (hovered) {
            if (leftEdgeGlow) leftEdge.onRampUp();
            if (rightEdgeGlow) rightEdge.onRampUp();
            if (topEdgeGlow) topEdge.onRampUp();
            if (bottomEdgeGlow) bottomEdge.onRampUp();
        } else {
            // release all edge effects
            if (leftEdgeGlow) leftEdge.onRampDown();
            if (rightEdgeGlow) rightEdge.onRampDown();
            if (topEdgeGlow) topEdge.onRampDown();
            if (bottomEdgeGlow) bottomEdge.onRampDown();
        }
        if (!areEdgeEffectsFinished()) {
            view.postInvalidateOnAnimation();
        }
    }
    
    public void draw(Canvas canvas) {
        boolean needsInvalidate = false;
        final int outerHeight = view.getHeight();
        final int outerWidth = view.getWidth();
        final int innerHeight = outerHeight - view.getPaddingTop() - view.getPaddingBottom();
        final int innerWidth = outerWidth - view.getPaddingLeft() - view.getPaddingRight();
        if (topEdgeGlow && !topEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(view.getPaddingLeft(), 0);
            topEdge.setSize(innerWidth, outerHeight);
            needsInvalidate |= topEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (rightEdgeGlow && !rightEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(90);
            canvas.translate(view.getPaddingTop(), -outerWidth);
            rightEdge.setSize(innerHeight, outerWidth);
            needsInvalidate |= rightEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (bottomEdgeGlow && !bottomEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(180);
            canvas.translate(-innerWidth - view.getPaddingLeft(), -outerHeight);
            bottomEdge.setSize(innerWidth, outerHeight);
            needsInvalidate |= bottomEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (leftEdgeGlow && !leftEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(270);
            canvas.translate(-innerHeight - view.getPaddingTop(), 0);
            leftEdge.setSize(innerHeight, outerWidth);
            needsInvalidate |= leftEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (needsInvalidate) {
            // Keep animating
            view.postInvalidateOnAnimation();
        }
    }
    
    public void setEdgeGlow(boolean left, boolean top, boolean right, boolean bottom) {
        leftEdgeGlow = left;
        rightEdgeGlow = right;
        topEdgeGlow = top;
        bottomEdgeGlow = bottom;
        if (!left) leftEdge.finish();
        if (!right) rightEdge.finish();
        if (!top) topEdge.finish();
        if (!bottom) bottomEdge.finish();
    }
    
    public void setEdgeGlowColorRes(int left, int top, int right, int bottom) {
        if (left != 0) leftEdgeColor = getColor(left);
        else leftEdgeColor = 0;
        if (right != 0) rightEdgeColor = getColor(right);
        else rightEdgeColor = 0;
        if (top != 0) topEdgeColor = getColor(top);
        else topEdgeColor = 0;
        if (bottom != 0) bottomEdgeColor = getColor(bottom);
        else bottomEdgeColor = 0;
        updateColors();
    }
    
    public void setHorizontalScrollable(boolean leftScrollable, boolean rightScrollable) {
        if (!leftScrollable && !rightScrollable) {
            // not scrollable at all
            leftEdgeGlow = false;
            rightEdgeGlow = false;
            leftEdge.finish();
            rightEdge.finish();
        } else {
            leftEdgeGlow = true;
            rightEdgeGlow = true;
            leftEdgeColor = getColor(leftScrollable ? 
                    EdgeEffectHelper.SCROLL_COLOR : EdgeEffectHelper.OVERSCROLL_COLOR);
            rightEdgeColor = getColor(rightScrollable ? 
                    EdgeEffectHelper.SCROLL_COLOR : EdgeEffectHelper.OVERSCROLL_COLOR);
            updateColors();
        }
    }
    
    public void setVerticalScrollable(boolean topScrollable, boolean bottomScrollable) {
        if (!topScrollable && !bottomScrollable) {
            topEdgeGlow = false;
            bottomEdgeGlow = false;
            topEdge.finish();
            bottomEdge.finish();
        } else {
            topEdgeGlow = true;
            bottomEdgeGlow = true;
            topEdgeColor = getColor(topScrollable ? 
                    EdgeEffectHelper.SCROLL_COLOR : EdgeEffectHelper.OVERSCROLL_COLOR);
            bottomEdgeColor = getColor(bottomScrollable ? 
                    EdgeEffectHelper.SCROLL_COLOR : EdgeEffectHelper.OVERSCROLL_COLOR);
            updateColors();
        }
    }

    private boolean areEdgeEffectsFinished() {
        return leftEdge.isFinished() && rightEdge.isFinished()
                && topEdge.isFinished() && bottomEdge.isFinished();
    }
    
    private void updateColors() {
        if (leftEdgeColor != 0) leftEdge.setColor(leftEdgeColor); 
        else leftEdge.clearColor();
        if (rightEdgeColor != 0) rightEdge.setColor(rightEdgeColor);
        else rightEdge.clearColor();
        if (topEdgeColor != 0) topEdge.setColor(topEdgeColor);
        else topEdge.clearColor();
        if (bottomEdgeColor != 0) bottomEdge.setColor(bottomEdgeColor);
        else bottomEdge.clearColor();
    }
    
    private int getColor(int res) {
        return view.getResources().getColor(res);
    }
}

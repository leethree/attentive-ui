package hk.hku.cs.srli.widget.util;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import hk.hku.cs.srli.widget.R;
import hk.hku.cs.srli.widget.util.HoverHandler.OnHoverMoveListener;
import hk.hku.cs.srli.widget.util.HoverHandler.OnLongHoverListener;

public class EdgeEffectHelper implements OnHoverMoveListener, OnLongHoverListener {
    
    public static final int DEFAULT_COLOR = 0;
    public static final int SCROLL_COLOR = android.R.color.holo_blue_dark;
    public static final int OVERSCROLL_COLOR = android.R.color.darker_gray;
    
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
    
    private boolean hoverMoving;
    
    // last hover positions
    private int lastX;
    private int lastY;
    
    public EdgeEffectHelper(View view) {
        this.view = view; 
        
        leftEdge = new EdgeEffect(view.getContext());
        rightEdge = new EdgeEffect(view.getContext());
        topEdge = new EdgeEffect(view.getContext());
        bottomEdge = new EdgeEffect(view.getContext());
        
        hoverMoving = false;
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
        if (!hoverMoving) {
            // prepare to move!
            lastX = x;
            lastY = y;
            hoverMoving = true;
            return;
        }
        
        final float doubleWidth = view.getWidth() * 2;
        final float doubleHeight = view.getHeight() * 2;
        float deltaX = x - lastX;
        float deltaY = y - lastY;
        
        if (deltaX < -5) {
            if (leftEdgeGlow) leftEdge.onPull(0.5f - x / doubleWidth);
        } else if (deltaX > 5) {
            if (rightEdgeGlow) rightEdge.onPull(x / doubleWidth);
        }
        if (deltaY < -5) {
            if (topEdgeGlow) topEdge.onPull(0.5f - y / doubleHeight);
        } else if (deltaY > 5) {
            if (bottomEdgeGlow) bottomEdge.onPull(y / doubleHeight);
        }

        lastX = x;
        lastY = y;
        if (!areEdgeEffectsFinished()) {
            // edge effects not finished, refresh UI
            view.postInvalidateOnAnimation();
        }
    }
    
    @Override
    public boolean onLongHover(View v, int x, int y) {
        if (leftEdgeGlow) leftEdge.onPull(0.25f);
        if (rightEdgeGlow) rightEdge.onPull(0.25f);
        if (topEdgeGlow) topEdge.onPull(0.25f);
        if (bottomEdgeGlow) bottomEdge.onPull(0.25f);
        
        if (!areEdgeEffectsFinished()) {
            view.postInvalidateOnAnimation();
        }
        
        // prevent decaying
        leftEdge.setDecay(false);
        rightEdge.setDecay(false);
        topEdge.setDecay(false);
        bottomEdge.setDecay(false);
        return true;
    }
    
    public void onHoverChanged(boolean hovered) {
        if (!hovered) {
            // release all edge effects
            if (leftEdgeGlow) leftEdge.onRelease();
            if (rightEdgeGlow) rightEdge.onRelease();
            if (topEdgeGlow) topEdge.onRelease();
            if (bottomEdgeGlow) bottomEdge.onRelease();
            hoverMoving = false;
            if (!areEdgeEffectsFinished()) {
                view.postInvalidateOnAnimation();
            }
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
            needsInvalidate |= topEdge.draw(canvas, true, false);
            canvas.restoreToCount(restoreCount);
        }
        if (rightEdgeGlow && !rightEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(90);
            canvas.translate(view.getPaddingTop(), -outerWidth);
            rightEdge.setSize(innerHeight, outerWidth);
            needsInvalidate |= rightEdge.draw(canvas, true, false);
            canvas.restoreToCount(restoreCount);
        }
        if (bottomEdgeGlow && !bottomEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(180);
            canvas.translate(-innerWidth - view.getPaddingLeft(), -outerHeight);
            bottomEdge.setSize(innerWidth, outerHeight);
            needsInvalidate |= bottomEdge.draw(canvas, true, false);
            canvas.restoreToCount(restoreCount);
        }
        if (leftEdgeGlow && !leftEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(270);
            canvas.translate(-innerHeight - view.getPaddingTop(), 0);
            leftEdge.setSize(innerHeight, outerWidth);
            needsInvalidate |= leftEdge.draw(canvas, true, false);
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
        if (left != 0) leftEdgeColor = view.getResources().getColor(left);
        else leftEdgeColor = 0;
        if (right != 0) rightEdgeColor = view.getResources().getColor(right);
        else rightEdgeColor = 0;
        if (top != 0) topEdgeColor = view.getResources().getColor(top);
        else topEdgeColor = 0;
        if (bottom != 0) bottomEdgeColor = view.getResources().getColor(bottom);
        else bottomEdgeColor = 0;
        updateColors();
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
}

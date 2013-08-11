
package hk.hku.cs.srli.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import hk.hku.cs.srli.widget.util.EdgeEffect;
import hk.hku.cs.srli.widget.util.HoverHandler;
import hk.hku.cs.srli.widget.util.HoverHandler.OnHoverMoveListener;

public class HoverEdgeGlowLayout extends FrameLayout implements OnHoverMoveListener {

    private boolean leftEdgeGlow = false;
    private boolean rightEdgeGlow = false;
    private boolean topEdgeGlow = false;
    private boolean bottomEdgeGlow = false;
    
    // use transparent color as default
    private int leftEdgeColor = 0;
    private int rightEdgeColor = 0;
    private int topEdgeColor = 0;
    private int bottomEdgeColor = 0;
    
    private HoverHandler hover;
    private EdgeEffect leftEdge;
    private EdgeEffect rightEdge;
    private EdgeEffect topEdge;
    private EdgeEffect bottomEdge;
    
    private int lastX;
    private int lastY;
    
    public HoverEdgeGlowLayout(Context context) {
        super(context);
        init(null, 0);
    }

    public HoverEdgeGlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HoverEdgeGlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EdgeGlow, defStyle, 0);
        
        try {
            leftEdgeGlow = a.getBoolean(R.styleable.EdgeGlow_leftEdgeGlow, leftEdgeGlow);
            rightEdgeGlow = a.getBoolean(R.styleable.EdgeGlow_rightEdgeGlow, rightEdgeGlow);
            topEdgeGlow = a.getBoolean(R.styleable.EdgeGlow_topEdgeGlow, topEdgeGlow);
            bottomEdgeGlow = a.getBoolean(R.styleable.EdgeGlow_bottomEdgeGlow, bottomEdgeGlow);
            leftEdgeColor = a.getColor(R.styleable.EdgeGlow_leftEdgeColor, leftEdgeColor);
            rightEdgeColor = a.getColor(R.styleable.EdgeGlow_rightEdgeColor, rightEdgeColor);
            topEdgeColor = a.getColor(R.styleable.EdgeGlow_topEdgeColor, topEdgeColor);
            bottomEdgeColor = a.getColor(R.styleable.EdgeGlow_bottomEdgeColor, bottomEdgeColor);
        } finally {
            a.recycle();
        }

        hover = new HoverHandler(this);
        hover.setOnHoverMoveListener(this);
        leftEdge = new EdgeEffect(getContext());
        rightEdge = new EdgeEffect(getContext());
        topEdge = new EdgeEffect(getContext());
        bottomEdge = new EdgeEffect(getContext());

        updateColors();
        
        setWillNotDraw(false);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        lastX = getWidth() / 2;
        lastY = getHeight() / 2;
    }
    
    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        if (!hovered) {
            // release all edge effects
            if (leftEdgeGlow) leftEdge.onRelease();
            if (rightEdgeGlow) rightEdge.onRelease();
            if (topEdgeGlow) topEdge.onRelease();
            if (bottomEdgeGlow) bottomEdge.onRelease();
            if (!areEdgeEffectsFinished()) {
                postInvalidateOnAnimation();
            }
        }
    }
    
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        //Log.v("onHoverEvent", ""+event);
        hover.onHoverEvent(event);
        // do not consume the event
        return super.onHoverEvent(event);
    }

    @Override
    public void onHoverMove(View v, int x, int y) {
        final float width = getWidth();
        final float height = getHeight();
        final float factor = width * height;
        float deltaX = x - lastX;
        float deltaY = y - lastY;
        // the height and width below are inverted by purpose
        if (deltaX < 0) {
            if (leftEdgeGlow) leftEdge.onPull(-deltaX * (1 - x) / factor);
        } else {
            if (rightEdgeGlow) rightEdge.onPull(deltaX * x / factor);
        }
        if (deltaY < 0) {
            if (topEdgeGlow) topEdge.onPull(-deltaY * (1 - y) / factor);
        } else {
            if (bottomEdgeGlow) bottomEdge.onPull(deltaY * y / factor);
        }
        lastX = x;
        lastY = y;
        if (!areEdgeEffectsFinished()) {
            // edge effects not finished, refresh UI
            postInvalidateOnAnimation();
        }
    }
    
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        boolean needsInvalidate = false;
        final int outerHeight = getHeight();
        final int outerWidth = getWidth();
        final int innerHeight = outerHeight - getPaddingTop() - getPaddingBottom();
        final int innerWidth = outerWidth - getPaddingLeft() - getPaddingRight();
        if (topEdgeGlow && !topEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(getPaddingLeft(), 0);
            topEdge.setSize(innerWidth, outerHeight);
            needsInvalidate |= topEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (rightEdgeGlow && !rightEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(90);
            canvas.translate(getPaddingTop(), -outerWidth);
            rightEdge.setSize(innerHeight, outerWidth);
            needsInvalidate |= rightEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (bottomEdgeGlow && !bottomEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(180);
            canvas.translate(-innerWidth - getPaddingLeft(), -outerHeight);
            bottomEdge.setSize(innerWidth, outerHeight);
            needsInvalidate |= bottomEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (leftEdgeGlow && !leftEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(270);
            canvas.translate(-innerHeight - getPaddingTop(), 0);
            leftEdge.setSize(innerHeight, outerWidth);
            needsInvalidate |= leftEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (needsInvalidate) {
            // Keep animating
            postInvalidateOnAnimation();
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
    
    public void setEdgeGlowColor(int left, int top, int right, int bottom) {
        leftEdgeColor = left;
        rightEdgeColor = right;
        topEdgeColor = top;
        bottomEdgeColor = bottom;
        updateColors();
    }
    
    private boolean areEdgeEffectsFinished() {
        return leftEdge.isFinished() && rightEdge.isFinished()
                && topEdge.isFinished() && bottomEdge.isFinished();
    }
    
    private void updateColors() {
        if (leftEdgeColor != 0) leftEdge.setColor(leftEdgeColor); else leftEdge.clearColor();
        if (rightEdgeColor != 0) rightEdge.setColor(rightEdgeColor);  else rightEdge.clearColor();
        if (topEdgeColor != 0) topEdge.setColor(topEdgeColor); else topEdge.clearColor();
        if (bottomEdgeColor != 0) bottomEdge.setColor(bottomEdgeColor); else bottomEdge.clearColor();
    }
}

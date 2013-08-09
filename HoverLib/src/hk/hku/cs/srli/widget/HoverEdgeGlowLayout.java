
package hk.hku.cs.srli.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.FrameLayout;

import hk.hku.cs.srli.widget.util.HoverHandler;
import hk.hku.cs.srli.widget.util.HoverHandler.OnHoverMoveListener;

public class HoverEdgeGlowLayout extends FrameLayout implements OnHoverMoveListener {

    private HoverHandler hover;
    private EdgeEffect leftEdge;
    private EdgeEffect rightEdge;
    private EdgeEffect topEdge;
    private EdgeEffect bottomEdge;
    
    private int lastX;
    private int lastY;
    
    public HoverEdgeGlowLayout(Context context) {
        super(context);
        init(context);
    }

    public HoverEdgeGlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HoverEdgeGlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        hover = new HoverHandler(this);
        hover.setOnHoverMoveListener(this);
        leftEdge = new EdgeEffect(context);
        rightEdge = new EdgeEffect(context);
        topEdge = new EdgeEffect(context);
        bottomEdge = new EdgeEffect(context);
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
            leftEdge.onRelease();
            rightEdge.onRelease();
            topEdge.onRelease();
            bottomEdge.onRelease();
            postInvalidateOnAnimation();
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
        if (deltaX < 0)
            leftEdge.onPull(- deltaX * (1 - x) / factor);
        else
            rightEdge.onPull(deltaX * x / factor);
        if (deltaY < 0)
            topEdge.onPull(- deltaY * (1 - y) / factor);
        else
            bottomEdge.onPull(deltaY * y / factor);
        lastX = x;
        lastY = y;
        if (deltaX != 0 || deltaY != 0) {
            // edge effects activated
            postInvalidateOnAnimation();
        }
    }
    
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        boolean needsInvalidate = false;
        final int height = getHeight() - getPaddingTop() - getPaddingBottom();
        final int width = getWidth() - getPaddingLeft() - getPaddingRight();
        if (!topEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(-getPaddingLeft(), -getPaddingTop());
            topEdge.setSize(width, height);
            needsInvalidate |= topEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (!rightEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(90);
            canvas.translate(-getPaddingTop(), -width + getPaddingLeft());
            rightEdge.setSize(height, width);
            needsInvalidate |= rightEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (!bottomEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(180);
            canvas.translate(-width + getPaddingLeft(), -height + getPaddingTop());
            bottomEdge.setSize(width, height);
            needsInvalidate |= bottomEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (!leftEdge.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(270);
            canvas.translate(-height + getPaddingTop(), -getPaddingLeft());
            leftEdge.setSize(height, width);
            needsInvalidate |= leftEdge.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        if (needsInvalidate) {
            // Keep animating
            postInvalidateOnAnimation();
        }
    }
}

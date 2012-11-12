package hk.hku.cs.srli.supermonkey;

import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.View;

public class CalibrationView extends View implements ValueAnimator.AnimatorUpdateListener {
    
    private CircleWrapper circle;
    
    private AnimatorListener listener;

    public CalibrationView(Context context) {
        super(context);
        circle = new CircleWrapper();
        circle.setX(0);
        circle.setY(0);
        circle.setR(CircleWrapper.DEFAULT_R);
        listener = null;
    }
    
    public void setListener(AnimatorListener listener) {
        this.listener = listener;
    }
    
    public void movePointTo(float x, float y) {
        // Convert from relative position to absolute position on screen.
        float destx = x * getWidth();
        float desty = y * getHeight();
        Log.v("CalibrationView", "movePointTo: " + destx + ", " + desty);
        animatedMoveTo(destx, desty, 2000);
    }
    
    public void focusAtPoint() {
        animatedShrinkExpand(1000);
    }
    
    private void animatedMoveTo(float x, float y, long duration) {
        ObjectAnimator oax = ObjectAnimator.ofFloat(circle, "x", circle.x, x);
        ObjectAnimator oay = ObjectAnimator.ofFloat(circle, "y", circle.y, y);
        oax.addUpdateListener(this);
        AnimatorSet aset = new AnimatorSet();
        aset.playTogether(oax, oay);
        aset.setDuration(duration);
        if (listener != null) aset.addListener(listener);
        aset.start();
    }
    
    private void animatedShrinkExpand(long duration) {
        ObjectAnimator oars = ObjectAnimator.ofFloat(circle, "r", circle.r, 1);
        ObjectAnimator oare = ObjectAnimator.ofFloat(circle, "r", 1, circle.r);
        oars.addUpdateListener(this);
        oare.addUpdateListener(this);
        AnimatorSet aset = new AnimatorSet();
        aset.playSequentially(oars, oare);
        aset.setDuration(duration);
        if (listener != null) aset.addListener(listener);
        aset.start();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        circle.draw(canvas);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.v("CalibrationView", "onMeasure: " + width + ", " + height);
        setMeasuredDimension(width, height);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }
    
    private class CircleWrapper {
        
        public static final float DEFAULT_R = 20;
        
        private ShapeDrawable drawable;
        
        private float r, x, y;
        
        public CircleWrapper() {
            this.r = DEFAULT_R;
            this.x = 0;
            this.y = 0;

            drawable = new ShapeDrawable(new OvalShape());
            drawable.getPaint().setColor(0xff74AC23);
        }
        
        public void setX(float x) {
            this.x = x;
        }
        
        public void setY(float y) {
            this.y = y;
        }
        
        public void setR(float r) {
            this.r = r;
        }
        
        public void draw(Canvas canvas) {
            updateDrawable();
            drawable.draw(canvas);
        }
        
        private void updateDrawable() {
            int left = Math.round(x - r);
            int top = Math.round(y - r);
            int right = Math.round(x + r);
            int bottom = Math.round(y + r);
            drawable.setBounds(new Rect(left, top, right, bottom));
        }
    }
}
package hk.hku.cs.srli.supermonkey;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
        startAnimation(animatedMoveTo(destx, desty), 2000);
    }
    
    public void shrinkPoint() {
        Log.v("CalibrationView", "shrinkPoint");
        startAnimation(animatedShrinkExpand(true), 1000);
    }
    
    public void expandPoint() {
        Log.v("CalibrationView", "expandPoint");
        startAnimation(animatedShrinkExpand(false), 1000);
    }
    
    private Animator animatedMoveTo(float x, float y) {
        ObjectAnimator oax = ObjectAnimator.ofFloat(circle, "x", circle.x, x);
        ObjectAnimator oay = ObjectAnimator.ofFloat(circle, "y", circle.y, y);
        oax.addUpdateListener(this);
        AnimatorSet aset = new AnimatorSet();
        aset.playTogether(oax, oay);
        return aset;
    }
    
    private Animator animatedShrinkExpand(boolean shrink) {
        ObjectAnimator oar = null;
        if (shrink)
            oar = ObjectAnimator.ofFloat(circle, "r", circle.r, 1);
        else
            oar = ObjectAnimator.ofFloat(circle, "r", circle.r, CircleWrapper.DEFAULT_R);
        oar.addUpdateListener(this);
        return oar;
    }
    
    private void startAnimation(Animator ani, long duration) {
        ani.setDuration(duration);
        if (listener != null) ani.addListener(listener);
        ani.start();
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
        private Paint bgPaint;
        
        private float r, x, y;
        
        public CircleWrapper() {
            this.r = DEFAULT_R;
            this.x = 0;
            this.y = 0;

            drawable = new ShapeDrawable(new OvalShape());
            drawable.getPaint().setColor(0xff74AC23);
            
            bgPaint = new Paint();
            bgPaint.setColor(Color.BLACK);
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
            canvas.drawPoint(x, y, bgPaint);
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
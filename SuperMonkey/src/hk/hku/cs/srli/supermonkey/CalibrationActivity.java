package hk.hku.cs.srli.supermonkey;

import android.os.Bundle;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;

import java.util.ArrayDeque;
import java.util.Deque;

public class CalibrationActivity extends Activity {

    private CalibrationView cview;
    private Choreographer choreographer;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cview = new CalibrationView(this);
        setContentView(cview);
        
        choreographer = new Choreographer();
        cview.setListener(choreographer);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Start animation.
            choreographer.dance();
        }
    }
    
    private void onCalibrationFinished() {
        Toast.makeText(this, "Calibration finished", Toast.LENGTH_LONG).show();
    }
    
    private class Choreographer extends AnimatorListenerAdapter {
        
        private Deque<Movement> sequence;
        
        public Choreographer() {
            sequence = new ArrayDeque<Movement>();
            addPoint(0.1f, 0.1f);
            addPoint(0.9f, 0.1f);
            addPoint(0.5f, 0.5f);
            addPoint(0.1f, 0.9f);
            addPoint(0.9f, 0.9f);
        }
        
        public void dance() {
            nextMove();
        }
        
        @Override
        public void onAnimationEnd(Animator animation) {
            nextMove();
        }
        
        private void nextMove() {
            if (sequence.peek() != null)
                sequence.poll().move();
            else
                onCalibrationFinished();
        }
        
        private void addPoint(final float x, final float y) {
            sequence.add(new Movement() {
                @Override
                public void move() {
                    cview.movePointTo(x, y);
                }
            });
            sequence.add(new Movement() {
                @Override
                public void move() {
                    cview.shrinkPoint();
                }
            });
            sequence.add(new Movement() {
                @Override
                public void move() {
                    cview.expandPoint();
                }
            });
        }
        
    }
    
    private interface Movement {
        public void move();
    }
}

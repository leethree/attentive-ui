package hk.hku.cs.srli.supermonkey;

import android.os.Bundle;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

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
        
        cview.setListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                choreographer.dance();
            }
        });
        
        choreographer = new Choreographer();
        new AlertDialog.Builder(this)
                .setTitle("Calibration")
                .setMessage("Please follow the green dot until calibration is finished.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Start animation.
                choreographer.dance();
            }
        }).show();
    }
    
    private void onCalibrationFinished() {
        Toast.makeText(this, "Calibration finished", Toast.LENGTH_LONG).show();
    }
    
    private class Choreographer {
        
        private Deque<Movement> sequence;
        
        public Choreographer() {
            sequence = new ArrayDeque<Movement>();
            addPoint(0.1f, 0.1f);
            addPoint(0.9f, 0.1f);
            addPoint(0.5f, 0.5f);
            addPoint(0.9f, 0.9f);
            addPoint(0.1f, 0.9f);
        }
        
        public void dance() {
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

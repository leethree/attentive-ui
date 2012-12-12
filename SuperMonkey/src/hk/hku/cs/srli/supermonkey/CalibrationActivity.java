package hk.hku.cs.srli.supermonkey;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import hk.hku.cs.srli.supermonkey.service.CalibrationService;
import hk.hku.cs.srli.supermonkey.service.EyeTrackerService;

import java.util.ArrayDeque;
import java.util.Deque;

public class CalibrationActivity extends Activity {
    
    private CalibrationView cview;
    private ProgressDialog progressDialog;
    
    private Choreographer choreographer;
    private CalibrationService calibService;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("CalibrationActivity", "onCreate");
        cview = new CalibrationView(this);
        
        // Create a progress dialog.
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        
        choreographer = new Choreographer();
        cview.setListener(choreographer);
        
        setContentView(cview);
        
        calibService = new CalibrationService(this, 
                new EyeTrackerCallback(), new CalibrationCallback());
        
        // Show loading indicator.
        progressDialog.setMessage("Starting...");
        progressDialog.show();
    }
    
    @Override
    protected void onStart() {
        Log.v("CalibrationActivity", "onStart");
        calibService.bind();
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        Log.v("CalibrationActivity", "onStop");
        calibService.abortCalibration();
        calibService.unbind();
        super.onStop();
    }
    
    private void onDanceFinished() {
        progressDialog.setMessage("Computing...");
        progressDialog.show();
        calibService.computeCalibration();
    }
    
    private void showInfoDialog() {
        // Create a information dialog.
        new AlertDialog.Builder(CalibrationActivity.this)
                .setTitle("Calibration started")
                .setMessage("Please follow the green dot until calibration is finished.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Start animation.
                        choreographer.startDance();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Cancel and stop calibration.
                        finish();
                    }
                }).show();
    }
    
    private class EyeTrackerCallback implements EyeTrackerService.Callback {

        @Override
        public void onServiceBound() {
            calibService.startCalibration();
        }

        @Override
        public void handleDConnect(boolean connnected) {}
        @Override
        public void handleETStatus(boolean ready) {}
        @Override
        public void handleETStartStop(boolean started) {}
        @Override
        public void handleMessage(String message) {}
        @Override
        public void handleError(String message) {}
    }
    
    private class CalibrationCallback implements CalibrationService.Callback {

        @Override
        public void handleStarted() {
            progressDialog.dismiss();
            showInfoDialog();
        }

        @Override
        public void handleAdded() {
            choreographer.nextMove();
        }

        @Override
        public void handleDone() {
            progressDialog.dismiss();
            Toast.makeText(CalibrationActivity.this, 
                           "Calibration finished", Toast.LENGTH_LONG).show();
        }

        @Override
        public void handleStopped() {
            finish();
        }

        @Override
        public void handleError(String message) {
            Toast.makeText(CalibrationActivity.this, 
                           "Error: " + message, Toast.LENGTH_LONG).show();
        }
    }
    
    private class Choreographer extends AnimatorListenerAdapter{
        
        private Deque<Movement> sequence;
        
        public Choreographer() {
            sequence = new ArrayDeque<Movement>();
        }
        
        public void startDance() {
            prepareSequence();
            nextMove();
        }
        
        public void nextMove() {
            if (sequence.peek() != null) {
                sequence.poll().move();
            } else {
                // Finish when there's no more movement in the sequence.
                onDanceFinished();
            }
        }
        
        @Override
        public void onAnimationEnd(Animator animation) {
            choreographer.nextMove();
        }
        
        private void prepareSequence() {
            sequence.clear();
            addPoint(0.1f, 0.1f);
            addPoint(0.9f, 0.1f);
            addPoint(0.5f, 0.5f);
            addPoint(0.9f, 0.9f);
            addPoint(0.1f, 0.9f);
            // Hide the point when animation is finished.
            sequence.add(new Movement() {
                @Override
                public void move() {
                    cview.hidePoint();
                }
            });
        }
        
        private void addPoint(final float x, final float y) {
            sequence.add(new Movement() {
                @Override
                public void move() {
                    cview.movePointTo(x, y);
                }
            });
            // Calibrate twice for each point.
            for (int i = 0; i < 2; i++) {
                sequence.add(new Movement() {
                    @Override
                    public void move() {
                        cview.shrinkPoint();
                    }
                });
                sequence.add(new Movement() {
                    @Override
                    public void move() {
                        calibService.addCalibrationPoint(x, y);
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
    }
    
    private interface Movement {
        public void move();
    }
}

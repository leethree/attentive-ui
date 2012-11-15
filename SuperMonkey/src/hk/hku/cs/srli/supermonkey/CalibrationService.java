package hk.hku.cs.srli.supermonkey;

import android.util.Log;

public class CalibrationService extends EyeTrackerService {

    private Callback callback;
    
    public CalibrationService() {
    }
    
    public CalibrationService(EyeTrackerService etservice) {
        super(etservice);
    }
    
    public void startCalibration() {
        send("calib_start");
    }
    
    public void addCalibrationPoint(float x, float y) {
        send("calib_add " + x + " " + y);
    }
    
    public void computeCalibration() {
        send("calib_compute");
    }
    
    public void abortCalibration() {
        send("calib_abort");
    }
    
    public void setCalibrationCallback(Callback callback) {
        this.callback = callback;
    }
    
    @Override
    protected void reportMessage(String command, String opt) {
        Log.v("CalibrationService", command + " " + opt);
        if (command.equals("calib_started")) {
            callback.handleStarted();
        } else if (command.equals("calib_added")) {
            callback.handleAdded();
        } else if (command.equals("calib_done")) {
            callback.handleDone();
        } else if (command.equals("calib_stopped")) {
            callback.handleStopped();
        } else if (command.equals("error")) {
            if (opt.length() > 0) callback.handleError(opt);
        } else
            super.reportMessage(command, opt);
    }
    
    public interface Callback {
        public void handleStarted();
        public void handleAdded();
        public void handleDone();
        public void handleStopped();
        public void handleError(String message);
    }
}

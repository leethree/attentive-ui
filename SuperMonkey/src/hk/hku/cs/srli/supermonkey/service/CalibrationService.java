package hk.hku.cs.srli.supermonkey.service;

import android.content.Context;
import android.util.Log;

public class CalibrationService extends ServiceControllerBase {

    private Callback callback;
    
    public CalibrationService(Context context, Callback callback) {
        super(context, callback);
        this.callback = callback;
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
    
    @Override
    protected void handleCommand(String command, String opt) {
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
        }
    }
    
    public interface Callback extends ServiceControllerBase.Callback {
        public void handleStarted();
        public void handleAdded();
        public void handleDone();
        public void handleStopped();
    }
}

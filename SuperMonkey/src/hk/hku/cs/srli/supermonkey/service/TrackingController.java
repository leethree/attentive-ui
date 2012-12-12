package hk.hku.cs.srli.supermonkey.service;

import android.content.Context;
import android.util.Log;

public class TrackingController extends ServiceControllerBase {
    
    private Callback callback;

    public TrackingController(Context context, Callback callback) {
        super(context, callback);
        this.callback = callback;
    }
    
    public void setParam(String param, String value) {
        send("set " + param + " " + value);
    }
    
    public void switchTracking(boolean on) {
        send(on ? "start" : "stop");
    }
    
    @Override
    protected void handleCommand(String command, String opt) {
        Log.v("TrackingController", command + " " + opt);
        if (command.equals("status")) {
            if (opt.length() > 0) {
                if (opt.equals("disconnected")) {
                    callback.handleETStatus(false);
                } else if (opt.equals("ready")) {
                    callback.handleETStatus(true);
                } else if (opt.equals("tracking")) {
                    callback.handleETStatus(true);
                    callback.handleETStartStop(true);
                }
            }
        } else if (command.equals("tracking_started")) {
            callback.handleETStartStop(true);
        } else if (command.equals("tracking_stopped")) {
            callback.handleETStartStop(false);
        } else if (command.equals("msg")) {
            if (opt.length() > 0) callback.handleMessage(opt);
        } else if (command.equals("error")) {
            if (opt.length() > 0) callback.handleError(opt);
        }
    }
    
    public interface Callback extends ServiceControllerBase.Callback {
        public void handleETStatus(boolean ready);
        public void handleETStartStop(boolean started);
        public void handleMessage(String message);
    }
}

package hk.hku.cs.srli.supermonkey.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;


public class EyeTrackerService {

    private Context context;
    
    private Callback callback;
    private SocketService.SocketBinder service;
    
    public EyeTrackerService(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }
    
    public void bind() {
        // Bind to service
        Intent intent = new Intent(context, SocketService.class);
        context.bindService(intent, svcConn, Context.BIND_AUTO_CREATE);
    }
    
    public void unbind() {
        if (service != null) context.unbindService(svcConn);
    }
    
    public void connect(String host, int port) {
        if (service != null) service.connect(host, port);
    }
    
    public void setParam(String param, String value) {
        send("set " + param + " " + value);
    }
    
    public void switchTracking(boolean on) {
        send(on ? "start" : "stop");
    }
    
    public void close() {
        if (service != null) service.close();
    }
    
    public boolean isConnected() {
        return service != null && service.isConnected();
    }
    
    public interface Callback {
        public void onServiceBound();
        public void handleDConnect(boolean connnected);
        public void handleETStatus(boolean ready);
        public void handleETStartStop(boolean started);
        public void handleMessage(String message);
        public void handleError(String message);
    }
    
    protected void send(String command) {
        if (service != null) service.send(command);
    }
    
    protected void handleCommand(String command, String opt) {
        Log.v("EyeTrackerService", command + " " + opt);
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
        } else if (command.equals("bye")) {
            service.close();
        }
    }
    
    private SocketService.SocketListener socketListener = new SocketService.SocketListener() {
    
        @Override
        public void onConnected() {
            // request for current status.
            send("status");
            callback.handleDConnect(true);
        }
        
        @Override
        public void onIncomingData(String data) {
            int spacePos = data.indexOf(' ');
            if (spacePos > 0) {
                String command = data.substring(0, spacePos);
                String opt = data.substring(spacePos + 1);
                handleCommand(command, opt);
            } else {
                handleCommand(data, "");
            }
        }
        
        public void onDisconnected() {
            callback.handleDConnect(false);
        }
        
        @Override
        public void onError(String message) {
            callback.handleError(message);
        }
    };
    
    private ServiceConnection svcConn=new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.v("EyeTrackerService", "onServiceConnected:" + EyeTrackerService.this);
            service = (SocketService.SocketBinder) binder;
            service.setListener(socketListener);
            callback.onServiceBound();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.v("EyeTrackerService", "onServiceDisconnected:" + EyeTrackerService.this);
            service = null;
        }
    };
}

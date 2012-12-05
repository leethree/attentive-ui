package hk.hku.cs.srli.supermonkey;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class EyeTrackerService {

    private Context context;
    
    private Callback callback;
    private SocketService.SocketBinder client;
    
    public EyeTrackerService(Context context) {
        this.context = context;
        
        // Bind to service
        Intent intent = new Intent(context, SocketService.class);
        context.bindService(intent, svcConn, Context.BIND_AUTO_CREATE);
    }
    
    public void connect(String host, int port) {
        if (client != null)
            client.connect(host, port);
    }
    
    public void switchTracking(boolean on) {
        if (on)
            send("start");
        else
            send("stop");
    }
    
    public void close() {
        if (client != null) {
            client.stop();
        }
    }
    
    public void unbound() {
        if (client != null) {
            client.stop();
            context.unbindService(svcConn);
        }
    }
    
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
    
    protected boolean send(String command) {
        if (client != null)
            return client.send(command);
        else
            return false;
    }
    
    private SocketService.SocketListener socketListener 
        = new SocketService.SocketListener() {
    
        public void report(final SocketService.ReportType type, final String message) {
            if (callback == null) return;
            callback.runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    switch (type) {
                    case MESSAGE:
                        Log.v("EyeTrackerService", message);
                        int spacePos = message.indexOf(' ');
                        if (spacePos > 0) {
                            String command = message.substring(0, spacePos);
                            String opt = message.substring(spacePos + 1);
                            reportMessage(command, opt);
                        } else
                            reportMessage(message);
                        break;
                    case CONNECTED:
                        callback.handleDConnect(true);
                        break;
                    case DISCONNECTED:
                        callback.handleDConnect(false);
                        break;
                    case ERROR:
                        callback.handleError(message);
                        break;
                    }
                }
            });
        }
    };
    
    private void reportMessage(String command) {
        reportMessage(command, "");
    }
    
    protected void reportMessage(String command, String opt) {
        if (command.equals("msg")) {
            if (opt.length() > 0) callback.handleMessage(opt);
        } else if (command.equals("ready")) {
            callback.handleETStatus(true);
        } else if (command.equals("not_connected")) {
            callback.handleETStatus(false);
        } else if (command.equals("tracking_started")) {
            callback.handleETStartStop(true);
        } else if (command.equals("tracking_stopped")) {
            callback.handleETStartStop(false);
        } else if (command.equals("error")) {
            if (opt.length() > 0) callback.handleError(opt);
        } else if (command.equals("bye")) {
            client.stop();
        }
    }
    
    public interface Callback {
        public void runOnUiThread(Runnable action);
        public void onServiceBound();
        public void handleDConnect(boolean connnected);
        public void handleETStatus(boolean ready);
        public void handleETStartStop(boolean started);
        public void handleMessage(String message);
        public void handleError(String message);
    }
    
    private ServiceConnection svcConn=new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.v("EyeTrackerService", "onServiceConnected" + this);
            client = (SocketService.SocketBinder) binder;
            client.setListener(socketListener);
            if (callback != null)
                callback.onServiceBound();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.v("EyeTrackerService", "onServiceDisconnected" + this);
            client = null;
        }
    };
}

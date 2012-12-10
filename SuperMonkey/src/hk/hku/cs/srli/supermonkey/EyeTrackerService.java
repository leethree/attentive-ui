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
    }
    
    public void bind() {
        // Bind to service
        Intent intent = new Intent(context, SocketService.class);
        context.bindService(intent, svcConn, Context.BIND_AUTO_CREATE);
    }
    
    public void unbind() {
        if (client != null) context.unbindService(svcConn);
    }
    
    public void connect(String host, int port) {
        if (client != null) client.connect(host, port);
    }
    
    public void switchTracking(boolean on) {
        send(on ? "start" : "stop");
    }
    
    public void close() {
        if (client != null) client.close();
    }
    
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    public boolean isConnected() {
        return client != null && client.isConnected();
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
        if (client != null) client.send(command);
    }
    
    protected void handleCommand(String command, String opt) {
        Log.v("EyeTrackerService", command + " " + opt);
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
            client.close();
        }
    }
    
    private SocketService.SocketListener socketListener = new SocketService.SocketListener() {
    
        @Override
        public void onConnected() {
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
            client = (SocketService.SocketBinder) binder;
            client.setListener(socketListener);
            if (callback != null) callback.onServiceBound();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.v("EyeTrackerService", "onServiceDisconnected:" + EyeTrackerService.this);
            client = null;
        }
    };
}

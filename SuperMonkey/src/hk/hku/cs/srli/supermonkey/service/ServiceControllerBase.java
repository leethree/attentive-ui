
package hk.hku.cs.srli.supermonkey.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public abstract class ServiceControllerBase {

    private Context context;
    private Callback callback;
    private SocketService.SocketBinder service;

    public ServiceControllerBase(Context context, Callback callback) {
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

    public boolean isConnected() {
        return service != null && service.isConnected();
    }
    
    public void close() {
        if (service != null) service.close();
    }

    public void connect(String host, int port) {
        if (service != null) service.connect(host, port);
    }

    protected void send(String data) {
        if (service != null) service.send(data);
    }

    protected interface Callback {
        public void onServiceBound();
        public void handleDConnect(boolean connected);
        public void handleError(String message);
    }
    
    protected abstract void handleCommand(String command, String opt);

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
    
    private ServiceConnection svcConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder binder) {
                Log.v("ServiceControllerBase", "onServiceConnected:" + ServiceControllerBase.this);
                service = (SocketService.SocketBinder) binder;
                service.setListener(socketListener);
                callback.onServiceBound();
            }
    
            public void onServiceDisconnected(ComponentName className) {
                Log.v("ServiceControllerBase", "onServiceDisconnected:" + ServiceControllerBase.this);
                service = null;
            }
        };
}

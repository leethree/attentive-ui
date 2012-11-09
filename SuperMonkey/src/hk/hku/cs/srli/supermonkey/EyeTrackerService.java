package hk.hku.cs.srli.supermonkey;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class EyeTrackerService {

    private ClientThread client;
    private WeakReference<Activity> activityRef;
    private Callback callback;
    
    public EyeTrackerService(Activity activity) {
        this.activityRef = new WeakReference<Activity>(activity);
    }
    
    public void connect(String host, int port) {
        if (client != null) close();    // Close existing connection.
        client = new ClientThread(host, port);
        new Thread(client).start();
    }
    
    public boolean write(String command) {
        if (client != null)
            return client.send(command);
        else
            return false;
    }
    
    public void close() {
        if (client != null) client.stop();
    }
    
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
    
    private void report(ReportType type) {
        report(type, null);
    }
    
    private void report(final ReportType type, final String message) {
        if (callback == null) return;
        activityRef.get().runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                switch (type) {
                case MESSAGE:
                    Log.i("EyeTrackerService", message);
                    callback.handleMessage(message);
                    break;
                case CONNECTED:
                    callback.handleConnected();
                    break;
                case DISCONNECTED:
                    callback.handleDisconnected();
                    break;
                case ERROR:
                    callback.handleError(message);
                    break;
                }
                
            }
        });
    }
    
    public interface Callback {
        public void handleConnected();
        public void handleDisconnected();
        public void handleMessage(String message);
        public void handleError(String message);
    }
    
    private enum ReportType {
        MESSAGE, CONNECTED, DISCONNECTED, ERROR
    }
    
    private class ClientThread implements Runnable {
        
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        
        private boolean running;
        
        private String host;
        private int port;
        
        public ClientThread(String host, int port) {
            this.host = host;
            this.port = port;
            this.socket = new Socket();
            this.running = false;
        }
        
        @Override
        public void run() {
            running = true;
            Log.v("EyeTrackerService.ClientThread", "Start running");
            try {
                InetAddress dstAddress = InetAddress.getByName(host);
                InetSocketAddress socketAddr = new InetSocketAddress(dstAddress, port);
                socket.connect(socketAddr);
                Log.d("EyeTrackerService.ClientThread", "Connected!");
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                report(ReportType.CONNECTED);
                while (running) {
                    String message = in.readLine();
                    if (message == null) break;     // The socket is disconnected.
                    report(ReportType.MESSAGE, message);
                }
                report(ReportType.DISCONNECTED);
            } catch (IOException e) {
                Log.e("EyeTrackerService.ClientThread.run", e.getMessage());
                report(ReportType.ERROR, e.getMessage());
            } finally {
                try {
                    socket.close();
                    out.close();
                    in.close();
                } catch (NullPointerException e) {
                } catch (IOException e) {
                    Log.e("EyeTrackerService.ClientThread.run", e.getMessage());
                }
                running = false;
                Log.d("EyeTrackerService.ClientThread", "Stopped.");
            }
        }
        
        public boolean send(String command) {
            if (out == null) return false;
            new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... params) {
                    String command = params[0];
                    out.println(command);
                    return null;
                }
            }.execute(command);
            return true;
        }
        
        public void stop() {
            if (!running) return;
            Log.d("EyeTrackerService.ClientThread", "Stopping...");
            if (socket.isConnected()) {
                running = false;
                send("bye");
            } else {
                // Socket is blocked when connecting. We need to close it here.
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e("EyeTrackerService.ClientThread.stop", e.getMessage());
                }
            }
        }
        
        public boolean isConnected() {
            return running && socket.isConnected();
        }
    }
    
}

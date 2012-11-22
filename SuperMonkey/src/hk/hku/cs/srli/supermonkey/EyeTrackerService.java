package hk.hku.cs.srli.supermonkey;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class EyeTrackerService {

    private ClientThread client;
    private Callback callback;
    
    public EyeTrackerService() {
    }
    
    public EyeTrackerService(EyeTrackerService that) {
        this.client = that.client;
        this.callback = that.callback;
    }
    
    public void connect(String host, int port) {
        if (client != null) close();    // Close existing connection.
        client = new ClientThread(host, port);
        new Thread(client).start();
    }
    
    public void switchTracking(boolean on) {
        if (on)
            send("start");
        else
            send("stop");
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
    
    protected boolean send(String command) {
        if (client != null)
            return client.send(command);
        else
            return false;
    }
    
    private void report(ReportType type) {
        report(type, null);
    }
    
    private void report(final ReportType type, final String message) {
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
        public void handleDConnect(boolean connnected);
        public void handleETStatus(boolean ready);
        public void handleETStartStop(boolean started);
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
                    Log.d("EyeTrackerService.ClientThread", "Send command: " + command);
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

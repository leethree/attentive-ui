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
import java.net.Socket;

public class EyeTrackerService {

    private ClientThread client;
    private WeakReference<Activity> parent;
    
    public EyeTrackerService(Activity parent) {
        this.parent = new WeakReference<Activity>(parent);
    }
    
    public void connect(String host, int port) {
        if (client != null) close();
        client = new ClientThread(host, port);
        new Thread(client).start();
    }
    
    public boolean write(String command) {
        return client.send(command);
    }
    
    public void close() {
        if (client != null)
            client.stop();
    }
    
    private void handleMessage(String message) {
        Log.i("EyeTrackerService", message);
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
            running = false;
        }
        
        @Override
        public void run() {
            running = true;
            try {
                InetAddress dstAddress = InetAddress.getByName(host);
                socket = new Socket(dstAddress, port);
                Log.i("EyeTrackerService.ClientThread", "Connected!");
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (running) {
                    final String message = in.readLine();
                    if (message == null) break;     // The socket is disconnected.
                    parent.get().runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            handleMessage(message);
                        }
                    });
                }
            } catch (IOException e) {
                Log.e("EyeTrackerService.ClientThread", e.getLocalizedMessage());
            } finally {
                stop();
                Log.i("EyeTrackerService.ClientThread", "Stopped.");
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
            Log.i("EyeTrackerService.ClientThread", "Stopping.");
            running = false;
            try {
                in.close();
                out.close();
                socket.close();
            } catch (NullPointerException e) {
            } catch (IOException e) {
                Log.e("EyeTrackerService.ClientThread", e.getLocalizedMessage());
            }
        }
    }
    
}

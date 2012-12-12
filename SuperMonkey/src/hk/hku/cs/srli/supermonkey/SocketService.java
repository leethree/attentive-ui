package hk.hku.cs.srli.supermonkey;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * An asynchronous socket client service.
 */
public class SocketService extends Service {

    private Handler handler = new Handler();
    private final IBinder binder = new SocketBinder();
    private SocketListener listener = new SocketListener() {
        // An empty listener used as a placeholder.
        @Override
        public void onConnected() {}
        @Override
        public void onIncomingData(String data) {}
        @Override
        public void onDisconnected() {}
        @Override
        public void onError(String message) {}
    };
    
    private ClientThread client;

    @Override
    public void onDestroy() {
        if (client != null) client.stop();
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    public class SocketBinder extends Binder {
        
        public void setListener(SocketListener listener) {
            // Wrap callback with adapter.
            SocketService.this.listener = new ListenerAdapter(listener);
        }
        
        public void connect(String host, int port) {
            if (client != null) client.stop();    // Close existing connection.
            client = new ClientThread(host, port);
            new Thread(client).start();
        }
        
        public void send(String data) {
            if (client != null) client.send(data);
        }
        
        public boolean isConnected() {
            return client != null && client.isConnected();
        }
        
        public void close() {
            if (client != null) client.stop();
        }
    }
    
    public interface SocketListener {
        public void onConnected();
        public void onIncomingData(String data);
        public void onDisconnected();
        public void onError(String message);
    }
    
    /**
     * Adapter for running listener callback on main thread.
     * Intended to be called by the socket client thread.
     */
    private class ListenerAdapter implements SocketListener{
        
        private SocketListener listener;
        
        public ListenerAdapter(SocketListener listener) {
            this.listener = listener;
        }
        
        public void onConnected() {
            handler.post(new Runnable() {
                @Override
                public void run() {listener.onConnected();}
            });
        }
        public void onIncomingData(final String data) {
            handler.post(new Runnable() {
                @Override
                public void run() {listener.onIncomingData(data);}
            });
        }
        public void onDisconnected() {
            handler.post(new Runnable() {
                @Override
                public void run() {listener.onDisconnected();}
            });
        }
        public void onError(final String message) {
            handler.post(new Runnable() {
                @Override
                public void run() {listener.onError(message);}
            });
        }
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
            Log.v("SocketService.ClientThread", "Start running");
            try {
                InetAddress dstAddress = InetAddress.getByName(host);
                InetSocketAddress socketAddr = new InetSocketAddress(dstAddress, port);
                socket.connect(socketAddr);
                Log.v("SocketService.ClientThread", "Connected!");
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                listener.onConnected();
                while (running) {
                    String message = in.readLine();
                    if (message == null) break;     // The socket is disconnected.
                    listener.onIncomingData(message);
                }
            } catch (IOException e) {
                Log.d("SocketService.ClientThread.run1", e.getMessage());
                // Report error only when the socket is connected.
                if (running) listener.onError(e.getMessage());
            } finally {
                try {
                    socket.close();
                    out.close();
                    in.close();
                } catch (NullPointerException e) {
                } catch (IOException e) {
                    Log.d("SocketService.ClientThread.run2", e.getMessage());
                }
                running = false;
                listener.onDisconnected();
                Log.v("SocketService.ClientThread", "Stopped.");
            }
        }
        
        public void send(String data) {
            if (out == null) return;
            // Send data in another thread because it might block.
            new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... params) {
                    String data = params[0];
                    out.println(data);
                    Log.v("SocketService.ClientThread", "Send data: " + data);
                    return null;
                }
            }.execute(data);
        }
        
        public void stop() {
            if (!running) return;
            Log.v("SocketService.ClientThread", "Stopping...");
            running = false;
            try {
                // Force close the socket from main thread to unblock the client thread.
                socket.close();
            } catch (IOException e) {
                Log.d("SocketService.ClientThread.stop", e.getMessage());
            }
        }
        
        public boolean isConnected() {
            return running && socket.isConnected();
        }
    }
}

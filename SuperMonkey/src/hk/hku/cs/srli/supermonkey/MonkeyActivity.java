package hk.hku.cs.srli.supermonkey;

import com.example.android.apis.graphics.TouchPaint;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MonkeyActivity extends Activity {

    private EditText monkeyStatus;
    private EditText etStatus;
    
    private ToggleButton etToggle;
    private Button caliButton;
    
    private EyeTrackerService etService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monkey);
        monkeyStatus = (EditText) findViewById(R.id.mStatusEditText);
        etStatus = (EditText) findViewById(R.id.etStatusEditText);
        etToggle = (ToggleButton) findViewById(R.id.etToggleButton);
        caliButton = (Button) findViewById(R.id.calibrateButton);

        monkeyStatus.setText("ready");
        etStatus.setText("disconnected");
        etToggle.setEnabled(false);
        caliButton.setEnabled(false);
    }

    @Override
    protected void onStart() {
        Log.v("MonkeyActivity", "onStart");
        super.onStart();
        lazyConnect();
    }
    
    @Override
    protected void onStop() {
        Log.v("MonkeyActivity", "onStop");
        if (etService != null) {
            etService.close();
            etService.setCallback(null);
            etService = null;
        }
        super.onStop();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_monkey, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle settings
        if (item.getItemId() == R.id.menu_settings) {
            updateSettings();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    public void updateSettings() {
        startActivity(new Intent(getBaseContext(), SettingsActivity.class));
    }
    
    public void onTestButtonClicked(View view) {
        // start TouchPaint
        startActivity(new Intent(getBaseContext(), TouchPaint.class));
    }
    
    public void onMToggleClicked(View view) {
        Log.v("MonkeyActivity", "onMToggleClicked");
    }
    
    public void onEtToggleClicked(View view) {
        boolean on = etToggle.isChecked();
        Log.v("MonkeyActivity", "onEtToggleClicked:" + on);
        etService.switchTracking(on);
        etToggle.setChecked(!on);    // Maintain original state.
    }
    
    private void lazyConnect() {
        if (etService == null) {
            etService = new EyeTrackerService(this);
            etService.setCallback(new EyeTrackerCallback());
        }
        if (!etService.isConnected()) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            try {
                String host = sharedPref.getString(SettingsActivity.KEY_PREF_ET_HOST, "");
                int port = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_ET_PORT, ""));
                if (host.length() > 0 && port > 0) {
                    etService.connect(host, port);
                    etStatus.setText("connecting to daemon...");
                }
                else
                    throw new IllegalArgumentException("Wrong host and port format.");
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private class EyeTrackerCallback implements EyeTrackerService.Callback {

        @Override
        public void handleConnected() {
            etToggle.setEnabled(true);
            caliButton.setEnabled(true);
            etStatus.setText("daemon connected");
            Toast.makeText(getApplicationContext(), "Daemon connected", Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void handleMessage(String message) {
            etStatus.setText(message);
        }

        @Override
        public void handleDisconnected() {
            etToggle.setEnabled(false);
            caliButton.setEnabled(false);
            etStatus.setText("daemon disconnected");
            Toast.makeText(getApplicationContext(), "Daemon disconnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void handleError(String message) {
            String text = "Error: " + message;
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void handleETStartStop(boolean started) {
            etToggle.setChecked(started);
            if (started)
                etStatus.setText("tracking...");
            else
                etStatus.setText("tracking stopped");
        }
    }
}

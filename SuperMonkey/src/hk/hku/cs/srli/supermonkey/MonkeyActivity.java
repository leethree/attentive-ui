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
    private EditText dStatus;
    private EditText etStatus;
    
    private ToggleButton dToggle;
    private ToggleButton etToggle;
    private Button caliButton;
    
    private EyeTrackerService etService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monkey);
        monkeyStatus = (EditText) findViewById(R.id.mStatusEditText);
        dStatus = (EditText) findViewById(R.id.dStatusEditText);
        etStatus = (EditText) findViewById(R.id.etStatusEditText);
        dToggle = (ToggleButton) findViewById(R.id.dToggleButton);
        etToggle = (ToggleButton) findViewById(R.id.etToggleButton);
        caliButton = (Button) findViewById(R.id.calibrateButton);

        monkeyStatus.setText("ready");
        dStatus.setText("disconnected");
        etStatus.setText("unknown");
        dToggle.setChecked(false);
        etToggle.setEnabled(false);
        etToggle.setChecked(false);
        // TODO (LeeThree): Commented for dev purpose. 
        // Uncomment this before merged to main branch.
        // caliButton.setEnabled(false);
        
        etService = new EyeTrackerService();
        etService.setCallback(new EyeTrackerCallback());
    }

    @Override
    protected void onStart() {
        Log.v("MonkeyActivity", "onStart");
        super.onStart();
        // Auto connect if needed.
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.KEY_PREF_ET_AUTOCONNECT, false))
            lazyConnect();
    }
    
    @Override
    protected void onStop() {
        Log.v("MonkeyActivity", "onStop");
        if (etService != null)
            etService.close();
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
            startActivity(new Intent(getBaseContext(), SettingsActivity.class));
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }
    
    public void onCalibrateButtonClicked(View view) {
        // start calibration
        startActivity(new Intent(getBaseContext(), CalibrationActivity.class));
    }
    
    public void onTestButtonClicked(View view) {
        // start TouchPaint
        startActivity(new Intent(getBaseContext(), TouchPaint.class));
    }
    
    public void onMToggleClicked(View view) {
        Log.v("MonkeyActivity", "onMToggleClicked");
    }
    
    public void onDToggleClicked(View view) {
        boolean on = dToggle.isChecked();
        Log.v("MonkeyActivity", "onDToggleClicked:" + on);
        if (on)
            lazyConnect();
        else
            etService.close();
        dToggle.setChecked(!on);    // Maintain original state.
    }
    
    public void onEtToggleClicked(View view) {
        boolean on = etToggle.isChecked();
        Log.v("MonkeyActivity", "onEtToggleClicked:" + on);
        etService.switchTracking(on);
        etToggle.setChecked(!on);    // Maintain original state.
    }
    
    private void lazyConnect() {
        if (!etService.isConnected()) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            try {
                String host = sharedPref.getString(SettingsActivity.KEY_PREF_ET_HOST, "");
                int port = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_ET_PORT, ""));
                if (host.length() > 0 && port > 0) {
                    etService.connect(host, port);
                    dStatus.setText("connecting...");
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
        public void handleDConnect(boolean connnected) {
            dToggle.setChecked(connnected);
            etToggle.setEnabled(false);
            caliButton.setEnabled(false);
            if (connnected)
                dStatus.setText("connected");
            else
                dStatus.setText("disconnected");
        }

        @Override
        public void handleETStatus(boolean ready) {
            etToggle.setEnabled(ready);
            etToggle.setChecked(false);
            caliButton.setEnabled(ready);
            if (ready)
                etStatus.setText("ready");
            else
                etStatus.setText("not connected");
        }
        
        @Override
        public void handleETStartStop(boolean started) {
            etToggle.setChecked(started);
            if (started)
                etStatus.setText("tracking...");
            else
                etStatus.setText("tracking stopped");
        }
        
        @Override
        public void handleMessage(String message) {
            etStatus.setText(message);
        }
        
        @Override
        public void handleError(String message) {
            String text = "Error: " + message;
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void runOnUiThread(Runnable action) {
            MonkeyActivity.this.runOnUiThread(action);
        }
    }
}

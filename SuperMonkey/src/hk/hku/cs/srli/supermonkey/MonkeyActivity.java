package hk.hku.cs.srli.supermonkey;

import com.example.android.apis.graphics.TouchPaint;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MonkeyActivity extends Activity {

    private EditText monkeyStatus;
    private EditText dStatus;
    private EditText etStatus;
    
    private ToggleButton monkeyToggle;
    private ToggleButton dToggle;
    private ToggleButton etToggle;
    private Button caliButton;
    
    private TextView infoText;
    
    private EyeTrackerService etService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MonkeyActivity", "onCreate");
        setContentView(R.layout.activity_monkey);
        monkeyStatus = (EditText) findViewById(R.id.mStatusEditText);
        dStatus = (EditText) findViewById(R.id.dStatusEditText);
        etStatus = (EditText) findViewById(R.id.etStatusEditText);
        monkeyToggle = (ToggleButton) findViewById(R.id.mToggleButton);
        dToggle = (ToggleButton) findViewById(R.id.dToggleButton);
        etToggle = (ToggleButton) findViewById(R.id.etToggleButton);
        caliButton = (Button) findViewById(R.id.calibrateButton);
        infoText = (TextView) findViewById(R.id.infoTextView);

        monkeyStatus.setText("not available");
        dStatus.setText("disconnected");
        etStatus.setText("unknown");
        monkeyToggle.setEnabled(false);
        dToggle.setEnabled(false);
        dToggle.setChecked(false);
        etToggle.setEnabled(false);
        etToggle.setChecked(false);
        caliButton.setEnabled(false);
        infoText.setText(getScreenInfo());
        
        etService = new EyeTrackerService(this);
        etService.setCallback(new EyeTrackerCallback());
    }

    @Override
    protected void onStart() {
        Log.v("MonkeyActivity", "onStart");
        etService.bind();
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        Log.v("MonkeyActivity", "onStop");
        etService.unbind();
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }
    
    public void onCalibrateButtonClicked(View view) {
        // start calibration
        startActivity(new Intent(this, CalibrationActivity.class));
    }
    
    public void onTestButtonClicked(View view) {
        // start TouchPaint
        startActivity(new Intent(this, TouchPaint.class));
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
        etStatus.setText(on ? "starting..." : "stopping...");
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
    
    private String getScreenInfo() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        double xinch = dm.widthPixels / dm.xdpi;
        double yinch = dm.heightPixels / dm.ydpi;
        String s = "Screen resolution: " + dm.widthPixels + " x " + dm.heightPixels + " px\n";
        s += "Screen size: " + String.format("%.2f", xinch) + " x "
                + String.format("%.2f", yinch) + " inch\n";
        s += "(" + String.format("%.2f", xinch * 2.54) + " x " 
                + String.format("%.2f", yinch * 2.54) + " cm)\n";
        return s;
    }
    
    private class EyeTrackerCallback implements EyeTrackerService.Callback {

        @Override
        public void handleDConnect(boolean connnected) {
            dToggle.setChecked(connnected);
            etToggle.setEnabled(false);
            caliButton.setEnabled(false);
            dStatus.setText(connnected ? "connected" : "disconnected");
        }

        @Override
        public void handleETStatus(boolean ready) {
            etToggle.setEnabled(ready);
            etToggle.setChecked(false);
            caliButton.setEnabled(ready);
            etStatus.setText(ready ? "ready" : "not connected");
        }
        
        @Override
        public void handleETStartStop(boolean started) {
            etToggle.setChecked(started);
            caliButton.setEnabled(!started);
            etStatus.setText(started ? "tracking..." : "tracking stopped");
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
        public void onServiceBound() {
            dToggle.setEnabled(true);
            // Auto connect if needed.
            if (PreferenceManager.getDefaultSharedPreferences(MonkeyActivity.this)
                    .getBoolean(SettingsActivity.KEY_PREF_ET_AUTOCONNECT, false))
                lazyConnect();
        }
    }
}

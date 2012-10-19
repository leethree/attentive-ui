package hk.hku.cs.srli.supermonkey;

import com.example.android.apis.graphics.TouchPaint;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MonkeyActivity extends Activity {

    private EditText monkeyStatus;
    private EditText etStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monkey);
        monkeyStatus = (EditText) findViewById(R.id.mStatusEditText);
        etStatus = (EditText) findViewById(R.id.etStatusEditText);

        monkeyStatus.setText("unknown");
        etStatus.setText("unknown");
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

    public void startTouchPaint() {
        startActivity(new Intent(getBaseContext(), TouchPaint.class));
    }

    public void updateSettings() {
        startActivity(new Intent(getBaseContext(), SettingsActivity.class));
    }
}

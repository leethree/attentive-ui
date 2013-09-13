
package hk.hku.cs.srli.factfinder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class TestActivity extends Activity
        implements View.OnClickListener, NumberPicker.OnValueChangeListener, OnCheckedChangeListener {

    
    private static int sDataset = 0;
    private static boolean sHover = false;
    
    private NumberPicker npd;
    private Switch sh;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
        npd = (NumberPicker) findViewById(R.id.number_picker_d);
        npd.setMinValue(0);
        npd.setMaxValue(FFApp.TESTS.size() - 1);
        npd.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npd.setOnValueChangedListener(this);
        
        sh = (Switch) findViewById(R.id.switch_h);
        sh.setOnCheckedChangeListener(this);
        
        Button start = (Button) findViewById(R.id.start_button);
        start.setOnClickListener(this);
        
        // restore data
        SharedPreferences settings = FFApp.getPreferences(this);
        sDataset = settings.getInt("ndataset", sDataset);
        sHover = settings.getBoolean("bhover", sHover);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        npd.setValue(sDataset);
        sh.setChecked(sHover);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        // save data
        SharedPreferences settings = FFApp.getPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("ndataset", sDataset);
        editor.putBoolean("bhover", sHover);
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        // change configuration
        FFApp.getApp(this).changeConfig(sDataset, sHover);
        Intent i = new Intent(this, MainActivity.class);
        // clear activity stack
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // start test
        startActivity(i);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        sDataset = npd.getValue();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        sHover = sh.isChecked();
    }

}

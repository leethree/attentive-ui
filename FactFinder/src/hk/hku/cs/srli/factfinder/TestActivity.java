
package hk.hku.cs.srli.factfinder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends Activity
        implements View.OnClickListener, NumberPicker.OnValueChangeListener, OnCheckedChangeListener {

    private static final int REQ_TEST = 25534;
    private static final int APP_THEME = R.style.AppTheme;
    private static final int APP_THEME_NO_HOVER = R.style.AppTheme_NoHover;
    private static final List<Integer> TESTS = new ArrayList<Integer>(4);
    //private static final int PRACTICE = R.xml.duck; // practice data set
    
    static {
        TESTS.add(R.xml.duck);
        TESTS.add(R.xml.burger);
        TESTS.add(R.xml.cheesecake);
        TESTS.add(R.xml.coffee);
        TESTS.add(R.xml.hotpot);
    }
    
    //private static int sParticipant = 1;
    //private static int sTrial = 0;
    private static int sDataset = 0;
    private static int sHover = 0;
    
    private static long timer;
    
    private NumberPicker npd;
    private Switch sh;
    private TextView status;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
        npd = (NumberPicker) findViewById(R.id.number_picker_d);
        npd.setMinValue(0);
        npd.setMaxValue(TESTS.size() - 1);
        npd.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npd.setOnValueChangedListener(this);
        
        sh = (Switch) findViewById(R.id.switch_h);
        sh.setOnCheckedChangeListener(this);
        
        Button start = (Button) findViewById(R.id.start_button);
        start.setOnClickListener(this);
        
        status = (TextView) findViewById(R.id.text_status);
        
        // restore data
        SharedPreferences settings = getSharedPreferences("FF_Test", 0);
        sDataset = settings.getInt("ndataset", sDataset);
        sHover = settings.getInt("nhover", sHover);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        npd.setValue(sDataset);
        sh.setChecked(sHover != 0);
        prepareTest();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        // save data
        SharedPreferences settings = getSharedPreferences("FF_Test", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("ndataset", sDataset);
        editor.putInt("nhover", sHover);
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        FFApp.log("Test", "Trial starting: D" + sDataset + " H" + sHover +
                " with config: "+ status.getText());
        Intent i = new Intent(this, MainActivity.class);
        // clear activity stack
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // start test
        startActivityForResult(i, REQ_TEST);
        timer = System.currentTimeMillis();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        sDataset = npd.getValue();
        prepareTest();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        sHover = sh.isChecked() ? 1 : 0;
        prepareTest();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        long duration = System.currentTimeMillis() - timer;
        // If the test completed and the request matches
        if (requestCode == REQ_TEST) {
            FFApp.log("Test", "Trial duration: " + duration * 0.001 + " s.");
            if (resultCode == Activity.RESULT_OK) {
                sHover = 1 - sHover;  // next trial
                FFApp.log("Test", "Trial ended OK.");
            } else {
                // trial not ended successfully
                FFApp.log("Test", "Trial cancelled.");
            }
        }
        
    }
    
    private void prepareTest() {
        // test sessions
        int data = sDataset;
        boolean hover = sHover != 0;
        FFApp.getApp(this).changeDataSet(TESTS.get(data));
        FFApp.getApp(this).setFFTheme(hover ? APP_THEME : APP_THEME_NO_HOVER);
        status.setText("d" + data + " h" + (hover ? 1 : 0));
        FFApp.sLogPrefix = "D" + sDataset + " H" + sHover;
    }
}

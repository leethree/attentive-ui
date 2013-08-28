
package hk.hku.cs.srli.factfinder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends Activity
        implements View.OnClickListener, NumberPicker.OnValueChangeListener {

    private static final int REQ_TEST = 25534;
    private static final int APP_THEME = R.style.AppTheme;
    private static final int APP_THEME_NO_HOVER = R.style.AppTheme_NoHover;
    private static final List<Integer> TESTS = new ArrayList<Integer>(4);
    private static final int PRACTICE = R.xml.duck; // practice data set
    
    static {
        TESTS.add(R.xml.burger);
        TESTS.add(R.xml.cheesecake);
        TESTS.add(R.xml.coffee);
        TESTS.add(R.xml.hotpot);
    }
    
    private static int sParticipant = 1;
    private static int sTrial = 0;
    
    private static long timer;
    
    private NumberPicker npp;
    private NumberPicker npt;
    private TextView status;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
        npp = (NumberPicker) findViewById(R.id.number_picker_p);
        npp.setMinValue(1);
        npp.setMaxValue(20);
        npp.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npp.setOnValueChangedListener(this);
        
        npt = (NumberPicker) findViewById(R.id.number_picker_t);
        npt.setMinValue(0);
        npt.setMaxValue(TESTS.size() * 2);
        npt.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npt.setOnValueChangedListener(this);
        
        Button start = (Button) findViewById(R.id.start_button);
        start.setOnClickListener(this);
        
        status = (TextView) findViewById(R.id.text_status);
        
        // restore data
        SharedPreferences settings = getSharedPreferences("FF_Test", 0);
        sParticipant = settings.getInt("nparticipant", sParticipant);
        sTrial = settings.getInt("ntrial", sTrial);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        npp.setValue(sParticipant);
        npt.setValue(sTrial);
        prepareTest();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        // save data
        SharedPreferences settings = getSharedPreferences("FF_Test", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("nparticipant", sParticipant);
        editor.putInt("ntrial", sTrial);
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        FFApp.log("Test", "Trial starting: P" + sParticipant + " T" + sTrial +
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
        sParticipant = npp.getValue();
        sTrial = npt.getValue();
        prepareTest();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        long duration = System.currentTimeMillis() - timer;
        // If the test completed and the request matches
        if (requestCode == REQ_TEST) {
            FFApp.log("Test", "Trial duration: " + duration * 0.001 + " s.");
            if (resultCode == Activity.RESULT_OK) {
                sTrial = sTrial % (TESTS.size() * 2) + 1;  // next trial
                FFApp.log("Test", "Trial ended OK.");
            } else {
                // trial not ended successfully
                FFApp.log("Test", "Trial cancelled.");
            }
        }
        
    }
    
    private void prepareTest() {
        if (sTrial <= 0) {
            // practice session
            FFApp.getApp(this).changeDataSet(PRACTICE);
            boolean hover = (sParticipant - 1) % 2 == 0;
            FFApp.getApp(this).setFFTheme(hover ? APP_THEME : APP_THEME_NO_HOVER);
            status.setText("practice h" + (hover ? 1 : 0));
            FFApp.sLogPrefix = "P" + sParticipant + " Practice";
        } else {
            // test sessions
            int data = (sTrial - 1) % TESTS.size();
            boolean hover = (sParticipant - 1) % 4 > 1 ^ ((sTrial - 1) / TESTS.size()) % 2 == 0;
            FFApp.getApp(this).changeDataSet(TESTS.get(data));
            FFApp.getApp(this).setFFTheme(hover ? APP_THEME : APP_THEME_NO_HOVER);
            status.setText("d" + data + " h" + (hover ? 1 : 0));
            FFApp.sLogPrefix = "P" + sParticipant + " T" + sTrial;
        }
    }
}

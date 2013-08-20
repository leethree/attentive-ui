
package hk.hku.cs.srli.factfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends Activity {

    private static final int REQ_TEST = 25534;
    private static int sStep = 0;
    private static final List<Integer> tests = new ArrayList<Integer>(4);
    
    static {
        tests.add(R.xml.burger);
        tests.add(R.xml.cheesecake);
        tests.add(R.xml.coffee);
        tests.add(R.xml.hotpot);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
        Button start = (Button) findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                prepareTest();
                startTest();
            }
        });
    }

    public void startTest() {
        Intent i = new Intent(this, MainActivity.class);
        // clear activity stack
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // launch settings
        startActivityForResult(i, REQ_TEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("onActivityResult", "req: " + requestCode);
        Log.v("onActivityResult", "result: " + resultCode);
        // If the test completed and the request matches
        if (resultCode == Activity.RESULT_OK && requestCode == REQ_TEST) {
            sStep++;
        }
    }
    
    private void prepareTest() {
        FFApp.getApp(this).changeDataSet(tests.get(sStep % tests.size()));
    }
}

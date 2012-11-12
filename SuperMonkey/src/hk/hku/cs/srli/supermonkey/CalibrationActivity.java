package hk.hku.cs.srli.supermonkey;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

public class CalibrationActivity extends Activity {

    private CalibrationView cview;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cview = new CalibrationView(this);
        setContentView(cview);
    }

    public class CalibrationView extends View {
        
        private ShapeDrawable circle;

        public CalibrationView(Context context) {
            super(context);
            
            circle = new ShapeDrawable(new OvalShape());
            circle.getPaint().setColor(0xff74AC23);
            circle.setBounds(100, 100, 150, 150);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            circle.draw(canvas);
        }
    }
}

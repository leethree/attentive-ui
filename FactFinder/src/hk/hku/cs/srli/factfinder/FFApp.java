package hk.hku.cs.srli.factfinder;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class FFApp extends Application {

    private DataSet mData;
    private Order mOrder;
    private int mTheme = R.style.AppTheme;
    
    public FFApp() {
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        // initialize dataset
        mData = new DataSet(this, R.xml.empty);
        mOrder = new Order(this);
        mTheme = getApplicationInfo().theme;
    }
    
    public void changeDataSet(int dataSource) {
        mOrder.reset();
        mData = new DataSet(this, dataSource);
    }
    
    public int getFFTheme() {
        return mTheme;
    }
    
    public void setFFTheme(int resid) {
        mTheme = resid;
    }
    
    // Utility method to get a instance.
    public static FFApp getApp(Context context) {
        return (FFApp) context.getApplicationContext();
    }
    
    public static DataSet getData(Context context) {
        return getApp(context).mData;
    }
    
    public static Order getOrder(Context context) {
        return getApp(context).mOrder;
    }
    
    public static void Log(String tag, String msg) {
        Log.i("FFApp.Log", "[" + tag + "] " + msg);
    }
}

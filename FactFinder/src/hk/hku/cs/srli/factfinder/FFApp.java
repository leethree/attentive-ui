package hk.hku.cs.srli.factfinder;

import android.app.Application;
import android.content.Context;

public class FFApp extends Application {

    private DataSet mData;
    private Order mOrder;
    
    public FFApp() {
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        // initialize dataset
        mData = new DataSet(this);
        mOrder = new Order(this);
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
}

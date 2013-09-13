package hk.hku.cs.srli.factfinder;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class FFApp extends Application {

    public static final String FF_PREF = "FF_Pref";

    public static final int APP_THEME = R.style.AppTheme;
    public static final int APP_THEME_NO_HOVER = R.style.AppTheme_NoHover;
    public static final List<Integer> TESTS = new ArrayList<Integer>(5);
    
    static {
        TESTS.add(R.xml.duck);
        TESTS.add(R.xml.burger);
        TESTS.add(R.xml.cheesecake);
        TESTS.add(R.xml.coffee);
        TESTS.add(R.xml.hotpot);
    }
    
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
        
        // load initial config from preferences
        SharedPreferences pref = getSharedPreferences(FF_PREF, 0);
        changeConfig(pref.getInt("ndataset", 0), pref.getBoolean("bhover", false));
    }
    
    public int getFFTheme() {
        return mTheme;
    }
    
    public void changeConfig(int dataset, boolean hover) {
        mOrder.reset();
        mData = new DataSet(this, TESTS.get(dataset));
        mTheme = hover ? APP_THEME : APP_THEME_NO_HOVER;
    }
    
    // Utility method to get a instance.
    public static FFApp getApp(Context context) {
        return (FFApp) context.getApplicationContext();
    }
    
    public static SharedPreferences getPreferences(Context context) {
        return getApp(context).getSharedPreferences(FF_PREF, 0);
    }
    
    public static DataSet getData(Context context) {
        return getApp(context).mData;
    }
    
    public static Order getOrder(Context context) {
        return getApp(context).mOrder;
    }
}


package hk.hku.cs.srli.factfinder;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DummyData {

    public static enum Category {
        PLACE(0), PERSON(1), PLANT(2);
        public final int id;
        private static final SparseArray<Category> map;
        static {
            map = new SparseArray<Category>(3);
            for(Category s : Category.values()) map.append(s.id, s);
        }
        private Category(int id) {
            this.id = id;
        }
        public static Category of(int i) {
            return map.get(i);
        }
    }
    
    public static class FactItem {
        public int id;
        public Category category;
        public String thumb;
        public String title;
        public String content;
    }
    
    private final Map<Category, SparseArray<FactItem>> mCatMap;
    private final Order mOrder;
    
    // singleton instance
    private static DummyData instance;
    
    private DummyData(Context context) {
        mCatMap = new HashMap<DummyData.Category, SparseArray<FactItem>>(3);
        mOrder = new Order(context);
        
        for(Category s : Category.values()) {
            mCatMap.put(s, new SparseArray<DummyData.FactItem>());
        }
        
        XmlPullParser parser = context.getResources().getXml(R.xml.data);
        
        try {
            parseData(parser);
        } catch (XmlPullParserException e) {
            Log.e("DummyData", "XML Error", e);
        } catch (IOException e) {
            Log.e("DummyData", "XML Error", e);
        }
    }
    
    public static DummyData getInstance(Context context) {
        if (instance == null) {
            instance = new DummyData(context);
        }
        return instance;
    }
    
    public SparseArray<FactItem> getCatData(Category cat) {
        return mCatMap.get(cat);
    }
    
    public FactItem getItem(Category cat, int id) {
        return mCatMap.get(cat).get(id);
    }
    
    public Order getOrder() {
        return mOrder;
    }
    
    private void parseData(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        FactItem item = null;
        String tag = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
             if(eventType == XmlPullParser.START_DOCUMENT) {
             } else if(eventType == XmlPullParser.START_TAG) {
                 tag = parser.getName();
                 if (tag.equals("item")) {
                     item = new FactItem();
                 }
             } else if(eventType == XmlPullParser.END_TAG) {
                 tag = parser.getName();
                 if (tag.equals("item")) {
                     // add item to category.
                     getCatData(item.category).append(item.id, item);
                     item = null;
                 }
                 tag = null;
             } else if(eventType == XmlPullParser.TEXT) {
                 String text = parser.getText();
                 if (item != null && tag != null) {
                     if (tag.equals("id")) {
                         item.id = Integer.parseInt(text);
                     } else if (tag.equals("category")) {
                         item.category = Category.of(Integer.parseInt(text));
                     } else if (tag.equals("title")) {
                         item.title = text;
                     } else if (tag.equals("content")) {
                         item.content = text;
                     } else if (tag.equals("thumb")) {
                         item.thumb = text;
                     }
                 }
             }
             eventType = parser.next();
        }
    }

}

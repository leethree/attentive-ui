
package hk.hku.cs.srli.factfinder;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class DummyData {

    public static class Category {
        private final int id;
        private String name;
        private SparseArray<FactItem> items;
        
        public Category(int id, String name) {
            this.id = id;
            this.name = name;
            items = new SparseArray<DummyData.FactItem>();
        }
        
        public String getName() {
            return name;
        }
        
        public SparseArray<FactItem> getItems() {
            return items;
        }
        
        @Override
        public String toString() {
            return "" + id + ": " + name;
        }
    }
    
    public static class FactItem {
        public int id;
        public int category;
        public String thumb;
        public String title;
        public String content;
    }
    
    private final SparseArray<Category> mCatMap;
    private final Order mOrder;
    
    // singleton instance
    private static DummyData instance;
    
    private DummyData(Context context) {
        mCatMap = new SparseArray<Category>();
        mOrder = new Order(context);

        XmlPullParser parser = context.getResources().getXml(R.xml.cheesecake);
        
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
    
    public int getNumberOfCategories() {
        return mCatMap.size();
    }
    
    public Category getCategoryAt(int index) {
        return mCatMap.valueAt(index);
    }
    
    public FactItem getItem(int index, int id) {
        return getCategoryAt(index).items.get(id);
    }
    
    public Order getOrder() {
        return mOrder;
    }
    
    private void parseData(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        FactItem item = null;
        String tag = null;
        boolean parsingCategory = false;
        int catId = 0;
        while (eventType != XmlPullParser.END_DOCUMENT) {
             if(eventType == XmlPullParser.START_DOCUMENT) {
             } else if(eventType == XmlPullParser.START_TAG) {
                 tag = parser.getName();
                 if (tag.equals("item")) {
                     item = new FactItem();
                 } else if (tag.equals("category-name")) {
                     parsingCategory = true; 
                 }
             } else if(eventType == XmlPullParser.END_TAG) {
                 tag = parser.getName();
                 if (tag.equals("item")) {
                     // add item to category.
                     mCatMap.get(item.category).items.append(item.id, item);
                     item = null;
                 } else if (tag.equals("category-name")) {
                     parsingCategory = false; 
                 }
                 tag = null;
             } else if(eventType == XmlPullParser.TEXT) {
                 String text = parser.getText();
                 if (parsingCategory) {
                     if (tag.equals("id")) {
                         catId = Integer.parseInt(text);
                     } else if (tag.equals("name")) {
                         mCatMap.put(catId, new Category(catId, text));
                     }
                 } else if (item != null && tag != null) {
                     if (tag.equals("id")) {
                         item.id = Integer.parseInt(text);
                     } else if (tag.equals("category")) {
                         item.category = Integer.parseInt(text);
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

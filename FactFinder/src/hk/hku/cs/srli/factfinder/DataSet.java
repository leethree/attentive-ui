
package hk.hku.cs.srli.factfinder;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class DataSet {

    public static class Category {
        private final int id;
        private String name;
        private SparseArray<DataItem> items;
        
        public Category(int id, String name) {
            this.id = id;
            this.name = name;
            items = new SparseArray<DataSet.DataItem>();
        }
        
        public String getName() {
            return name;
        }
        
        public SparseArray<DataItem> getItems() {
            return items;
        }
        
        @Override
        public String toString() {
            return "" + id + ": " + name;
        }
    }
    
    public static class DataItem {
        public int id;
        public int category;
        public String thumb;
        public String title;
        public String content;
    }
    
    private final SparseArray<Category> mCatMap;
        
    public DataSet(Context context) {
        mCatMap = new SparseArray<Category>();

        XmlPullParser parser = context.getResources().getXml(R.xml.cheesecake);
        
        try {
            parseData(parser);
        } catch (XmlPullParserException e) {
            Log.e("DataSet", "XML Error", e);
        } catch (IOException e) {
            Log.e("DataSet", "XML Error", e);
        }
    }

    public int getNumberOfCategories() {
        return mCatMap.size();
    }
    
    public Category getCategoryAt(int index) {
        return mCatMap.valueAt(index);
    }
    
    public DataItem getItem(int index, int id) {
        return getCategoryAt(index).items.get(id);
    }
    
    private void parseData(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        DataItem item = null;
        String tag = null;
        boolean parsingCategory = false;
        int catId = 0;
        while (eventType != XmlPullParser.END_DOCUMENT) {
             if(eventType == XmlPullParser.START_DOCUMENT) {
             } else if(eventType == XmlPullParser.START_TAG) {
                 tag = parser.getName();
                 if (tag.equals("item")) {
                     item = new DataItem();
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

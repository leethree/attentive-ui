
package hk.hku.cs.srli.factfinder;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

public class DataSet {

    public static class Category {
        private final int id;
        private final String name;
        private final SparseArray<DataItem> items;
        
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
        private int category;
        public String thumb;
        public String title; // full name
        public String name; // short name
        public String type;
        public String content;
        public int price; // in cents
        public int nlikes;
        
        @Override
        public String toString() {
            if (name != null && name.length() > 0)
                return name;
            else
                return title;
        }
    }
    
    public static String formatMoney(int price) {
        if (price != 0)
            // convert from cents to dollars
            return "$" + sDf.format(price * 0.01);
        else
            return "free";
    }
    
    private static final DecimalFormat sDf = new DecimalFormat("#0.00");
    private final SparseArray<Category> mCatMap;
    private final Random mRandom; 
        
    public DataSet(Context context, int dataSource) {
        mCatMap = new SparseArray<Category>();
        mRandom = new Random(dataSource);

        XmlPullParser parser = context.getResources().getXml(dataSource);
        
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
    
    // reversed query
    public Category getCategoryFromItem(DataItem item) {
        return mCatMap.get(item.category);
    }
    
    public DataItem getItem(int index, int id) {
        return getCategoryAt(index).items.get(id);
    }
    
    private void parseData(XmlPullParser parser) throws XmlPullParserException, IOException {
        // TODO: refactor this mess
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
                     // assign a random number
                     // TODO: use real data
                     item.nlikes = mRandom.nextInt(80);
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
                     } else if (tag.equals("name")) {
                         item.name = text;
                     } else if (tag.equals("content")) {
                         item.content = text;
                     } else if (tag.equals("thumb")) {
                         item.thumb = text;
                     } else if (tag.equals("price")) {
                         item.price = (int) (Double.parseDouble(text) * 100);
                     } else if (tag.equals("type")) {
                         item.type = text;
                     }
                 }
             }
             eventType = parser.next();
        }
    }

}

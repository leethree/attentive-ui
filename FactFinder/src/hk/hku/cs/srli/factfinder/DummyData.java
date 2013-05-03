
package hk.hku.cs.srli.factfinder;

import android.content.res.Resources;
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
        private static final Map<Integer, Category> map;
        static {
            map = new HashMap<Integer, DummyData.Category>(3);
            for(Category s : Category.values()) map.put(s.id, s);
        }
        private Category(int id) {
            this.id = id;
        }
        public static Category of(int i) {
            return map.get(i);
        }
        public static Category parse(String s) {
            return map.get(Integer.parseInt(s));
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
    
    private static DummyData instance;
    
    private DummyData(Resources res) {
        mCatMap = new HashMap<DummyData.Category, SparseArray<FactItem>>(3);
        for(Category s : Category.values()) {
            mCatMap.put(s, new SparseArray<DummyData.FactItem>());
        }
        
        XmlPullParser parser = res.getXml(R.xml.data);
        
        try {
            parseData(parser);
        } catch (XmlPullParserException e) {
            Log.e("DummyData", "XML Error", e);
        } catch (IOException e) {
            Log.e("DummyData", "XML Error", e);
        }
    }
    
    public static DummyData getInstance(Resources res) {
        if (instance == null) {
            instance = new DummyData(res);
        }
        return instance;
    }
    
    public SparseArray<FactItem> getCatData(Category cat) {
        return mCatMap.get(cat);
    }
    
    public FactItem getItem(Category cat, int id) {
        return mCatMap.get(cat).get(id);
    }
    
    private void parseData(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        FactItem item = null;
        String tag = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
             if(eventType == XmlPullParser.START_DOCUMENT) {
                 Log.v("parseData", "Start document");
             } else if(eventType == XmlPullParser.START_TAG) {
                 tag = parser.getName();
                 Log.v("parseData", "Start tag " + tag);
                 if (tag.equals("item")) {
                     item = new FactItem();
                 }
             } else if(eventType == XmlPullParser.END_TAG) {
                 tag = parser.getName();
                 Log.v("parseData", "End tag " + tag);
                 if (tag.equals("item")) {
                     // add item to category.
                     getCatData(item.category).append(item.id, item);
                     Log.v("parseData", "New item " + item.toString());
                     item = null;
                 }
                 tag = null;
             } else if(eventType == XmlPullParser.TEXT) {
                 String text = parser.getText();
                 Log.v("parseData", "Text " + text);
                 if (item != null && tag != null) {
                     if (tag.equals("id")) {
                         item.id = Integer.parseInt(text);
                     } else if (tag.equals("category")) {
                         item.category = Category.parse(text);
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
    
    // references to our images
    public static int[][] sThumbIds = {
            {
                    R.drawable.sample_3,
                    R.drawable.sample_4, R.drawable.sample_5,
                    R.drawable.sample_6, R.drawable.sample_7,
                    R.drawable.sample_0, R.drawable.sample_1,
                    R.drawable.sample_2, R.drawable.sample_3,
                    R.drawable.sample_4, R.drawable.sample_5,
                    R.drawable.sample_6, R.drawable.sample_7,
                    R.drawable.sample_0, R.drawable.sample_1,
                    R.drawable.sample_2, R.drawable.sample_3,
                    R.drawable.sample_4, R.drawable.sample_5,
                    R.drawable.sample_6, R.drawable.sample_7
            },
            {
                    R.drawable.sample_0, R.drawable.sample_1,
                    R.drawable.sample_2, R.drawable.sample_3,
                    R.drawable.sample_4, R.drawable.sample_5,
                    R.drawable.sample_6, R.drawable.sample_7,
                    R.drawable.sample_2, R.drawable.sample_3,
                    R.drawable.sample_4, R.drawable.sample_5,
                    R.drawable.sample_6, R.drawable.sample_7,
                    R.drawable.sample_0, R.drawable.sample_1,
                    R.drawable.sample_2, R.drawable.sample_3,
                    R.drawable.sample_4, R.drawable.sample_5,
                    R.drawable.sample_6, R.drawable.sample_7
            },
            {
                    R.drawable.sample_4, R.drawable.sample_5,
                    R.drawable.sample_6, R.drawable.sample_7,
                    R.drawable.sample_2,
                    R.drawable.sample_0, R.drawable.sample_1,
                    R.drawable.sample_2, R.drawable.sample_3,
                    R.drawable.sample_4, R.drawable.sample_5,
                    R.drawable.sample_6, R.drawable.sample_7,
                    R.drawable.sample_3,
                    R.drawable.sample_4, R.drawable.sample_5,
                    R.drawable.sample_6, R.drawable.sample_7
            }
    };
}

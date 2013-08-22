package hk.hku.cs.srli.monkeydemo.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoContent {

    public static List<DemoItem> ITEMS = new ArrayList<DemoItem>();

    /**
     * A map of demo items, by ID.
     */
    public static Map<String, DemoItem> ITEM_MAP = new HashMap<String, DemoItem>();

    static {
        // Add 5 sample items.
        addItem(new DemoItem("1", "Buttons", new ButtonsFragment()));
        //addItem(new DemoItem("2", "Web", new WebViewFragment()));
        //addItem(new DemoItem("3", "Ellipsis", new TextFragment()));
        addItem(new DemoItem("4", "Scroll", new ScrollFragment()));
        addItem(new DemoItem("5", "Swipe", new SwipeFragment()));
    }

    private static void addItem(DemoItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A struct representing a demo.
     */
    public static class DemoItem {
        public String id;
        public String title;
        public DemoFragmentBase fragment;
        
        public DemoItem(String id, String content, DemoFragmentBase fragment) {
            this.id = id;
            this.title = content;
            this.fragment = fragment;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}

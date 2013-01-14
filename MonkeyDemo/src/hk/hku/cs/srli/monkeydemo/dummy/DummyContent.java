
package hk.hku.cs.srli.monkeydemo.dummy;

import hk.hku.cs.srli.monkeydemo.ButtonsFragment;
import hk.hku.cs.srli.monkeydemo.TypeDetailFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    static {
        // Add 3 sample items.
        addItem(new DummyItem("1", "Helvetica", new ButtonsFragment()));
        addItem(new DummyItem("2", "Futura"));
        addItem(new DummyItem("3", "Univers"));
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;
        public TypeDetailFragment fragment;

        public DummyItem(String id, String content) {
            this.id = id;
            this.content = content;
            this.fragment = new TypeDetailFragment();
        }
        
        public DummyItem(String id, String content, TypeDetailFragment fragment) {
            this.id = id;
            this.content = content;
            this.fragment = fragment;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}

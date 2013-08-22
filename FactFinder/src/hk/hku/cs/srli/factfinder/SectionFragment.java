package hk.hku.cs.srli.factfinder;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import hk.hku.cs.srli.factfinder.DataSet.DataItem;

public class SectionFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    
    private int mSectionNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GridView gridview = (GridView) getView().findViewById(R.id.grid_view);
        ImageAdapter adapter = new ImageAdapter(getActivity(), mSectionNumber);
        gridview.setAdapter(adapter);
    }
    
    public static class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private int mSection;
        private SparseArray<DataItem> mFacts;

        public ImageAdapter(Context c, int section) {
            mContext = c;
            mSection = section;
            mFacts = FFApp.getData(c).getCategoryAt(section).getItems();
        }

        @Override
        public int getCount() {
            return mFacts.size();
        }

        @Override
        public DataItem getItem(int position) {
            return mFacts.valueAt(position);
        }

        @Override
        public long getItemId(int position) {
            return mFacts.keyAt(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {  // if it's not recycled, inflate it.
                convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
            }
            ImageView imageView = (ImageView) convertView.findViewById(R.id.item_image_view);
            
            // find image from assets
            try {
                String thumb = getItem(position).thumb;
                if (thumb != null && thumb.length() > 0) {
                    imageView.setImageDrawable(
                            Drawable.createFromResourceStream(mContext.getResources(), null, 
                                    mContext.getAssets().open(thumb), null));
                } else imageView.setImageResource(R.drawable.placeholder);
            } catch (IOException e) {
                // Image loading failed, use placeholder instead.
                imageView.setImageResource(R.drawable.placeholder);
            }
            imageView.setOnClickListener(new ItemClickListenerAdapter(position) {
    
                @Override
                public void onClick(View v, int position) {
                    FFApp.log("Main UI", "Click grid item: section " + mSection + " position " + position);
                    Intent i = new Intent(mContext, DetailActivity.class);
                    i.putExtra("id", (int) getItemId(position)).putExtra("section", mSection);
                    // launch detailed view
                    mContext.startActivity(i);
                    FFApp.log("Nav", "Open detail screen."); 
                }
            });
            
            TextView text = (TextView) convertView.findViewById(R.id.item_text_view);
            text.setText(getItem(position).title);
            
            Button price = (Button) convertView.findViewById(R.id.item_button_price);
            price.setText(DataSet.formatMoney(getItem(position).price));
            price.setOnClickListener(new ItemClickListenerAdapter(position) {
                
                @Override
                public void onClick(View v, int position) {
                    FFApp.log("Main UI", "Click quick add button: section " + mSection + " position " + position);
                    FFApp.getOrder(mContext).add(getItem(position));
                }
            });

            return convertView;
        }
        
        // adapter for handling item clicks
        private abstract class ItemClickListenerAdapter implements View.OnClickListener {
            private int mPosition;
            
            public ItemClickListenerAdapter(int position) {
                this.mPosition = position;
            }
            
            public abstract void onClick(View v, int position);
            
            @Override
            public final void onClick(View v) {
                onClick(v, mPosition);
            }
        }
    }
}

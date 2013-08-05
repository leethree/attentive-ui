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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import hk.hku.cs.srli.factfinder.DummyData.FactItem;

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
        gridview.setAdapter(new ImageAdapter(getActivity(), mSectionNumber));

        gridview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(getActivity(), DetailActivity.class);
                i.putExtra("id", (int) id).putExtra("section", mSectionNumber);
                startActivity(i);
            }
        });
    }
    
    public static class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private SparseArray<FactItem> mFacts;

        public ImageAdapter(Context c, int section) {
            mContext = c;
            mFacts = FFApp.getData(c).getCategoryAt(section).getItems();
        }

        @Override
        public int getCount() {
            return mFacts.size();
        }

        @Override
        public FactItem getItem(int position) {
            return mFacts.valueAt(position);
        }

        @Override
        public long getItemId(int position) {
            return mFacts.keyAt(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {  // if it's not recycled, inflate it.
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
            }
            ImageView imageView = (ImageView) convertView.findViewById(R.id.item_image_view);
            TextView textView = (TextView) convertView.findViewById(R.id.item_text_view);

            // find image from assets
            try {
                imageView.setImageDrawable(
                        Drawable.createFromResourceStream(mContext.getResources(), null, 
                                mContext.getAssets().open(getItem(position).thumb), null));
            } catch (IOException e) {
                // Image loading failed, use placeholder instead.
                imageView.setImageResource(R.drawable.placeholder);
            }
            textView.setText(getItem(position).title);
            return convertView;
        }

    }
}

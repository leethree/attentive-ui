package hk.hku.cs.srli.factfinder;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import hk.hku.cs.srli.factfinder.DummyData.Category;
import hk.hku.cs.srli.factfinder.DummyData.FactItem;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
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
                Log.v("onItemClick", "id " + id + ", position " + position);
                i.putExtra("id", (int) id).putExtra("section", mSectionNumber);
                startActivity(i);
            }
        });
    }
    
    public static class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private SparseArray<FactItem> mFacts;

        public ImageAdapter(Context c, int n) {
            mContext = c;
            mFacts = DummyData.getInstance(c.getResources()).getCatData(Category.of(n));
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
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                
                // set sizes
                int gutter = mContext.getResources().getDimensionPixelSize(R.dimen.grid_gutter);
                int height = mContext.getResources().getDimensionPixelSize(R.dimen.grid_column_width);
                LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, height);

                imageView.setLayoutParams(lp);
                imageView.setPadding(gutter, gutter, gutter, gutter);
                imageView.setCropToPadding(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }

            // find image resource ID
            int thumbId = mContext.getResources().getIdentifier(
                    getItem(position).thumb, "drawable", mContext.getPackageName());
            imageView.setImageResource(thumbId);
            return imageView;
        }

    }
}
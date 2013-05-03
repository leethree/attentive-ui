package hk.hku.cs.srli.factfinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

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
        GridView gridview = (GridView) getView().findViewById(R.id.gridView1);
        gridview.setAdapter(new ImageAdapter(getActivity(), mSectionNumber));
        
        gridview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(getActivity(), DetailActivity.class);
                i.putExtra("id", position);
                startActivity(i);
            }
        });
    }
    
    public static class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private int[] mImages;

        public ImageAdapter(Context c, int n) {
            mContext = c;
            mImages = DummyData.sThumbIds[n];
        }

        @Override
        public int getCount() {
            return mImages.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                
                // set size
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

            imageView.setImageResource(mImages[position]);
            return imageView;
        }

    }
}
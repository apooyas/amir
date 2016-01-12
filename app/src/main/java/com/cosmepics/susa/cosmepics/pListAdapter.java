package com.cosmepics.susa.cosmepics;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.util.List;
import java.util.TreeSet;
/**
 * Created by Amir on 20/04/2015.
 * Custom adapter to show search result products
 */

class pListAdapter extends ArrayAdapter<product> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private TreeSet<Integer> sectionHeader = new TreeSet<>();

    pListAdapter(Context context, List<product> products) {
        super(context, R.layout.product, products);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater pListInflater = LayoutInflater.from(getContext());
        product pItem = getItem(position);

        if (convertView == null) {
            if (position==0) { //section title for matched products
                convertView = pListInflater.inflate(R.layout.list_section_title, null);
                TextView searchResultSectionTV= (TextView) convertView.findViewById(R.id.searchResultSectionTV);
                searchResultSectionTV.setText(" matched product(s)");
            } /*else {
                if (position ==3) { //section title for similar but not exactly-matched products
                    convertView = pListInflater.inflate(R.layout.list_section_title, null);
                    TextView searchResultSectionTV= (TextView) convertView.findViewById(R.id.searchResultSectionTV);
                    searchResultSectionTV.setText("Similar Colors in the order of relevance");
                }*/ else {
                    convertView = pListInflater.inflate(R.layout.product,parent,false);
                    final ImageView productIV = (ImageView) convertView.findViewById(R.id.productIV);
                    final ImageView brandLogoIV = (ImageView) convertView.findViewById(R.id.brandLogoIV);
                    TextView pSerieTV = (TextView) convertView.findViewById(R.id.pSerieTV);
                    TextView pShadeNameTV = (TextView) convertView.findViewById(R.id.pShadeNameTV);
                    TextView pFinishTV = (TextView) convertView.findViewById(R.id.pFinishTV);
                    TextView longLastingTV = (TextView) convertView.findViewById(R.id.pLongLastingTV);

                    AsyncHttpClient client = new AsyncHttpClient();

                    //ImageViews to be populated by the image files retrieved by FileAsyncHttpResponseHandler
                    //NOTE: all file name upper/lower cases must be as per standard
                    client.get(getContext().getResources().getString(R.string.product_img_url) + pItem.p_img_file, new FileAsyncHttpResponseHandler(getContext()) {
                        @Override
                        public void onSuccess(int statusCode,
                                              org.apache.http.Header[] headers,
                                              java.io.File file) {
                            productIV.setImageURI(Uri.fromFile(file));
                        }

                        @Override
                        public void onFailure(int statusCode,
                                              org.apache.http.Header[] headers,
                                              java.lang.Throwable throwable,
                                              java.io.File file){
                            throwable.printStackTrace();
                        }
                    });

                    client.get(getContext().getResources().getString(R.string.brand_logo_img_url) + pItem.brand.toLowerCase() + ".png", new FileAsyncHttpResponseHandler(getContext()) {
                        @Override
                        public void onSuccess(int statusCode,
                                              org.apache.http.Header[] headers,
                                              java.io.File file) {
                            brandLogoIV.setImageURI(Uri.fromFile(file));
                        }

                        @Override
                        public void onFailure(int statusCode,
                                              org.apache.http.Header[] headers,
                                              java.lang.Throwable throwable,
                                              java.io.File file){
                            throwable.printStackTrace();
                        }
                    });

                    //TextViews To be populated by the SQL search result from the Server
                    pSerieTV.setText(pItem.p_serie);
                    pShadeNameTV.setText(pItem.shade_name);
                    pFinishTV.setText(pItem.finish);
                    if (pItem.long_lasting){
                        longLastingTV.setText("Long Lasting");
                    }
                }

        }

   // }

        return convertView;
    }

}

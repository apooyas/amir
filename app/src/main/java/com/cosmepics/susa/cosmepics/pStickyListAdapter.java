package com.cosmepics.susa.cosmepics;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Amir on 20/04/2015.
 * Custom adapter to show search result products
 */
public class pStickyListAdapter extends BaseAdapter implements StickyListHeadersAdapter,Filterable {
    private Context mContext;
    private LayoutInflater inflater;
    private List<product> products;

    //Two data sources, the original data and filtered data
    private List<product> originalPList;
    private List<product> filteredPList;

    public pStickyListAdapter(Context context, List<product> p) {
        mContext = context;
        inflater = LayoutInflater.from(context);
        originalPList = p;
        filteredPList = p;

    }

    @Override
    public int getCount() {
        return filteredPList.size();
    }

    @Override
    public product getItem(int position) {
        return filteredPList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderItemViewHolder holder;
        if (convertView == null) {
            holder = new HeaderItemViewHolder();
            convertView = inflater.inflate(R.layout.list_section_title, parent, false);
            holder.tv = (TextView) convertView.findViewById(R.id.searchResultSectionTV);
            convertView.setTag(holder);
        } else {
            holder = (HeaderItemViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        String headerText = filteredPList.get(position).is_matched? "Matched Products":"Similar Products";
            holder.tv.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon

        // return 0 when color matched and 1 when not
        long id = filteredPList.get(position).is_matched? 0:1;
        return id;
    }

    class HeaderItemViewHolder {
        TextView tv;
    }

    class ItemViewHolder {
        View v;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder holder;
        if (convertView == null) {
            holder = new ItemViewHolder();
            convertView = inflater.inflate(R.layout.product, parent, false);

            //setup the holder to hold current product
            holder.v = convertView.findViewById(R.id.productLL);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder) convertView.getTag();
        }

        //Initialize the holder

        final product pItem = getItem(position);
        final ImageView productIV = (ImageView) convertView.findViewById(R.id.productIV);
        final ImageView brandLogoIV = (ImageView) convertView.findViewById(R.id.brandLogoIV);

        TextView pTypeTV = (TextView) convertView.findViewById(R.id.pTypeTV);
        TextView pNameTV = (TextView) convertView.findViewById(R.id.pNameTV);
        TextView pCodeTV = (TextView) convertView.findViewById(R.id.pCodeTV);
        TextView pFinishTV = (TextView) convertView.findViewById(R.id.pFinishTV);
        TextView longLastingTV = (TextView) convertView.findViewById(R.id.pLongLastingTV);
        TextView priceLevelTV = (TextView) convertView.findViewById(R.id.priceLevelTV);
        //TextView distanceTV=(TextView) convertView.findViewById(R.id.pDistanceTV); Just for testing


        AsyncHttpClient client = new AsyncHttpClient();

        //ImageViews to be populated by the image files retrieved by FileAsyncHttpResponseHandler
        //NOTE: all file name upper/lower cases must be as per standard
        String imgURL=parent.getContext().getResources().getString(R.string.product_img_url) + pItem.p_code_mfc + ".jpg";
        client.get(parent.getContext().getResources().getString(R.string.product_img_url) + pItem.p_code_mfc + ".jpg", new FileAsyncHttpResponseHandler(parent.getContext()) {
            @Override
            public void onSuccess(int statusCode,
                                  org.apache.http.Header[] headers,
                                  java.io.File file) {
                productIV.setImageURI(Uri.fromFile(file));
            }

            @Override
            public void onFailure(int statusCode,
                                  org.apache.http.Header[] headers,
                                  Throwable throwable,
                                  java.io.File file){
                                    switch (pItem.product_type.toLowerCase()){
                                        //types under Lip group
                                        case ("lipstick"):{
                                            productIV.setImageResource(R.drawable.ic_lipstick);
                                            break;
                                        }
                                        case ("lip pencil"):{
                                            productIV.setImageResource(R.drawable.ic_action_cancel);
                                            break;
                                        }case ("lip gloss"):{
                                            productIV.setImageResource(R.drawable.ic_action_cancel);
                                            break;
                                        }
                                        case ("lip satin"):{
                                            productIV.setImageResource(R.drawable.ic_action_cancel);
                                            break;
                                        }
                                        case ("lip sets"):{
                                            productIV.setImageResource(R.drawable.ic_action_cancel);
                                            break;
                                        }
                                        case ("lip kits and bags"):{
                                            productIV.setImageResource(R.drawable.ic_action_cancel);
                                            break;
                                        }
                                        //types under Eye group
                                        case ("eyeshadow"):{
                                            productIV.setImageResource(R.drawable.ic_eyeshadow);
                                            break;
                                        }
                                        case ("eyeliner"):{
                                            productIV.setImageResource(R.drawable.ic_action_cancel);
                                            break;
                                        }
                                        //types under Nail group
                                        case ("nail polish"):{
                                            productIV.setImageResource(R.drawable.ic_nailpolish);
                                            break;
                                        }
                                    }

                                    throwable.printStackTrace();
            }
        });

        client.get(parent.getContext().getResources().getString(R.string.brand_logo_img_url) + pItem.brand.toLowerCase() + ".png", new FileAsyncHttpResponseHandler(parent.getContext()) {
            @Override
            public void onSuccess(int statusCode,
                                  org.apache.http.Header[] headers,
                                  java.io.File file) {
                brandLogoIV.setImageURI(Uri.fromFile(file));
            }

            @Override
            public void onFailure(int statusCode,
                                  org.apache.http.Header[] headers,
                                  Throwable throwable,
                                  java.io.File file){
                throwable.printStackTrace();
            }
        });

        //TextViews To be populated by the SQL search result from the Server
        pTypeTV.setText(pItem.product_type);
        pNameTV.setText(pItem.p_name);
        pCodeTV.setText(pItem.p_code_mfc);
        pFinishTV.setText(pItem.finish);
        if (pItem.long_lasting){
            longLastingTV.setText("Yes");
        }else{
            longLastingTV.setText("No");
        }

        if (pItem.price.intValue() <=10 ){ //this should be changed to method that takes product type into account for price ranges
            priceLevelTV.setText("$");}
        else if ((pItem.price.intValue() >10 ) && (pItem.price.intValue() <20 )){priceLevelTV.setText("$$");}
        else {priceLevelTV.setText("$$$");}
        //distanceTV.setText(String.valueOf(pItem.color_distance)); Just used for testing

        // Listen for ListView Item Click
 /*       convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Send single item click data to SingleItemView Class
                Intent productSelectedIntent = new Intent(mContext, productDetailActivity.class);
                // Pass product ID
                productSelectedIntent.putExtra("productID",pItem.id);
                mContext.startActivity(productSelectedIntent);
            }
        });*/

        holder.v=convertView; //If it doesn't work I have to use holder.v directly in above commands.
        return convertView;
        }

    @Override
    public Filter getFilter() {
        {
            return new Filter()
            {
                @Override
                protected FilterResults performFiltering(CharSequence filterParam)
                {
                    FilterResults results = new FilterResults();

                    //If there's nothing to filter on, return the original data for your list
                    if(filterParam == null || filterParam.length() == 0 || filterParam=="NoFilter")
                    {
                        results.values = originalPList;
                        results.count = originalPList.size();
                    }
                    else
                    {
                        List<product> filterResultsData = new ArrayList<>(); //If this causes type errors then replace all Lists with ArrayList
                        String filterKey=(String) filterParam;
                        String filterKeyValue="";
                        if (filterKey.indexOf("=")>0){ //if filterParam is (key=value) pair
                            filterKeyValue= filterKey.substring(filterKey.indexOf("=")+1,filterKey.length());
                            filterKey = filterKey.substring(0,filterKey.indexOf("="));
                        }

                        switch (filterKey) {  //If you find a match, add it to your new ArrayList
                            case "Lip":
                            case "Nail":
                            case "Eye": {
                                for (product data : originalPList) {
                                    //In this loop filter through originalPList and compare each item to filterParam.
                                    if (data.product_group.equalsIgnoreCase(filterKey)) {
                                        filterResultsData.add(data);
                                    }
                                }
                                break;
                            }
                            case "Brand": {
                                for (product data : originalPList) {
                                    //In this loop filter through originalPList and compare each item to filterParam.
                                    if (data.brand.equalsIgnoreCase(filterKeyValue)) {
                                        filterResultsData.add(data);
                                    }
                                }
                                break;
                            }
                            case "Price": {
                                switch (filterKeyValue) {
                                    case "$": {
                                        for (product data : originalPList) {
                                            //In this loop filter through originalPList and compare each item to filterParam.
                                            if (data.price.intValue() <= 10) { //this should be changed to method that takes product type into account for price ranges
                                                filterResultsData.add(data);
                                            }
                                        }
                                        break;
                                    }
                                    case "$$": {
                                        for (product data : originalPList) {
                                            //In this loop filter through originalPList and compare each item to filterParam.
                                            if (data.price.intValue() > 10 && data.price.intValue() <= 20) { //this should be changed to method that takes product type into account for price ranges
                                                filterResultsData.add(data);
                                            }
                                        }
                                        break;
                                    }
                                    case "$$$": {
                                        for (product data : originalPList) {
                                            //In this loop filter through originalPList and compare each item to filterParam.
                                            if (data.price.intValue() >20) { //this should be changed to method that takes product type into account for price ranges
                                                filterResultsData.add(data);
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            case "Finish": {
                                for (product data : originalPList) {
                                    //In this loop filter through originalPList and compare each item to filterParam.
                                    if (data.finish.equalsIgnoreCase(filterKey)) {
                                        filterResultsData.add(data);
                                    }
                                }
                                break;
                            }
                            case "Long Lasting":{
                                for (product data : originalPList) {
                                    //In this loop filter through originalPList and compare each item to filterParam.
                                    if (data.long_lasting) {
                                        filterResultsData.add(data);
                                    }
                                }
                                break;
                            }
                        }

                        results.values = filterResultsData;
                        results.count = filterResultsData.size();
                    }

                    return results;
                }

                @Override
                protected void publishResults(CharSequence filterParam, FilterResults filterResults)
                {
                    filteredPList = (List<product>)filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }
}

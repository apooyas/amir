package com.cosmepics.susa.cosmepics;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.cosmepics.susa.cosmepics.product.JSON2product;


public class pListActivity extends Activity {

/*
    private static final int ACTION_FILTER_EYE = 1;
    private static final int ACTION_FILTER_LIP = 2;
    private static final int ACTION_FILTER_NAIL = 3;
*/

    private Menu globalPickColorMenu;
    static int globalPickedRGB=0;
    static int globalColorMatchRadius=100; //to be overwritten by radius parameter value from server
    TextView searchResultTV;
    static boolean globalFilterOnFlag;
    pStickyListAdapter globalPListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        try{
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO );
            //URI paramsURI = Uri.parse(getApplicationContext().getResources().getString(R.string.parameters_url));
            File fXmlFile = new File(getResources().getString(R.string.parameters_url));
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            globalColorMatchRadius =Integer.parseInt(doc.getElementsByTagName("color_match_radius").item(0).getTextContent());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        populateProductList();
    }

    public void populateProductList() {
        final ProgressDialog prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Searching");
        prgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prgDialog.setIndeterminate(true);
        prgDialog.show();
        RequestParams params = new RequestParams();
        globalPickedRGB = getIntent().getIntExtra("mColor", 0);
        //globalPickedRGB = 16728593;   //for test only
        String product_type = getIntent().getStringExtra("product_type");
        params.put("color", globalPickedRGB);
        params.put("product_type", product_type);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://cosmepicsws.appspot.com/byColorServlet_new", params, new JsonHttpResponseHandler() { //Production Environment
        //client.get("http://10.0.3.2:8888/byColorServlet", params, new JsonHttpResponseHandler() { //Test Environment

            //Async response handling
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, org.json.JSONArray response){
                // Hide the Progress Dialog
                prgDialog.hide();
//                searchResultTV.setText(response.length()+ " results found");

                List<product> products = new ArrayList<>();
                try {
                    // Convert JSON result set to product object
                    for (int i = 0; i < response.length(); i++) {
                        products.add(JSON2product(response.getJSONObject(i)));
                    }


                    //Setup adapter
                    StickyListHeadersListView stickyList = (StickyListHeadersListView) findViewById(R.id.list);
                    globalPListAdapter = new pStickyListAdapter(getApplicationContext(),products);
                    stickyList.setAdapter(globalPListAdapter);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.String responseString, java.lang.Throwable throwable) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Device might not be connected to Internet", Toast.LENGTH_LONG).show();
                }
            }
        }); //End of Asynch http handler
    }


   /* public void dispatchFilterIntent(int actionCode){
        Intent listFilterIntent = new Intent(this, pListActivity.class);
        switch (actionCode){
            case ACTION_FILTER_EYE:{
                listFilterIntent.putExtra("product_type","Eye");
                break;
            }
            case ACTION_FILTER_LIP:{
                listFilterIntent.putExtra("product_type","Lip");
                break;
            }
            case ACTION_FILTER_NAIL:{
                listFilterIntent.putExtra("product_type","Nail");
            }
        }
        listFilterIntent.putExtra("mColor",getIntent().getIntExtra("mColor", 0));
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_p_list, menu);
        globalPickColorMenu = menu;
        menu.getItem(0).getActionView().setBackgroundColor(globalPickedRGB); //Display the selected color to the user on top of the screen
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemID = item.getItemId();
        if (globalFilterOnFlag){// highlight the filter icon to show the filter is ON
            globalPickColorMenu.getItem(1).setIcon(R.drawable.ic_filter_on);
        } else {
            globalPickColorMenu.getItem(1).setIcon(R.drawable.ic_filter);
        }

        switch (itemID){
            case R.id.barCancelFilter:{
                globalPListAdapter.getFilter().filter("NoFilter");
                globalFilterOnFlag=false;
                break;
            }
            case R.id.barEye:{
                globalPListAdapter.getFilter().filter("Eye");
                globalFilterOnFlag=true;
                break;
            }
            case R.id.barLip:{
                globalPListAdapter.getFilter().filter("Lip");
                globalFilterOnFlag=true;
                break;
            }
            case R.id.barNail:{
                globalPListAdapter.getFilter().filter("Nail");
                globalFilterOnFlag=true;
                break;
            }
            case R.id.mac:{
                globalPListAdapter.getFilter().filter("Brand=Mac");
                globalFilterOnFlag=true;
                break;
            }
            case R.id.UD:{
                globalPListAdapter.getFilter().filter("Brand=Urban Decay");
                globalFilterOnFlag=true;
                break;
            }
            case R.id.barPriceLow:{
                globalPListAdapter.getFilter().filter("Price=$");
                globalFilterOnFlag=true;
                break;
            }
            case R.id.barPriceMedium:{
                globalPListAdapter.getFilter().filter("Price=$$");
                globalFilterOnFlag=true;
                break;
            }
            case R.id.barPriceHigh:{
                globalPListAdapter.getFilter().filter("Price=$$$");
                globalFilterOnFlag=true;
                break;
            }
            case R.id.barLonglasting:{
                globalPListAdapter.getFilter().filter("Long Lasting");
                globalFilterOnFlag=true;
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
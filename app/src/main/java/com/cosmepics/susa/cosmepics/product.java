package com.cosmepics.susa.cosmepics;

import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by Amir on 09/05/2015.
 */
public class product {
    int id; //product unique id in the database
    String p_code_mfc; //product code by manufacturer
    long color; //color code of the product
    String product_type; //second level in product hierarchy with values: {Lipstick, Lip gloss, Lip liner,...}
    String product_group; //top level in product hierarchy with values:{Lip, Eye, Nail}
    String brand;
    String finish;
    int spf; //sun protection factor
    String ingredients;
    String year_season; //year season of the product release
    String p_serie; //product serie as named by manufacturer
    String p_name; //product name
    Boolean long_lasting; //is product long lasting flag
    String upc; //Unified Product Code
    BigDecimal price; //normal (not sale) price of products per the manufacturer's website in USD
    int color_distance; //color distance of this product from selected color in current search
    Boolean is_matched; //True if the color distance of this product is within the color-match-radius

    public product() {
        this.id = 0;
        this.p_name = " ";
    }
    public static product JSON2product(JSONObject obj) {
        product p = new product();
        p.id = obj.optInt("id");
        p.p_code_mfc = obj.optString("p_code_mfc");
        p.color = ((obj.optInt("r") & 0x0ff) << 16) | ((obj.optInt("g") & 0x0ff) << 8) | (obj.optInt("b") & 0x0ff);
        p.product_type=obj.optString("product_type");
        p.product_group=obj.optString("product_group");
        p.brand=obj.optString("brand");
        p.finish = obj.optString("finish");
        p.spf = obj.optInt("spf");
        p.ingredients=obj.optString("ingredients");
        p.year_season=obj.optString("year_season");
        p.p_serie=obj.optString("p_serie");
        p.p_name=obj.optString("p_name");
        p.long_lasting=obj.optBoolean("long_lasting");
        p.upc=obj.optString("upc");
        if (obj.optString("price")=="null"){p.price= BigDecimal.ZERO;}
            else {p.price= new BigDecimal(obj.optString("price"));}
        p.color_distance=obj.optInt("distance");
        //p.is_matched= p.color_distance>pListActivity.globalColorMatchRadius;
        p.is_matched= p.color_distance<100;
        return p;
    }
}

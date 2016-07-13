/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package demoApplication.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mastercard.hackathon.R;
import com.mastercard.hackathon.ui.ActivityList.FrameMarkers;
import com.mastercard.hackathon.utils.MeshObject;
import com.mastercard.hackathon.utils.Product;
import com.mastercard.hackathon.utils.Teapot;
import com.mastercard.hackathon.utils.WatchObject;


// This is the main activity which launches on startup.
// In reality this will be written by the app dev, not us. The function of this
// class is to represent the page containing products, and to pass the info required to
// model/buy the products to the sdk.

public class ActivityLauncher extends ListActivity
{
    private static final int NUM_PRODUCTS = 2;
    private Product[] products;

    private String mActivities[] = {"Frame Markers"};

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            R.layout.activities_list_text_view, mActivities);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activities_list);
        setListAdapter(adapter);

    }
    
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        
        Intent intent = new Intent(this, FrameMarkers.class);
        intent.putExtra("ABOUT_TEXT_TITLE", mActivities[position]);

        switch (position)
        {
            case 0:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.FrameMarkers.FrameMarkers");
                break;
        }
        
        startActivity(intent);
        
    }

    /*
    private void loadProducts() {
        String[] names = getProductNames();
        String[] descriptions = getProductDescriptions();
        double[] prices = getProductPrices();
        MeshObject[] models = getModels();

        products = new Product[NUM_PRODUCTS];
        for(int i = 0; i < products.length; i++) {
            products[i] = new Product(names[i], descriptions[i], prices[i], null, null);
            products[i].setModel(models[i]);
        }
    }

    // These are just some dummy functions to mock input into the sdk by the app dev.
    private MeshObject[] getModels() {
        MeshObject[] models = new MeshObject[2];
        models[0] = new WatchObject();
        models[1] = new Teapot();
        return models;
    }

    private String[] getProductNames() {
        String[] names = new String[2];
        names[0] = "Designer Watch";
        names[1] = "Teapot";
        return names;
    }

    private double[] getProductPrices() {
        double[] prices = new double[2];
        prices[0] = 149.99;
        prices[1] = 39.99;
        return prices;
    }

    private String[] getProductDescriptions() {
        String[] descriptions = new String[2];
        descriptions[0] = "Hand-crafted by the blind watchmakers of southern Italy," +
                " this elegant chronograph is the perfect addition to any collection.";
        descriptions[1] = "This postminimalism-inspired teapot was designed" +
                " by famed sculptor Fran Stalinovskovich" +
                " and adds a touch of class to any teatime.";
        return descriptions;
    }
    */
}

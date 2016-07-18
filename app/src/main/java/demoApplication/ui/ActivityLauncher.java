/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package demoApplication.ui;

import android.app.Activity;
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

public class ActivityLauncher extends Activity
{
    private static final int NUM_PRODUCTS = 2;
    private Product[] products;

    private String mActivities[] = {"Designer Watch", "Teapot", "Stereo System", "LCD Television", "Coffee Machine", "Leather Couch", "Bookshelf", "Bedside Table"};

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activities_list);

    }

    public void onArClick(View v) {
        Intent intent = new Intent(this, FrameMarkers.class);
        startActivity(intent);
    }

    /*
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
        
    }*/
}

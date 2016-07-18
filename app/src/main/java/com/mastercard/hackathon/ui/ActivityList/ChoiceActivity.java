package com.mastercard.hackathon.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mastercard.hackathon.R;

public class ChoiceActivity extends Activity {

    private String name;
    private String description;
    private Double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        Intent intent = getIntent();
        name = intent.getStringExtra("product_name");
        description = intent.getStringExtra("product_description");
        price = intent.getDoubleExtra("product_price", 0);
    }

    public void onPayButtonClick(View v) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("product_name", name);
        intent.putExtra("product_description", description);
        intent.putExtra("product_price", price);
        startActivity(intent);
    }

    public void onMasterPassClick(View v) {
        Intent intent = new Intent(this, PaymentActivity.class);
        startActivity(intent);
    }
}

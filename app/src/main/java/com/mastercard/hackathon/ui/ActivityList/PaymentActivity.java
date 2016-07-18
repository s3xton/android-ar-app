package com.mastercard.hackathon.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.mastercard.hackathon.R;
import com.simplify.android.sdk.CardEditor;
import com.simplify.android.sdk.CardToken;
import com.simplify.android.sdk.Simplify;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Conor on 13/07/2016.
 */
public class PaymentActivity extends Activity {

    private String name;
    private String description;
    private Double price;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_payment);

        Intent intent = getIntent();
        name = intent.getStringExtra("product_name");
        description = intent.getStringExtra("product_description");
        price = intent.getDoubleExtra("product_price", 0);

        TextView textView = (TextView) findViewById(R.id.item_name);
        textView.setText(name);
        textView = (TextView) findViewById(R.id.item_description);
        textView.setText("€" + price + " - " + description);



        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Simplify.init("sbpb_ZDhiMDI4ZGUtMzE4OS00MGQwLWE2YzktZTIxMTBjOGMxOTNl");

        // init card editor
        final CardEditor cardEditor = (CardEditor) findViewById(R.id.card_editor);
        final Button checkoutButton = (Button) findViewById(R.id.checkout_button);
        // add state change listener
        cardEditor.addOnStateChangedListener(new CardEditor.OnStateChangedListener() {
            @Override
            public void onStateChange(CardEditor cardEditor) {
                // true: card editor contains valid and complete card information
                checkoutButton.setEnabled(cardEditor.isValid());
            }
        });

        // add checkout button click listener
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create a card token
                Simplify.createCardToken(cardEditor.getCard(), new CardToken.Callback() {
                    @Override
                    public void onSuccess(CardToken cardToken) {
                        System.out.println("got card token");

                        URL url = null;
                        HttpURLConnection con = null;
                        try {
                            url = new URL("http://mc-hack.herokuapp.com//charge.php");
                            con = (HttpURLConnection) url.openConnection();
                            //add reuqest header
                            con.setRequestMethod("POST");
                            con.setRequestProperty("User-Agent", "Mozilla/5.0");
                            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                            String urlParameters = "simplifyToken="+cardToken.getId()+"&amount=" + (price*100);

                            // Send post request
                            con.setDoOutput(true);
                            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                            wr.writeBytes(urlParameters);
                            wr.flush();
                            wr.close();

                            int responseCode = con.getResponseCode();
                            System.out.println("\nSending 'POST' request to URL : " + url);
                            System.out.println("Post parameters : " + urlParameters);
                            System.out.println("Response Code : " + responseCode);

                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(con.getInputStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();
                            //print result
                            System.out.println(response.toString());

                            Intent intent = new Intent(PaymentActivity.this, ConfirmationActivity.class);
                            startActivity(intent);
                            //
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            con.disconnect();
                        }
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("Didn't get card token");
                    }
                });
            }
        });


    }


}
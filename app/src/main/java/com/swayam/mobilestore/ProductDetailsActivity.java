package com.swayam.mobilestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.nikartm.button.FitButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductDetailsActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailsActivity";
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.imageView) ImageView imageView;
    @BindView(R.id.summary) TextView summary;
    @BindView(R.id.priceValue) TextView priceValue;
    @BindView(R.id.scrollView) ScrollView scrollView;
    private int pId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        ButterKnife.bind(this);

        int id = getIntent().getIntExtra("id",0);

        String url = App.URL + "product_by_id.php?id="+id;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                int id = response.getJSONObject(i).getInt("id");
                                pId = id;
                                String brandName = response.getJSONObject(i).getString("category");
                                String model = response.getJSONObject(i).getString("sub_category");
                                double price = response.getJSONObject(i).getDouble("price");
                                String picturePath = response.getJSONObject(i).getString("picture_path");
                                String pSummary = response.getJSONObject(i).getString("summary");

                                scrollView.setVisibility(View.VISIBLE);

                                toolbar.setTitle(model);
                                summary.setText(pSummary);
                                priceValue.setText("Rs. "+price);
                                String url = App.URL + picturePath;
                                Picasso.get().load(url).into(imageView);
                            }
                        }catch (Exception e){
                            Toast.makeText(ProductDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse: "+ error.getMessage());
                    }
                });

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void addToCart(View view) {

        View dialogView = getLayoutInflater().inflate(R.layout.cart_view,null);
        EditText editText = dialogView.findViewById(R.id.quantityField);
        FitButton button = dialogView.findViewById(R.id.fitButton);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView)
                .create();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    int quantity = Integer.parseInt(editText.getText().toString());
                    addToCart(quantity);
                }catch (Exception e){}
            }
        });

        dialog.show();
    }

    public void addToCart(int quantity){
        String url = App.URL+"add_to_cart.php?username="+MainUser.getCurrentUser()+"&product_id="+pId+"&quantity="+quantity;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(ProductDetailsActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                        if (response.toLowerCase().contains("success")){
                            startActivity(new Intent(ProductDetailsActivity.this,CartActivity.class));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ProductDetailsActivity.this, "Something went wrong please try again", Toast.LENGTH_SHORT).show();
                    }
                });
        
        Volley.newRequestQueue(this).add(request);
    }
}
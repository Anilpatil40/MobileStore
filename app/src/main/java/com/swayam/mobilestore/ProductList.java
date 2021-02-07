package com.swayam.mobilestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.nikartm.button.FitButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductList extends AppCompatActivity {
    private static final String TAG = "ProductList";
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    RecyclerViewAdapter adapter = new RecyclerViewAdapter();
    private String brandName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        ButterKnife.bind(this);

        brandName = getIntent().getStringExtra("brand");
        toolbar.setTitle(brandName);

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void selectedItem(int id) {
                Intent intent = new Intent(ProductList.this,ProductDetailsActivity.class);
                intent.putExtra("id",id);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        String url = App.PROJECT_URL + "products_by_category.php?category="+brandName;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                int id = response.getJSONObject(i).getInt("id");
                                String model = response.getJSONObject(i).getString("model");
                                double price = response.getJSONObject(i).getDouble("price");
                                String picturePath = response.getJSONObject(i).getString("picture_path");
                                Product product = new Product(id,brandName,price,model,null,picturePath);
                                adapter.addProduct(product);
                            }
                        }catch (Exception e){
                            Toast.makeText(ProductList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.Holder>{
        ArrayList<Product> products = new ArrayList<>();
        private OnItemClickListener onItemClickListener;

        public void addProduct(Product product){
            products.add(product);
            notifyItemInserted(products.size()-1);
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_view,parent,false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Product product = products.get(position);
            int id = product.getId();
            holder.brandName.setText(product.getCategory());
            holder.modelName.setText(product.getModel());
            holder.priceValue.setText("Rs. "+product.getPrice());
            String url = App.PROJECT_IMAGES_URL+product.getImagePath();
            Picasso.get().load(url).into(holder.imageView);
            Log.i(TAG, "onBindViewHolder: "+holder.itemView.getTag());
            holder.fitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.selectedItem(id);
                }
            });

        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class Holder extends RecyclerView.ViewHolder{
            @BindView(R.id.imageView) ImageView imageView;
            @BindView(R.id.brandName) TextView brandName;
            @BindView(R.id.modelName) TextView modelName;
            @BindView(R.id.priceValue) TextView priceValue;
            @BindView(R.id.fitButton) FitButton fitButton;

            public Holder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        interface OnItemClickListener{
            void selectedItem(int id);
        }
    }
}
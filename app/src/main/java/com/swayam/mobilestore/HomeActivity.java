package com.swayam.mobilestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "CustomAnnotation";
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    String url = App.PROJECT_URL+"product_categories.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        adapter = new RecyclerViewAdapter();

        toolbar.setTitle(MainUser.getCurrentUser());
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void selectedItem(String brand) {
                Intent intent = new Intent(HomeActivity.this,ProductList.class);
                intent.putExtra("brand",brand);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                adapter.addBrand(response.getJSONObject(i).getString("category"));
                            }
                        }catch (Exception e){
                            Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        Volley.newRequestQueue(this).add(request);
    }

    public void logOut(MenuItem item){
        startActivity(new Intent(this,LoginActivity.class));
        MainUser.logout();
        finish();
    }

    public void showCart(MenuItem item){
        startActivity(new Intent(this,CartActivity.class));
    }

    static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.Holder>{
        private ArrayList<String> brands = new ArrayList<>();
        private OnItemClickListener onItemClickListener;

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.brand_item_view,parent,false);
            return new Holder(view);
        }

        public void addBrand(String brand){
            brands.add(brand);
            notifyItemInserted(brands.size()-1);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            String brand = brands.get(position);
            holder.brandName.setText(brand);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.selectedItem(brand);
                }
            });

            String url = App.PROJECT_LOGOS_URL + brand +".png";
            Picasso.get().load(url).into(holder.brandLogo);
        }

        @Override
        public int getItemCount() {
            return brands.size();
        }

        class Holder extends RecyclerView.ViewHolder{
            @BindView(R.id.brandName) TextView brandName;
            @BindView(R.id.brandLogo) ImageView brandLogo;

            public Holder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        interface OnItemClickListener{
            void selectedItem(String brand);
        }
    }
}
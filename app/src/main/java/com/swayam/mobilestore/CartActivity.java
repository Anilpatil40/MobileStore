package com.swayam.mobilestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.nikartm.button.FitButton;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.random.customdialog.CustomDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CartActivity extends AppCompatActivity {
    private static final String TAG = "CartActivity";
    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @BindView(R.id.totalPrice) TextView totalPrice;
    private RecyclerViewAdapter adapter = new RecyclerViewAdapter();
    private PayPalConfiguration configuration;
    private String paymentDesc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);

        adapter.setOnDeleteButtonClicked(new RecyclerViewAdapter.OnDeleteButtonClicked() {
            @Override
            public void deletedProduct(int productId) {
                deleteFromCart(productId);
            }
        });
        recyclerView.setAdapter(adapter);

        refreshPage();

        configuration = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(App.PAYPAL_CLIENT_ID);

        Intent payPalIntentService = new Intent(this,PayPalService.class);
        payPalIntentService.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,configuration);
        startService(payPalIntentService);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void refreshPage(){
        adapter.clear();

        String url = App.PROJECT_URL + "products_on_cart.php?username=" + MainUser.getCurrentUser();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            paymentDesc = "";
                            double totalAmount = 0;
                            for (int i = 0; i < response.length(); i++) {
                                int id = response.getJSONObject(i).getInt("id");
                                String category = response.getJSONObject(i).getString("category");
                                String model = response.getJSONObject(i).getString("sub_category");
                                double price = response.getJSONObject(i).getDouble("price");
                                String picturePath = response.getJSONObject(i).getString("picture_path");
                                int quantity = response.getJSONObject(i).getInt("quantity");
                                Product product = new Product(id,category,price,model,null,picturePath);
                                product.addQuantity(quantity);
                                adapter.addProduct(product);

                                paymentDesc += "," + model;

                                totalAmount = totalAmount + (price * quantity);
                            }
                            totalPrice.setText(""+totalAmount);
                        }catch (Exception e){
                            Toast.makeText(CartActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void deleteFromCart(int productId) {
        adapter.clear();

        String url = App.PROJECT_URL + "delete_from_cart.php?username=" + MainUser.getCurrentUser() + "&product_id="+productId;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(CartActivity.this, response, Toast.LENGTH_SHORT).show();
                        refreshPage();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        Volley.newRequestQueue(this).add(request);
    }

    public void proceedToPay(View view) {
        double price = Double.parseDouble(totalPrice.getText().toString());
        if(price == 0)
            return;

        PayPalPayment payPalPayment = new PayPalPayment(BigDecimal.valueOf(price),"USD",
                paymentDesc.substring(1,paymentDesc.length()),PayPalPayment.PAYMENT_INTENT_SALE);
        Intent payPalPaymentIntent = new Intent(this, PaymentActivity.class);
        payPalPaymentIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,configuration);
        payPalPaymentIntent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        startActivityForResult(payPalPaymentIntent,1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000){
            if (resultCode == RESULT_OK){
                PaymentConfirmation confirmation = (PaymentConfirmation) data.getExtras().
                        get("com.paypal.android.sdk.paymentConfirmation");
                order(confirmation);
            }else {
                showError("Something went wrong please try again");
            }
        }
    }

    private void order(PaymentConfirmation confirmation){
        String state = confirmation.getProofOfPayment().getState();
        Log.i(TAG, "order: "+state);
        if (!state.equals("approved")){
            showError("Something went wrong");
            return;
        }
        try {
            String username = MainUser.getCurrentUser();
            String transaction_id = confirmation.getProofOfPayment().getTransactionId();
            String created_date = confirmation.getProofOfPayment().getCreateTime();
            JSONObject payment = confirmation.getPayment().toJSONObject();
            double price = payment.getDouble("amount");
            String short_desc = payment.getString("short_description");

            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("please wait...");
            dialog.show();

            String url = App.PROJECT_URL + "order.php?username="+username+"&transaction_id="+transaction_id+
                    "&created_date="+created_date+"&price="+price+"&short_desc="+short_desc;
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            dialog.dismiss();
                            if (response.equals("success")){
                                refreshPage();
                                showSuccess("Thank You for using our product");
                            }else {
                                showError(response);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            showError("Something went wrong");
                        }
                    });

            Volley.newRequestQueue(CartActivity.this).add(request);
        }catch (Exception e){
            showError(e.getMessage());
        }
    }

    public void showError(String message){
        CustomDialog dialog = new CustomDialog(this,CustomDialog.FAILURE);
        dialog.setTitle("FAILED");
        dialog.setContentText(message);
        dialog.show();
    }

    public void showSuccess(String message){
        CustomDialog dialog = new CustomDialog(this,CustomDialog.SUCCESS);
        dialog.setTitle("SUCCESS");
        dialog.setContentText(message);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,PayPalService.class));
    }

    static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.Holder>{
        private ArrayList<Product> products = new ArrayList<>();
        private OnDeleteButtonClicked onDeleteButtonClicked;

        public void addProduct(Product product){
            products.add(product);
            notifyItemInserted(products.size()-1);
        }

        public void clear(){
            products = new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_view,parent,false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Product product = products.get(position);
            holder.productId.setText(""+product.getId());
            holder.modelName.setText(product.getModel());
            holder.priceValue.setText("Rs. "+product.getPrice());
            holder.quantityValue.setText(""+product.getQuantity());

            String url = App.PROJECT_IMAGES_URL + product.getImagePath();
            Picasso.get().load(url).into(holder.imageView);

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDeleteButtonClicked != null)
                        onDeleteButtonClicked.deletedProduct(product.getId());
                }
            });
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class Holder extends RecyclerView.ViewHolder{
            @BindView(R.id.productId) TextView productId;
            @BindView(R.id.modelName) TextView modelName;
            @BindView(R.id.priceValue) TextView priceValue;
            @BindView(R.id.quantityValue) TextView quantityValue;
            @BindView(R.id.imageView) ImageView imageView;
            @BindView(R.id.deleteButton) FitButton deleteButton;

            public Holder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }
        }

        public void setOnDeleteButtonClicked(OnDeleteButtonClicked onDeleteButtonClicked) {
            this.onDeleteButtonClicked = onDeleteButtonClicked;
        }

        interface OnDeleteButtonClicked{
            void deletedProduct(int productId);
        }
    }
}
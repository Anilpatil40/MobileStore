package com.swayam.mobilestore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.username) EditText usernameField;
    @BindView(R.id.password) EditText passwordField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (MainUser.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
            finish();
        }
    }

    public void logIn(View view) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        if (username.equals("") || password.equals("")){
            return;
        }

        String url = App.URL + "login.php?username="+username+"&password="+password;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null)
                            return;
                        if (response.contains("success")){
                            String user = response.replaceAll(".*&username=([^&.]*)&.*","$1");
                            MainUser.login(user);
                            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                            finish();
                        }else {
                            Toast.makeText(LoginActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        Volley.newRequestQueue(this).add(request);
    }

    public void signUp(View view) {
        startActivity(new Intent(this,RegisterActivity.class));
    }


}
package com.swayam.mobilestore;

import android.app.Application;
import android.widget.TextView;

public class App extends Application {
    public static final String URL = "http://192.168.0.101:80/website/";
    public static final String PAYPAL_CLIENT_ID = "ATSP7Uem54Ib_nuK4o3vO1GO3SwkqHhw1WMuFBUYCGCyjSJ2deCxe39acfr37y24-CaFwH3R-ucYP4Kz";

    TextView textView;

    @Override
    public void onCreate() {
        super.onCreate();
        MainUser.initAccount(this);
    }
}

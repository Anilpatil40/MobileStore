package com.swayam.mobilestore;

import android.app.Application;
import android.widget.TextView;

public class App extends Application {
    public static final String PROJECT_URL = "https://swayam-apps.000webhostapp.com/mobilestore/";
    public static final String PROJECT_IMAGES_URL = PROJECT_URL + "images/";
    public static final String PROJECT_LOGOS_URL = PROJECT_URL + "logos/";
    public static final String PAYPAL_CLIENT_ID = "ATSP7Uem54Ib_nuK4o3vO1GO3SwkqHhw1WMuFBUYCGCyjSJ2deCxe39acfr37y24-CaFwH3R-ucYP4Kz";

    @Override
    public void onCreate() {
        super.onCreate();
        MainUser.initAccount(this);
    }
}

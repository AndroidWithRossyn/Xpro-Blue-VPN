package com.webihostapp.xprofreevpnapp.activities;

import static com.webihostapp.xprofreevpnapp.utils.AdsUtility.id;
import static com.webihostapp.xprofreevpnapp.utils.BillConfig.IN_PURCHASE_KEY;
import static com.webihostapp.xprofreevpnapp.utils.BillConfig.One_Month_Sub;
import static com.webihostapp.xprofreevpnapp.utils.BillConfig.One_Year_Sub;
import static com.webihostapp.xprofreevpnapp.utils.BillConfig.PRIMIUM_STATE;
import static com.webihostapp.xprofreevpnapp.utils.BillConfig.Six_Month_Sub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryPurchasesParams;
import com.webihostapp.xprofreevpnapp.BuildConfig;
import com.webihostapp.xprofreevpnapp.MainApp;
import com.webihostapp.xprofreevpnapp.Preference;
import com.webihostapp.xprofreevpnapp.R;
import com.webihostapp.xprofreevpnapp.utils.AdsUtility;
import com.webihostapp.xprofreevpnapp.utils.AppOpenManagerTwo;
import com.webihostapp.xprofreevpnapp.utils.BillConfig;
import com.webihostapp.xprofreevpnapp.utils.NetworkStateUtility;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    Preference preference;
    BillingClient billingClient;

    public String SKU_DELAROY_MONTHLY;
    public String SKU_DELAROY_SIXMONTH;
    public String SKU_DELAROY_YEARLY;
    public String base64EncodedPublicKey;
    private String mDelaroySku;

    boolean mSubscribedToDelaroy = false;


    boolean isSubRestored = false;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        preference = new Preference(SplashActivity.this);
        preference.setStringpreference(IN_PURCHASE_KEY, BuildConfig.IN_APPKEY);
        preference.setStringpreference(One_Month_Sub, BuildConfig.MONTHLY);
        preference.setStringpreference(Six_Month_Sub, BuildConfig.SIX_MONTH);
        preference.setStringpreference(One_Year_Sub, BuildConfig.YEARLY);


        base64EncodedPublicKey = preference.getStringpreference(IN_PURCHASE_KEY, base64EncodedPublicKey);
        SKU_DELAROY_MONTHLY = preference.getStringpreference(One_Month_Sub, SKU_DELAROY_MONTHLY);
        SKU_DELAROY_SIXMONTH = preference.getStringpreference(Six_Month_Sub, SKU_DELAROY_SIXMONTH);
        SKU_DELAROY_YEARLY = preference.getStringpreference(One_Year_Sub, SKU_DELAROY_YEARLY);


        Log.d("TAGJSON", "onResponse: ");


        unlockdata();
        if (NetworkStateUtility.isOnline(this)) {
            checkSubscription();
            getApiData();
            //setUpBilling();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.network_error))
                    .setMessage(getString(R.string.network_error_message))
                    .setNegativeButton(getString(R.string.ok),
                            (dialog, id) -> {
                                dialog.cancel();
                                onBackPressed();
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }


    }

    private void unlockdata() {
        preference.setBooleanpreference(PRIMIUM_STATE, mSubscribedToDelaroy);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    void checkSubscription() {

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {
        }).build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (billingResult1, list) -> {
                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    Log.e("TAG", list.size() + " size");
                                    if (list.size() > 0) {
                                        preference.setBooleanpreference(PRIMIUM_STATE, true);
                                        int i = 0;
                                        for (Purchase purchase : list) {
                                            //Here you can manage each product, if you have multiple subscription
                                            Log.d("testOffer", purchase.getOriginalJson()); // Get to see the order information
                                            Log.d("testOffer", " index" + i);
                                            i++;
                                        }
                                    } else {
                                        preference.setBooleanpreference(PRIMIUM_STATE, false);
                                    }

                                    getApiData();

                                }
                            });

                }

            }
        });
    }


    private void getApiData() {

        MainApp.getAppInstance().initHydraSdk();
        AdsUtility.id="touchvpn";
        //Log.d("TAGJSON", "id ");
        AdsUtility.admobAppOpenId = "ca-app-pub-3940256099942544/3419835294";
        AdsUtility.admobBannerId = "ca-app-pub-3940256099942544/6300978111";
        AdsUtility.admobInterstitialId = "ca-app-pub-3940256099942544/1033173712";
        AdsUtility.admobNativeId = "ca-app-pub-3940256099942544/2247696110";

        Preference preference = new Preference(SplashActivity.this);
        if (!preference.isBooleenPreference(BillConfig.PRIMIUM_STATE)) {
            AdsUtility.initAdmob(SplashActivity.this);
            AppOpenManagerTwo.fetchAd(SplashActivity.this, new AdsUtility.AdFinished() {
                @Override
                public void onAdFinished() {
                    Intent myIntent = new Intent(getApplicationContext(), GetStartedActivity.class);
                    startActivity(myIntent);
                    finish();
                }
            });
        } else {
            Intent myIntent = new Intent(getApplicationContext(), GetStartedActivity.class);
            startActivity(myIntent);
            finish();
        }

    }
}






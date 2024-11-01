package com.webihostapp.xprofreevpnapp.activities;

import static com.webihostapp.xprofreevpnapp.utils.BillConfig.PRIMIUM_STATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.webihostapp.xprofreevpnapp.BuildConfig;
import com.webihostapp.xprofreevpnapp.Preference;
import com.webihostapp.xprofreevpnapp.R;
import com.webihostapp.xprofreevpnapp.databinding.ActivityNewUiBinding;
import com.webihostapp.xprofreevpnapp.utils.AdsUtility;
import com.webihostapp.xprofreevpnapp.utils.BillConfig;
import com.webihostapp.xprofreevpnapp.utils.Converter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;
import java.util.Map;

import unified.vpn.sdk.Callback;
import unified.vpn.sdk.RemainingTraffic;
import unified.vpn.sdk.UnifiedSdk;
import unified.vpn.sdk.VpnException;
import unified.vpn.sdk.VpnState;


public abstract class UIActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    protected static final String TAG = MainActivity.class.getSimpleName();


    private static InterstitialAd mInterstitialAd;

    Toolbar toolbar;
    Preference preference;
    boolean mSubscribedToDelaroy = false;
    boolean connected = false;
    String mDelaroySku = "";
    boolean mAutoRenewEnabled = false;

    DrawerLayout navDrawer;
    private NavigationView navigationView;
    int clickPosition;
    ImageView btnshare;
    ImageView ivfaq, ivShare, ivPrivacy, ivRate;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    final Runnable mUIUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
            checkRemainingTraffic();
            mUIHandler.postDelayed(mUIUpdateRunnable, 10000);
        }
    };


    protected abstract void isLoggedIn(Callback<Boolean> callback);

    protected abstract void loginToVpn();

    protected abstract void isConnected(Callback<Boolean> callback);

    protected abstract void connectToVpn();

    protected abstract void disconnectFromVnp();

    protected abstract void chooseServer();

    protected abstract void getCurrentServer(Callback<String> callback);

    protected abstract void checkRemainingTraffic();

    void complain(String message) {
        alert("Error: " + message);
    }

    void alert(String message) {
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    private void unlockdata() {


        if (!preference.isBooleenPreference(PRIMIUM_STATE)) {
            binding.premium.setVisibility(View.VISIBLE);


            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_prem).setVisible(true);

        } else {
            binding.premium.setVisibility(View.GONE);


            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_prem).setVisible(false);
        }


        MobileAds.initialize(this, initializationStatus -> {
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                Log.d("MyApp", String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status.getDescription(), status.getLatency()));
            }
            AdsUtility.loadAdmobBanner(this, findViewById(R.id.adView));

        });


    }

    public void unlock() {
        preference.setBooleanpreference(PRIMIUM_STATE, true);
    }


    ActivityNewUiBinding binding;

    ImageView img_connect;
    TextView connectionStateTextView;


    TextView selectedServerTextView;

    ImageView country_flag;
    ImageView premium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loginToVpn();
        img_connect = findViewById(R.id.img_connect);
        connectionStateTextView = findViewById(R.id.connection_state);
        selectedServerTextView = findViewById(R.id.selected_server);
        country_flag = findViewById(R.id.country_flag);
        premium = findViewById(R.id.premium);

        ivfaq = findViewById(R.id.imgfaq);
        ivShare = findViewById(R.id.imgshare);
        ivPrivacy = findViewById(R.id.imgprivacy);
        ivRate = findViewById(R.id.imgrate);

        btnshare = findViewById(R.id.share_us_tv);
        btnshare.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                // If the navigation drawer is not open then open it, if its already open then close it.
                if (!navDrawer.isDrawerOpen(GravityCompat.START))
                    navDrawer.openDrawer(Gravity.START);
                else navDrawer.closeDrawer(Gravity.END);
            }
        });

        ivfaq.setOnClickListener(view -> startActivity(new Intent(UIActivity.this, Faq.class)));

        ivShare.setOnClickListener(view -> {
            Intent ishare = new Intent(Intent.ACTION_SEND);
            ishare.setType("text/plain");
            String sAux = "\n" + getResources().getString(R.string.app_name) + "\n\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=" + getApplication().getPackageName();
            ishare.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(ishare, "choose one"));
        });
        ivPrivacy.setOnClickListener(view -> {
            Uri uri = Uri.parse(getResources().getString(R.string.privacy_policy_link)); // missing 'http://' will cause crashed
            Intent intent_policy = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent_policy);
        });
        ivRate.setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));

        });

        navDrawer = findViewById(R.id.activity_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(UIActivity.this, navDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navDrawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);


        premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UIActivity.this, GetPremiumActivity.class);
                startActivity(intent);

            }
        });

        preference = new Preference(this);
        if (BuildConfig.USE_IN_APP_PURCHASE) {
            unlockdata();
        } else {
            preference.setBooleanpreference(PRIMIUM_STATE, false);
            binding.premium.setVisibility(View.GONE);


            MobileAds.initialize(this, initializationStatus -> {
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }
                AdsUtility.loadAdmobBanner(this, findViewById(R.id.adView));
                //AdsUtility.loadNativeAd(UIActivity.this, nativeAdLayout, R.layout.native_ad_layout);

            });


        }

        binding.premium.setOnClickListener(v -> {
            premiumMenu();
        });

        binding.imgConnect.setOnClickListener(v -> {
            onConnectBtnClick();
        });

        binding.optimalServerBtn.setOnClickListener(v -> {
            onServerChooserClick();
        });


    }


    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isConnected(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    startUIUpdateTask();
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUIUpdateTask();
    }


    public void premiumMenu() {
        startActivity(new Intent(this, GetPremiumActivity.class));
    }


    public void onConnectBtnClick() {
        isConnected(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    disconnectFromVnp();
                } else {
                    connectToVpn();
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
            }
        });
    }


    public void onServerChooserClick() {
        chooseServer();
    }


    protected void startUIUpdateTask() {
        stopUIUpdateTask();
        mUIHandler.post(mUIUpdateRunnable);
    }

    protected void stopUIUpdateTask() {
        mUIHandler.removeCallbacks(mUIUpdateRunnable);
        updateUI();
    }


    protected void updateUI() {
        UnifiedSdk.getVpnState(new Callback<VpnState>() {
            @Override
            public void success(@NonNull VpnState vpnState) {
                switch (vpnState) {
                    case IDLE: {

                        binding.connectionState.setText("Disconnected");
                        binding.imgConnect.setVisibility(View.VISIBLE);
                        if (connected) {
                            connected = false;
                        }

                        binding.imgConnect.setImageResource(R.drawable.disconnectedbtn);
                        binding.countryFlag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                        binding.selectedServer.setText(R.string.select_country);
                        ChangeBlockVisibility();
                        binding.uploadingSpeed.setText("0.0 Kb");
                        binding.downloadingSpeed.setText("0.0 Kb");
                        binding.animationView.pauseAnimation();
                        binding.animationView.setVisibility(View.INVISIBLE);


                        hideConnectProgress();
                        break;
                    }
                    case CONNECTED: {
                        binding.imgConnect.setVisibility(View.VISIBLE);
                        binding.imgConnect.setImageResource(R.drawable.connected_btn);
                        if (!connected) {
                            connected = true;
                        }
                        binding.animationView.pauseAnimation();
                        binding.animationView.setVisibility(View.INVISIBLE);

                        binding.connectionState.setText("Connected");
                        hideConnectProgress();
                        break;
                    }
                    case CONNECTING_VPN:
                    case CONNECTING_CREDENTIALS:
                    case CONNECTING_PERMISSIONS: {
                        binding.connectionState.setText("Connecting");
                        ChangeBlockVisibility();
                        binding.animationView.playAnimation();
                        binding.animationView.setVisibility(View.VISIBLE);
                        binding.imgConnect.setVisibility(View.INVISIBLE);
                        binding.countryFlag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                        binding.selectedServer.setText(R.string.select_country);
                        showConnectProgress();
                        break;
                    }
                    case PAUSED: {
                        ChangeBlockVisibility();
                        binding.countryFlag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                        binding.selectedServer.setText(R.string.select_country);
                        break;
                    }
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
                binding.countryFlag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                binding.selectedServer.setText(R.string.select_country);
            }
        });
        getCurrentServer(new Callback<String>() {
            @Override
            public void success(@NonNull final String currentServer) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.countryFlag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                        binding.selectedServer.setText(R.string.select_country);
                        if (!currentServer.equals("")) {
                            Locale locale = new Locale("", currentServer);
                            Resources resources = getResources();
                            String sb = "drawable/" + currentServer.toLowerCase();
                            binding.countryFlag.setImageResource(resources.getIdentifier(sb, null, getPackageName()));
                            binding.selectedServer.setText(locale.getDisplayCountry());
                        } else {
                            binding.countryFlag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                            binding.selectedServer.setText(R.string.select_country);
                        }
                    }
                });
            }

            @Override
            public void failure(@NonNull VpnException e) {
                binding.countryFlag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                binding.selectedServer.setText(R.string.select_country);
            }
        });
    }

    private void ChangeBlockVisibility() {
        if (BuildConfig.USE_IN_APP_PURCHASE) {
            if (preference.isBooleenPreference(PRIMIUM_STATE)) {
                binding.premium.setVisibility(View.GONE);
            } else {
                binding.premium.setVisibility(View.VISIBLE);
            }
        } else {
            binding.premium.setVisibility(View.GONE);
        }
    }

    private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {


        int fadeInDuration = 500;
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(images[imageIndex]);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);

        animation.setRepeatCount(1);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (images.length - 1 > imageIndex) {
                    animate(imageView, images, imageIndex + 1, forever); //Calls itself until it gets to the end of the array
                } else {
                    if (forever) {
                        animate(imageView, images, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
    }


    protected void updateTrafficStats(long outBytes, long inBytes) {
        String outString = Converter.humanReadableByteCountOld(outBytes, false);
        String inString = Converter.humanReadableByteCountOld(inBytes, false);

        binding.uploadingSpeed.setText(inString);
        binding.downloadingSpeed.setText(outString);

    }

    protected void updateRemainingTraffic(RemainingTraffic remainingTrafficResponse) {
        if (remainingTrafficResponse.isUnlimited()) {

        } else {
            String trafficUsed = Converter.megabyteCount(remainingTrafficResponse.getTrafficUsed()) + "Mb";
            String trafficLimit = Converter.megabyteCount(remainingTrafficResponse.getTrafficLimit()) + "Mb";

        }
    }

    //protected void ShowIPaddera(String ipaddress) {
    //    server_ip.setText(ipaddress);
    //  }


    protected void showConnectProgress() {

    }

    protected void hideConnectProgress() {

    }

    protected void showMessage(String msg) {
        Toast.makeText(UIActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


/*
    public void LoadBannerAd() {
        RelativeLayout adContainer = findViewById(R.id.adView);
        if (BuildConfig.GOOGlE_AD) {
            AdMod.buildAdBanner(getApplicationContext(), adContainer, 0, new AdMod.MyAdListener() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onFaildToLoad(int i) {
                }
            });
        }
    }
*/

    private void LoadInterstitialAd() {
        if (BuildConfig.GOOGlE_AD) {
            Preference preference = new Preference(UIActivity.this);
            if (!preference.isBooleenPreference(BillConfig.PRIMIUM_STATE)) {
                AdRequest adRequest = new AdRequest.Builder().build();
                InterstitialAd.load(this, AdsUtility.admobInterstitialId, adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.e(TAG, "onAdLoaded");
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.e("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.e("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                mInterstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_prem) {
            Intent intent = new Intent(UIActivity.this, GetPremiumActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_contact_us) {
            Intent intent1 = new Intent(Intent.ACTION_SENDTO);
            intent1.setData(Uri.parse("mailto:"));
            intent1.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.Email)});
            intent1.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));


            try {
                startActivity(Intent.createChooser(intent1, getString(R.string.sendmail)));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, getResources().getString(R.string.nomailappfound) + "", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(this, getResources().getString(R.string.unexpected) + "", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_rate) {
            rateUsmain();
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=" + getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        }
        navDrawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onClick(View view) {

    }

    private void rateUsmain() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));

    }


}
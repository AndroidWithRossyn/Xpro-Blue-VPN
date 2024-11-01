package com.webihostapp.xprofreevpnapp.activities;

import android.annotation.SuppressLint;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.webihostapp.xprofreevpnapp.dialog.CountryData;
import com.webihostapp.xprofreevpnapp.dialog.LoginDialog;
import com.webihostapp.xprofreevpnapp.utils.AdsUtility;
import com.webihostapp.xprofreevpnapp.MainApp;
import com.webihostapp.xprofreevpnapp.R;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import unified.vpn.sdk.AuthMethod;
import unified.vpn.sdk.Callback;
import unified.vpn.sdk.CompletableCallback;
import unified.vpn.sdk.Country;
import unified.vpn.sdk.HydraTransport;
import unified.vpn.sdk.HydraVpnTransportException;
import unified.vpn.sdk.NetworkRelatedException;
import unified.vpn.sdk.PartnerApiException;
import unified.vpn.sdk.RemainingTraffic;
import unified.vpn.sdk.SessionConfig;
import unified.vpn.sdk.SessionInfo;
import unified.vpn.sdk.TrackingConstants;
import unified.vpn.sdk.TrafficListener;
import unified.vpn.sdk.TrafficRule;
import unified.vpn.sdk.UnifiedSdk;
import unified.vpn.sdk.User;
import unified.vpn.sdk.VpnException;
import unified.vpn.sdk.VpnPermissionDeniedException;
import unified.vpn.sdk.VpnPermissionRevokedException;
import unified.vpn.sdk.VpnState;
import unified.vpn.sdk.VpnStateListener;
import unified.vpn.sdk.VpnTransportException;

import static com.webihostapp.xprofreevpnapp.utils.BillConfig.BUNDLE;
import static com.webihostapp.xprofreevpnapp.utils.BillConfig.COUNTRY_DATA;
import static com.webihostapp.xprofreevpnapp.utils.BillConfig.SELECTED_COUNTRY;
import static unified.vpn.sdk.OpenVpnTransport.TRANSPORT_ID_TCP;
import static unified.vpn.sdk.OpenVpnTransport.TRANSPORT_ID_UDP;

public class MainActivity extends UIActivity implements TrafficListener, VpnStateListener, LoginDialog.LoginConfirmationInterface {

    private static com.google.android.gms.ads.interstitial.InterstitialAd mInterstitialAd;

    private String selectedCountry = "";
    private String ServerIPaddress = "00.000.000.00";
    @SuppressLint("NonConstantResourceId")


    @Override
    protected void onStart() {
        super.onStart();
        UnifiedSdk.addTrafficListener(this);
        UnifiedSdk.addVpnStateListener(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        UnifiedSdk.removeVpnStateListener(this);
        UnifiedSdk.removeTrafficListener(this);
    }

    @Override
    public void onTrafficUpdate(long bytesTx, long bytesRx) {
        updateUI();
        updateTrafficStats(bytesTx, bytesRx);
    }

    @Override
    public void vpnStateChanged(@NonNull VpnState vpnState) {
        updateUI();
    }



    @Override
    public void vpnError(@NonNull VpnException e) {
        updateUI();
        handleError(e);
    }

    @Override
    protected void isLoggedIn(Callback<Boolean> callback) {
        UnifiedSdk.getInstance().getBackend().isLoggedIn(callback);
    }

    @Override
    protected void loginToVpn() {
        Log.e(TAG, "loginToVpn: 1111");
        AuthMethod authMethod = AuthMethod.anonymous();
        UnifiedSdk.getInstance().getBackend().login(authMethod, new Callback<User>() {
            @Override
            public void success(@NonNull User user) {
                updateUI();
            }

            @Override
            public void failure(@NonNull VpnException e) {
                updateUI();
                handleError(e);
            }
        });
    }

    @Override
    protected void isConnected(Callback<Boolean> callback) {
        UnifiedSdk.getVpnState(new Callback<VpnState>() {
            @Override
            public void success(@NonNull VpnState vpnState) {
                callback.success(vpnState == VpnState.CONNECTED);
            }

            @Override
            public void failure(@NonNull VpnException e) {
                callback.success(false);
            }
        });
    }

    @Override
    protected void connectToVpn() {
        isLoggedIn(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    List<String> fallbackOrder = new ArrayList<>();
//                    fallbackOrder.add(HydraTransport.TRANSPORT_ID);
                    fallbackOrder.add(TRANSPORT_ID_TCP);
                    fallbackOrder.add(TRANSPORT_ID_UDP);
                    showConnectProgress();
                    List<String> bypassDomains = new LinkedList<>();
                    bypassDomains.add("*facebook.com");
                    bypassDomains.add("*wtfismyip.com");
                    UnifiedSdk.getInstance().getVpn().start(new SessionConfig.Builder()
                            .withReason(TrackingConstants.GprReasons.M_UI)
                            .withTransportFallback(fallbackOrder)
                            .withVirtualLocation(selectedCountry)
                            .withTransport(HydraTransport.TRANSPORT_ID)
                            .addDnsRule(TrafficRule.Builder.bypass().fromDomains(bypassDomains))
                            .build(), new CompletableCallback() {
                        @Override
                        public void complete() {


                            AdsUtility.showInterAds(MainActivity.this, new AdsUtility.AdFinished() {
                                @Override
                                public void onAdFinished() {
                                    hideConnectProgress();
                                    startUIUpdateTask();
                                }
                            });



                        }

                        @Override
                        public void error(@NonNull VpnException e) {
                            hideConnectProgress();
                            updateUI();
                            handleError(e);
                        }
                    });
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
            }
        });
    }


    @Override
    protected void disconnectFromVnp() {
        showConnectProgress();
        UnifiedSdk.getInstance().getVpn().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
            @Override
            public void complete() {
                AdsUtility.showInterAds(MainActivity.this, new AdsUtility.AdFinished() {
                    @Override
                    public void onAdFinished() {
                        hideConnectProgress();
                        stopUIUpdateTask();
                    }
                });
                //LoadInterstitialAd();
            }

            @Override
            public void error(@NonNull VpnException e) {
                hideConnectProgress();
                updateUI();
                handleError(e);

            }
        });
    }

    @Override
    protected void chooseServer() {
        isLoggedIn(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    AdsUtility.showInterAds(MainActivity.this, new AdsUtility.AdFinished() {
                        @Override
                        public void onAdFinished() {
                            startActivityForResult(new Intent(MainActivity.this, ServerActivity.class), 3000);
                        }
                    });
                } else {
                    showMessage("Please Wait 5 second");
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3000) {
            if (resultCode == RESULT_OK) {
                Gson gson = new Gson();
                Bundle args = data.getBundleExtra(BUNDLE);
                CountryData item = gson.fromJson(args.getString(COUNTRY_DATA), CountryData.class);
                onRegionSelected(item);
                connectToVpn();
            }
        }
    }

    @Override
    protected void getCurrentServer(final Callback<String> callback) {
        UnifiedSdk.getVpnState(new Callback<VpnState>() {
            @Override
            public void success(@NonNull VpnState state) {
                if (state == VpnState.CONNECTED) {
                    UnifiedSdk.getStatus(new Callback<SessionInfo>() {
                        @Override
                        public void success(@NonNull SessionInfo sessionInfo) {
                            ServerIPaddress = sessionInfo.getCredentials().getServers().get(0).getAddress();
                            //  ShowIPaddera(ServerIPaddress);
                            callback.success(sessionInfo.getCredentials().getServers().get(0).getCountry());



                        }

                        @Override
                        public void failure(@NonNull VpnException e) {
                            callback.success(selectedCountry);
                        }
                    });
                } else {
                    callback.success(selectedCountry);
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
                callback.failure(e);
            }
        });
    }

    @Override
    protected void checkRemainingTraffic() {
        UnifiedSdk.getInstance().getBackend().remainingTraffic(new Callback<RemainingTraffic>() {
            @Override
            public void success(@NonNull RemainingTraffic remainingTraffic) {
                updateRemainingTraffic(remainingTraffic);
            }

            @Override
            public void failure(@NonNull VpnException e) {
                updateUI();
                handleError(e);
            }
        });
    }

    @Override
    public void setLoginParams(String hostUrl, String carrierId) {
        ((MainApp) getApplication()).setNewHostAndCarrier(hostUrl, carrierId);
    }

    @Override
    public void loginUser() {
        loginToVpn();
    }

    public void onRegionSelected(CountryData item) {

        final Country new_countryValue = item.getCountryvalue();
        if (!item.isPro()) {
            selectedCountry = new_countryValue.getCountry();
            preference.setStringpreference(SELECTED_COUNTRY, selectedCountry);
            Toast.makeText(this, "Click to Connect VPN", Toast.LENGTH_SHORT).show();
            updateUI();
            UnifiedSdk.getVpnState(new Callback<VpnState>() {
                @Override
                public void success(@NonNull VpnState state) {
                    if (state == VpnState.CONNECTED) {
                        showMessage("Reconnecting to VPN with " + selectedCountry);
                        UnifiedSdk.getInstance().getVpn().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
                            @Override
                            public void complete() {
                                connectToVpn();
                            }

                            @Override
                            public void error(@NonNull VpnException e) {
                                // In this case we try to reconnect
                                selectedCountry = "";
                                preference.setStringpreference(SELECTED_COUNTRY, selectedCountry);
                                connectToVpn();
                            }
                        });
                    }
                }

                @Override
                public void failure(@NonNull VpnException e) {
                }
            });
        } else {
            Intent intent = new Intent(MainActivity.this, GetPremiumActivity.class);
            startActivity(intent);
        }
    }

    public void handleError(Throwable e) {
        if (e instanceof NetworkRelatedException) {
            showMessage("Check internet connection");
        } else if (e instanceof VpnException) {
            if (e instanceof VpnPermissionRevokedException) {
                showMessage("User revoked vpn permissions");
            } else if (e instanceof VpnPermissionDeniedException) {
                showMessage("User canceled to grant vpn permissions");
            } else if (e instanceof VpnTransportException) {
                HydraVpnTransportException hydraVpnTransportException = (HydraVpnTransportException) e;
                if (hydraVpnTransportException.getCode() == HydraVpnTransportException.HYDRA_ERROR_BROKEN) {
                    showMessage("Connection with vpn server was lost");
                } else if (hydraVpnTransportException.getCode() == HydraVpnTransportException.HYDRA_DCN_BLOCKED_BW) {
                    showMessage("Client traffic exceeded");
                } else {
                    showMessage("Error in VPN transport");
                }
            } else {
                Log.e(TAG, "Error in VPN Service ");
            }
        } else if (e instanceof PartnerApiException) {
            switch (((PartnerApiException) e).getContent()) {
                case PartnerApiException.CODE_NOT_AUTHORIZED:
                    showMessage("User unauthorized");
                    break;
                case PartnerApiException.CODE_TRAFFIC_EXCEED:
                    showMessage("Server unavailable");
                    break;
                default:
                    showMessage("Other error. Check PartnerApiException constants");
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Are you sure you want to leave the application?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}

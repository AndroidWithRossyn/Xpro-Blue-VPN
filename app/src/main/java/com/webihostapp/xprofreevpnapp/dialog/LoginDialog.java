package com.webihostapp.xprofreevpnapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.webihostapp.xprofreevpnapp.utils.AdsUtility;
import com.webihostapp.xprofreevpnapp.BuildConfig;
import com.webihostapp.xprofreevpnapp.MainApp;
import com.webihostapp.xprofreevpnapp.R;


public class LoginDialog extends DialogFragment {

    EditText hostUrlEditText;
    EditText carrierIdEditText;
    TextView login_btn;

    private LoginConfirmationInterface loginConfirmationInterface;

    View view;

    public LoginDialog() {
    }

    public static LoginDialog newInstance() {
        LoginDialog frag = new LoginDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_login, container);
        hostUrlEditText = view.findViewById(R.id.host_url_ed);
        carrierIdEditText = view.findViewById(R.id.carrier_id_ed);
        login_btn = view.findViewById(R.id.login_btn);

        login_btn.setOnClickListener(v -> onLoginBtnClick());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SharedPreferences prefs = ((MainApp) getActivity().getApplication()).getPrefs();

        hostUrlEditText.setText(prefs.getString(BuildConfig.STORED_HOST_URL_KEY, BuildConfig.BASE_HOST));
        carrierIdEditText.setText(prefs.getString(BuildConfig.STORED_CARRIER_ID_KEY, AdsUtility.id));

        hostUrlEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        if (ctx instanceof LoginConfirmationInterface) {
            loginConfirmationInterface = (LoginConfirmationInterface) ctx;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginConfirmationInterface = null;
    }


    void onLoginBtnClick() {
        String hostUrl = hostUrlEditText.getText().toString();
        if (hostUrl.equals("")) hostUrl = BuildConfig.BASE_HOST;
        String carrierId = carrierIdEditText.getText().toString();
        if (carrierId.equals("")) carrierId = AdsUtility.id;
        loginConfirmationInterface.setLoginParams(hostUrl, carrierId);
        loginConfirmationInterface.loginUser();
        dismiss();
    }

    public interface LoginConfirmationInterface {
        void setLoginParams(String hostUrl, String carrierId);

        void loginUser();
    }
}

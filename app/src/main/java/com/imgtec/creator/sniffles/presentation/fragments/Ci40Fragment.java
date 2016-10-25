package com.imgtec.creator.sniffles.presentation.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.Preferences;
import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.data.api.jsonrpc.JsonRPCApiService;
import com.imgtec.creator.sniffles.presentation.ActivityComponent;
import com.imgtec.creator.sniffles.presentation.MainActivity;
import com.imgtec.creator.sniffles.presentation.helpers.ToolbarHelper;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;


import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;


public class Ci40Fragment extends BaseFragment {

  private static final String ARG_IP_ADDR = "ip_addr";
  private static final String ARG_USER_NAME = "user_name";
  private static final String ARG_PASSWORD = "password";

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private String ipAddr;
  private String userName;
  private String password;

  @BindView(R.id.provisioning) AppCompatButton provision;
  @BindView(R.id.unprovision) AppCompatButton unprovision;
  @BindView(R.id.ip) TextView ip;
  @BindView(R.id.isProvisioned) TextView isProvisioned;

  @Inject @Named("Main") Handler mainHandler;
  @Inject Preferences prefs;
  @Inject ToolbarHelper toolbarHelper;
  @Inject JsonRPCApiService jsonRpc;

  public Ci40Fragment() {
    // Required empty public constructor
  }

  public static Ci40Fragment newInstance(String ipAddr, String userName, String password) {
    Ci40Fragment fragment = new Ci40Fragment();
    Bundle args = new Bundle();
    args.putString(ARG_IP_ADDR, ipAddr);
    args.putString(ARG_USER_NAME, userName);
    args.putString(ARG_PASSWORD, password);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      ipAddr = getArguments().getString(ARG_IP_ADDR);
      userName = getArguments().getString(ARG_USER_NAME);
      password = getArguments().getString(ARG_PASSWORD);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_ci40, container, false);
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  @Override
  protected void setupToolbar() {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }
    actionBar.show();
    actionBar.setTitle(R.string.ci_40);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);
    actionBar.setShowHideAnimationEnabled(true);
    setHasOptionsMenu(false);
  }

  @Override
  public void setupDrawer() {
    drawerHelper.getDrawerToggle().setDrawerIndicatorEnabled(false);
    drawerHelper.getDrawerToggle().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
    drawerHelper.getDrawerToggle().setToolbarNavigationClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getFragmentManager().popBackStack();
        drawerHelper.getDrawerToggle().syncState();
      }
    });
    drawerHelper.lockDrawer();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    ip.setText(ipAddr);
  }

  @Override
  public void onResume() {
    super.onResume();
    requestIfProvisioned();
  }

  private void requestIfProvisioned() {
    toolbarHelper.showProgress();
    jsonRpc.isProvisioned(ipAddr, userName, password, new IsProvisionedCallback(this));
  }

  @OnClick(R.id.provisioning)
  void onProvisioning() {
    provision.setEnabled(false);
    toolbarHelper.showProgress();

    final String key = prefs.getKey();
    final String secret = prefs.getSecret();
    jsonRpc.provision(ipAddr, userName, password, key, secret, new JsonRpcCallback(this));
  }

  @OnClick(R.id.unprovision)
  void onUnprovision() {
    //TODO: implement
    toolbarHelper.hideProgress();
  }

  private void notifyOnboardingSuccess(final String ipAddress) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
      toolbarHelper.hideProgress();
        provision.setEnabled(true);
        Toast.makeText(getContext(), "Provisioning successful!", Toast.LENGTH_LONG).show();
      }
    });
    requestIfProvisioned();
  }

  private void notifyOnboardingFailure(final Throwable t) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        toolbarHelper.hideProgress();
        provision.setEnabled(true);
        Toast.makeText(getContext(), "Provisioning failed! " + t.getMessage(), Toast.LENGTH_LONG).show();
      }
    });
  }


  private void onIsProvisioned(final Boolean result) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        toolbarHelper.hideProgress();
        isProvisioned.setText(result ? "[YES]" : "[NO]");
      }
    });
  }


  private void onIsProvisionedFailure(Throwable t) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        toolbarHelper.hideProgress();
        isProvisioned.setTextColor(Color.RED);
        isProvisioned.setText("[FAILED]");
      }
    });
  }

  private static class IsProvisionedCallback implements ApiCallback<JsonRPCApiService, Boolean> {

    private final WeakReference<Ci40Fragment> fragment;

    public IsProvisionedCallback(Ci40Fragment fragment) {
      this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void onSuccess(JsonRPCApiService service, Boolean result) {
      Ci40Fragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.onIsProvisioned(result);
      }
    }

    @Override
    public void onFailure(JsonRPCApiService service, Throwable t) {
      Ci40Fragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.onIsProvisionedFailure(t);
      }
    }
  }



  private static class JsonRpcCallback implements ApiCallback<JsonRPCApiService,String> {

    private final WeakReference<Ci40Fragment> fragment;

    public JsonRpcCallback(Ci40Fragment fragment) {
      this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void onSuccess(JsonRPCApiService service, String ipAddress) {

      Ci40Fragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.notifyOnboardingSuccess(ipAddress);
      }
    }

    @Override
    public void onFailure(JsonRPCApiService service, Throwable t) {
      Ci40Fragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.notifyOnboardingFailure(t);
      }
    }
  }
}

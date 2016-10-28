package com.imgtec.creator.sniffles.presentation.fragments;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.Preferences;
import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.data.api.jsonrpc.JsonRPCApiService;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.RpcInfo;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.Wireless;
import com.imgtec.creator.sniffles.presentation.ActivityComponent;
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

  private static final String UNKNOWN = "[UNKNOWN]";
  private static final String ARG_IP_ADDR = "ip_addr";
  private static final String ARG_USER_NAME = "user_name";
  private static final String ARG_PASSWORD = "password";

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private String ipAddr;
  private String userName;
  private String password;

  @BindView(R.id.startConfiguration) AppCompatButton startConfiguration;
  @BindView(R.id.removeConfiguration) AppCompatButton removeConfiguration;
  @BindView(R.id.ip) TextView ip;
  @BindView(R.id.isConfigured) TextView isConfigured;
  @BindView(R.id.host) TextView host;
  @BindView(R.id.ssid) TextView ssid;
  @BindView(R.id.mac_addr) TextView mac;

  @Inject @Named("Main") Handler mainHandler;
  @Inject Preferences prefs;
  @Inject ToolbarHelper toolbarHelper;
  @Inject JsonRPCApiService jsonRpc;
  private AlertDialog clientNameDialog;

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
    ip.setText(String.format("[%s]", ipAddr));
  }

  @Override
  public void onResume() {
    super.onResume();
    requestIfProvisioned();
  }

  @Override
  public void onPause() {
    if (clientNameDialog != null) {
      clientNameDialog.dismiss();
      clientNameDialog = null;
    }
    super.onPause();
  }

  private void requestIfProvisioned() {
    toolbarHelper.showProgress();
    jsonRpc.isConfigured(ipAddr, userName, password, new IsProvisionedCallback(this));
    jsonRpc.requestInfo(ipAddr, userName, password, new BoardDataCallback(this));
  }

  @OnClick(R.id.startConfiguration)
  void onStartConfiguration() {
    showSetNameDialog();
  }

  private void showSetNameDialog() {
    final View dialogView = getLayoutInflater(null).inflate(R.layout.client_name_dialog, null);
    final EditText name = (EditText) dialogView.findViewById(R.id.name);
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
    builder
        .setTitle(R.string.enter_client_name)
        .setView(dialogView)
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            clientNameDialog = null;
          }
        })
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {

            dialog.dismiss();
            clientNameDialog = null;

            setButtonsEnabled(false);
            toolbarHelper.showProgress();

            final String key = prefs.getKey();
            final String secret = prefs.getSecret();
            final String clientName = name.getText().toString();

            jsonRpc.onboarding(ipAddr, userName, password, key, secret, clientName,
                new JsonRpcCallback(Ci40Fragment.this));
          }
        });

    clientNameDialog = builder.create();
    clientNameDialog.show();
  }

  private void setButtonsEnabled(boolean enabled) {
    startConfiguration.setEnabled(enabled);
    removeConfiguration.setEnabled(enabled);
  }

  @OnClick(R.id.removeConfiguration)
  void onRemoveConfiguration() {
    jsonRpc.removeConfiguration(ipAddr, userName, password, new RemoveConfigurationCallback(Ci40Fragment.this));
    toolbarHelper.hideProgress();
  }

  private void onIsProvisioned(final Boolean result) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        toolbarHelper.hideProgress();
        isConfigured.setText(result ? "[YES]" : "[NO]");
      }
    });
  }


  private void onIsProvisionedFailure(Throwable t) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        toolbarHelper.hideProgress();
        isConfigured.setTextColor(Color.RED);
        isConfigured.setText("[FAILED]");
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

  static class RemoveConfigurationCallback implements ApiCallback<JsonRPCApiService,Boolean> {

    private final WeakReference<Ci40Fragment> fragment;

    public RemoveConfigurationCallback(Ci40Fragment fragment) {
      super();
      this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void onSuccess(JsonRPCApiService service, Boolean result) {
      Ci40Fragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.notifyRemoveConfigurationFinished(result);
      }
    }

    @Override
    public void onFailure(JsonRPCApiService service, Throwable t) {
      Ci40Fragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.notifyRemoveConfigurationFailed(t);
      }
    }
  }

  static class BoardDataCallback implements ApiCallback<JsonRPCApiService,RpcInfo> {

    private final WeakReference<Ci40Fragment> fragment;

    public BoardDataCallback(Ci40Fragment fragment) {
      super();
      this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void onSuccess(JsonRPCApiService service, RpcInfo result) {
      Ci40Fragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.notifyDataReceived(result);
      }
    }

    @Override
    public void onFailure(JsonRPCApiService service, Throwable t) {
      Ci40Fragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.notifyBoardDataFailed(t);
      }
    }
  }

  private void notifyDataReceived(final RpcInfo result) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        if (result == null) {
          return;
        }
        host.setText(String.format("[%s]", (result.getHost() != null) ? result.getHost() : UNKNOWN));
        final Wireless w = result.getWireless();
        if (w != null) {
          ssid.setText(String.format("[%s]", (w.getSsid() != null) ? w.getSsid() : UNKNOWN));
          mac.setText(String.format("[%s]", (w.getMacaddr() != null) ? w.getMacaddr() : UNKNOWN));
        }
      }
    });
  }

  private void notifyBoardDataFailed(final Throwable t) {
    notifyOperationFinished("Getting board data failed! " + t.getMessage());
  }

  private void notifyOnboardingSuccess(final String ipAddress) {
    notifyOperationFinished("Onboarding successful!");
    requestIfProvisioned();
  }

  private void notifyOnboardingFailure(final Throwable t) {
    notifyOperationFinished("Onboarding failed! " + t.getMessage());
  }

  private void notifyRemoveConfigurationFinished(Boolean result) {
    notifyOperationFinished("Configuration removed!");
    requestIfProvisioned();
  }

  private void notifyRemoveConfigurationFailed(Throwable t) {
    notifyOperationFinished("Failed to remove configuration!" + t.getMessage());
  }

  private void notifyOperationFinished(final String message) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        toolbarHelper.hideProgress();
        setButtonsEnabled(true);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
      }
    });
  }
}

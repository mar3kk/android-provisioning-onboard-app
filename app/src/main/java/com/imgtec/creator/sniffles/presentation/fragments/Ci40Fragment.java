/*
 * <b>Copyright (c) 2016, Imagination Technologies Limited and/or its affiliated group companies
 *  and/or licensors. </b>
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are permitted
 *  provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *      and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *      conditions and the following disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors may be used to
 *      endorse or promote products derived from this software without specific prior written
 *      permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 *  WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

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
import com.imgtec.creator.sniffles.data.api.jsonrpc.JsonRPCApiService;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.RpcInfo;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.Wireless;
import com.imgtec.creator.sniffles.presentation.ActivityComponent;
import com.imgtec.creator.sniffles.presentation.helpers.ToolbarHelper;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    requestInfo();
  }

  @Override
  public void onPause() {
    if (clientNameDialog != null) {
      clientNameDialog.dismiss();
      clientNameDialog = null;
    }
    super.onPause();
  }

  private void requestInfo() {
    toolbarHelper.showProgress();
    jsonRpc.isConfigured(ipAddr, userName, password, new IsProvisionedCallback(this, mainHandler));
    jsonRpc.requestInfo(ipAddr, userName, password, new BoardDataCallback(this, mainHandler));
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
                new JsonRpcCallback(Ci40Fragment.this, mainHandler));
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
    jsonRpc.removeConfiguration(ipAddr, userName, password, new RemoveConfigurationCallback(Ci40Fragment.this, mainHandler));
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


  private static class IsProvisionedCallback extends AbstractCallback<Ci40Fragment, JsonRPCApiService, Boolean> {

    public IsProvisionedCallback(Ci40Fragment fragment, Handler mainHandler) {
      super(fragment, mainHandler);
    }

    @Override
    protected void onSuccess(Ci40Fragment fragment, JsonRPCApiService service, Boolean result) {
      fragment.onIsProvisioned(result);
    }

    @Override
    protected void onFailure(Ci40Fragment fragment, JsonRPCApiService service, Throwable t) {
      fragment.onIsProvisionedFailure(t);
    }
  }

  private static class JsonRpcCallback extends AbstractCallback<Ci40Fragment, JsonRPCApiService,String> {

    public JsonRpcCallback(Ci40Fragment fragment, Handler mainHandler) {
      super(fragment, mainHandler);
    }

    @Override
    public void onSuccess(Ci40Fragment fragment, JsonRPCApiService service, String ipAddress) {

      fragment.notifyOnboardingSuccess(ipAddress);
    }

    @Override
    public void onFailure(Ci40Fragment fragment, JsonRPCApiService service, Throwable t) {

      fragment.notifyOnboardingFailure(t);
    }
  }

  static class RemoveConfigurationCallback extends AbstractCallback<Ci40Fragment, JsonRPCApiService, Boolean> {

    public RemoveConfigurationCallback(Ci40Fragment fragment, Handler mainHandler) {
      super(fragment, mainHandler);
    }

    @Override
    public void onSuccess(Ci40Fragment fragment, JsonRPCApiService service, Boolean result) {

      fragment.notifyRemoveConfigurationFinished(result);
    }

    @Override
    public void onFailure(Ci40Fragment fragment, JsonRPCApiService service, Throwable t) {

      fragment.notifyRemoveConfigurationFailed(t);
    }
  }

  static class BoardDataCallback extends AbstractCallback<Ci40Fragment, JsonRPCApiService,RpcInfo> {

    public BoardDataCallback(Ci40Fragment fragment, Handler mainHandler) {
      super(fragment, mainHandler);
    }

    @Override
    public void onSuccess(Ci40Fragment fragment, JsonRPCApiService service, RpcInfo result) {

      fragment.notifyDataReceived(result);
    }

    @Override
    public void onFailure(Ci40Fragment fragment, JsonRPCApiService service, Throwable t) {

      fragment.notifyBoardDataFailed(t);
    }
  }

  private void notifyDataReceived(final RpcInfo result) {
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

  private void notifyBoardDataFailed(final Throwable t) {
    notifyOperationFinished("Getting board data failed! " + t.getMessage());
  }

  private void notifyOnboardingSuccess(final String ipAddress) {
    notifyOperationFinished("Onboarding successful!");
    requestInfo();
  }

  private void notifyOnboardingFailure(final Throwable t) {
    notifyOperationFinished("Onboarding failed! " + t.getMessage());
  }

  private void notifyRemoveConfigurationFinished(Boolean result) {
    notifyOperationFinished("Configuration removed!");
    requestInfo();
  }

  private void notifyRemoveConfigurationFailed(Throwable t) {
    notifyOperationFinished("Failed to remove configuration!" + t.getMessage());
  }

  private void notifyOperationFinished(final String message) {

    toolbarHelper.hideProgress();
    setButtonsEnabled(true);
    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
  }
}

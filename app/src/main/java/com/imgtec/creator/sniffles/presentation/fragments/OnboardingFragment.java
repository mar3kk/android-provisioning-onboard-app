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


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.NsdService;
import com.imgtec.creator.sniffles.data.NsdServiceImpl;
import com.imgtec.creator.sniffles.data.Preferences;
import com.imgtec.creator.sniffles.data.ServiceInfo;
import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.data.api.jsonrpc.JsonRPCApiService;
import com.imgtec.creator.sniffles.presentation.ActivityComponent;
import com.imgtec.creator.sniffles.presentation.adapters.DiscoveredServicesAdapter;
import com.imgtec.creator.sniffles.presentation.helpers.FragmentHelper;
import com.imgtec.creator.sniffles.presentation.helpers.ToolbarHelper;
import com.imgtec.creator.sniffles.presentation.views.HorizontalItemDecoration;
import com.imgtec.creator.sniffles.presentation.views.RecyclerItemClickSupport;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;

public class OnboardingFragment extends BaseFragment {

  final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject @Named("Main") Handler mainHandler;
  @Inject Preferences prefs;
  @Inject NsdService nsdService;
  @Inject JsonRPCApiService jsonRpc;
  @Inject ToolbarHelper toolbarHelper;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  private DiscoveredServicesAdapter adapter;

  private AlertDialog rpcLoginDialog;
  private ProgressDialog progressDialog;

  private String userName;
  private String password;

  public OnboardingFragment() {
    // Required empty public constructor
  }

  public static OnboardingFragment newInstance() {
    return new OnboardingFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_onboarding, container, false);
  }

  @Override
  protected void setupToolbar() {
    super.setupToolbar();
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }
    actionBar.show();
    actionBar.setTitle(R.string.onboarding);
    actionBar.setDisplayHomeAsUpEnabled(false);
    actionBar.setHomeButtonEnabled(true);
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupList();
    nsdService.discoverServices();
  }

  private void setupList() {
    final DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
    itemAnimator.setAddDuration(300);
    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new HorizontalItemDecoration(getActivity()));
    recyclerView.setHasFixedSize(true);
    recyclerView.setItemAnimator(itemAnimator);

    adapter = new DiscoveredServicesAdapter();
    recyclerView.setAdapter(adapter);

    RecyclerItemClickSupport.addTo(recyclerView)
        .setOnItemClickListener(new RecyclerItemClickSupport.OnItemClickListener() {
          @Override
          public void onItemClicked(RecyclerView recyclerView, int position, View view) {

            logger.debug("Starting provisioning for board with id: {}", adapter.getItem(position));
            showRpcLoginDialog(adapter.getItem(position).getIpAddress());
          }
        });
  }

  @Override
  public void onResume() {
    super.onResume();
    if (nsdService != null) {
      nsdService.addDiscoveryServiceListener(discoveryListener);
    }
  }

  @Override
  public void onPause() {
    super.onPause();

    toolbarHelper.hideProgress();

    if (nsdService != null) {
      nsdService.removeDiscoveryServiceListener(discoveryListener);
    }
    if (rpcLoginDialog != null) {
      rpcLoginDialog.dismiss();
      rpcLoginDialog = null;
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (nsdService != null) {
      nsdService.stopDiscovery();
    }
  }

  private void showRpcLoginDialog(final String ipAddress) {
    LayoutInflater inflater = getLayoutInflater(null);
    final View dialogView = inflater.inflate(R.layout.rpc_login_dialog, null);
    final EditText username = (EditText) dialogView.findViewById(R.id.username);
    final EditText passwd = (EditText) dialogView.findViewById(R.id.password);

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
    builder
        .setTitle(R.string.enter_credentials)
        .setView(dialogView)
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            rpcLoginDialog = null;
          }
        })
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {

            userName = username.getText().toString();
            password = passwd.getText().toString();

            hideSoftKeyboard(passwd);

            dialog.dismiss();
            rpcLoginDialog = null;

            showProgressDialog();
            jsonRpc.authorize(ipAddress, userName, password,
                new JsonRpcAuthCallback(OnboardingFragment.this, mainHandler));

          }
        });

    rpcLoginDialog = builder.create();
    rpcLoginDialog.show();

  }

  private void hideSoftKeyboard(View view) {

    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

  }

  private void showProgressDialog() {
    if (progressDialog != null) {
      hideProgressDialog();
    }
    final String message = getActivity().getResources().getString(R.string.logging_in);
    progressDialog = ProgressDialog.show(getActivity(),
        getActivity().getString(R.string.please_wait_with_dots), message, true);
    progressDialog.setCanceledOnTouchOutside(false);
  }

  private void hideProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
      progressDialog = null;
    }
  }

  private NsdService.DiscoveryServiceListener discoveryListener =
      new NsdService.DiscoveryServiceListener() {

        @Override
        public void onDiscoveryStarted(NsdService service, String serviceType) {
          toolbarHelper.showProgress();
        }

        @Override
        public void onDiscoveryFailed(NsdServiceImpl service) {
          toolbarHelper.hideProgress();
        }

        @Override
        public void onDiscoveryFinished(NsdService service, String serviceType) {
          toolbarHelper.hideProgress();
        }

        @Override
        public void onServiceFound(NsdService service, final ServiceInfo serviceInfo) {
          mainHandler.post(new Runnable() {
            @Override
            public void run() {
              int pos = adapter.getPosition(serviceInfo);
              if (pos < 0) {
                adapter.add(serviceInfo);
                adapter.notifyItemInserted(adapter.getPosition(serviceInfo));
              }
            }
          });
        }

        @Override
        public void onServiceLost(final NsdService service, final ServiceInfo serviceInfo,
                                  final int errorCode) {
          mainHandler.post(new Runnable() {
            @Override
            public void run() {
              int pos = adapter.getPosition(serviceInfo);
              if (pos > -1) {
                adapter.remove(pos);
                adapter.notifyItemRemoved(pos);
              }
            }
          });
        }
      };

  private void showToast(String msg) {
    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
  }

  private void loadNextFragment(String ipAddr, String username, String password) {

    Ci40Fragment fragment = Ci40Fragment.newInstance(ipAddr, username, password);
    FragmentHelper.replaceFragment(getActivity().getSupportFragmentManager(),  fragment);
  }

  private static class JsonRpcAuthCallback implements JsonRPCApiService.AuthorizationCallback {

    private final WeakReference<OnboardingFragment> fragment;
    private final Handler mainHandler;

    public JsonRpcAuthCallback(OnboardingFragment fragment, Handler mainHandler) {
      this.fragment = new WeakReference<>(fragment);
      this.mainHandler = mainHandler;
    }

    @Override
    public void onSuccess(final String ipAddr, final String username,
                          final String password, final String result) {
      if (result != null) {
        mainHandler.post(new Runnable() {
          @Override
          public void run() {
            OnboardingFragment f = fragment.get();
            if (f != null && f.isAdded()) {
              f.hideProgressDialog();
              f.loadNextFragment(ipAddr, username, password);
            }
          }
        });
      }
    }

    @Override
    public void onFailure(final Throwable t) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          OnboardingFragment f = fragment.get();
          if (f != null && f.isAdded()) {
            f.hideProgressDialog();
            f.showToast("Json-RPC login failed! " + t.getMessage());
          }
        }
      });
    }
  }
}

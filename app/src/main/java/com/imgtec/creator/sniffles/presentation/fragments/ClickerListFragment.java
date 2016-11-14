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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.data.api.jsonrpc.JsonRPCApiService;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.Clicker;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.JsonRPCResponse;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.ProvisioningDaemonState;
import com.imgtec.creator.sniffles.presentation.ActivityComponent;
import com.imgtec.creator.sniffles.presentation.adapters.ClickersAdapter;
import com.imgtec.creator.sniffles.presentation.views.RecyclerItemClickSupport;
import com.imgtec.di.HasComponent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;

public class ClickerListFragment extends BaseFragment implements ClickersAdapter.ClickersAdapterListener {

  private static final String ARG_IP_ADR = "ip";
  private static final String ARG_USERNAME = "username";
  private static final String ARG_PASSWORD = "password";

  private String ipAddr;
  private String username;
  private String password;

  private ProgressDialog progressDialog;

  @Inject JsonRPCApiService jsonRpc;
  @Inject @Named("Main") Handler mainHandler;
  @BindView(R.id.clicker_list_rv) RecyclerView recyclerView;
  ScheduledExecutorService executor;
  private ClickersAdapter adapter;


  public static ClickerListFragment newInstance(String ipAddr, String username, String password) {

    Bundle args = new Bundle();
    args.putString(ARG_IP_ADR,ipAddr);
    args.putString(ARG_USERNAME, username);
    args.putString(ARG_PASSWORD, password);

    ClickerListFragment fragment = new ClickerListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_clicker_list, container, false);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    ipAddr = args.getString(ARG_IP_ADR);
    username = args.getString(ARG_USERNAME);
    password = args.getString(ARG_PASSWORD);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    adapter = new ClickersAdapter(getContext(), this);
    recyclerView.setAdapter(adapter);
    RecyclerItemClickSupport.addTo(recyclerView)
        .setOnItemClickListener(new RecyclerItemClickSupport.OnItemClickListener() {
          @Override
          public void onItemClicked(RecyclerView recyclerView, int position, View view) {
            requestSelectClicker(adapter.getItem(position).getClickerID());
          }
        });
  }

  @Override
  public void onResume() {
    super.onResume();
    executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        requestProvisioningDaemonState();
      }
    }, 0, 5, TimeUnit.SECONDS);
  }

  @Override
  public void onPause() {
    executor.shutdownNow();
    hideProgressDialog();
    super.onPause();
  }

  @Override
  public void onStartProvisioningClicked(Clicker clicker) {
    requestStartProvisioning();
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  private void showProgressDialog() {
    if (progressDialog != null) {
      if (progressDialog.isShowing()) {
        return;
      }
    }
    final String message = getActivity().getResources().getString(R.string.logging_in);
    progressDialog = ProgressDialog.show(getContext(),
        getActivity().getString(R.string.please_wait_with_dots), message, true);
    progressDialog.setCancelable(false);
  }

  protected void hideProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
      progressDialog = null;
    }
  }

  private void requestProvisioningDaemonState() {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        jsonRpc.getProvisioningDaemonState(ipAddr, username, password, provisioningDaemonStateCallback);
      }
    });
  }

  private void requestSelectClicker(final int clickerID) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        showProgressDialog();
        jsonRpc.selectClicker(ipAddr, username, password, clickerID, selectClickerCallback);
      }
    });
  }

  private void requestStartProvisioning() {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        showProgressDialog();
        jsonRpc.startProvisioning(ipAddr, username, password, startProvisioningCallback);
      }
    });
  }



  private ApiCallback<JsonRPCApiService, JsonRPCResponse<ProvisioningDaemonState>> provisioningDaemonStateCallback = new ApiCallback<JsonRPCApiService, JsonRPCResponse<ProvisioningDaemonState>>() {

    @Override
    public void onSuccess(JsonRPCApiService service, final JsonRPCResponse<ProvisioningDaemonState> response) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          hideProgressDialog();
          if (response.getResult() != null) {
            adapter.clear();
            adapter.addAll(response.getResult().getClickers());
            adapter.notifyDataSetChanged();
          } else {
            if (response.getError().getCode() == 4) {
              jsonRpc.startProvisioningDaemon(ipAddr, username, password, startProvisioningDaemonCallback);
            } else {
              showToast(response.getError().getMessage());
            }
          }
        }
      });
    }

    @Override
    public void onFailure(JsonRPCApiService service, final Throwable t) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          hideProgressDialog();
          showToast(String.format("Failed to execute provisioningDaemonState method. %s", t.getMessage()));
        }
      });
    }
  };

  private ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>> startProvisioningDaemonCallback = new ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>>() {

    @Override
    public void onSuccess(JsonRPCApiService service, final JsonRPCResponse<Boolean> response) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          if (response.getResult() != null && response.getResult()) {
            jsonRpc.getProvisioningDaemonState(ipAddr, username, password, provisioningDaemonStateCallback);
          } else {
            hideProgressDialog();
            showToast("Couldn't start provisioning daemon on Ci40.");
          }
        }
      });
    }

    @Override
    public void onFailure(JsonRPCApiService service, final Throwable t) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          hideProgressDialog();
          showToast(String.format("Failed to execute startProvisioningDaemon method. %s", t.getMessage()));
        }
      });
    }
  };

  private ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>> startProvisioningCallback = new ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>>() {

    @Override
    public void onSuccess(JsonRPCApiService service, final JsonRPCResponse<Boolean> result) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          if (result.getResult() != null && result.getResult()) {
            jsonRpc.getProvisioningDaemonState(ipAddr, username, password, provisioningDaemonStateCallback);
          } else {
            showToast(result.getError().getMessage());
          }
        }
      });
    }

    @Override
    public void onFailure(JsonRPCApiService service, final Throwable t) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          hideProgressDialog();
          showToast(String.format("Failed to execute startProvisioning method. %s", t.getMessage()));
        }
      });

    }
  };

  private ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>> selectClickerCallback = new ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>>() {

    @Override
    public void onSuccess(JsonRPCApiService service, final JsonRPCResponse<Boolean> response) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          if (response.getResult() != null && response.getResult()) {
            jsonRpc.getProvisioningDaemonState(ipAddr, username, password, provisioningDaemonStateCallback);
          } else {
            hideProgressDialog();
            showToast(response.getError().getMessage());
          }

        }
      });
    }

    @Override
    public void onFailure(JsonRPCApiService service, final Throwable t) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          hideProgressDialog();
          showToast(String.format("Failed to execute selectClicker method. %s", t.getMessage()));
        }
      });

    }
  };

  private void showToast(String msg) {
    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
  }



}

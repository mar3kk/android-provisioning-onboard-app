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


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.NsdService;
import com.imgtec.creator.sniffles.data.NsdServiceImpl;
import com.imgtec.creator.sniffles.data.Preferences;
import com.imgtec.creator.sniffles.data.ServiceInfo;
import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.data.api.jsonrpc.JsonRPCApiService;
import com.imgtec.creator.sniffles.presentation.ActivityComponent;
import com.imgtec.creator.sniffles.presentation.MainActivity;
import com.imgtec.creator.sniffles.presentation.adapters.DiscoveredServicesAdapter;
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

            final String key = ((MainActivity) getActivity()).getKey();
            final String secret = ((MainActivity) getActivity()).getSecret();
            logger.debug("Starting provisioning for board with id: {}", adapter.getItem(position));
            jsonRpc.execute(adapter.getItem(position).getIpAddress(), key, secret,
                new JsonRpcCallback(OnboardingFragment.this));
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
    if (nsdService != null) {
      nsdService.removeDiscoveryServiceListener(discoveryListener);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (nsdService != null) {
      nsdService.stopDiscovery();
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
        public void onServiceLost(NsdService service, ServiceInfo serviceInfo, int errorCode) {

        }
      };

  private void notifyOnboardingSuccess(final String ipAddress) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {

        int index = -1;
        for (int i = 0; i < adapter.getItemCount(); ++i) {
          ServiceInfo si = adapter.getItem(i);
          if (si.getIpAddress().equals(ipAddress)) {
            index = i;
            break;
          }
        }

        if (index > -1) {
          adapter.getItem(index).setProvisioned(true);
          adapter.notifyItemChanged(index);
        }
      }
    });
  }

  private void notifyOnboardingFailure(Throwable t) {

  }

  private static class JsonRpcCallback implements ApiCallback<JsonRPCApiService,String> {

    private final WeakReference<OnboardingFragment> fragment;

    public JsonRpcCallback(OnboardingFragment fragment) {
      this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void onSuccess(JsonRPCApiService service, String ipAddress) {

      OnboardingFragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.notifyOnboardingSuccess(ipAddress);
      }
    }

    @Override
    public void onFailure(JsonRPCApiService service, Throwable t) {
      OnboardingFragment f = fragment.get();
      if (f != null && f.isAdded()) {
        f.notifyOnboardingFailure(t);
      }
    }
  }
}

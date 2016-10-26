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


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.data.api.deviceserver.DeviceServerApiService;
import com.imgtec.creator.sniffles.data.api.pojo.Client;
import com.imgtec.creator.sniffles.data.api.pojo.Clients;
import com.imgtec.creator.sniffles.presentation.ActivityComponent;
import com.imgtec.creator.sniffles.presentation.adapters.ClientsAdapter;
import com.imgtec.creator.sniffles.presentation.helpers.DrawerHelper;
import com.imgtec.creator.sniffles.presentation.helpers.FragmentHelper;
import com.imgtec.creator.sniffles.presentation.helpers.ToolbarHelper;
import com.imgtec.creator.sniffles.presentation.views.HorizontalItemDecoration;
import com.imgtec.creator.sniffles.presentation.views.RecyclerItemClickSupport;
import com.imgtec.di.HasComponent;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;

/**
 *
 */
public class ClientsFragment extends BaseFragment {

  @BindView(R.id.clients_list) RecyclerView recyclerView;

  @Inject DrawerHelper drawerHelper;
  @Inject ToolbarHelper toolbarHelper;
  @Inject @Named("Main") Handler mainHandler;
  @Inject DeviceServerApiService deviceServerApiService;

  private ClientsAdapter adapter;
  private AlertDialog retryDialog;

  public ClientsFragment() {
    // Required empty public constructor
  }

  public static ClientsFragment newInstance() {

    return new ClientsFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_clients, container, false);
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  @Override
  public void setupToolbar() {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }
    actionBar.show();
    actionBar.setTitle(R.string.clients);
    actionBar.setDisplayHomeAsUpEnabled(false);
    actionBar.setHomeButtonEnabled(true);
    setHasOptionsMenu(true);
  }

  @Override
  protected void setupDrawer() {
    super.setupDrawer();
    drawerHelper.setSelector(0);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    final DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
    itemAnimator.setAddDuration(200);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.addItemDecoration(new HorizontalItemDecoration(getActivity()));
    recyclerView.setHasFixedSize(true);
    recyclerView.setItemAnimator(itemAnimator);

    adapter = new ClientsAdapter();
    recyclerView.setAdapter(adapter);

    RecyclerItemClickSupport.addTo(recyclerView)
        .setOnItemClickListener(new RecyclerItemClickSupport.OnItemClickListener() {
          @Override
          public void onItemClicked(RecyclerView recyclerView, int position, View view) {
            Client client = adapter.getItem(position);
            //TODO: implement detailed view
          }
        });

    requestClients();
  }

  @Override
  public void onPause() {
    if (retryDialog != null) {
      retryDialog.dismiss();
      retryDialog = null;
    }
    super.onPause();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_clients_fragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.action_refresh:
        requestClients();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void requestClients() {
    toolbarHelper.showProgress();
    deviceServerApiService.requestClients(new DeviceServerApiService.Filter<Client>() {
      @Override
      public boolean accept(Client filter) {
        return true;
      }
    }, new ClientsCallback(this, mainHandler));
  }

  private void showRetryDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder
        .setMessage(R.string.no_client_found)
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            retryDialog = null;
          }
        })
        .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            requestClients();
            dialog.dismiss();
            retryDialog = null;
          }
        });

    retryDialog = builder.create();
    retryDialog.show();
  }

  static class ClientsCallback implements ApiCallback<DeviceServerApiService,Clients> {

    private final WeakReference<ClientsFragment> fragment;
    private final Handler mainHandler;

    public ClientsCallback(ClientsFragment fragment, Handler mainHandler) {
      super();
      this.fragment = new WeakReference<>(fragment);
      this.mainHandler = mainHandler;
    }

    @Override
    public void onSuccess(DeviceServerApiService service, final Clients result) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          ClientsFragment f = fragment.get();
          if (f != null && f.isAdded()) {
            f.toolbarHelper.hideProgress();
            if (result.getItems().size() == 0) {
              f.showRetryDialog();
              return;
            }
            f.adapter.clear();
            f.adapter.addAll(result.getItems());
            f.adapter.notifyDataSetChanged();
          }
        }
      });
    }

    @Override
    public void onFailure(DeviceServerApiService service, Throwable t) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          ClientsFragment f = fragment.get();
          if (f != null && f.isAdded()) {
            f.toolbarHelper.hideProgress();
          }
        }
      });
    }
  }
}

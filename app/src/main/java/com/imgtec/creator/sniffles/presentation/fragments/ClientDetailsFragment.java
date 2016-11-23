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
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.api.deviceserver.DeviceServerApiService;
import com.imgtec.creator.sniffles.data.api.pojo.Client;
import com.imgtec.creator.sniffles.data.api.pojo.DeviceInfo;
import com.imgtec.creator.sniffles.presentation.ActivityComponent;
import com.imgtec.creator.sniffles.presentation.adapters.ClientDetailsAdapter;
import com.imgtec.creator.sniffles.presentation.adapters.ClientsAdapter;
import com.imgtec.creator.sniffles.presentation.helpers.FragmentHelper;
import com.imgtec.creator.sniffles.presentation.helpers.UiHelper;
import com.imgtec.creator.sniffles.presentation.views.HorizontalItemDecoration;
import com.imgtec.creator.sniffles.presentation.views.RecyclerItemClickSupport;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;

/**
 *
 */
public class ClientDetailsFragment extends BaseFragment {

  static final String CLIENT = "CLIENT";
  final Logger logger = LoggerFactory.getLogger(getClass());

  @BindView(R.id.client_details_list) RecyclerView recyclerView;

  @Inject DeviceServerApiService deviceServerApiService;
  @Inject @Named("Main") Handler mainHandler;

  ClientDetailsAdapter adapter;
  Client client;
  ProgressDialog progressDialog;

  public ClientDetailsFragment() {
    // Required empty public constructor
  }

  public static ClientDetailsFragment newInstance(Client client) {
    ClientDetailsFragment fragment = new ClientDetailsFragment();
    Bundle args = new Bundle();
    args.putString(CLIENT, serialize(client));
    fragment.setArguments(args);
    return fragment;
  }

  static String serialize(Client client) {
    return new GsonBuilder().create().toJson(client, Client.class);
  }

  static Client deserialize(String clientString) {
    return new GsonBuilder().create().fromJson(clientString, Client.class);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      client = deserialize(getArguments().getString(CLIENT));
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_client_details, container, false);
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
    actionBar.setTitle(client.getName());
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

    final DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
    itemAnimator.setAddDuration(200);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.addItemDecoration(new HorizontalItemDecoration(getActivity()));
    recyclerView.setHasFixedSize(true);
    recyclerView.setItemAnimator(itemAnimator);

    adapter = new ClientDetailsAdapter();
    recyclerView.setAdapter(adapter);
  }

  @Override
  public void onResume() {
    super.onResume();
    requestClientDetails();
    showProgressDialog();
  }

  @Override
  public void onPause() {
    hideProgressDialog();
    super.onPause();
  }

  private void showProgressDialog() {
    hideProgressDialog();
    progressDialog = UiHelper.showProgressDialog(getContext(), getString(R.string.please_wait_with_dots), null, true);
  }

  private void hideProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
      progressDialog = null;
    }
  }

  private void requestClientDetails() {
    logger.debug("Requesting client {} details");
    deviceServerApiService.requestClientDetails(client, new ClientDetailsCallback(this, mainHandler));
  }

  static class ClientDetailsCallback extends AbstractCallback<ClientDetailsFragment,DeviceServerApiService,List<DeviceInfo>> {


    public ClientDetailsCallback(ClientDetailsFragment fragment, Handler mainHandler) throws IllegalArgumentException {
      super(fragment, mainHandler);
    }

    @Override
    protected void onSuccess(ClientDetailsFragment fragment, DeviceServerApiService service, List<DeviceInfo> result) {
      if (result.size() == 0) {
        return;
      }
      final DeviceInfo info = result.get(0);
      List<Pair<String, Object>> pairs = toPair(info);

      fragment.tryAddElement(pairs);
    }

    private List<Pair<String, Object>> toPair(DeviceInfo info) {
      List<Pair<String, Object>> pairs = new ArrayList<>();
      Class clazz = info.getClass();
      Field[] fields = clazz.getDeclaredFields();

      for (Field f: fields) {
        f.setAccessible(true);
        try {
          pairs.add(Pair.create(f.getName(), f.get(info)));
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
      return pairs;
    }

    @Override
    protected void onFailure(ClientDetailsFragment fragment, DeviceServerApiService service, Throwable t) {
      fragment.hideProgressDialog();
      Toast.makeText(fragment.getContext(), "Requesting client details failed! " + t.getMessage(),
          Toast.LENGTH_LONG).show();
    }
  }

  private void tryAddElement(List<Pair<String, Object>> pairs) {
    hideProgressDialog();
    adapter.clear();
    adapter.addAll(pairs);
    adapter.notifyDataSetChanged();
  }
}

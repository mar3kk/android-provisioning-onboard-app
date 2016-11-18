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

package com.imgtec.creator.sniffles.data.api.deviceserver;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.imgtec.creator.sniffles.data.Preferences;
import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.data.api.oauth.OauthManager;
import com.imgtec.creator.sniffles.data.api.pojo.Api;
import com.imgtec.creator.sniffles.data.api.pojo.Client;
import com.imgtec.creator.sniffles.data.api.pojo.Clients;
import com.imgtec.creator.sniffles.data.api.pojo.DeviceInfo;
import com.imgtec.creator.sniffles.data.api.pojo.Instances;
import com.imgtec.creator.sniffles.data.api.pojo.OauthToken;
import com.imgtec.creator.sniffles.data.api.pojo.ObjectType;
import com.imgtec.creator.sniffles.data.api.pojo.ObjectTypes;
import com.imgtec.creator.sniffles.data.api.requests.ClientsRequest;
import com.imgtec.creator.sniffles.data.api.requests.GetRequest;
import com.imgtec.creator.sniffles.data.api.requests.InstancesRequest;
import com.imgtec.creator.sniffles.utils.Condition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 *
 */
public class DeviceServerApiServiceImpl implements DeviceServerApiService {

  final Context appContext;
  final HttpUrl url;
  final OkHttpClient client;
  final OauthManager oauthManager;
  final ExecutorService executorService;
  private final Preferences preferences;

  public DeviceServerApiServiceImpl(Context appContext,
                                    HttpUrl url,
                                    OkHttpClient client,
                                    OauthManager oauthManager,
                                    ExecutorService executorService,
                                    Preferences preferences) {
    super();
    this.appContext = appContext;
    this.url = url;
    this.client = client;
    this.oauthManager = oauthManager;
    this.executorService = executorService;
    this.preferences = preferences;
  }

  @Override
  public void login(@NonNull final String key, @NonNull final String secret, final boolean rememberMe,
                    @NonNull final ApiCallback<DeviceServerApiService, OauthToken> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          oauthManager.authorize(client, key, secret);

          //store settings and notify
          preferences.setKeepMeLoggedIn(rememberMe);
          preferences.setRefreshToken(oauthManager.getOauthToken().getRefreshToken());

          callback.onSuccess(DeviceServerApiServiceImpl.this, oauthManager.getOauthToken());
        }
        catch (Exception e) {
          callback.onFailure(DeviceServerApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public void login(final String refreshToken, final ApiCallback<DeviceServerApiService, OauthToken> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          oauthManager.authorize(client, refreshToken);
          callback.onSuccess(DeviceServerApiServiceImpl.this, oauthManager.getOauthToken());
        }
        catch (Exception e) {
          callback.onFailure(DeviceServerApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public final Clients getClients(Filter<Client> filter) throws IOException {

    Api api = new GetRequest<Api>(url.toString()).execute(client, Api.class);

    //Get clients count
    Clients clients = new ClientsRequest(api.getLinkByRel("clients").getHref())
        .execute(client, Clients.class);

    int clientsCount = clients.getPageInfo().getTotalCount();
    clients = new ClientsRequest(api.getLinkByRel("clients").getHref(), clientsCount)
        .execute(client, Clients.class);


    if (filter != null) {
      List<Client> list = new ArrayList<>();
      for (Client client: clients.getItems()) {
        if (filter.accept(client)) {
          list.add(client);
        }
      }
      clients.setItems(list);
    }

    return clients;
  }

  @Override
  public void requestClients(final Filter<Client> filter,
                             final ApiCallback<DeviceServerApiService, Clients> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Clients clients = getClients(filter);
          callback.onSuccess(DeviceServerApiServiceImpl.this, clients);
        }
        catch (Exception e) {
          callback.onFailure(DeviceServerApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public void requestClientDetails(final Client client,
                                   final ApiCallback<DeviceServerApiService, List<DeviceInfo>> callback) {

    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Condition.checkArgument(client != null, "Client cannot be null");
          Condition.checkArgument(callback != null, "Callback cannot be null");

          ObjectTypes result = getObjectTypes(client, new DeviceServerApiService.Filter<ObjectType>() {

                @Override
                public boolean accept(ObjectType filter) {
                  return filter.getObjectTypeID().equals("3"); //IPSO
                }
              });

          if (result.getItems().size() == 0) {
            callback.onFailure(DeviceServerApiServiceImpl. this,
                new RuntimeException("No ObjectType found matching filtering criteria."));
          }

          List<DeviceInfo> infos = new ArrayList<>();
          for (ObjectType obj : result.getItems()) {  //should be one element
            Instances<DeviceInfo> instances = getInstancesForObject(obj);
            for (int i = 0; i < instances.getItems().size(); ++i) {
              infos.add(instances.getItems().get(i));
            }
          }
          callback.onSuccess(DeviceServerApiServiceImpl.this, infos);
        }
        catch (final Exception e) {
          callback.onFailure(DeviceServerApiServiceImpl. this, e);
        }
      }
    });
  }

  //package:

  final ObjectTypes getObjectTypes(Client client, Filter<ObjectType> filter) throws IOException {

    ObjectTypes objectTypes = new GetRequest<ObjectTypes>(client.getLinkByRel("objecttypes").getHref())
        .execute(DeviceServerApiServiceImpl.this.client, ObjectTypes.class);

    if (filter != null) {
      List<ObjectType> list = new ArrayList<>();
      for (ObjectType obj: objectTypes.getItems()) {
        if (filter.accept(obj)) {
          list.add(obj);
        }
      }
      objectTypes.setItems(list);
    }
    return objectTypes;
  }

  Instances<DeviceInfo> getInstancesForObject(ObjectType obj) throws IOException {
    Instances<DeviceInfo> instances = new InstancesRequest<DeviceInfo>(obj.getLinkByRel("instances").getHref())
        .execute(client, new TypeToken<Instances<DeviceInfo>>(){});
    return instances;
  }
}

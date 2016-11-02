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

package com.imgtec.creator.sniffles.app;

import android.content.SharedPreferences;
import android.os.Handler;

import com.imgtec.creator.sniffles.data.DataModule;
import com.imgtec.creator.sniffles.data.NsdService;
import com.imgtec.creator.sniffles.data.SecurePreferences;
import com.imgtec.creator.sniffles.data.api.ApiModule;
import com.imgtec.creator.sniffles.data.api.accountserver.AccountServerApiService;
import com.imgtec.creator.sniffles.data.api.accountserver.AccountServerModule;
import com.imgtec.creator.sniffles.data.api.accountserver.IdConfig;
import com.imgtec.creator.sniffles.data.api.deviceserver.DeviceServerApiService;
import com.imgtec.creator.sniffles.data.api.deviceserver.DeviceServerModule;
import com.imgtec.creator.sniffles.data.api.jsonrpc.JsonRPCApiService;
import com.imgtec.creator.sniffles.data.api.jsonrpc.JsonRPCModule;
import com.imgtec.creator.sniffles.data.api.oauth.OauthTokenWrapper;
import com.imgtec.creator.sniffles.network.NetworkHelper;
import com.imgtec.creator.sniffles.network.NetworkModule;
import com.imgtec.di.PerApp;

import javax.inject.Named;

import dagger.Component;
import okhttp3.OkHttpClient;

@PerApp
@Component(
    modules = {
        ApplicationModule.class,
        NetworkModule.class,
        DataModule.class,
        ApiModule.class,
        AccountServerModule.class,
        DeviceServerModule.class,
        JsonRPCModule.class
    }
)
public interface ApplicationComponent {

  final class Initializer {

    private Initializer() {}

    static ApplicationComponent init(App application) {
      return DaggerApplicationComponent
          .builder()
          .applicationModule(new ApplicationModule(application))
          .build();
    }
  }

  App inject(App app);

  SharedPreferences getSharedPreferences();
  SecurePreferences getSecurePreferences();

  NetworkHelper getNetworkHelper();

  @Named("Main") Handler getHandler();

  @Named("AccountServer") OkHttpClient getASOkHttpClient();
  AccountServerApiService getAccountServerApi();
  IdConfig getIdConfig();

  @Named("DeviceServer") OkHttpClient getDSOkHttpClient();
  @Named("DeviceServer") OauthTokenWrapper getDSTokenWrapper();
  DeviceServerApiService getDeviceServerApi();


  NsdService getNsdService();
  JsonRPCApiService getJsonRPCService();
}


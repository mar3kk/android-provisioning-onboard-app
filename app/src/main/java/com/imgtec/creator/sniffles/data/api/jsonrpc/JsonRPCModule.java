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

package com.imgtec.creator.sniffles.data.api.jsonrpc;

import android.content.Context;

import com.imgtec.creator.sniffles.app.App;
import com.imgtec.creator.sniffles.data.api.ApiModule;
import com.imgtec.creator.sniffles.network.ssl.TrustyHostnameVerifier;
import com.imgtec.creator.sniffles.network.ssl.TrustySSLSocketFactory;
import com.imgtec.creator.sniffles.network.ssl.TrustyTrustManager;
import com.imgtec.di.PerApp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import javax.inject.Named;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

@Module(
    includes = {
        ApiModule.class
    }
)
public class JsonRPCModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonRPCModule.class);


  @Provides @PerApp @Named("JsonRPC")
  OkHttpClient provideOkHttpClient(App app, Cache cache) {

    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient.Builder builder = new OkHttpClient
        .Builder()
        .cache(cache)
        .addInterceptor(loggingInterceptor);


    TrustManager trustManager = new TrustyTrustManager();

    ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .tlsVersions(TlsVersion.TLS_1_2)
        .allEnabledCipherSuites()
        .build();

    try {
      builder
          .sslSocketFactory(new TrustySSLSocketFactory(trustManager), (X509TrustManager) trustManager);
    } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
      LOGGER.error("Failed to create {}. App may not be able to communicate with Ci40 board", TrustySSLSocketFactory.class.getSimpleName(), e);
    }
    builder
        .hostnameVerifier(new TrustyHostnameVerifier())
        .protocols(Collections.singletonList(Protocol.HTTP_1_1))
        .connectionSpecs(Arrays.asList(cs))
        .addInterceptor(loggingInterceptor);
    return builder.build();
  }


  @Provides @PerApp
  JsonRPCApiService provideJsonRPCApiService(Context appContext,
                                             @Named("JsonRPC") OkHttpClient client,
                                             ExecutorService executorService) {
    return new JsonRPCApiServiceImpl(appContext, client, executorService);
  }

}

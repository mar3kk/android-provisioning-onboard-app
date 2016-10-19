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

import com.imgtec.creator.sniffles.data.api.ApiCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;


import okhttp3.OkHttpClient;

/**
 *
 */
public class JsonRPCApiServiceImpl implements JsonRPCApiService {

  final Logger logger = LoggerFactory.getLogger(getClass());
  final Context appContext;
  final OkHttpClient client;
  final ExecutorService executorService;
  long id = 1;

  public JsonRPCApiServiceImpl(Context appContext,
                               OkHttpClient client,
                               ExecutorService executorService) {
    super();
    this.appContext = appContext;
    this.client = client;
    this.executorService = executorService;
  }

  @Override
  public void execute(final String ipAddress, final String key, final String secret,
                      final ApiCallback<JsonRPCApiService, String> callback) {

    executorService.execute(new Runnable() {
      @Override
      public void run() {
        final String token = authorize(ipAddress);
        logger.debug("JSON-RPC: token = {}", token);

        try {
          final String params = String.format("%s %d %s %s %s %s",
              "/root/imgtec_generate",
              id,
              "cert",
              "https://deviceserver.flowcloud.systems",
              key,
              secret);
          RpcData data = new RpcData();
          data.setMethod("exec");
          data.setParams(Arrays.asList(params));
          data.setId(id++);

          ExecSysCallRequest sysCallRequest = new ExecSysCallRequest(ipAddress, token, data);
          Map<String, String> m = (Map<String, String>) sysCallRequest.execute(client, Map.class);
          if (callback != null) {
            callback.onSuccess(JsonRPCApiServiceImpl.this, ipAddress);
          }
        }
        catch (Exception e) {
          logger.warn("JSON-RPC: syscall failed!");
          if (callback != null) {
            callback.onFailure(JsonRPCApiServiceImpl.this, e);
          }
        }
      }
    });
  }

  private String authorize(String ipAddr) throws IllegalStateException {

    try {
      RpcData auth = new RpcData();
      auth.setMethod("login");
      auth.setParams(Arrays.asList("root", "password"));
      JsonRPCAuthRequest authRequest =
          new JsonRPCAuthRequest(ipAddr, auth);
      logger.debug("JSON-RPC: Performing auth request: {}", authRequest.getUrl());

      Map<String, String> m = (Map<String, String>) authRequest.execute(client, Map.class);
      return m.get("result");
    }
    catch (IOException e) {

    }

    throw new IllegalStateException("JSON-RPC: Authorization failed!");
  }
}

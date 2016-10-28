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

import com.google.gson.Gson;
import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.RpcData;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.RpcInfo;
import com.imgtec.creator.sniffles.data.utils.Condition;

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
  public void authorize(final String ipAddress, final String userName, final String password,
                        final AuthorizationCallback callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {

          Condition.check(callback != null, "Callback cannot be null");
          final String token = authorize(ipAddress, userName, password);
          if (token == null) {
            callback.onFailure(new AuthorizationFailedException());
            return;
          }

          callback.onSuccess(ipAddress, userName, password, token);

        }
        catch (Exception e) {
          logger.warn("JSON-RPC: calling authorize failed!", e);
          callback.onFailure(e);
        }
      }
    });
  }

  @Override
  public void isConfigured(final String ipAddr, final String userName,
                           final String password, final ApiCallback<JsonRPCApiService, Boolean> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {

        final String token = authorize(ipAddr, userName, password);
        if (token == null) {
          throw new AuthorizationFailedException();
        }

        Condition.check(callback != null, "Callback cannot be null");

        try {
          final String params = String.format("%s %s",
              "/usr/lib/lua/creator/rpc.lua",
              "isProvisioned");

          Map<String, String> m = performExecSysCall(params, id, ipAddr, token);
          callback.onSuccess(JsonRPCApiServiceImpl.this, Boolean.parseBoolean(m.get("result").trim()));

        } catch (Exception e) {
          logger.warn("JSON-RPC: calling isConfigured failed!", e);
          callback.onFailure(JsonRPCApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public void onboarding(final String ipAddress, final String userName, final String password,
                         final String clientName, final String key, final String secret,
                         final ApiCallback<JsonRPCApiService, String> callback) {

    executorService.execute(new Runnable() {
      @Override
      public void run() {

        final String token = authorize(ipAddress, userName, password);
        logger.debug("JSON-RPC: token = {}", token);

        Condition.check(callback != null, "Callback cannot be null");

        try {
          final String params = String.format("%s %s %s %s %s",
              "/usr/lib/lua/creator/rpcOnBoarding.lua",
              "https://deviceserver.flowcloud.systems",
              clientName.isEmpty() ? "" : clientName,
              key,
              secret);

          Map<String, String> m = performExecSysCall(params, id, ipAddress, token);

          if (m.get("result").isEmpty()) {
            throw new RuntimeException("Unknown error");
          }
          callback.onSuccess(JsonRPCApiServiceImpl.this, ipAddress);
        } catch (Exception e) {
          logger.warn("JSON-RPC: syscall failed!");

          callback.onFailure(JsonRPCApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public void removeConfiguration(final String ipAddr, final String userName, final String password,
                                  final ApiCallback<JsonRPCApiService, Boolean> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {

        final String token = authorize(ipAddr, userName, password);
        if (token == null) {
          throw new AuthorizationFailedException();
        }

        Condition.check(callback != null, "Callback cannot be null");

        try {
          final String params = String.format("%s",
              "/usr/lib/lua/creator/rpcRemoveConfiguration.lua");

          Map<String, String> m = performExecSysCall(params, id, ipAddr, token);
          callback.onSuccess(JsonRPCApiServiceImpl.this, Boolean.parseBoolean(m.get("result").trim()));

        } catch (Exception e) {
          logger.warn("JSON-RPC: calling isConfigured failed!", e);
          callback.onFailure(JsonRPCApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public void requestInfo(final String ipAddr, final String userName, final String password,
                          final ApiCallback<JsonRPCApiService, RpcInfo> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {

        final String token = authorize(ipAddr, userName, password);
        if (token == null) {
          throw new AuthorizationFailedException();
        }

        Condition.check(callback != null, "Callback cannot be null");

        try {
          final String params = String.format("%s %s",
              "/usr/lib/lua/creator/rpc.lua", "getInfo");

          Map<String, String> m = performExecSysCall(params, id, ipAddr, token);
          RpcInfo data = new Gson().fromJson(m.get("result"), RpcInfo.class);
          callback.onSuccess(JsonRPCApiServiceImpl.this, data);

        } catch (Exception e) {
          logger.warn("JSON-RPC: calling isConfigured failed!", e);
          callback.onFailure(JsonRPCApiServiceImpl.this, e);
        }
      }
    });
  }

  private String authorize(String ipAddr, final String userName, final String password)
      throws IllegalStateException {

    try {
      Condition.check(ipAddr != null, "IP address cannot be null");
      Condition.check(userName != null && password != null, "Username and password cannot be null.");

      RpcData auth = new RpcData();
      auth.setMethod("login");
      auth.setParams(Arrays.asList(userName, password));
      JsonRPCAuthRequest authRequest =
          new JsonRPCAuthRequest(ipAddr, auth);
      logger.debug("JSON-RPC: Performing auth request: {}", authRequest.getUrl());

      Map<String, String> m = (Map<String, String>) authRequest.execute(client, Map.class);
      return m.get("result");
    }
    catch (IOException e) {
      logger.warn("");
    }

    throw new IllegalStateException("JSON-RPC: Authorization failed!");
  }

  private Map<String, String> performExecSysCall(String params, long id, String ipAddress, String token)
      throws IOException {

    RpcData data = new RpcData();
    data.setMethod("exec");
    data.setParams(Arrays.asList(params));
    data.setId(id++);

    ExecSysCallRequest sysCallRequest = new ExecSysCallRequest(ipAddress, token, data);
    return (Map<String, String>) sysCallRequest.execute(client, Map.class);
  }
}

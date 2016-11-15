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

import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.JsonRPCResponse;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.ProvisioningDaemonState;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.RpcInfo;

/**
 *
 */
public interface JsonRPCApiService {


  interface AuthorizationCallback {
    void onSuccess(final String ipAddr, final String userName, final String password,
                   final String token);
    void onFailure(final Throwable t);
  }

  void authorize(String ipAddress, final String userName, final String password,
                 AuthorizationCallback callback);

  void isConfigured(String ipAddr, final String userName, final String password,
                    ApiCallback<JsonRPCApiService, Boolean> callback);

  void onboarding(final String ipAddress, final String userName, final String password,
                  final String clientName, final String key, final String secret,
                  ApiCallback<JsonRPCApiService, String> callback);

  void removeConfiguration(String ipAddr, String userName, String password,
                           final ApiCallback<JsonRPCApiService, Boolean> callback);

  void requestInfo(String ipAddr, String userName, String password,
                   final ApiCallback<JsonRPCApiService, JsonRPCResponse<RpcInfo>> callback);

  void isProvisioningDaemonRunning(String ipAddr, String userName, String password, final ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>> callback);

  void getProvisioningDaemonState(String ipAddr, String userName, String password, final ApiCallback<JsonRPCApiService, JsonRPCResponse<ProvisioningDaemonState>> callback);

  void startProvisioningDaemon(String ipAddr, String userName, String password, final ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>> callback);

  void selectClicker(String ipAddr, String userName, String password, int clickerID, final ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>> callback);

  void startProvisioning(String ipAddr, String username, String password, final ApiCallback<JsonRPCApiService, JsonRPCResponse<Boolean>> callback);
}

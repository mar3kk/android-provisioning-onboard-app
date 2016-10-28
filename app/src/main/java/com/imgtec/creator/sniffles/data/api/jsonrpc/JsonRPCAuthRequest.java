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


import com.google.gson.GsonBuilder;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.RpcData;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class JsonRPCAuthRequest<T> {

  private final String url;
  private final RpcData auth;

  public JsonRPCAuthRequest(String ipAddr, RpcData auth) {
    this.url = String.format("http://%s/cgi-bin/luci/rpc/auth", ipAddr);
    this.auth = auth;
  }

  private Request prepareRequest() {
    final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    return new Request.Builder()
        .url(url)
        .addHeader("Content-Type",  "application/json")
        .addHeader("Cache-Control", "no-cache")
        .post(RequestBody.create(JSON, new GsonBuilder().create().toJson(auth)))
        .build();
  }

  public T execute(OkHttpClient client, Class returnType) throws IOException {
    Request request = prepareRequest();
    okhttp3.Response response = client.newCall(request).execute();
    if (response.isSuccessful()) {
      return (T) new GsonBuilder()
          .create()
          .fromJson(response.body().string(), returnType);
    }
    throw new RuntimeException("Request failed with code:" + response.code());
  }

  public String getUrl() {
    return url;
  }
}

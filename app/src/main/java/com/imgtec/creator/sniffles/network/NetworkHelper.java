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

package com.imgtec.creator.sniffles.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

/**
 *
 */
public class NetworkHelper {

  private final Context appContext;

  public NetworkHelper(Context appContext) {
    this.appContext = appContext;
  }

  public boolean isWifiConnected() {
    NetworkInfo netInfo;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      netInfo = getWifiNetworkInfo();
    } else {
      netInfo = getNetworkInfo();
    }

    return netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
  }

  public NetworkInfo getNetworkInfo() {
    final ConnectivityManager conMan = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    return conMan.getActiveNetworkInfo();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public NetworkInfo getWifiNetworkInfo() {
    final ConnectivityManager conMan = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    for (Network network : conMan.getAllNetworks()) {
      NetworkInfo networkInfo = conMan.getNetworkInfo(network);
      if (networkInfo != null && conMan.getNetworkInfo(network).getType() == ConnectivityManager.TYPE_WIFI) {
        return networkInfo;
      }
    }
    return null;
  }

}

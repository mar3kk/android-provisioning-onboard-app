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

package com.imgtec.creator.sniffles.network.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class TrustySSLSocketFactory extends SSLSocketFactory {

  SSLContext context = SSLContext.getInstance("TLS");

  private static final String[] COMPAT_CIPHER_SUITES = {
      "SSL_RSA_WITH_RC4_128_MD5",
  };

  public TrustySSLSocketFactory(TrustManager tm) throws NoSuchAlgorithmException, KeyManagementException,
      KeyStoreException, UnrecoverableKeyException {
    super();
    context.init(null, new TrustManager[]{tm}, null);
  }


  @Override
  public String[] getDefaultCipherSuites() {
    return new String[0];
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return context.getSocketFactory().getSupportedCipherSuites();
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
    SSLSocket sock = (SSLSocket) context.getSocketFactory().createSocket(s, host, port, autoClose);
    configureSocket(sock);
    return sock;
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    SSLSocket sock = (SSLSocket) context.getSocketFactory().createSocket(host, port);
    configureSocket(sock);
    return sock;
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
    SSLSocket sock = (SSLSocket) context.getSocketFactory().createSocket(host, port, localHost, localPort);
    configureSocket(sock);
    return sock;
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    SSLSocket sock = (SSLSocket) context.getSocketFactory().createSocket(host, port);
    configureSocket(sock);
    return sock;
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
    SSLSocket sock = (SSLSocket) context.getSocketFactory().createSocket(address, port, localAddress, localPort);
    configureSocket(sock);
    return sock;
  }

  protected void configureSocket(SSLSocket socket) {
    socket.setEnabledCipherSuites(getCipherSuites());
  }

  private String[] getCipherSuites() {
    String[] originalSuites = context.getSocketFactory().getDefaultCipherSuites();
    List<String> allSupportedSuites = new ArrayList<>(Arrays.asList(originalSuites));
    Set<String> compatSuites = new HashSet<>(Arrays.asList(getSupportedCipherSuites()));
    for (String cipherSuite : COMPAT_CIPHER_SUITES) {
      if ((!allSupportedSuites.contains(cipherSuite)) && (compatSuites.contains(cipherSuite))) {
        allSupportedSuites.add(cipherSuite);
      }
    }
    if (allSupportedSuites.size() == originalSuites.length) {
      // No changes to the default list
      return originalSuites;
    }

    return allSupportedSuites.toArray(new String[allSupportedSuites.size()]);
  }

}

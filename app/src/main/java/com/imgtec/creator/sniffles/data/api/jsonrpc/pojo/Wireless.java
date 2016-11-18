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

package com.imgtec.creator.sniffles.data.api.jsonrpc.pojo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Wireless {
  @SerializedName(".name")
  @Expose
  private String name;
  @SerializedName(".anonymous")
  @Expose
  private Boolean anonymous;
  @SerializedName("ssid")
  @Expose
  private String ssid;
  @SerializedName("encryption")
  @Expose
  private String encryption;
  @SerializedName("device")
  @Expose
  private String device;
  @SerializedName("key")
  @Expose
  private String key;
  @SerializedName("macaddr")
  @Expose
  private String macaddr;
  @SerializedName("mode")
  @Expose
  private String mode;
  @SerializedName(".type")
  @Expose
  private String type;
  @SerializedName("network")
  @Expose
  private String network;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getAnonymous() {
    return anonymous;
  }

  public void setAnonymous(Boolean anonymous) {
    this.anonymous = anonymous;
  }

  public String getSsid() {
    return ssid;
  }

  public void setSsid(String ssid) {
    this.ssid = ssid;
  }

  public String getEncryption() {
    return encryption;
  }

  public void setEncryption(String encryption) {
    this.encryption = encryption;
  }

  public String getDevice() {
    return device;
  }

  public void setDevice(String device) {
    this.device = device;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getMacaddr() {
    return macaddr;
  }

  public void setMacaddr(String macaddr) {
    this.macaddr = macaddr;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }
}

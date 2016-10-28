package com.imgtec.creator.sniffles.data.api.jsonrpc.pojo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RpcInfo {
  @SerializedName("host")
  @Expose
  private String host;
  @SerializedName("wireless")
  @Expose
  private Wireless wireless;


  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Wireless getWireless() {
    return wireless;
  }

  public void setWireless(Wireless wireless) {
    this.wireless = wireless;
  }

}


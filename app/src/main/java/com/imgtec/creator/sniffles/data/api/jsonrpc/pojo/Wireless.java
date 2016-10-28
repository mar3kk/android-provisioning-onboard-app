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

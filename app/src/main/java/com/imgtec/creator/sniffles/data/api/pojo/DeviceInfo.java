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

package com.imgtec.creator.sniffles.data.api.pojo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfo extends Hateoas {

  @SerializedName("InstanceID")
  @Expose
  private String instanceID;
  @SerializedName("Manufacturer")
  @Expose
  private String manufacturer;
  @SerializedName("ModelNumber")
  @Expose
  private String modelNumber;
  @SerializedName("SerialNumber")
  @Expose
  private String serialNumber;
  @SerializedName("FirmwareVersion")
  @Expose
  private String firmwareVersion;
  @SerializedName("AvailablePowerSources")
  @Expose
  private List<Integer> availablePowerSources = new ArrayList<Integer>();
  @SerializedName("PowerSourceVoltages")
  @Expose
  private List<Integer> powerSourceVoltages = new ArrayList<Integer>();
  @SerializedName("PowerSourceCurrents")
  @Expose
  private List<Integer> powerSourceCurrents = new ArrayList<Integer>();
  @SerializedName("BatteryLevel")
  @Expose
  private Integer batteryLevel;
  @SerializedName("MemoryFree")
  @Expose
  private Integer memoryFree;
  @SerializedName("ErrorCodes")
  @Expose
  private List<Integer> errorCodes = new ArrayList<Integer>();
  @SerializedName("CurrentTime")
  @Expose
  private String currentTime;
  @SerializedName("UTCOffset")
  @Expose
  private String utcOffset;
  @SerializedName("Timezone")
  @Expose
  private String timezone;
  @SerializedName("SupportedBindingandModes")
  @Expose
  private String supportedBindingandModes;
  @SerializedName("DeviceType")
  @Expose
  private String deviceType;
  @SerializedName("HardwareVersion")
  @Expose
  private String hardwareVersion;
  @SerializedName("SoftwareVersion")
  @Expose
  private String softwareVersion;


  public String getInstanceID() {
    return instanceID;
  }

  public void setInstanceID(String instanceID) {
    this.instanceID = instanceID;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getModelNumber() {
    return modelNumber;
  }

  public void setModelNumber(String modelNumber) {
    this.modelNumber = modelNumber;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public String getFirmwareVersion() {
    return firmwareVersion;
  }

  public void setFirmwareVersion(String firmwareVersion) {
    this.firmwareVersion = firmwareVersion;
  }

  public List<Integer> getAvailablePowerSources() {
    return availablePowerSources;
  }

  public void setAvailablePowerSources(List<Integer> availablePowerSources) {
    this.availablePowerSources = availablePowerSources;
  }

  public List<Integer> getPowerSourceVoltages() {
    return powerSourceVoltages;
  }

  public void setPowerSourceVoltages(List<Integer> powerSourceVoltages) {
    this.powerSourceVoltages = powerSourceVoltages;
  }

  public List<Integer> getPowerSourceCurrents() {
    return powerSourceCurrents;
  }

  public void setPowerSourceCurrents(List<Integer> powerSourceCurrents) {
    this.powerSourceCurrents = powerSourceCurrents;
  }

  public Integer getBatteryLevel() {
    return batteryLevel;
  }

  public void setBatteryLevel(Integer batteryLevel) {
    this.batteryLevel = batteryLevel;
  }

  public Integer getMemoryFree() {
    return memoryFree;
  }

  public void setMemoryFree(Integer memoryFree) {
    this.memoryFree = memoryFree;
  }

  public List<Integer> getErrorCodes() {
    return errorCodes;
  }

  public void setErrorCodes(List<Integer> errorCodes) {
    this.errorCodes = errorCodes;
  }

  public String getCurrentTime() {
    return currentTime;
  }

  public void setCurrentTime(String currentTime) {
    this.currentTime = currentTime;
  }

  public String getUTCOffset() {
    return utcOffset;
  }

  public void setUTCOffset(String uTCOffset) {
    this.utcOffset = uTCOffset;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getSupportedBindingandModes() {
    return supportedBindingandModes;
  }

  public void setSupportedBindingandModes(String supportedBindingandModes) {
    this.supportedBindingandModes = supportedBindingandModes;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getHardwareVersion() {
    return hardwareVersion;
  }

  public void setHardwareVersion(String hardwareVersion) {
    this.hardwareVersion = hardwareVersion;
  }

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public void setSoftwareVersion(String softwareVersion) {
    this.softwareVersion = softwareVersion;
  }
}


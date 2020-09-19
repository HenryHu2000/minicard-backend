package org.skygreen.miniprogram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginCodeDto {
  @JsonProperty("session_key")
  private String sessionKey;
  private String openid;

  public LoginCodeDto() {

  }

  public String getSessionKey() {
    return sessionKey;
  }

  public void setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
  }

  public String getOpenid() {
    return openid;
  }

  public void setOpenid(String openid) {
    this.openid = openid;
  }

  @Override
  public String toString() {
    return String.format("{\"session_key\":\"%s\",\"openid\":\"%s\"}", sessionKey, openid);
  }

}

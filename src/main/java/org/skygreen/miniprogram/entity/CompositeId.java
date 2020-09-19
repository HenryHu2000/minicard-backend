package org.skygreen.miniprogram.entity;

import java.io.Serializable;
import java.util.Objects;

public class CompositeId implements Serializable {

  private static final long serialVersionUID = 1L;

  private Integer id;
  private String openid;

  public CompositeId() {

  }

  public CompositeId(Integer id, String openid) {
    this.id = id;
    this.openid = openid;
  }

  public Integer getId() {
    return id;
  }

  public String getOpenid() {
    return openid;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, openid);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    CompositeId userId = (CompositeId) o;
    if (id != userId.id)
      return false;
    return openid.equals(userId.openid);
  }
}

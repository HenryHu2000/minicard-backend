package org.skygreen.miniprogram.service;

import java.util.Map;
import org.skygreen.miniprogram.entity.User;

public interface IBusinessCardService {

  Integer getUserId(String session);

  User getUser(Integer id);

  User setUser(String session, Map<String, String[]> parameterMap);

  String loginUser(String jsCode) throws Exception;

  byte[] getQRCode(Integer id);

}
